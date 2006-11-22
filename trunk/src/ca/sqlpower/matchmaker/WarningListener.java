package ca.sqlpower.matchmaker;

/**
 * The WarningListener interface defines a method for receiving a warning message
 * from some source of warning messages.
 * 
 * @see MatchMakerSession#addWarningListener(WarningListener)
 */
public interface WarningListener {
    
    /**
     * Called by the warning source when there is a new warning message.
     */
    void handleWarning(String message);
}
