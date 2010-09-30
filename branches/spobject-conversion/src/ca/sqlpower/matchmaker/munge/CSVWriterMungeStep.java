/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
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

package ca.sqlpower.matchmaker.munge;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.NonProperty;


/**
 * A munge step that extract all input values into a csv file.
 * There is a separator that can be inserted between each concatenated
 * value.
 */
public class CSVWriterMungeStep extends AbstractMungeStep {

	/**
	 * The absolute path of the file to be stored
	 */
	public static final String FILE_PATH_PARAM = "fileName";

	/**
	 * This tells the munge step to delete the contents of the file
	 * if it already exists. This can be "true" or "false"
	 */
	public static final String CLEAR_FILE = "clearFile";
	
	/**
	 * The character used for quoted elements. 
	 */
	public static final String QUOTE_PARAM = "quote";
	
	/**
	 * The character used for escaping quoted characters or escape characters.
	 */
	public static final String ESCAPE_PARAM = "escape";
	
	/**
	 * The value of this parameter will be placed between each concatenated value.
	 */
	public static final String SEPARATOR_PARAM = "separator";
	
	/**
	 * The contents to be written to the file.
	 */
	private final List<String[]> contents = new ArrayList<String[]>();
	
	/**
	 * The api that does the actual work of converting and writing the
	 * contents to the csv file.
	 */
	private CSVWriter csvWriter;

	@Constructor
	public CSVWriterMungeStep() {
		super("CSV Writer", true);
		InputDescriptor desc = new InputDescriptor("csvWriter", String.class);
		super.addInput(desc);
		super.setDefaultInputClass(String.class);
		
		setSeparator(CSVWriter.DEFAULT_SEPARATOR);
		setFilePath("csvwriter.csv");
		setDoClearFile(true);
		setQuoteChar(CSVWriter.DEFAULT_QUOTE_CHARACTER);
		setEscapeChar(CSVWriter.DEFAULT_ESCAPE_CHARACTER);
	}

	public Boolean doCall() throws Exception {
		String[] line = new String[getMSOInputs().size()];
		int i = 0;
		for (MungeStepOutput<String> mso : getMSOInputs()) {
			line[i++] = mso.getData();
		}
		contents.add(line);
		return true;
	}

	@Override
	public void doClose() throws Exception {
		contents.clear();
	}
	
	/**
	 * This is where all of the writing to the file gets done.
	 * So rollback does nothing, which is correct.
	 */
	@Override
	public void doCommit() throws Exception {
		FileWriter writer = new FileWriter(new File(getFilePath()),
				!getDoClearFile());
		
		csvWriter = new CSVWriter(writer, getSeparator(), getQuoteChar(),
				getEscapeChar());
		csvWriter.writeAll(contents);
		csvWriter.close();
	}
	
	@NonProperty
	public void setFilePath(String filePath) {
		setParameter(FILE_PATH_PARAM, new File(filePath).getAbsolutePath());
	}

	@NonProperty
	public String getFilePath() {
		return getParameter(FILE_PATH_PARAM);
	}

	@NonProperty
	public void setSeparator(char separator) {
		setParameter(SEPARATOR_PARAM, separator + "");
	}

	@NonProperty
	public char getSeparator() {
		return getParameter(SEPARATOR_PARAM).charAt(0);
	}
	
	@NonProperty
	public void setQuoteChar(char quote) {
		setParameter(QUOTE_PARAM, quote + "");
	}
	
	@NonProperty
	public char getQuoteChar() {
		return getParameter(QUOTE_PARAM).charAt(0);
	}
	
	@NonProperty
	public void setEscapeChar(char escape) {
		setParameter(ESCAPE_PARAM, escape + "");
	}
	
	@NonProperty
	public char getEscapeChar() {
		return getParameter(ESCAPE_PARAM).charAt(0);
	}
	
	@NonProperty
	public void setDoClearFile(boolean doClear) {
		setParameter(CLEAR_FILE, doClear);
	}
	
	@NonProperty
	public boolean getDoClearFile() {
		return getBooleanParameter(CLEAR_FILE);
	}
}
