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
	
	static class UnfilteredResult {
		public int dstId;
		public int dstMatchPos;
		public int srcMatchPos;
		public int gramLen;
	}
	public EditDistanceJoiner(){
		mGlobalIndex = new ArrayList<ArrayList<HashMap<String, ArrayList<Integer>>>>();
		mStrings = new ArrayList<String>();
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
	private void buildIndex() {
		mMaxLength = 0;
		for (int lineNo = 0; lineNo < mStrings.size(); lineNo++) {
			String stringIndexing = mStrings.get(lineNo);
			int l = stringIndexing.length();//3 3 2
			mMaxLength = Math.max(mMaxLength, l);
			while (mGlobalIndex.size() <= l) {
				int strLen = 0;
				ArrayList<HashMap<String, ArrayList<Integer>>> subIndex = new ArrayList<HashMap<String, ArrayList<Integer>>>();
				while (strLen < mThreshold + 1) { 
					subIndex.add(new HashMap<String, ArrayList<Integer>>());
					strLen++;
				}
				mGlobalIndex.add(subIndex);
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
					mGlobalIndex.get(l).get(i).get(gram).add(lineNo);
				} else {
					ArrayList<Integer> invertedList = new ArrayList<Integer>();
					invertedList.add(lineNo);
					mGlobalIndex.get(l).get(i).put(gram, invertedList);
				}
				startPos += gramLen;
			}
		}
		mDistanceBuffer = new int[mMaxLength][mMaxLength];
		for (int i = 0; i < mMaxLength; i++) {
			mDistanceBuffer[0][i] = i;
			mDistanceBuffer[i][0] = i;
		}
	}
	public ArrayList<EditDistanceJoinResult> getJoinResults(int threshold) {
		mThreshold = threshold;
		buildIndex();
		ArrayList<EditDistanceJoinResult> results = new ArrayList<EditDistanceJoinResult>();
		for (int srcId = 0; srcId < mStrings.size(); srcId++) {
			ArrayList<UnfilteredResult> resultsBeforeRefining = new ArrayList<UnfilteredResult>();
			getResultsFromIndex(srcId, resultsBeforeRefining);
			refineResults(srcId, resultsBeforeRefining, results);
		}
		return results;
	}
	private void getResultsFromIndex(int srcId, ArrayList<UnfilteredResult> resultsBeforeRefining){
		String src = mStrings.get(srcId);
		int srcLen = src.length();
		for (int dstLen = srcLen; dstLen <= Math.min(mGlobalIndex.size() - 1, srcLen + mThreshold); dstLen++) {
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
			int leftDistance = calculateEditDistanceWithThreshold(srcLeft, dstLeft, 
				mThreshold); // - Math.abs(srcRightLen - dstRightLen));
			if (leftDistance > mThreshold) {
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
	}
	public void populate(List<String> strings){
		mStrings.addAll(strings);
	}
	
}