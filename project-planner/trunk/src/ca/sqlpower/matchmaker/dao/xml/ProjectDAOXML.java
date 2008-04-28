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
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.MatchMakerSettings;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.dao.ProjectDAO;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.SQLPowerUtils;

/**
 * Data access object for a MatchMakerProject and all its child objects. This
 * DAO can read and write a project to/from an XML file.
 * <p>
 * Here are some common techniques employed in the XML generation. If you are
 * a developer looking to extend this class (this will be necessary whenever we
 * change the attributes of the Project class or any of its descendants), please
 * pay close attention:
 * <ul>
 *  <li>The {@link #indent} variable keeps track of the current level of indentation in the XML file
 *  <li>There are four primitives for writing to the output: {@link #print(String)},
 *      {@link #println(String)}, {@link #niprint(String)} and {@link #niprintln(String)}.
 *      They all have good doc comments so you know what they do. Don't write directly to {@link #out}!
 *  <li>There are several higher-level print methods implemented on top of the four primitives.
 *      These are handy because they take care of escaping XML metacharacters in the appropriate places.
 *      They also have good doc comments. Use these in preference to the primitives when possible.
 *  <li>Values that are typcially short and that can't contain newlines are printed as attribtues using
 *      {@link #printAttribute(String, Object)}.
 *  <li>Values that are typically long and/or contain newlines are printed as elements using
 *      {@link #printElement(String, String)}. These elements come before any children of the containing
 *      element.
 *  <li>If an object property normally printed as an attribute is null, its attribute name=value pair is
 *      omitted from the tag. This is taken care of automatically by {@link #printAttribute(String, Object)}.
 *  <li>If an object property normally printed as an element is null, its element is minimized and contains
 *      attribtue null="true". This is taken care of automatically by {@link #printElement(String, String)}.
 *  <li>Enums are printed using their name, <i>not</i> their toString(). This is taken care of automatically
 *      in {@link #printAttribute(String, Object)}.
 *  <li>Dates are formatted using {@link #df}. This is taken care of automatically
 *      in {@link #printAttribute(String, Object)}.
 *  <li>Elements that need to be referenced by other elements use the special XML "ID" attribute.
 *  <li>All references to other elements are done in attributes whose names end with "-ref".
 *  <li>All references refer <i>back</i> to elements that have already been printed. No forward references!
 *  <li>Every XML id in the file is unique. (This is a basic XML rule, and we follow it).
 * </ul>
 *
 */
public class ProjectDAOXML implements ProjectDAO {

    private static final Logger logger = Logger.getLogger(ProjectDAOXML.class);
    
    /**
     * The output stream this DAO instance writes to, if this DAO instance is
     * capable of output. If this is not an output instance, the output stream
     * will be null.
     */
    private PrintWriter out;

    /**
     * Current level of indentation while writing the output file. This value is used
     * by the {@link #println(String)} and {@link #print(String)} methods.
     */
    private int indent;
    
    /**
     * Date format used to represent all date/time values in the XML file.
     */
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    
    /**
     * Creates a new write-only Project DAO. All of the findXXX() methods of
     * this new DAO instance will throw UnsupportedOperationException if called.
     * 
     * @param out The output stream the XML project description will be written to.
     */
    public ProjectDAOXML(OutputStream out) {
        this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
    }
    
    public long countProjectByName(String name) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: ProjectDAOXML.countProjectByName()");
        return 0;
    }

    public List<Project> findAllProjectsWithoutFolders() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: ProjectDAOXML.findAllProjectsWithoutFolders()");
        return null;
    }

    public Project findByName(String name) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: ProjectDAOXML.findByName()");
        return null;
    }

    public boolean isThisProjectNameAcceptable(String name) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: ProjectDAOXML.isThisProjectNameAcceptable()");
        return true;
    }

    public void delete(Project deleteMe) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: ProjectDAOXML.delete()");
        
    }

    public List<Project> findAll() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: ProjectDAOXML.findAll()");
        return null;
    }

    public Class<Project> getBusinessClass() {
        return Project.class;
    }

    public void save(Project p) {
        println("<?xml version='1.0' encoding='UTF-8'?>");
        println("");
        println("<matchmaker-projects>");
        indent++;
        
        int projectID = 0;
        
        print("<project");
        printAttribute("id", "project." + projectID);
        printCommonAttributes(p);
        printAttribute("type", p.getType());
        niprintln(">");
        indent++;
        
        if (p.getSourceTable() != null) {
            print("<source-table");
            printAttribute("datasource", p.getSourceTableSPDatasource());
            printAttribute("catalog", p.getSourceTableCatalog());
            printAttribute("schema", p.getSourceTableSchema());
            printAttribute("table", p.getSourceTableName());
            niprintln(">");
            indent++;

            printElement("where-filter", p.getFilter());
            
            try {
                if (p.getSourceTableIndex() != null) {
                    SQLIndex idx = p.getSourceTableIndex();
                    printIndex(idx);
                }
                indent--;
                println("</source-table>");
            } catch (ArchitectException ex) {
                throw new ArchitectRuntimeException(ex);
            }
        }
        
        // source view omitted (not used by anything)
        
        if (p.getResultTable() != null) {
            print("<result-table");
            printAttribute("datasource", p.getResultTableSPDatasource());
            printAttribute("catalog", p.getResultTableCatalog());
            printAttribute("schema", p.getResultTableSchema());
            printAttribute("table", p.getResultTableName());
            niprintln(">");
            indent++;
            printElement("where-filter", p.getFilter());
            indent--;
            println("</result-table>");
        }

        if (p.getXrefTable() != null) {
            print("<xref-table");
            printAttribute("datasource", p.getXrefTableSPDatasource());
            printAttribute("catalog", p.getXrefTableCatalog());
            printAttribute("schema", p.getXrefTableSchema());
            printAttribute("table", p.getXrefTableName());
            niprintln(">");
            indent++;
            printElement("where-filter", p.getFilter());
            indent--;
        }

        print("<munge-settings");
        printSettingsAttributes(p.getMungeSettings());
        printAttribute("clear-match-pool", p.getMungeSettings().isClearMatchPool());
        printAttribute("auto-match-threshold", p.getMungeSettings().getAutoMatchThreshold());
        niprintln(" />");

        print("<merge-settings");
        printSettingsAttributes(p.getMergeSettings());
        printAttribute("augment-null", p.getMergeSettings().getAugmentNull());
        printAttribute("backup", p.getMergeSettings().getBackUp());
        niprintln(" />");

        int processID = 0;
        
        for (MungeProcess mp : p.getMungeProcesses()) {
            print("<munge-process");
            printAttribute("id", "process." + projectID + "." + processID);
            printCommonAttributes(mp);
            printAttribute("active", mp.getActive());
            printAttribute("colour", mp.getColour());
            printAttribute("priority", mp.getMatchPriority());
            niprintln(">");
            indent++;
            printElement("description", mp.getDesc());
            printElement("where-filter", mp.getFilter());
            
            // maps for instances to XML IDs
            Map<MungeStep, String> steps = new HashMap<MungeStep, String>();
            Map<MungeStepOutput<?>, String> outputs = new HashMap<MungeStepOutput<?>, String>();
            
            int stepID = 0;
            
            for (MungeStep step : mp.getChildren()) {
                print("<munge-step");
                String stepIDStr = "step." + projectID + "." + processID + "." + stepID;
                steps.put(step, stepIDStr);
                printAttribute("id", stepIDStr);
                printCommonAttributes(step);
                printAttribute("step-type", step.getClass().getName());
                // TODO distinguish input step(s)
                // TODO distinguish result step
                niprintln(">");
                indent++;
                for (String pname : step.getParameterNames()) {
                    String pval = step.getParameter(pname);
                    if (pval != null) {
                        print("<parameter");
                        printAttribute("name", pname);
                        niprint(">");
                        niprint(SQLPowerUtils.escapeXML(pval));
                        niprintln("</parameter>");
                    }
                }
                
                int outputID = 0;
                
                for (MungeStepOutput<?> mso : step.getChildren()) {
                    print("<output");
                    String outputIDStr = "output." + projectID + "." + processID + "." + stepID + "." + outputID;
                    outputs.put(mso, outputIDStr);
                    printAttribute("id", outputIDStr);
                    printCommonAttributes(mso);
                    printAttribute("data-type", mso.getType().getName());
                    niprintln("/>");
                    outputID++;
                }
                stepID++;
                indent--;
                println("</munge-step>");
            }

            println("<connections>");
            indent++;
            for (MungeStep step : mp.getChildren()) {
                print("<munge-step");
                printAttribute("ref", steps.get(step));
                niprintln(">");
                indent++;
                for (MungeStepOutput<?> mso : step.getMSOInputs()) {
                    print("<input");
                    if (mso == null) {
                        printAttribute("connected", false);
                    } else {
                        printAttribute("from-ref", outputs.get(mso));
                    }
                    niprintln(" />");
                }
                indent--;
                println("</munge-step>");
            }
            indent--;
            println("</connections>");
            
            processID++;
            indent--;
            println("</munge-process>");
        }
        
        indent--;
        println("</project>");
        
        indent--;
        println("</matchmaker-projects>");
        out.flush();
        out.close();
    }

    private void printIndex(SQLIndex idx) {
        try {
            print("<unique-index");
            printAttribute("name", idx.getName());
            niprintln(">");
            indent++;
            for (SQLIndex.Column c : idx.getChildren()) {
                print("<column");
                printAttribute("name", c.getName());
                niprintln(" />");
            }
            indent--;
            println("</unique-index>");
        } catch (ArchitectException ex) {
            throw new ArchitectRuntimeException(ex);
        }
    }
    
    /**
     * Prints an XML element with no attributes that contains the given text.
     * The given text will have XML metacharacters escaped so it's valid XML
     * CDATA. If the given text is null, the produced XML element will be
     * minimized and contain the attribute <code>null="true"</code>.
     * 
     * @param name
     *            The element name
     * @param text
     *            The textual contents of the element. Do not escape XML
     *            metacharacters before passing in the text; this will be done
     *            for you.
     */
    private void printElement(String name, String text) {
        print("<");
        niprint(name);
        if (text == null) {
            printAttribute("null", true);
            niprintln(" />");
        } else {
            niprint(">");
            niprint(SQLPowerUtils.escapeXML(text));
            niprint("</");
            niprint(name);
            niprintln(">");
        }
    }

    /**
     * Prints all the settings that are common between MatchSettings and MergeSettings.
     * @param settings
     */
    private void printSettingsAttributes(MatchMakerSettings settings) {
        printCommonAttributes(settings);
        printAttribute("append-to-log", settings.getAppendToLog());
        printAttribute("debug", settings.getDebug());
        printAttribute("description", settings.getDescription());
        printAttribute("last-run", settings.getLastRunDate());
        if (settings.getLog() != null) {
            printAttribute("log-file", settings.getLog().getPath());
        }
        printAttribute("process-count", settings.getProcessCount());
        printAttribute("send-email", settings.getSendEmail());
    }
    
    /**
     * Prints the attribute values common to all MatchMakerObjects.
     * Currently, this is just the name and visibility status.
     * 
     * @param mmo The object whose attributes to print.
     */
    private void printCommonAttributes(MatchMakerObject<?, ?> mmo) {
        printAttribute("name", mmo.getName());
        printAttribute("visible", String.valueOf(mmo.isVisible()));
    }

    /**
     * Helper method to print an XML attribute name=value pair (to go in a start
     * tag). The output value will be preceded by a single space. No trailing
     * space will be produced. Null values are represented by outputting nothing
     * (no name, no value, no space... nothing!).
     * <p>
     * Special care is taken with some types of values:
     * <dl>
     *  <dt>Date  <dd>Formatted as yyyy-MM-dd HH:mm:ss Z
     *  <dt>Color <dd>Formatted as 6-digit hex RGB value (can be parsed with {@link Color#getColor(String)}).
     * </dl>
     * All other values are printed using {@link String#valueOf(Object)}.
     * 
     * @param name
     *            The name of the attribute to print
     * @param value
     *            The attribute's value. This value will be turned into a
     *            string, then escaped and quoted so it's a legal XML attribute
     *            value. If the value is null, this method produces no output.
     */
    private void printAttribute(String name, Object value) {
        if (value == null) return;
        niprint(" ");
        niprint(name);
        niprint("=\"");
        String strval;
        if (value instanceof Enum) {
            strval = ((Enum) value).name();
        } else if (value instanceof Date) {
                strval = df.format((Date) value);
        } else if (value instanceof Color) {
            Color c = (Color) value;
            strval = String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
        } else {
            strval = String.valueOf(value);
        }
        niprint(SQLPowerUtils.escapeXML(strval));
        niprint("\"");
    }
    
    /**
     * Prints the given string to the output stream, preceded by the current amount of
     * indentation and followed by a newline.
     * 
     * @param str
     */
    private void println(String str) {
        for (int i = 0; i < indent; i++) {
            out.print(" ");
        }
        out.println(str);
    }

    /**
     * Prints the given string to the output stream, preceded by the current amount of
     * indentation. No newline is appended to the output.
     * 
     * @param str
     */
    private void print(String str) {
        for (int i = 0; i < indent; i++) {
            out.print(" ");
        }
        out.print(str);
    }

    /**
     * Prints the given string to the output stream, followed by a newline.
     * No indentation will be output.
     * 
     * @param str
     */
    private void niprintln(String str) {
        out.println(str);
    }

    /**
     * Prints the given string to the output stream. No indentation, no newline.
     * 
     * @param str
     */
    private void niprint(String str) {
        out.print(str);
    }

    static class ProjectSAXHandler extends DefaultHandler {
        
        /**
         * The list of projects we read from the XML file.
         */
        private List<Project> projects;
        
        /**
         * The current project we're reading from the file.
         */
        private Project project;
        
        /**
         * The current nesting location in the XML file.
         */
        Stack<String> xmlContext;

        /**
         * The parent folder we add the loaded projects to.
         */
        private final PlFolder<Project> parentFolder;
        
        private Map<String, Project> projectIdMap;
        
        private Locator locator;

        private final MatchMakerSessionContext sessionContext;
        
        private StringBuilder text;

        /**
         * The current munge process we're reading from the file.  If not under a munge-process
         * element, this will be null.
         */
        private MungeProcess process;
        
        ProjectSAXHandler(MatchMakerSessionContext sessionContext, PlFolder<Project> parentFolder) {
            this.sessionContext = sessionContext;
            this.parentFolder = parentFolder;
            
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            try {
                xmlContext.push(qName);
                
                if (qName.equals("matchmaker-projects")) {
                    projects = new ArrayList<Project>();
                    projectIdMap = new HashMap<String, Project>();

                } else if (qName.equals("project")) {
                    project = new Project();
                    parentFolder.addChild(project);
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

                    project.addMungeProcess(process);
                    
                } else if (qName.equals("munge-step")) {
                    String type = attributes.getValue("step-type");
                    if (!type.startsWith("ca.sqlpower.matchmaker.munge")) {
                        throw new SAXException("Illegal step type " + type);
                    }
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

                }
            } catch (ArchitectException e) {
                throw new SAXException("Failed to read database structure at " + locator, e);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (text != null) {
                if (qName.equals("where-filter")) {
                    if (xmlContext.peek().equals("source-table")) {
                        project.setFilter(text.toString());
                    } else if (qName.equals("munge-process")) {
                        process.setFilter(text.toString());
                    }
                } else if (qName.equals("description")) {
                    if (qName.equals("munge-process")) {
                        process.setDesc(text.toString());
                    }
                }
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
    }
}
