package ca.sqlpower.matchmaker;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class MatchMakerVersion extends Task {

    // The method executing the task
    public void execute() throws BuildException {
        getProject().setNewProperty(
                "app_ver_major", 
                Integer.toString(MatchMakerSessionContext.APP_VERSION.getMajor()) );
        getProject().setNewProperty(
                "app_ver_minor",
                Integer.toString(MatchMakerSessionContext.APP_VERSION.getMinor()) );
        getProject().setNewProperty(
                "app_ver_tiny",
                Integer.toString(MatchMakerSessionContext.APP_VERSION.getTiny()) );
    }

    // The setter for the "message" attribute
    public void setMessage(String msg) {
    }
}
