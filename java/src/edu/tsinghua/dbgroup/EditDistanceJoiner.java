package edu.tsinghua.dbgroup;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.TreeMap;
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
	private TreeMap<Integer, ArrayList<HashMap<String, ArrayList<Integer>>>> mGlobalIndex;
	private int mThreshold;
	private int[][] mDistanceBuffer;
	private int mMaxLength;
	
	static class UnfilteredResult {
		public int dstId;
		public int dstMatchPos;
		public int srcMatchPos;
		public int gramLen;
	}
	public EditDistanceJoiner(){
		mGlobalIndex = new TreeMap<Integer, ArrayList<HashMap<String, ArrayList<Integer>>>>();
		mStrings = new ArrayList<String>();
		mMaxLength = 0;
	}
	public int calculateEditDistanceWithThreshold(String s1, String s2, int threshold) {
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
	private void indexStringById(int stringId, String stringIndexing){
		int l = stringIndexing.length();//3 3 2
		if (!mGlobalIndex.containsKey(l)) {
			int strLen = 0;
			ArrayList<HashMap<String, ArrayList<Integer>>> subIndex = new ArrayList<HashMap<String, ArrayList<Integer>>>();
			while (strLen < mThreshold + 1) { 
				subIndex.add(new HashMap<String, ArrayList<Integer>>());
				strLen++;
			}
			mGlobalIndex.put(l, subIndex);
		}
		int shortGramLen = l / (mThreshold + 1); //8/3=2
		int longGramNum = l - shortGramLen * (mThreshold + 1); //2=8-2*3
		int startPos = 0;
		for (int i = 0; i < mThreshold + 1; i++) {
			int gramLen;
			if (i < longGramNum) {
				gramLen = shortGramLen + 1;
			} else {
				gramLen = shortGramLen;
			}
			String gram = stringIndexing.substring(startPos, startPos + gramLen);
			if (mGlobalIndex.get(l).get(i).containsKey(gram)) {
				mGlobalIndex.get(l).get(i).get(gram).add(stringId);
			} else {
				ArrayList<Integer> invertedList = new ArrayList<Integer>();
				invertedList.add(stringId);
				mGlobalIndex.get(l).get(i).put(gram, invertedList);
			}
			startPos += gramLen;
		}
	}
	public void initEditDistanceBuffer(){
		mDistanceBuffer = new int[mMaxLength][mMaxLength];
		for (int i = 0; i < mMaxLength; i++) {
			mDistanceBuffer[0][i] = i;
			mDistanceBuffer[i][0] = i;
		}
	}
	public ArrayList<EditDistanceJoinResult> getJoinResults(int threshold) {
		mThreshold = threshold;
		initEditDistanceBuffer();
		Collections.sort(mStrings, new Comparator<String>(){
		    @Override
		    public int compare(String o1, String o2) {  
		      if (o1.length() > o2.length()) {
		         return 1;
		      } else if (o1.length() < o2.length()) {
		         return -1;
		      }
		      return o1.compareTo(o2);
		    }
		});
		ArrayList<EditDistanceJoinResult> results = new ArrayList<EditDistanceJoinResult>();
		int maxCandidateLen = 0;
		int srcId = 0;
		int dstId = 0;
		while(srcId < mStrings.size()){
			int srcLen = mStrings.get(srcId).length();
			maxCandidateLen = srcLen + mThreshold;
			while(mGlobalIndex.isEmpty() || 
				(mGlobalIndex.lastKey() <= maxCandidateLen && dstId < mStrings.size())){
				String stringIndexing = mStrings.get(dstId);
				indexStringById(dstId, stringIndexing);
				dstId++;
			}
			ArrayList<UnfilteredResult> resultsBeforeRefining = new ArrayList<UnfilteredResult>();
			getResultsFromIndex(srcId, resultsBeforeRefining);
			refineResults(srcId, resultsBeforeRefining, results);
			mGlobalIndex.subMap(0, true, srcLen, false).clear();
			srcId++;
		}
		return results;
	}
	private void getResultsFromIndex(int srcId, ArrayList<UnfilteredResult> resultsBeforeRefining){
		String src = mStrings.get(srcId);
		int srcLen = src.length();
		for (int dstLen = srcLen; dstLen <= Math.min(mGlobalIndex.lastKey(), srcLen + mThreshold); dstLen++) {
			if(!mGlobalIndex.containsKey(dstLen)){
				continue;
			}
			for (int gramNo = 0; gramNo <= mThreshold; gramNo++) {
				int candidateGramPos = dstLen / (mThreshold + 1) * gramNo;
				int candidateGramLen;
				if (gramNo < dstLen % (mThreshold + 1)) {
					candidateGramLen = dstLen / (mThreshold + 1) + 1;
					candidateGramPos += gramNo;
				} else {
					candidateGramPos += dstLen % (mThreshold + 1);
					candidateGramLen = dstLen / (mThreshold + 1);
				}
				int startPos = Math.max(candidateGramPos - mThreshold, 0);
				int endPos = Math.min(candidateGramPos + mThreshold, srcLen - candidateGramLen);
				for (; startPos <= endPos; startPos++) {
					String gram = src.substring(startPos, startPos + candidateGramLen);
					ArrayList<Integer> invertedList = mGlobalIndex.get(dstLen).get(gramNo).get(gram);
					if (invertedList != null) {
						for (int k = 0; k < invertedList.size(); k++) {
							int dstId = invertedList.get(k);
							if(dstId <= srcId){
								continue;
							}
							UnfilteredResult t = new UnfilteredResult();
							t.dstId = dstId;
							t.dstMatchPos = candidateGramPos;
							t.srcMatchPos = startPos;
							t.gramLen = candidateGramLen;
							resultsBeforeRefining.add(t);
						}
					}
				}
			}
		}
		Collections.sort(resultsBeforeRefining, new Comparator<UnfilteredResult>() {
			@Override
			public int compare(UnfilteredResult a, UnfilteredResult b) {
				if (a.dstId < b.dstId)
					return -1;
				if (a.dstId > b.dstId)
					return 1;
				return 0;
			}
		});
	}
	private void refineResults(int srcId, ArrayList<UnfilteredResult> resultsBeforeRefining,
		ArrayList<EditDistanceJoinResult> results){
		HashSet<Integer> matchStringIds = new HashSet<Integer>();
		for (UnfilteredResult t : resultsBeforeRefining) {
			int dstId = t.dstId;
			if(matchStringIds.contains(dstId)){
				continue;
			}
			int dstMatchPos = t.dstMatchPos;
			int srcMatchPos = t.srcMatchPos;
			String dst = mStrings.get(dstId);
			String src = mStrings.get(srcId);
			int len = t.gramLen;
			String srcLeft = src.substring(0, srcMatchPos);
			String dstLeft = mStrings.get(dstId).substring(0, dstMatchPos);
			int srcRightLen = src.length() - srcMatchPos - len;
			int dstRightLen = dst.length() - dstMatchPos - len;
			int leftThreshold = mThreshold - Math.abs(srcRightLen - dstRightLen);
			int leftDistance = calculateEditDistanceWithThreshold(srcLeft, dstLeft, 
				leftThreshold);
			if (leftDistance > leftThreshold) {
				continue;
			} else {
				int rightThreshold = mThreshold - leftDistance;
				String srcRight = src.substring(srcMatchPos + len);
				String dstRight = dst.substring(dstMatchPos + len);
				int rightDistance = calculateEditDistanceWithThreshold(srcRight, dstRight, rightThreshold);
				if (rightDistance > rightThreshold) {
					continue;
				} else {
					matchStringIds.add(dstId);
					EditDistanceJoinResult r = new EditDistanceJoinResult();
					r.src = mStrings.get(srcId);
					r.dst = mStrings.get(dstId);
					r.similarity = leftDistance + rightDistance;
					results.add(r);
				}
			}
		}
	}
	public void populate(String s) {
		mStrings.add(s);
		mMaxLength = Math.max(mMaxLength, s.length());
	}
	public void populate(List<String> strings){
		for(String s : strings){
			populate(s);
		}
	}
	
}