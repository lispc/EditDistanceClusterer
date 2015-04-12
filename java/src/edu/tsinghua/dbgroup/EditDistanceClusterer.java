package edu.tsinghua.dbgroup;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;
import edu.tsinghua.dbgroup.*;
class EditDistanceClusterer {
	private EditDistanceJoiner mJoiner;
	private List<String> mStrings;
	public static class SizeComparator implements Comparator<Set<Serializable>> {
        public int compare(Set<Serializable> o1, Set<Serializable> o2) {
            return o2.size() - o1.size();
        }
    }
	public EditDistanceClusterer(){
		mJoiner = new EditDistanceJoiner();
		mStrings = new ArrayList<String>();
	}
	public void Populate(String s){
		mStrings.add(s);
		mJoiner.Populate(s);
	}
	public List<Set<Serializable>> getClusters(int radius) {
		Map<Serializable, Set<Serializable>> clusterMap = new HashMap<Serializable, Set<Serializable>>();
		ArrayList<EditDistanceJoinResult> results = mJoiner.GetJoinRawResults(radius);
		for (EditDistanceJoinResult item : results) {
			String a = item.src;
			String b = item.dst;
			if (a == b) continue;
			if (clusterMap.containsKey(a) && clusterMap.get(a).contains(b)) continue;
			if (clusterMap.containsKey(b) && clusterMap.get(b).contains(a)) continue;
			Set<Serializable> l1 = null;
			if (!clusterMap.containsKey(a)) {
				l1 = new TreeSet<Serializable>();
				l1.add(a);
				clusterMap.put(a, l1);
			} else {
				l1 = clusterMap.get(a);
			}
			l1.add(b);
			Set<Serializable> l2 = null;
			if (!clusterMap.containsKey(b)) {
				l2 = new TreeSet<Serializable>();
				l2.add(b);
				clusterMap.put(b, l2);
			} else {
				l2 = clusterMap.get(b);
			}
			l2.add(a);
		}
		Set<Set<Serializable>> clusters = new HashSet<Set<Serializable>>();
		for (Entry<Serializable, Set<Serializable>> e : clusterMap.entrySet()) {
			Set<Serializable> v = e.getValue();
			//System.out.println(v.getClass().getName() + " : " + v.hashCode() + " vs " + System.identityHashCode(v));
			if (v.size() > 1) {
				clusters.add(v);
			}
		}
		List<Set<Serializable>> sortedClusters = new ArrayList<Set<Serializable>>(clusters);

		Collections.sort(sortedClusters, new SizeComparator());

		return sortedClusters;
	}
}