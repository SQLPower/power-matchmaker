package ca.sqlpower.matchmaker;

/**
 *
 * Merge strategy handles a per table setup of the merge engine
 *
 * FIXME Write the code
 */
public class MergeStrategy
	extends AbstractMatchMakerObject<MergeStrategy, MatchMakerObject> {

	private Long oid;

	public MergeStrategy() {

	}

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
		final MergeStrategy other = (MergeStrategy) obj;
		if (oid == null) {
			if (other.oid != null)
				return false;
		} else if (!oid.equals(other.oid))
			return false;
		return true;
	}

}
