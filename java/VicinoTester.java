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
        String inputFileName = "input.data";
        if (args.length == 2) {
            inputFileName = args[1];
        }
        //System.out.println("Hello World");
        List<String> strings = null;
        try {
            strings = FileUtils.readLines(new File("input.data"), "UTF-8");
            for (String s : strings) {
                System.out.println(s);
            }
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        System.out.println("clusterer initing");
        NGramClusterer clusterer = new NGramClusterer(new LevenshteinDistance(), 6);
        System.out.println("clusterer inited");
        for (String s : strings) {
            clusterer.populate(s);
        }
        System.out.println("clusterer populated");
        List<Set<Serializable>> results = clusterer.getClusters(1);
        System.out.println("calc ended");
        System.out.println("size is " + results.size());
        for (Set<Serializable> set : results) {
            System.out.println("=== new cluster ===");
            for (Serializable elem : set) {
                System.out.println(elem.toString());
            }
            System.out.println("=== end cluster ===");
        }
        System.out.println("clusters ended");
        //block
        System.exit(0);
    }
}
        
