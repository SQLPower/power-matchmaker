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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;


/**
 * A munge step that extract all input values into a csv file.
 * There is a separator that can be inserted between each concatenated
 * value.
 */
public class CSVWriterMungeStep extends AbstractMungeStep {

	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));
	
	/**
	 * The absolute path of the file to be stored
	 */
	private String filePath;

	/**
	 * This tells the munge step to delete the contents of the file
	 * if it already exists.
	 */
	private boolean clearFile;
	
	/**
	 * The character used for quoted elements. 
	 */
	private char quoteChar;
	
	/**
	 * The character used for escaping quoted characters or escape characters.
	 */
	private char escapeChar;
	
	/**
	 * The value of this parameter will be placed between each concatenated value.
	 */
	private char separator; 
	
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
		setClearFile(true);
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
				!getClearFile());
		
		csvWriter = new CSVWriter(writer, getSeparator(), getQuoteChar(),
				getEscapeChar());
		csvWriter.writeAll(contents);
		csvWriter.close();
	}
	
	@Accessor
	public String getFilePath() {
		return filePath;
	}

	@Mutator
	public void setFilePath(String filePath) {
		String oldPath = this.filePath;
		this.filePath = filePath;
		firePropertyChange("filePath", oldPath, filePath);
	}

	@Accessor
	public boolean getClearFile() {
		return clearFile;
	}

	@Mutator
	public void setClearFile(boolean clearFile) {
		boolean oldVal = this.clearFile;
		this.clearFile = clearFile;
		firePropertyChange("clearFile", oldVal, clearFile);
	}

	@Accessor
	public char getQuoteChar() {
		return quoteChar;
	}

	@Mutator
	public void setQuoteChar(char quoteChar) {
		char oldQuote = this.quoteChar;
		this.quoteChar = quoteChar;
		firePropertyChange("quoteChar", oldQuote, quoteChar);
	}

	@Accessor
	public char getEscapeChar() {
		return escapeChar;
	}

	@Mutator
	public void setEscapeChar(char escapeChar) {
		char oldEscape = this.escapeChar;
		this.escapeChar = escapeChar;
		firePropertyChange("escapeChar", oldEscape, escapeChar);
	}

	@Accessor
	public char getSeparator() {
		return separator;
	}

	@Mutator
	public void setSeparator(char separator) {
		char oldSeparator = this.separator;
		this.separator = separator;
		firePropertyChange("separator", oldSeparator, separator);
	}
}
