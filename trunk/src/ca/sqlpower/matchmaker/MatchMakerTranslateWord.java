/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker;

public class MatchMakerTranslateWord
	extends AbstractMatchMakerObject<MatchMakerTranslateWord,MatchMakerObject> {

	private Long oid = 0L;
	private String from = "";
	private String to = "";

	public MatchMakerTranslateWord() {
	}
    
	/**
	 * Return the from value.  
	 * If the from value is null return "" if it dosn't 
	 * Done this way to stop update storm in hibernate
	 */
	public String getFrom() {
		if (from == null) return "";
		return from;
	}

    /**
     * Some databases will behave badly if you have nulls nested in subselects
     * so we change null to "" otherwise this is a normal setter.
     */
	public void setFrom(String from) {
		String oldValue = this.from;
		this.from = from;
		getEventSupport().firePropertyChange("from", oldValue, this.from);
	}

	/**
	 * Return the to value.  
	 * If the from value is null return "" if it dosn't 
	 * Done this way to stop update storm in hibernate
	 */
	public String getTo() {
		if (to == null) return "";
		return to;
	}

    /**
     * Some databases will behave badly if you have nulls nested in subselects
     * so we change null to "" otherwise this is a normal setter.
     */
	public void setTo(String to) {
		String oldValue = this.to;
		this.to = to;
		getEventSupport().firePropertyChange("to", oldValue, this.to);
	}

	@Override
	public String getName() {
		return from + " \u2192 " + to;
	}
	
	@Override
	protected void addImpl(int index, MatchMakerObject child) {
		throw new IllegalStateException("MatchMakerTranslateWord does not allow child!");
	}
	
    @Override
    public boolean allowsChildren() {
        return false;
    }

	@Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 0;
        result = PRIME * result + ((getFrom() == null) ? 0 : getFrom().hashCode());
        result = PRIME * result + ((getParent() == null) ? 0 : getParent().hashCode());
        result = PRIME * result + ((getTo() == null) ? 0 : getTo().hashCode());
        return result;
    }

	@Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof MatchMakerTranslateWord))
            return false;
        final MatchMakerTranslateWord other = (MatchMakerTranslateWord) obj;
        if (getFrom() == null) {
            if (other.getFrom() != null)
                return false;
        } else if (!getFrom().equals(other.getFrom()))
            return false;
        if (getParent() == null) {
            if (other.getParent() != null)
                return false;
        } else if (!getParent().equals(other.getParent()))
            return false;
        if (getTo() == null) {
            if (other.getTo() != null)
                return false;
        } else if (!getTo().equals(other.getTo()))
            return false;
        return true;
    }

    @Override
    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	buf.append("OID: ").append(oid);
    	buf.append(" Parent: ").append(this.getParent());
    	buf.append(" From:").append(getFrom());
    	buf.append("->To:").append(getTo());
    	return buf.toString();
    }
    
    public MatchMakerTranslateWord duplicate(MatchMakerObject parent, MatchMakerSession session) {
    	MatchMakerTranslateWord w = new MatchMakerTranslateWord();
    	w.setName(getName());
    	w.setFrom(getFrom());
    	w.setTo(getTo());
    	w.setParent(parent);
    	w.setSession(session);
    	w.setVisible(isVisible());
    	return w;
    }
}
