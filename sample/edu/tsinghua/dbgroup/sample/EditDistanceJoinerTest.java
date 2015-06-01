package edu.tsinghua.dbgroup.sample;
import java.util.*;
import java.io.*;
import edu.tsinghua.dbgroup.*;
class EditDistanceJoinerTest {
    public static void main (String[] args) {
        if(args.length != 4){
            System.err.println("EditDistanceJoinerTest threshold fileName outputFile threadNum");
            return;
        }
        int threshold = Integer.parseInt(args[0]);
        ArrayList<String> lines = new ArrayList<String>();
        try(BufferedReader br = new BufferedReader(new FileReader(args[1]))) {
            String line = br.readLine();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }
        } catch (IOException e) {
            System.err.println(e.toString());
            System.exit(-1);
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(args[2], "UTF-8");
        } catch (IOException e) {
            System.err.println(e.toString());
            System.exit(-1);
        }
        int threadNum = Integer.parseInt(args[3]);
        EditDistanceJoiner joiner = new EditDistanceJoiner(threshold, threadNum);
        joiner.populate(lines);
        System.err.println("Thread number : " + threadNum);
        long startTime = System.currentTimeMillis();
        ArrayList<EditDistanceJoinResult> results = joiner.getJoinResults();
        System.err.println("Time cost : " + 
            (System.currentTimeMillis() - startTime) + " ms");
        for (EditDistanceJoinResult item : results) {
            writer.println(item.src + " " + item.dst);
        }
        writer.close();
    }
}
