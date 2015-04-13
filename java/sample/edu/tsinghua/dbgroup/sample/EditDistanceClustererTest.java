package edu.tsinghua.dbgroup.sample;
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
class EditDistanceClustererTest {
	public static void main (String[] args) {
		int threshold = Integer.parseInt(args[0]);
		List<String> strings = null;
		try {
			strings = FileUtils.readLines(new File(args[1]), "UTF-8");
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		EditDistanceClusterer clusterer = new EditDistanceClusterer();
		for(String s : strings){
			//System.out.println(s);
			clusterer.populate(s);
		}
		List<Set<Serializable>> results = clusterer.getClusters(threshold);
        for (Set<Serializable> set : results) {
            System.out.println("=== new cluster ===");
            for (Serializable elem : set) {
                System.out.println(elem.toString());
            }
            System.out.println("=== end cluster ===");
        }
	}
}
