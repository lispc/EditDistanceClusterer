package lispc;
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
import org.apache.commons.io.FileUtils;
import java.util.Comparator;
public class Joiner {
	static private List<String> strings = new ArrayList<String>();
	static private ArrayList<ArrayList<HashMap<String, ArrayList<Integer>>>> globalIndex =
	    new ArrayList<ArrayList<HashMap<String, ArrayList<Integer>>>>();
	static private int threshold;
	static boolean inited = false;
	static private int[][] distanceBuffer;
	public static class SizeComparator implements Comparator<Set<Serializable>> {
        public int compare(Set<Serializable> o1, Set<Serializable> o2) {
            return o2.size() - o1.size();
        }
    }
	public static class JoinResult {
		public int srcId;
		public int dstId;
		public int sim;
		@Override
		public int hashCode() {
			return srcId * dstId;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null ) {
				return false;
			}
			if (!(obj instanceof JoinResult)) {
				return false;
			}
			JoinResult other = (JoinResult)obj;
			if (other == this) {
				return true;
			}
			return srcId == other.srcId && dstId == other.dstId;
		}
	}
	static class Tuple {
		public int i1;
		public int i2;
		public int i3;
		public int i4;
	}
	static class JoinComparator implements Comparator<JoinResult> {
		@Override
		public int compare(JoinResult a, JoinResult b) {
			if (a.srcId < b.srcId)
				return -1;
			if (a.srcId == b.srcId && a.dstId < b.dstId)
				return -1;
			if (a.srcId == b.srcId && a.dstId == b.dstId && a.sim < b.sim)
				return -1;
			if (a.srcId == b.srcId && a.dstId == b.dstId && a.sim == b.sim)
				return 0;
			return 1;
		}
	}
	static public void Init() {
		if (inited) {
			return;
		}
		inited = true;
		distanceBuffer = new int[1024][1024];
		for (int i = 0; i < 1024; i++) {
			distanceBuffer[0][i] = i;
			distanceBuffer[i][0] = i;
		}
	}
	static {
		Init();
	}
	static public int EditDistance(String s1, String s2, int threshold) {
		//System.out.println("dis "+s1+" "+s2+" "+threshold);
		if (threshold < 0) {
			return 0;
		}
		int l1 = s1.length();
		int l2 = s2.length();
		if (l1 == 0) {
			return l2;
		}
		if (l2 == 0) {
			return l1;
		}
		for (int j = 1; j <= l1; j++) {
			int start = Math.max(j - threshold, 1);
			int end = Math.min(l2, j + threshold);
			if (j - threshold - 1 >= 1) {
				distanceBuffer[j - threshold - 1][j] = threshold + 1;
			}
			for (int i = start; i <= end; i++) {
				if (s1.charAt(j - 1) == s2.charAt(i - 1)) {
					//System.out.println("xy:"+i+" "+j);
					distanceBuffer[i][j] = distanceBuffer[i - 1][j - 1];
				} else {
					distanceBuffer[i][j] = Math.min(distanceBuffer[i - 1][j - 1] + 1,
					                                Math.min(distanceBuffer[i - 1][j] + 1, distanceBuffer[i][j - 1] + 1));
				}
			}
			if (end < l2)
				distanceBuffer[end + 1][j] = threshold + 1;
			boolean earlyTerminateFlag = true;
			for (int i = start; i <= end; i++) {
				if (distanceBuffer[i][j] <= threshold) {
					earlyTerminateFlag = false;
					break;
				}
			}
			if (earlyTerminateFlag)
				return threshold + 1;
		}
		return distanceBuffer[l2][l1];
	}
	static public void BuildIndex() {
		int line_id = 0;
		for (String indexee : strings) {
			//-System.out.println("str:"+indexee);
			int l = indexee.length();//3 3 2
			while (globalIndex.size() <= l) {
				int c = 0;
				ArrayList<HashMap<String, ArrayList<Integer>>> s_index = new ArrayList<HashMap<String, ArrayList<Integer>>>();
				while (c < threshold + 1) {
					HashMap<String, ArrayList<Integer>> ss_index = new HashMap<String, ArrayList<Integer>>();
					s_index.add(ss_index);
					c++;
				}
				globalIndex.add(s_index);
			}
			int lb = l / (threshold + 1); //8/3=2
			int long_num = l - lb * (threshold + 1); //2=8-2*3
			int start_pos = 0;
			for (int i = 0; i < threshold + 1; i++) {
				int len;
				if (i < long_num) {
					len = lb + 1;
				} else {
					len = lb;
				}
				String seg = indexee.substring(start_pos, start_pos + len);
				if (globalIndex.get(l).get(i).containsKey(seg)) {
					globalIndex.get(l).get(i).get(seg).add(line_id);
				} else {
					ArrayList<Integer> vi = new ArrayList<Integer>();
					vi.add(line_id);
					globalIndex.get(l).get(i).put(seg, vi);
				}
				start_pos += len;
			}
			line_id += 1;
		}
	}
	static public ArrayList<JoinResult> Join() {
		ArrayList<JoinResult> results = new ArrayList<JoinResult>();
		int item_id = 0;
		for (String item : strings) {
			ArrayList<Tuple> local_mid_res = new ArrayList<Tuple>();
			int item_len = item.length();
			for (int target_len = Math.max(0, item_len - threshold); target_len <= Math.min(globalIndex.size() - 1, item_len + threshold); target_len++) {
				for (int tt = 0; tt <= threshold; tt++) {
					int pos = target_len / (threshold + 1) * tt;
					int ss_len;
					if (tt < target_len % (threshold + 1)) {
						ss_len = target_len / (threshold + 1) + 1;
						pos += tt;
					} else {
						pos += target_len % (threshold + 1);
						ss_len = target_len / (threshold + 1);
					}
					int minpos = Math.max(pos - threshold, 0);
					int maxpos = Math.min(pos + threshold, item_len - ss_len);
					for (; minpos <= maxpos; minpos++) {
						String seg = item.substring(minpos, minpos + ss_len);
						ArrayList<Integer> localList = globalIndex.get(target_len).get(tt).get(seg);
						if (localList != null) {
							for (int k = 0; k < localList.size(); k++) {
								int id = localList.get(k);
								Tuple t = new Tuple();
								t.i1 = id;
								t.i2 = pos;
								t.i3 = minpos;
								t.i4 = ss_len;
								local_mid_res.add(t);
							}
						}
					}
				}
			}
			for (Tuple t : local_mid_res) {
				int tid = t.i1;
				int tpos = t.i2;
				int ipos = t.i3;
				int len = t.i4;
				String item_l = item.substring(0, ipos);
				String target_l = strings.get(tid).substring(0, tpos);
				int ed_value = EditDistance(item_l, target_l, threshold);
				if (ed_value > threshold) {
					continue;
				} else {
					int r_tao = threshold - ed_value;
					String item_r = item.substring(ipos + len);
					String target_r = strings.get(tid).substring(tpos + len);
					int r_ed = EditDistance(item_r, target_r, r_tao);
					if (r_ed > r_tao) {
						continue;
					} else {
						JoinResult r = new JoinResult();
						r.srcId = tid;
						r.dstId = item_id;
						r.sim = ed_value + r_ed;
						results.add(r);
					}
				}

			}
			item_id++;
		}
		Collections.sort(results, new JoinComparator());
		Set<JoinResult> set = new LinkedHashSet<JoinResult>();
		set.addAll(results);
		results.clear();
		results.addAll(set);
		return results;
		//Collections.sort(result, new JoinComparator());
		//result.erase( unique( result.begin(), result.end(), same), result.end() );
	}
	public static void populate(String s) {
		strings.add(s);
	}
	public static List<Set<Serializable>> getClusters(int radius) {
		threshold = radius;
		Map<Serializable, Set<Serializable>> cluster_map = new HashMap<Serializable, Set<Serializable>>();
		BuildIndex();
		ArrayList<JoinResult> results = Join();
		for (JoinResult item : results) {
			String a = strings.get(item.srcId);
			String b = strings.get(item.dstId);
			if (a == b) continue;
			if (cluster_map.containsKey(a) && cluster_map.get(a).contains(b)) continue;
			if (cluster_map.containsKey(b) && cluster_map.get(b).contains(a)) continue;
			Set<Serializable> l = null;
			if (!cluster_map.containsKey(a)) {
				l = new TreeSet<Serializable>();
				l.add(a);
				cluster_map.put(a, l);
			} else {
				l = cluster_map.get(a);
			}
			l.add(b);
			Set<Serializable> l2 = null;
			if (!cluster_map.containsKey(b)) {
				l2 = new TreeSet<Serializable>();
				l2.add(b);
				cluster_map.put(b, l2);
			} else {
				l2 = cluster_map.get(b);
			}
			l2.add(a);
		}


		Set<Set<Serializable>> clusters = new HashSet<Set<Serializable>>();
		for (Entry<Serializable, Set<Serializable>> e : cluster_map.entrySet()) {
			Set<Serializable> v = e.getValue();
			System.out.println(v.getClass().getName() + " : " + v.hashCode() + " vs " + System.identityHashCode(v));
			if (v.size() > 1) {
				clusters.add(v);
			}
		}
		List<Set<Serializable>> sorted_clusters = new ArrayList<Set<Serializable>>(clusters);

		Collections.sort(sorted_clusters, new SizeComparator());

		return sorted_clusters;
	}
	public static void main (String[] args) {
		//Init();
		//System.out.println(EditDistance("_aal", "_aal", 2));
		for (String s : args) {
			//System.out.println(s);
		}
		//System.out.println(new Joiner().EditDistance("abcd","bcde",5));
		threshold = Integer.parseInt(args[0]);

		try {
			strings = FileUtils.readLines(new File(args[1]), "UTF-8");
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		BuildIndex();
		ArrayList<JoinResult> results = Join();
		for (JoinResult item : results) {
			if (item.dstId > item.srcId) {
				System.out.println("" + (item.srcId + 1) + " " + (item.dstId + 1));
			}
		}
	}
}