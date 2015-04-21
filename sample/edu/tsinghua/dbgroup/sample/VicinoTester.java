package edu.tsinghua.dbgroup.sample;
import java.util.List;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import org.apache.commons.io.FileUtils;
import edu.mit.simile.vicino.clustering.NGramClusterer;
import edu.mit.simile.vicino.distances.LevenshteinDistance;
class VicinoTester {
    public static void main (String[] args) {
        int threshold = Integer.parseInt(args[0]);
        String inputFileName = null; //"input.data";
        //if (args.length == 2) {
            inputFileName = args[1];
        //}
        List<String> strings = null;
        try {
            strings = FileUtils.readLines(new File(inputFileName), "UTF-8");
            for (String s : strings) {
                //System.out.println(s);
            }
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        NGramClusterer clusterer = new NGramClusterer(new LevenshteinDistance(), 6);
        for (String s : strings) {
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
        //block
        System.exit(0);
    }
}

