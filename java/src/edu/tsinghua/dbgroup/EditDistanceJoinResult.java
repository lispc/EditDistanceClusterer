package edu.tsinghua.dbgroup;
public class EditDistanceJoinResult {
	public int srcId;
	public int dstId;
	public int similarity;
	@Override
	public int hashCode() {
		return srcId * dstId;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == null ) {
			return false;
		}
		if (!(obj instanceof EditDistanceJoinResult)) {
			return false;
		}
		EditDistanceJoinResult other = (EditDistanceJoinResult)obj;
		if (other == this) {
			return true;
		}
		return srcId == other.srcId && dstId == other.dstId;
	}
}