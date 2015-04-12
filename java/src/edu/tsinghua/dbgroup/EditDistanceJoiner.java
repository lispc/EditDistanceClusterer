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
public class EditDistanceJoiner {
	private List<String> mStrings;
	private ArrayList<ArrayList<HashMap<String, ArrayList<Integer>>>> mGlobalIndex;
	private int mThreshold;
	private int[][] mDistanceBuffer;
	private int mMaxLength;
	
	static class Tuple {
		public int i1;
		public int i2;
		public int i3;
		public int i4;
	}
	public EditDistanceJoiner(){
		mGlobalIndex = new ArrayList<ArrayList<HashMap<String, ArrayList<Integer>>>>();
		mStrings = new ArrayList<String>();
	}
	public int CalculateEditDistanceWithThreshold(String s1, String s2, int threshold) {
		if (threshold < 0) {
			return 0;
		}
		if (threshold == 0) {
			return s1.equals(s2) ? 0 : 1;
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
				mDistanceBuffer[j - threshold - 1][j] = threshold + 1;
			}
			for (int i = start; i <= end; i++) {
				if (s1.charAt(j - 1) == s2.charAt(i - 1)) {
					mDistanceBuffer[i][j] = mDistanceBuffer[i - 1][j - 1];
				} else {
					mDistanceBuffer[i][j] = Math.min(mDistanceBuffer[i - 1][j - 1] + 1,
					                                Math.min(mDistanceBuffer[i - 1][j] + 1, mDistanceBuffer[i][j - 1] + 1));
				}
			}
			if (end < l2)
				mDistanceBuffer[end + 1][j] = threshold + 1;
			boolean earlyTerminateFlag = true;
			for (int i = start; i <= end; i++) {
				if (mDistanceBuffer[i][j] <= threshold) {
					earlyTerminateFlag = false;
					break;
				}
			}
			if (earlyTerminateFlag)
				return threshold + 1;
		}
		return mDistanceBuffer[l2][l1];
	}
	private void BuildIndex() {
		mMaxLength = 0;
		for (int line_id = 0; line_id < mStrings.size(); line_id++) {
			String indexee = mStrings.get(line_id);
			int l = indexee.length();//3 3 2
			mMaxLength = Math.max(mMaxLength, l);
			while (mGlobalIndex.size() <= l) {
				int c = 0;
				ArrayList<HashMap<String, ArrayList<Integer>>> s_index = new ArrayList<HashMap<String, ArrayList<Integer>>>();
				while (c < mThreshold + 1) {
					HashMap<String, ArrayList<Integer>> ss_index = new HashMap<String, ArrayList<Integer>>();
					s_index.add(ss_index);
					c++;
				}
				mGlobalIndex.add(s_index);
			}
			int lb = l / (mThreshold + 1); //8/3=2
			int long_num = l - lb * (mThreshold + 1); //2=8-2*3
			int start_pos = 0;
			for (int i = 0; i < mThreshold + 1; i++) {
				int len;
				if (i < long_num) {
					len = lb + 1;
				} else {
					len = lb;
				}
				String seg = indexee.substring(start_pos, start_pos + len);
				if (mGlobalIndex.get(l).get(i).containsKey(seg)) {
					mGlobalIndex.get(l).get(i).get(seg).add(line_id);
				} else {
					ArrayList<Integer> vi = new ArrayList<Integer>();
					vi.add(line_id);
					mGlobalIndex.get(l).get(i).put(seg, vi);
				}
				start_pos += len;
			}
		}
		mDistanceBuffer = new int[mMaxLength][mMaxLength];
		for (int i = 0; i < mMaxLength; i++) {
			mDistanceBuffer[0][i] = i;
			mDistanceBuffer[i][0] = i;
		}
	}
	public ArrayList<EditDistanceJoinResult> GetJoinRawResults(int threshold) {
		mThreshold = threshold;
		BuildIndex();
		ArrayList<EditDistanceJoinResult> results = new ArrayList<EditDistanceJoinResult>();
		int item_id = 0;
		for (String item : mStrings) {
			ArrayList<Tuple> local_mid_res = new ArrayList<Tuple>();
			int item_len = item.length();
			for (int target_len = Math.max(0, item_len - mThreshold); target_len <= Math.min(mGlobalIndex.size() - 1, item_len + mThreshold); target_len++) {
				for (int tt = 0; tt <= mThreshold; tt++) {
					int pos = target_len / (mThreshold + 1) * tt;
					int ss_len;
					if (tt < target_len % (mThreshold + 1)) {
						ss_len = target_len / (mThreshold + 1) + 1;
						pos += tt;
					} else {
						pos += target_len % (mThreshold + 1);
						ss_len = target_len / (mThreshold + 1);
					}
					int minpos = Math.max(pos - mThreshold, 0);
					int maxpos = Math.min(pos + mThreshold, item_len - ss_len);
					for (; minpos <= maxpos; minpos++) {
						String seg = item.substring(minpos, minpos + ss_len);
						ArrayList<Integer> localList = mGlobalIndex.get(target_len).get(tt).get(seg);
						if (localList != null) {
							for (int k = 0; k < localList.size(); k++) {
								int id = localList.get(k);
								if(id <= item_id){
									continue;
								}
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
			Collections.sort(local_mid_res, new Comparator<Tuple>() {
				@Override
				public int compare(Tuple a, Tuple b) {
					if (a.i1 < b.i1)
						return -1;
					if (a.i1 > b.i1)
						return 1;
					return 0;
				}
			});
			HashSet<Integer> matchStringIds = new HashSet<Integer>();
			for (Tuple t : local_mid_res) {
				int tid = t.i1;
				if(matchStringIds.contains(tid)){
					continue;
				}
				int tpos = t.i2;
				int ipos = t.i3;
				int len = t.i4;
				String item_l = item.substring(0, ipos);
				String target_l = mStrings.get(tid).substring(0, tpos);
				int ed_value = CalculateEditDistanceWithThreshold(item_l, target_l, mThreshold);
				if (ed_value > mThreshold) {
					continue;
				} else {
					int r_tao = mThreshold - ed_value;
					String item_r = item.substring(ipos + len);
					String target_r = mStrings.get(tid).substring(tpos + len);
					int r_ed = CalculateEditDistanceWithThreshold(item_r, target_r, r_tao);
					if (r_ed > r_tao) {
						continue;
					} else {
						matchStringIds.add(tid);
						EditDistanceJoinResult r = new EditDistanceJoinResult();
						r.src = mStrings.get(item_id);
						r.dst = mStrings.get(tid);
						r.similarity = ed_value + r_ed;
						results.add(r);
					}
				}

			}
			item_id++;
		}
		return results;
	}
	public void Populate(String s) {
		mStrings.add(s);
	}
	public void Populate(List<String> strings){
		mStrings.addAll(strings);
	}
	
}