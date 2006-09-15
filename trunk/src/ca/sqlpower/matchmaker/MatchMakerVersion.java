package ca.sqlpower.matchmaker;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class MatchMakerVersion extends Task {

	public static final String APP_VERSION_MAJOR = "1";
    public static final String APP_VERSION_MINOR = "0";
    public static final String APP_VERSION_TINY = "20";
    public static final String APP_VERSION = APP_VERSION_MAJOR+"."+
                                            APP_VERSION_MINOR+"." +
                                            APP_VERSION_TINY;

    // The method executing the task
    public void execute() throws BuildException {
        getProject().setNewProperty("app_ver_major", APP_VERSION_MAJOR );
        getProject().setNewProperty("app_ver_minor", APP_VERSION_MINOR );
        getProject().setNewProperty("app_ver_tiny", APP_VERSION_TINY );
    }

    // The setter for the "message" attribute
    public void setMessage(String msg) {
    }
}
