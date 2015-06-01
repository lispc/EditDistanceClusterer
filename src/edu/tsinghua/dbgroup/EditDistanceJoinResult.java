package edu.tsinghua.dbgroup;
public class EditDistanceJoinResult {
    public String src;
    public String dst;
    public int similarity;
    @Override
    public int hashCode() {
        return src.hashCode() * dst.hashCode();
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
        return src.equals(other.src) && dst.equals(other.dst);
    }
}
