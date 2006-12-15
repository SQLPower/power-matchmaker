package ca.sqlpower.matchmaker.swingui;

import java.util.Collection;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphModel;

public class MatchPoolGraphModel implements GraphModel<SourceTableRecord, PotentialMatchRecord> {

    private static final Logger logger = Logger.getLogger(MatchPoolGraphModel.class);
    private final MatchPool pool;
    
    public MatchPoolGraphModel(MatchPool pool) {
        this.pool = pool;
    }
    
    public Collection<PotentialMatchRecord> getEdges() {
        return pool.getPotentialMatches();
    }

    public Collection<SourceTableRecord> getNodes() {
        return pool.getSourceTableRecords();
    }

    public Collection<SourceTableRecord> getAdjacentNodes(SourceTableRecord node) {
        return node.getAdjacentNodes();
    }

    public Collection<PotentialMatchRecord> getInboundEdges(SourceTableRecord node) {
        return node.getInboundEdges();
    }

    public Collection<PotentialMatchRecord> getOutboundEdges(SourceTableRecord node) {
        return node.getOutboundEdges();
    }
    
}
