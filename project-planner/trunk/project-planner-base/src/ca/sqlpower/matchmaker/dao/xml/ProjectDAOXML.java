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
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.MatchMakerSettings;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.dao.ProjectDAO;
import ca.sqlpower.matchmaker.munge.AbstractMungeStep;
import ca.sqlpower.matchmaker.munge.InputDescriptor;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.AbstractMungeStep.Input;
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
     * The thing that provides input and output streams for us to read/write XML
     * to/from.
     */
    private final IOHandler ioHandler;

    private final MatchMakerSession session;
    
    /**
     * Creates a new write-only Project DAO. All of the findXXX() methods of
     * this new DAO instance will throw UnsupportedOperationException if called.
     * 
     * @param out The output stream the XML project description will be written to.
     */
    public ProjectDAOXML(MatchMakerSession session, IOHandler ioHandler) {
        this.session = session;
        this.ioHandler = ioHandler;
        ioHandler.setDAO(this);
    }
    
    public long countProjectByName(String name) {
        logger.debug("Stub call: ProjectDAOXML.countProjectByName()");
        return 0;
    }

    public List<Project> findAllProjectsWithoutFolders() {
        logger.debug("Stub call: ProjectDAOXML.findAllProjectsWithoutFolders()");
        return null;
    }

    public Project findByName(String name) {
        logger.debug("Stub call: ProjectDAOXML.findByName()");
        return null;
    }

    public boolean isThisProjectNameAcceptable(String name) {
        logger.debug("Stub call: ProjectDAOXML.isThisProjectNameAcceptable()");
        return true;
    }

    /**
     * Refreshes the given project by reading in the appropriate project description
     * XML file.
     * 
     * @param project The project to refresh. It must have a non-null OID.
     */
    public void refresh(Project project) {
        if (project.getOid() == null) {
            throw new NullPointerException("Can't update project with null oid");
        }
        InputStream rawInStream = ioHandler.getInputStream(project);
        InputStream inStream = new BufferedInputStream(rawInStream);
        
        ProjectSAXHandler handler = new ProjectSAXHandler(project.getSession().getContext(), project);
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            // turn off validation parser.setProperty()
            parser.parse(inStream, handler);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (SAXException ex) {
            if (ex.getException() != null) {
                throw new RuntimeException(ex.getException());
            }
            throw new RuntimeException(ex);
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                inStream.close();
            } catch (IOException ex) {
                logger.error("Couldn't close input stream", ex);
            }
        }
    }

    /**
     * Returns all the projects the current user has access to, but these
     * projects are sparsely populated (probably just the name and oid fields).
     * They will fill in their details by making another call to this DAO when
     * they are asked for information they don't have.
     */
    public List<Project> findAll() {
        return ioHandler.createProjectList();
    }

    public Class<Project> getBusinessClass() {
        return Project.class;
    }
   
    public Project duplicate(Project p, String name) {
    	boolean isOwner = p.isOwner();
    	boolean canModify = p.canModify();

    	if (p.getOid() == null){
    		// if the original project was a new project itself
    		save(p);
    	}
    	
    	long oldId = p.getOid();
    	String oldName = p.getName();
    	
    	// this causes the save to save as a new entry, the "duplicate"
    	p.setIsOwner(false);
    	
    	p.setName(name);
    	Long newOid = null;
    	try {
    		save(p);	
    		newOid = p.getOid();
    	} finally {
    		// we don't want to mess up the original project if anything goes wrong
    		// returns original project to its original state
    		p.setOid(oldId);
    		p.setName(oldName);
    		p.setIsOwner(isOwner);
    		p.setCanModify(canModify);
    	}
    	
    	Project newProject = new Project(newOid, name, p.getDescription(), (ProjectDAO) session.getDAO(Project.class));
    	newProject.setSession(p.getSession());
    	
    	return newProject;
    }

    public void save(Project p) {
        this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(ioHandler.createOutputStream(p))));

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
        
        printElement("description", p.getDescription());
        
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
        
        for (MungeProcess mp : p.getChildren()) {
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
                for (Input msi : ((AbstractMungeStep) step).getInputs()) {
                    print("<input");
                    printAttribute("name", msi.getName());
                    printAttribute("data-type", msi.getType());
                    if (msi.getCurrent() == null) {
                        printAttribute("connected", false);
                    } else {
                        printAttribute("from-ref", outputs.get(msi.getCurrent()));
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
        } else if (value instanceof Class) {
                strval = ((Class) value).getName();
        } else if (value instanceof Date) {
                strval = df.format((Date) value);
        } else if (value instanceof Color) {
            Color c = (Color) value;
            strval = String.format("0x%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
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
        
        ProjectSAXHandler(MatchMakerSessionContext sessionContext, Project project) {
            this.sessionContext = sessionContext;
            this.project = project;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            try {
                xmlContext.push(qName);
                
                if (qName.equals("matchmaker-projects")) {
                    projectIdMap = new HashMap<String, Project>();

                } else if (qName.equals("project")) {
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

                    project.addChild(process); // FIXME have to do this in endElement
                    
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
                    } else if (parentIs("project")) {
                        project.setDescription(text.toString());
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
    }

    public void delete(Project project) {
        if (project.getOid() != null) {
            ioHandler.delete(project);
        }
    }
}
