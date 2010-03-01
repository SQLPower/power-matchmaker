/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru.
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.dao.xml;

import java.awt.Color;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSettings;
import ca.sqlpower.matchmaker.MergeSettings;
import ca.sqlpower.matchmaker.MungeSettings;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.matchmaker.MungeSettings.AutoValidateSetting;
import ca.sqlpower.matchmaker.MungeSettings.PoolFilterSetting;
import ca.sqlpower.matchmaker.TableMergeRules.ChildMergeActionType;
import ca.sqlpower.matchmaker.munge.AbstractMungeStep;
import ca.sqlpower.matchmaker.munge.InputDescriptor;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.AbstractMungeStep.Input;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.util.Version;

public class ProjectSAXHandler extends DefaultHandler {
    
    private static final Logger logger = Logger.getLogger(ProjectSAXHandler.class);

    /**
     * This is the version we support reading.
     * <p>
     * Forward compatibility policy: It will be allowable to read files with a
     * newer version, as long as the major and minor version numbers are the
     * same as the supported version. For example, if the supported version is
     * 2.3.4, we can read 2.3.5 and 2.3.455, but not 2.4.0.
     * <p>
     * Backward compatibility policy: we can read older files that have the same
     * major version number and the supported minor version or less. There is no
     * compatibility between major versions.
     */
    public static final Version SUPPORTED_EXPORT_VERSION = new Version("1.1.0");
    
    private final DateFormat df = new SimpleDateFormat(ProjectDAOXML.DATE_FORMAT);
    
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

    /**
     * The session all MMOs read in from the XML document should belong to.
     */
    private final MatchMakerSession session;

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

    private SQLIndex currentIndex;

    /**
     * The current table merge rules object we're reading in.
     */
    private TableMergeRules tableMergeRules;
    
    private Map<String, TableMergeRules> tableMergeRulesIdMap = new HashMap<String, TableMergeRules>();

    private ColumnMergeRules columnMergeRules;

    ProjectSAXHandler(MatchMakerSession session) {
        this.session = session;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            xmlContext.push(qName);

            if (qName.equals("matchmaker-projects")) {
                projectIdMap = new HashMap<String, Project>();
                String fileFormat = attributes.getValue("export-format");
                checkMandatory("export-format", fileFormat);
                Version formatVersion = new Version(fileFormat);
                
                Version fileMajorMinorVersion = new Version(formatVersion, 2);
                Version supportedMajorMinorVersion = new Version(SUPPORTED_EXPORT_VERSION, 2);
                if (fileMajorMinorVersion.compareTo(supportedMajorMinorVersion) > 0) {
                    throw new SAXException(
                            "The export file format is " + fileFormat + ", but I only understand " +
                            supportedMajorMinorVersion.toString() + ".x or older. Try importing " +
                            "into a newer version of DQguru.");
                }

            } else if (qName.equals("project")) {
                project = new Project();
                project.setSession(session);
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
                        logger.warn("Unexpected attribute of <project>: " + aname + "=" + aval + " at " + locationAsString());
                    }

                }

                checkMandatory("id", id);
                checkMandatory("type", project.getType());

            } else if (qName.equals("source-table")) {

                for (int i = 0; i < attributes.getLength(); i++) {
                    String aname = attributes.getQName(i);
                    String aval = attributes.getValue(i);

                    if (aname.equals("datasource")) {
                        DataSourceCollection plini = session.getContext().getPlDotIni();
                        SPDataSource datasource = plini.getDataSource(aval);
                        if (datasource == null) {
                            throw new SAXException(
                                    "Data Source \""+aval+"\" not found! Please create a " +
                            "data source with this name and try the import again.");
                        }
                        project.setSourceTableSPDatasource(aval);
                    } else if (aname.equals("catalog")) {
                        project.setSourceTableCatalog(aval);
                    } else if (aname.equals("schema")) {
                        project.setSourceTableSchema(aval);
                    } else if (aname.equals("table")) {
                        project.setSourceTableName(aval);
                    } else {
                        logger.warn("Unexpected attribute of <source-table>: " + aname + "=" + aval + " at " + locationAsString());
                    }
                }

                checkMandatory("table", project.getSourceTableName());
                checkMandatory("datasource", project.getSourceTableSPDatasource());

            } else if (qName.equals("where-filter")) {
                if (attributes.getValue("null") != null || !Boolean.valueOf(attributes.getValue("null"))) {
                    text = new StringBuilder();
                    // will pick up contents in endElement
                }

            } else if (qName.equals("description")) {
                if (attributes.getValue("null") != null || !Boolean.valueOf(attributes.getValue("null"))) {
                    text = new StringBuilder();
                    // will pick up contents in endElement
                }

            } else if (qName.equals("unique-index")) {
                currentIndex = new SQLIndex();
                currentIndex.setName(attributes.getValue("name"));
                checkMandatory("name", currentIndex.getName());
                
                if (parentIs("source-table")) {
                    project.setSourceTableIndex(currentIndex);
                } else if (parentIs("table-merge-rule")) {
                    tableMergeRules.setTableIndex(currentIndex);
                } else {
                    throw new SAXException("Found <unique-index> element in wrong place: " + locationAsString());
                }
                // TODO verify column list against actual index in endElement()

            } else if (qName.equals("column")) {
                if (!parentIs("unique-index")) {
                    throw new SAXException("Found <column> element in wrong place: " + locationAsString());
                }
                String colName = attributes.getValue("name");
                checkMandatory("name", colName);
                SQLColumn col = project.getSourceTable().getColumnByName(colName);
                if (col == null) {
                    throw new SAXException(
                            "Source table unique index column \""+colName+"\"" +
                            " is not in the source table (at " + locationAsString() + ")");
                }
                currentIndex.addIndexColumn(col, SQLIndex.AscendDescend.UNSPECIFIED);

            } else if (qName.equals("result-table")) {
                
                for (int i = 0; i < attributes.getLength(); i++) {
                    String aname = attributes.getQName(i);
                    String aval = attributes.getValue(i);

                    if (aname.equals("datasource")) {
                        DataSourceCollection plini = session.getContext().getPlDotIni();
                        SPDataSource datasource = plini.getDataSource(aval);
                        if (datasource == null) {
                            throw new SAXException(
                                    "Data Source \""+aval+"\" not found! Please create a " +
                            "data source with this name and try the import again.");
                        }
                        project.setResultTableSPDatasource(aval);
                    } else if (aname.equals("catalog")) {
                        project.setResultTableCatalog(aval);
                    } else if (aname.equals("schema")) {
                        project.setResultTableSchema(aval);
                    } else if (aname.equals("table")) {
                        project.setResultTableName(aval);
                    } else {
                        logger.warn("Unexpected attribute of <result-table>: " + aname + "=" + aval + " at " + locationAsString());
                    }
                }

                checkMandatory("table", project.getResultTableName());
                checkMandatory("datasource", project.getResultTableSPDatasource());

            } else if (qName.equals("munge-settings")) {
                MungeSettings ms = project.getMungeSettings();
                
                for (int i = 0; i < attributes.getLength(); i++) {
                    String aname = attributes.getQName(i);
                    String aval = attributes.getValue(i);

                    if (handleMatchMakerSetting(ms, aname, aval)) {
                        // ok, it was a generic setting
                    } else if (aname.equals("clear-match-pool")) {
                        ms.setClearMatchPool(Boolean.valueOf(aval));
                    } else if (aname.equals("auto-match-threshold")) {
                        ms.setAutoMatchThreshold(Short.parseShort(aval));
                    } else if (aname.equals("last-backup-number")) {
                        ms.setLastBackupNo(Long.parseLong(aval));
                    } else if (aname.equals("use-batch-execution")) {
                        ms.setUseBatchExecution(Boolean.valueOf(aval));
                    } else if (aname.equals("auto-write-autovalidated-addresses")) {
                        ms.setAutoWriteAutoValidatedAddresses(Boolean.valueOf(aval));
                    } else if (aname.equals("pool-filter-setting")) {
                    	ms.setPoolFilterSetting(PoolFilterSetting.valueOf(aval));
                    } else if (aname.equals("auto-validate-setting")) {
                    	ms.setAutoValidateSetting(AutoValidateSetting.valueOf(aval));
                    } else {
                        logger.warn("Unexpected attribute of <munge-settings>: " + aname + "=" + aval + " at " + locationAsString());
                    }

                }

            } else if (qName.equals("merge-settings")) {
                MergeSettings ms = project.getMergeSettings();
                
                for (int i = 0; i < attributes.getLength(); i++) {
                    String aname = attributes.getQName(i);
                    String aval = attributes.getValue(i);

                    if (handleMatchMakerSetting(ms, aname, aval)) {
                        // ok, it was a generic setting
                    } else if (aname.equals("augment-null")) {
                        ms.setAugmentNull(Boolean.valueOf(aval));
                    } else if (aname.equals("backup")) {
                        ms.setBackUp(Boolean.valueOf(aval));
                    } else {
                        logger.warn("Unexpected attribute of <merge-settings>: " + aname + "=" + aval + " at " + locationAsString());
                    }

                }

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
                    } else if (aname.equals("validate")) {
                        process.setValidate(Boolean.valueOf(aval));
                    } else if (aname.equals("active")) {
                        process.setActive(Boolean.valueOf(aval));
                    } else if (aname.equals("colour")) {
                        process.setColour(Color.decode(aval));
                    } else if (aname.equals("priority")) {
                        process.setMatchPriority(Integer.valueOf(aval));
                    } else {
                        logger.warn("Unexpected attribute of <munge-process>: " + aname + "=" + aval + " at " + locationAsString());
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
                        logger.warn("Unexpected attribute of <munge-step>: " + aname + "=" + aval + " at " + locationAsString());
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
                        logger.warn("Unexpected attribute of <munge-step>: " + aname + "=" + aval + " at " + locationAsString());
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
                    throw new SAXException("Bad munge step reference \"" + stepId + "\" at " + locationAsString());
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
                            throw new SAXException("Bad munge step output reference \""+aval+"\" at " + locationAsString());
                        }
                    } else {
                        logger.warn("Unexpected attribute of <input>: " + aname + "=" + aval + " at " + locationAsString());
                    }

                }

                checkMandatory("name", name);
                checkMandatory("type", type);
                if (connected) {
                    checkMandatory("from-ref", fromOutput);
                } else {
                    if (fromOutput != null) {
                        throw new SAXException("Found an input connection on an unconnected input! at " + locationAsString());
                    }
                }
                InputDescriptor inDesc = new InputDescriptor(name, type);
                Input in = new Input(null, inDesc, step);
                if (connected) {
                    in.setCurrent(fromOutput);
                }

                stepInputs.add(in);

            } else if (qName.equals("table-merge-rule")) {
                tableMergeRules = new TableMergeRules();
                tableMergeRulesIdMap.put(attributes.getValue("id"), tableMergeRules);
                project.addTableMergeRule(tableMergeRules);
                
                for (int i = 0; i < attributes.getLength(); i++) {
                    String aname = attributes.getQName(i);
                    String aval = attributes.getValue(i);

                    if (aname.equals("name")) {
                        tableMergeRules.setName(aval);
                    } else if (aname.equals("visible")) {
                        tableMergeRules.setVisible(Boolean.valueOf(aval));
                    } else if (aname.equals("datasource")) {
                        DataSourceCollection plini = session.getContext().getPlDotIni();
                        SPDataSource datasource = plini.getDataSource(aval);
                        if (datasource == null) {
                            throw new SAXException(
                                    "Data Source \""+aval+"\" not found! Please create a " +
                            "data source with this name and try the import again.");
                        }
                        tableMergeRules.setSpDataSource(aval);
                    } else if (aname.equals("catalog")) {
                        tableMergeRules.setCatalogName(aval);
                    } else if (aname.equals("schema")) {
                        tableMergeRules.setSchemaName(aval);
                    } else if (aname.equals("table")) {
                        tableMergeRules.setTableName(aval);
                    } else if (aname.equals("child-merge-action")) {
                        tableMergeRules.setChildMergeAction(ChildMergeActionType.valueOf(aval));
                    } else {
                        logger.warn("Unexpected attribute of <table-merge-rule>: " + aname + "=" + aval + " at " + locationAsString());
                    }
                }
                
            } else if (qName.equals("column-merge-rule")) {
                
                columnMergeRules = new ColumnMergeRules();
                for (int i = 0; i < attributes.getLength(); i++) {
                    String aname = attributes.getQName(i);
                    String aval = attributes.getValue(i);
                    
                    if (aname.equals("column-name")) {
                        columnMergeRules.setColumnName(aval);
                    } else if (aname.equals("name")) {
                        columnMergeRules.setName(aval);
                    } else if (aname.equals("visible")) {
                        columnMergeRules.setVisible(Boolean.parseBoolean(aval));
                    } else if (aname.equals("action-type")) {
                        columnMergeRules.setActionType(MergeActionType.valueOf(aval));
                    } else if (aname.equals("in-pk")) {
                        columnMergeRules.setInPrimaryKey(Boolean.parseBoolean(aval));
                    } else if (aname.equals("imported-key-column-name")) {
                        columnMergeRules.setImportedKeyColumnName(aval);
                    }
                }
                
                checkMandatory("column-name", columnMergeRules.getColumnName());
                
                tableMergeRules.addChild(columnMergeRules);

            } else if (qName.equals("update-statement")) {
                
                if (attributes.getValue("null") != null && Boolean.valueOf(attributes.getValue("null"))) {
                    text = null;
                } else {
                    text = new StringBuilder();
                }

            } else if (qName.equals("merge-rule-connection")) {
                
                String parentId = attributes.getValue("parent-ref");
                String childId = attributes.getValue("child-ref");
                checkMandatory("parent-ref", parentId);
                checkMandatory("child-ref", childId);
                
                TableMergeRules parent = tableMergeRulesIdMap.get(parentId);
                TableMergeRules child = tableMergeRulesIdMap.get(childId);
                
                if (parent == null) {
                    throw new SAXException(
                            "Parent table merge rules with id=\"" + parentId + "\" not found");
                }
                if (child == null) {
                    throw new SAXException(
                            "Child table merge rules with id=\"" + childId + "\" not found");
                }

                child.setParentMergeRule(parent);

            }
            
        } catch (Exception e) {
            SAXException sex = new SAXException("Project import failed at " + locationAsString(), e);
            sex.initCause(e);
            throw sex;
        }
    }

    /**
     * Tries to set a MatchMakerSettings attribute on the given object.
     * 
     * @param ms The MatchMakerSettings instance to set the setting on.
     * @param aname The name of the XML attribute
     * @param aval The value of the XML attribute
     * @return True if the attribute was recognised and set on ms; false if it was not.
     * @throws ParseException If the last run date can't be parsed.
     */
    private boolean handleMatchMakerSetting(MatchMakerSettings ms, String aname, String aval) throws ParseException {
        if (aname.equals("name")) {
            ms.setName(aval);
        } else if (aname.equals("visible")) {
            ms.setVisible(Boolean.valueOf(aval));
        } else if (aname.equals("append-to-log")) {
            ms.setAppendToLog(Boolean.valueOf(aval));
        } else if (aname.equals("debug")) {
            ms.setDebug(Boolean.valueOf(aval));
        } else if (aname.equals("description")) {
            ms.setDescription(aval);
        } else if (aname.equals("last-run")) {
            ms.setLastRunDate(df.parse(aval));
        } else if (aname.equals("log-file")) {
            ms.setLog(new File(aval));  // XXX windows vs unix problems
        } else if (aname.equals("process-count")) {
            ms.setProcessCount(Integer.parseInt(aval));
        } else if (aname.equals("send-email")) {
            ms.setSendEmail(Boolean.valueOf(aval));
        } else {
            return false;
        }
        return true;
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
            throw new SAXException("Illegal munge step output type \"" + className + "\" at " + locationAsString());
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (text != null) {
            if (qName.equals("where-filter")) {
                System.out.println("Found where filter. parent is "+xmlContext.get(xmlContext.size() - 2));
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
            } else if (qName.equals("update-statement")) {
                if (parentIs("column-merge-rule")) {
                    columnMergeRules.setUpdateStatement(text.toString());
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
            throw new SAXException("Missing mandatory attribute \""+attName+"\" of element \""+xmlContext.peek()+"\" at " + locationAsString());
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
    
    private String locationAsString() {
        return "line " + locator.getLineNumber() + " col " + locator.getColumnNumber();
    }
}
