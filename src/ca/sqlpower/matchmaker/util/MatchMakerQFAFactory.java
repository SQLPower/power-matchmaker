package ca.sqlpower.matchmaker.util;

import ca.sqlpower.architect.qfa.ArchitectExceptionReportFactory;
import ca.sqlpower.architect.qfa.ExceptionReport;
import ca.sqlpower.architect.qfa.QFAFactory;

public class MatchMakerQFAFactory implements QFAFactory {
    ArchitectExceptionReportFactory aerf = new ArchitectExceptionReportFactory();
    
    public ExceptionReport createExceptionReport(Throwable exception) {
        return aerf.createExceptionReport(exception);
    }

}
