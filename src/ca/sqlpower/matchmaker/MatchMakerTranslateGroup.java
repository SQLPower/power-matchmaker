package ca.sqlpower.matchmaker;


public class MatchMakerTranslateGroup<C extends MatchMakerTranslateWord>
	extends AbstractMatchMakerObject<MatchMakerTranslateGroup, C> {

	private Long oid;

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((oid == null) ? 0 : oid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MatchMakerTranslateGroup other = (MatchMakerTranslateGroup) obj;
		if (oid == null) {
			if (other.oid != null)
				return false;
		} else if (!oid.equals(other.oid))
			return false;
		return true;
	}

	public MatchMakerTranslateGroup() {
	}

}
