package ca.sqlpower.matchmaker.swingui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSUtils.FileExtensionFilter;

public class JTableExporter extends JFileChooser {

	/**
	 * JTable Exporter, save the JTable content to a file
	 * file types are: CSV,XML
	 * @param owner  parent component
	 * @param table  JTable, set the table name to something you want to save in the XML file,
	 * like Database Table Name or SQL statement, it will be XML escaped
	 */
	public JTableExporter ( Component owner, JTable table ) {
		super();
		setFileFilter(SPSUtils.CSV_FILE_FILTER);
		setFileFilter(SPSUtils.XML_FILE_FILTER);

		int returnVal = showSaveDialog(owner);

		while (true) {
			if (returnVal == JFileChooser.CANCEL_OPTION) {
				break;
			} else if (returnVal == JFileChooser.APPROVE_OPTION) {

				FileExtensionFilter fef = (FileExtensionFilter) getFileFilter();
				File file = getSelectedFile();
				String fileName = file.getPath();
				String fileExt = SPSUtils.FileExtensionFilter.getExtension(file);
				if ( fileExt.length() == 0 ) {
					file = new File(fileName + "." +
							fef.getFilterExtension(new Integer(0)));
				}
				if ( file.exists() &&
						( JOptionPane.showOptionDialog(owner,
								"Are your sure you want to overwrite this file?",
								"Confirm Overwrite", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE,null,
								null,
								null) == JOptionPane.NO_OPTION ) ) {
					returnVal = showSaveDialog(owner);
				}
				else {
					writeDocument(owner,table,fef,file);
					break;
				}
			}
		}
	}

	protected void writeDocument (Component owner,
			JTable table,
			FileExtensionFilter fef,
			File file) {
		try {
			if ( fef == SPSUtils.CSV_FILE_FILTER ) {
				writeDocumentCSV(owner,table,file);
			} else if ( fef == SPSUtils.XML_FILE_FILTER ) {
				writeDocumentXml(owner,table,file);
			} else {
				throw new IllegalStateException("Unsupported File Type!");
			}
		} catch (IOException e1) {
			SPSUtils.showExceptionDialog(owner,
					"Save file Error!", e1, new MatchMakerQFAFactory());
		}
	}

	protected void writeDocumentXml (Component owner, JTable table, File file) throws IOException {
		PrintWriter out = new PrintWriter(file);
		IOUtils ioo = new IOUtils();

		ioo.println(out,"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		ioo.println(out,"<EXPORT TABLENAME=\""+
				ArchitectUtils.escapeXML(table.getName())+"\">");
        ioo.indent++;

        for ( int row=0; row<table.getRowCount(); row++ ) {
        	ioo.println(out,"<row rowid=\"" + row + "\">");
        	ioo.indent++;
        	for ( int column=0; column<table.getColumnCount(); column++ ) {
        		Object o = table.getValueAt(row,column);
        		ioo.print(out,"<col" + column + " name=\"" +
        				ArchitectUtils.escapeXML(table.getColumnName(column))+
        				"\">");
        		if ( o != null ) {
        			ioo.niprint(out,ArchitectUtils.escapeXML(o.toString()));
        		}
        		ioo.niprintln(out,"</col" + column +">");
        	}
        	ioo.indent--;
        	ioo.println(out,"</row>");
        }
        ioo.indent--;
        ioo.println(out, "</EXPORT>");


		out.close();
	}

	protected void writeDocumentCSV (Component owner, JTable table, File file) throws IOException {

		PrintWriter out = new PrintWriter(file);
		for ( int column=0; column<table.getColumnCount(); column++ ) {
			if ( column > 0 )
				out.print(",");
			out.print( ArchitectUtils.quoteCSVStr(table.getColumnName(column)) );
		}
		out.println("");

		for ( int row=0; row<table.getRowCount(); row++ ) {
			for ( int column=0; column<table.getColumnCount(); column++ ) {
				Object o = table.getValueAt(row,column);
				out.print(ArchitectUtils.quoteCSV(o));
				out.print(",");
			}
			out.println("");
		}
		out.close();

	}
}
