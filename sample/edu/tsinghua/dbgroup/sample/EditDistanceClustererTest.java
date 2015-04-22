package edu.tsinghua.dbgroup.sample;
import java.util.*;
import java.io.*;
import edu.tsinghua.dbgroup.*;
class EditDistanceClustererTest {
    public static void main (String[] args) {
		if(args.length != 2){
			System.out.println("EditDistanceClustererTest threshold fileName");
			System.exit(-1);
		}
        int threshold = Integer.parseInt(args[0]);
        EditDistanceClusterer clusterer = new EditDistanceClusterer();
        try(BufferedReader br = new BufferedReader(new FileReader(args[1]))) {
            String line = br.readLine();
            while (line != null) {
                clusterer.populate(line);
                line = br.readLine();
            }
        } catch (IOException e) {
            System.out.println(e.toString());
            System.exit(-1);
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
