/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

import java.io.PrintWriter;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.util.SQLPowerUtils;

/**
 * A simple class that can be used to export a translate group to xml.
 */
public class TranslateGroupDAOXML {
	
	private final PrintWriter out;
	
	private int indent = 0;

	public TranslateGroupDAOXML(PrintWriter out) {
		this.out = out;
	}

	public void save(MatchMakerTranslateGroup groupToSave) {
		println("<?xml version='1.0' encoding='UTF-8'?>");
        println("");
        println("<matchmaker-translate-group export-format=\""+ ProjectSAXHandler.SUPPORTED_EXPORT_VERSION.toString() + "\">");
        indent++;
        printIndent();
        out.print("<translate-group ");
		out.print("name=\"" + SQLPowerUtils.escapeXML(groupToSave.getName()) + "\" ");
		out.print("oid=\"" + groupToSave.getOid() + "\"");
		out.println(">");
		indent++;
		for (MatchMakerTranslateWord translateWord : groupToSave.getChildren()) {
			printIndent();
			out.print("<translate-word ");
			out.print("name=\"" + SQLPowerUtils.escapeXML(translateWord.getName()) + "\" ");
			out.print("from=\"" + SQLPowerUtils.escapeXML(translateWord.getFrom()) + "\" ");
			out.print("to=\"" + SQLPowerUtils.escapeXML(translateWord.getTo()) + "\"");
			out.println("/>");
		}
		indent--;
		printIndent();
		out.println("</translate-group>");
		indent--;
		println("</matchmaker-translate-group>");
		out.close();
	}

	/**
     * Prints the given string to the output stream, preceded by the current amount of
     * indentation and followed by a newline.
     * 
     * @param str
     */
    private void println(String str) {
    	printIndent();
        out.println(str);
    }
    
    private void printIndent() {
        for (int i = 0; i < indent; i++) {
            out.print(" ");
        }
    }
}
