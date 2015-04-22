package edu.tsinghua.dbgroup.sample;
import java.util.*;
import java.io.*;
import edu.tsinghua.dbgroup.*;
class EditDistanceJoinerTest {
	public static void main (String[] args) {
		if(args.length != 2){
			System.out.println("EditDistanceJoinerTest threshold fileName");
			return;
		}
		int threshold = Integer.parseInt(args[0]);
		EditDistanceJoiner joiner = new EditDistanceJoiner();
        try(BufferedReader br = new BufferedReader(new FileReader(args[1]))) {
            String line = br.readLine();
            while (line != null) {
                joiner.populate(line);
                line = br.readLine();
            }
        } catch (IOException e) {
            System.out.println(e.toString());
            System.exit(-1);
        }
        long startTime = System.currentTimeMillis();
		ArrayList<EditDistanceJoinResult> results = joiner.getJoinResults(threshold);
		System.err.println("Time cost : " + (System.currentTimeMillis() - startTime) / 1000 + 
			" seconds");
		for (EditDistanceJoinResult item : results) {
			System.out.println(item.src + " " + item.dst);
		}
	}
}
