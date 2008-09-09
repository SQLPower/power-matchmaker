/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.dao.xml;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.munge.AbstractMungeStep;
import ca.sqlpower.matchmaker.munge.InputDescriptor;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.AbstractMungeStep.Input;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;

public class ProjectSAXHandler extends DefaultHandler {
    
    private static final Logger logger = Logger.getLogger(ProjectSAXHandler.class);
    
    /**
     * All of the allowable classes for munge step inputs/outputs. We enumerate these
     * here so that we can't be compromised when someone feeds us a project file and
     * tricks us into creating a class with a dangerous static initializer.
     */
    private static Set<String> acceptableMungeStepOutputTypes = new HashSet<String>();
    static {
        acceptableMungeStepOutputTypes.add("java.math.BigDecimal");
        acceptableMungeStepOutputTypes.add("java.lang.String");
        acceptableMungeStepOutputTypes.add("java.util.Date");
        acceptableMungeStepOutputTypes.add("java.lang.Boolean");
        acceptableMungeStepOutputTypes.add("java.lang.Object");
    }

    /**
     * All projects read from the file.
     */
    private final List<Project> projects = new ArrayList<Project>();
    
    /**
     * The current project we're reading from the file.
     */
    private Project project;

    /**
     * The current nesting location in the XML file.
     */
    Stack<String> xmlContext = new Stack<String>();

    private Map<String, Project> projectIdMap;

    private Locator locator;

    private final MatchMakerSessionContext sessionContext;

    private StringBuilder text;

    /**
     * The current munge process we're reading from the file.  If not under a munge-process
     * element, this will be null.
     */
    private MungeProcess process;

    /**
     * The current munge step we're reading from the file.  If not under a munge-step
     * element, this will be null.
     */
    private AbstractMungeStep step;

    /**
     * Mapping of step ids to step instances within the current munge process.
     */
    private Map<String, AbstractMungeStep> mungeStepIdMap;

    /**
     * The name of the current parameter we're reading. This value will be null if not
     * under a parameter element.
     */
    private String parameterName;

    /**
     * Mapping of munge step output ids to output instances within the current munge step.
     */
    private Map<String, MungeStepOutput<?>> mungeStepOutputIdMap;

    /**
     * Child list of the current munge step we're loading. The step's normal child list will
     * be replaced by this list in the step's endElement handler.
     */
    private List<MungeStepOutput> stepChildren;

    /**
     * Inputs of the current munge step we're processing. The step's normal input list will be
     * replaced by this one in endElement.
     */
    private List<Input> stepInputs;

    private List<AbstractMungeStep> steps;

    ProjectSAXHandler(MatchMakerSessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            xmlContext.push(qName);

            if (qName.equals("matchmaker-projects")) {
                projectIdMap = new HashMap<String, Project>();

            } else if (qName.equals("project")) {
                project = new Project();
                projects.add(project);
                
                String id = null;

                for (int i = 0; i < attributes.getLength(); i++) {
                    String aname = attributes.getQName(i);
                    String aval = attributes.getValue(i);

                    if (aname.equals("id")) {
                        projectIdMap.put(aval, project);
                        id = aval;
                    } else if (aname.equals("name")) {
                        project.setName(aval);
                    } else if (aname.equals("visible")) {
                        project.setVisible(Boolean.valueOf(aval));
                    } else if (aname.equals("type")) {
                        project.setType(Project.ProjectMode.valueOf(aval));
                    } else {
                        logger.warn("Unexpected attribute of <project>: " + aname + "=" + aval + " at " + locator);
                    }

                }

                checkMandatory("id", id);
                checkMandatory("type", project.getType());

            } else if (qName.equals("source-table")) {

                SPDataSource datasource = null;
                String catalog = null;
                String schema = null;
                String table = null;

                for (int i = 0; i < attributes.getLength(); i++) {
                    String aname = attributes.getQName(i);
                    String aval = attributes.getValue(i);

                    if (aname.equals("datasource")) {
                        DataSourceCollection plini = sessionContext.getPlDotIni();
                        datasource = plini.getDataSource(aval);
                        if (datasource == null) {
                            throw new SAXException(
                                    "Data Source \""+aval+"\" not found! Please create a " +
                            "data source with this name and try again");
                        }
                    } else if (aname.equals("catalog")) {
                        catalog = aval;
                    } else if (aname.equals("schema")) {
                        schema = aval;
                    } else if (aname.equals("table")) {
                        table = aval;
                    } else {
                        logger.warn("Unexpected attribute of <source-table>: " + aname + "=" + aval + " at " + locator);
                    }
                }

                checkMandatory("table", table);
                checkMandatory("datasource", datasource);

                SQLDatabase db = new SQLDatabase(datasource);
                SQLTable t = db.getTableByName(catalog, schema, table);

                if (t == null) {
                    throw new SAXException("Couldn't find table "+catalog+"."+schema+"."+table+" in database "+datasource.getName()+" at " + locator);
                }

                project.setSourceTable(t);

            } else if (qName.equals("where-filter")) {
                if (attributes.getValue("null") != null && !Boolean.valueOf(attributes.getValue("null"))) {
                    text = new StringBuilder();
                    // will pick up contents in endElement
                }

            } else if (qName.equals("description")) {
                if (attributes.getValue("null") != null && !Boolean.valueOf(attributes.getValue("null"))) {
                    text = new StringBuilder();
                    // will pick up contents in endElement
                }

            } else if (qName.equals("unique-index")) {
                // TODO in matchmaker

            } else if (qName.equals("result-table")) {
                // TODO in matchmaker

            } else if (qName.equals("munge-settings")) {
                // TODO in matchmaker

            } else if (qName.equals("merge-settings")) {
                // TODO in matchmaker

            } else if (qName.equals("munge-process")) {
                process = new MungeProcess();
                mungeStepIdMap = new HashMap<String, AbstractMungeStep>();
                mungeStepOutputIdMap = new HashMap<String, MungeStepOutput<?>>();
                steps = new ArrayList<AbstractMungeStep>();

                String id = null;

                for (int i = 0; i < attributes.getLength(); i++) {
                    String aname = attributes.getQName(i);
                    String aval = attributes.getValue(i);

                    if (aname.equals("id")) {
                        id = aval;
                    } else if (aname.equals("name")) {
                        process.setName(aval);
                    } else if (aname.equals("visible")) {
                        process.setVisible(Boolean.valueOf(aval));
                    } else if (aname.equals("active")) {
                        process.setActive(Boolean.valueOf(aval));
                    } else if (aname.equals("colour")) {
                        process.setColour(Color.decode(aval));
                    } else if (aname.equals("priority")) {
                        process.setMatchPriority(Short.valueOf(aval));
                    } else {
                        logger.warn("Unexpected attribute of <munge-process>: " + aname + "=" + aval + " at " + locator);
                    }

                }

                checkMandatory("id", id);
                checkMandatory("name", process.getName());

                project.addMungeProcess(process); // FIXME have to do this in endElement

            } else if (qName.equals("munge-step") && parentIs("munge-process")) {
                String type = attributes.getValue("step-type");
                if (!type.startsWith("ca.sqlpower.matchmaker.munge")) {
                    throw new SAXException("Illegal step type " + type);
                }

                Class<?> c = Class.forName(type);
                Class<? extends AbstractMungeStep> stepClass = c.asSubclass(AbstractMungeStep.class);
                step = stepClass.newInstance();

                String id = null;

                for (int i = 0; i < attributes.getLength(); i++) {
                    String aname = attributes.getQName(i);
                    String aval = attributes.getValue(i);

                    if (aname.equals("id")) {
                        mungeStepIdMap.put(aval, step);
                        id = aval;
                    } else if (aname.equals("step-type")) {
                        // taken care of above (so we could create the instance!)
                    } else if (aname.equals("name")) {
                        step.setName(aval);
                    } else if (aname.equals("visible")) {
                        step.setVisible(Boolean.valueOf(aval));
                    } else {
                        logger.warn("Unexpected attribute of <munge-step>: " + aname + "=" + aval + " at " + locator);
                    }

                }

                checkMandatory("id", id);
                checkMandatory("name", step.getName());

                stepChildren = new ArrayList<MungeStepOutput>();
                logger.debug("enqueuing step " + step.getName());
                steps.add(step);

            } else if (qName.equals("parameter") && parentIs("munge-step")) {

                parameterName = attributes.getValue("name");
                text = new StringBuilder();
                // we recover the name and value in endElement()

            } else if (qName.equals("output") && parentIs("munge-step")) {

                MungeStepOutput mso = new MungeStepOutput();

                String id = null;

                for (int i = 0; i < attributes.getLength(); i++) {
                    String aname = attributes.getQName(i);
                    String aval = attributes.getValue(i);

                    if (aname.equals("id")) {
                        mungeStepOutputIdMap.put(aval, mso);
                        id = aval;
                    } else if (aname.equals("name")) {
                        mso.setName(aval);
                    } else if (aname.equals("visible")) {
                        mso.setVisible(Boolean.valueOf(aval));
                    } else if (aname.equals("data-type")) {
                        checkAcceptableOutputType(aval);
                        mso.setType(Class.forName(aval));
                    } else {
                        logger.warn("Unexpected attribute of <munge-step>: " + aname + "=" + aval + " at " + locator);
                    }

                }

                checkMandatory("id", id);
                checkMandatory("name", mso.getName());
                checkMandatory("type", mso.getType());

                // all children get attached to step in endElement()
                mso.setParent(step);
                stepChildren.add(mso);

            } else if (qName.equals("connections") && parentIs("munge-process")) {
                // container element

            } else if (qName.equals("munge-step") && parentIs("connections")) {
                String stepId = attributes.getValue("ref");
                checkMandatory("ref", stepId);

                step = mungeStepIdMap.get(stepId);
                if (step == null) {
                    throw new SAXException("Bad munge step reference \"" + stepId + "\" at " + locator);
                }

                stepInputs = new ArrayList<Input>();

            } else if (qName.equals("input") && parentIs("munge-step")) {

                String name = null;
                Class type = null;
                MungeStepOutput fromOutput = null;
                boolean connected = true;

                for (int i = 0; i < attributes.getLength(); i++) {
                    String aname = attributes.getQName(i);
                    String aval = attributes.getValue(i);

                    if (aname.equals("name")) {
                        name = aval;
                    } else if (aname.equals("data-type")) {
                        checkAcceptableOutputType(aval);
                        type = Class.forName(aval);
                    } else if (aname.equals("connected")) {
                        connected = Boolean.valueOf(aval);
                    } else if (aname.equals("from-ref")) {
                        fromOutput = mungeStepOutputIdMap.get(aval);
                        if (fromOutput == null) {
                            throw new SAXException("Bad munge step output reference \""+aval+"\" at " + locator);
                        }
                    } else {
                        logger.warn("Unexpected attribute of <input>: " + aname + "=" + aval + " at " + locator);
                    }

                }

                checkMandatory("name", name);
                checkMandatory("type", type);
                if (connected) {
                    checkMandatory("from-ref", fromOutput);
                } else {
                    if (fromOutput != null) {
                        throw new SAXException("Found an input connection on an unconnected input! at " + locator);
                    }
                }
                InputDescriptor inDesc = new InputDescriptor(name, type);
                Input in = new Input(null, inDesc, step);
                if (connected) {
                    in.setCurrent(fromOutput);
                }

                stepInputs.add(in);

            }

        } catch (ArchitectException e) {
            throw new SAXException("Failed to read database structure at " + locator, e);
        } catch (ClassNotFoundException e) {
            throw new SAXException("Class not found at " + locator, e);
        } catch (InstantiationException e) {
            throw new SAXException("Could not create instance at " + locator, e);
        } catch (IllegalAccessException e) {
            throw new SAXException("No public constructor found at " + locator, e);
        }
    }

    /**
     * Throws an exception if the given class name is not an acceptable MungeStepOutput
     * data type.
     * 
     * @param className The class name to verify
     * @throws SAXException If the given class name is not acceptable.
     */
    private void checkAcceptableOutputType(String className) throws SAXException {
        if (!acceptableMungeStepOutputTypes.contains(className)) {
            throw new SAXException("Illegal munge step output type \"" + className + "\" at " + locator);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (text != null) {
            if (qName.equals("where-filter")) {
                if (parentIs("source-table")) {
                    project.setFilter(text.toString());
                } else if (parentIs("munge-process")) {
                    process.setFilter(text.toString());
                }
            } else if (qName.equals("description")) {
                if (parentIs("munge-process")) {
                    process.setDesc(text.toString());
                }
            } else if (qName.equals("parameter")) {
                if (parentIs("munge-step")) {
                    step.setParameter(parameterName, text.toString());
                }
            }
        }

        if (qName.equals("project")) {
            project = null;
        } else if (qName.equals("munge-process") && parentIs("project")) {
            for (AbstractMungeStep step : steps) {
                logger.debug("adding step " + step.getName());
                process.addChild(step);
            }
            process = null;
            mungeStepOutputIdMap = null;
        } else if (qName.equals("munge-step") && parentIs("munge-process")) {
            logger.debug("setting children to " + stepChildren);
            step.setChildren(stepChildren);
            stepChildren = null;
            step = null;
        } else if (qName.equals("munge-step") && parentIs("connections")) {
            step.setInputs(stepInputs);
            stepInputs = null;
            step = null;
        }
        text = null;
        xmlContext.pop();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (text != null) {
            text.append(ch, start, length);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
     * Throws an informative exception if the given value is null.
     * 
     * @param attName Name of the attribute that's supposed to contain the value
     * @param value The value actually recovered from the attribute (if any)
     * @throws SAXException If value is null.
     */
    private void checkMandatory(String attName, Object value) throws SAXException {
        if (value == null) {
            throw new SAXException("Missing mandatory attribute \""+attName+"\" of element \""+xmlContext.peek()+"\" at " + locator);
        }
    }

    /**
     * Returns true if the name of the parent element in the XML context
     * (the one just below the top of the stack) is the given name.
     * 
     * @param qName The name to check for equality with the parent element name.
     * @return If qName == parent element name
     */
    private boolean parentIs(String qName) {
        return xmlContext.get(xmlContext.size() - 2).equals(qName);
    }
    
    /**
     * Returns all projects that have been read by this handler.
     */
    public List<Project> getProjects() {
        return projects;
    }
}
