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
import org.apache.commons.io.FileUtils;
import edu.tsinghua.dbgroup.*;
class EditDistanceJoinerTest {
	public static void main (String[] args) {
		EditDistanceJoiner joiner = new EditDistanceJoiner();
		if(args.length != 2){
			System.out.println("EditDistanceJoinerTest 2 author.data");
			return;
		}
		int threshold = Integer.parseInt(args[0]);
		List<String> strings = null;
		try {
			strings = FileUtils.readLines(new File(args[1]), "UTF-8");
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		joiner.Populate(strings);
		ArrayList<EditDistanceJoinResult> results = joiner.GetJoinRawResults(2);
		for (EditDistanceJoinResult item : results) {
			System.out.println(item.src + " " + item.dst);
		}
	}
}