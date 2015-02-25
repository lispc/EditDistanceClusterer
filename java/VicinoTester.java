//package lispc;
import java.util.List;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import org.apache.commons.io.FileUtils;
import edu.mit.simile.vicino.clustering.NGramClusterer;
import edu.mit.simile.vicino.distances.LevenshteinDistance;
import lispc.Joiner;
class VicinoTester {
    public static void main (String[] args) {
        String inputFileName = "input.data";
        if (args.length == 2) {
            inputFileName = args[1];
        }
        List<String> strings = null;
        try {
            strings = FileUtils.readLines(new File("input.data"), "UTF-8");
            for (String s : strings) {
                System.out.println(s);
            }
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        NGramClusterer clusterer = new NGramClusterer(new LevenshteinDistance(), 6);
        for (String s : strings) {
            Joiner.populate(s);
            clusterer.populate(s);
        }
        List<Set<Serializable>> results = Joiner.getClusters(1);
        for (Set<Serializable> set : results) {
            System.out.println("=== new cluster 1===");
            for (Serializable elem : set) {
                System.out.println(elem.toString());
            }
            System.out.println("=== end cluster 1===");
        }
        List<Set<Serializable>> results2 = clusterer.getClusters(1);
        for (Set<Serializable> set : results2) {
            System.out.println("=== new cluster 2===");
            for (Serializable elem : set) {
                System.out.println(elem.toString());
            }
            System.out.println("=== end cluster 2===");
        }
        //block
        System.exit(0);
    }
}

