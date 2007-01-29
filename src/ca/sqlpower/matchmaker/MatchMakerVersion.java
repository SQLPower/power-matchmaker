package ca.sqlpower.matchmaker;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import ca.sqlpower.util.Version;

public class MatchMakerVersion extends Task {

    /**
     * The version of this MatchMaker front end.
     */
    public static final Version APP_VERSION = new Version(5,13,13);

    // The method executing the task
    public void execute() throws BuildException {
        getProject().setNewProperty(
                "app_ver_major",
                Integer.toString(MatchMakerVersion.APP_VERSION.getMajor()) );
        getProject().setNewProperty(
                "app_ver_minor",
                Integer.toString(MatchMakerVersion.APP_VERSION.getMinor()) );
        getProject().setNewProperty(
                "app_ver_tiny",
                Integer.toString(MatchMakerVersion.APP_VERSION.getTiny()) );
    }

    // The setter for the "message" attribute
    public void setMessage(String msg) {
    	// NOTUSED
    }
}
