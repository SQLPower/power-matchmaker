package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.Color;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A log4j appender which prints logging messages into a Swing document.
 */
class DocumentAppender extends AppenderSkeleton {

	/**
	 * The document to add logging messages to.
	 */
	private final Document doc;

	/**
	 * The visual appearance attributes of the text we put into doc.
	 */
	private final SimpleAttributeSet attributes = new SimpleAttributeSet();
	
	/**
	 * Creates a Log4J appender that writes into the given Swing Document.
	 */
	public DocumentAppender(Document doc) {
		this.doc = doc;
		StyleConstants.setForeground(attributes, Color.BLACK);
		layout = new PatternLayout("%d %p %m\n");
	}
	
	/**
	 * Appends the log message to the target document.
	 */
	@Override
	protected void append(LoggingEvent evt) {
		try {
			doc.insertString(doc.getLength(), layout.format(evt), attributes);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is a no-op.
	 */
	public void close() {
		// nothing to do
	}

	/**
	 * I'm not sure if this should return true.
	 */
	public boolean requiresLayout() {
		return true;
	}
	
}