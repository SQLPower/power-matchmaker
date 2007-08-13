package ca.sqlpower.matchmaker;

/**
 * A checked exception meant to be thrown when there is a problem with
 * the match maker's environment that the end user should be capable of
 * fixing via user settings/prefs/etc.
 * <p>
 * In general, the message for this exception should include text that
 * will explain to the user how the problem can be solved.
 */
public class MatchMakerConfigurationException extends Exception {

    /**
     * Creates a configuration exception with the given message and no
     * chained cause.
     */
    public MatchMakerConfigurationException(String message) {
        super(message);
    }

    /**
     * Creates a configuration exception with the given message and another
     * exception which is the cause of this one.
     */
    public MatchMakerConfigurationException(String message, Exception cause) {
        super(message, cause);
    }
}
