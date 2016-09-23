package nearsoft.academy.bigdata.recommendation;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static nearsoft.academy.bigdata.recommendation.Utilities.decompressGzipFile;

/**
 * Created by liver on 19/09/16.
 */
public class MovieRecommender {
    private HashMap<String, Integer> usersMap;
    private HashBiMap<String, Integer> itemsMap;
    private int reviews;
    private UserBasedRecommender recommender;

    public MovieRecommender(String path) throws IOException, TasteException {
        this.usersMap = new HashMap<String, Integer>();
        this.itemsMap = HashBiMap.create();
        this.reviews = 0;
        setupRecommender(path);

    }

    public int getTotalReviews() {
        return this.reviews;
    }

    public int getTotalProducts() {
        return this.itemsMap.size();
    }

    public int getTotalUsers() {
        return this.usersMap.size();
    }


    public List<String> getRecommendationsForUser(String user) throws TasteException{
        List<String> recommendationsForUser = new ArrayList<String>();
        List<RecommendedItem> recommendations = this.recommender.recommend(this.usersMap.get(user), 5);
        BiMap<Integer, String> itemsMapInv = this.itemsMap.inverse();
        for (RecommendedItem recommendation : recommendations) {
            int recommendationID = (int) recommendation.getItemID();
            recommendationsForUser.add(itemsMapInv.get(recommendationID));
        }
        return recommendationsForUser;
    }


    public void setupRecommender(String path) throws IOException, TasteException {
        FileInputStream fin = new FileInputStream(path);
        GZIPInputStream gzis = new GZIPInputStream(fin);
        InputStreamReader xover = new InputStreamReader(gzis);

        Integer usersCounter = 0;
        Integer itemsCounter = 0;

        String outputFile = "movies.csv";

        try{
            BufferedReader is = new BufferedReader(xover);
            String line;
            int[] entries = new int[2];
            FileWriter csvOutput = new FileWriter(outputFile);

            try{
                //Read File Line By Line
                while ((line = is.readLine()) != null) {
                    if(line.contains("product/productId: ")){
                        String item = line.substring(19);
                        if (!itemsMap.containsKey(item)) {
                            itemsCounter++;
                            itemsMap.put(item, itemsCounter);
                            entries[1] = itemsCounter;
                        }
                        else{
                            entries[1] = itemsMap.get(item);
                        }
                    } else if (line.contains("review/userId: ")) {
                        String user = line.substring(15);
                        if (!usersMap.containsKey(user)) {
                            usersCounter++;
                            usersMap.put(user, usersCounter);
                            entries[0] = usersCounter;
                        }
                        else{
                            entries[0] = usersMap.get(user);
                        }
                    } else if (line.contains("review/score: ")) {
                        float score = Float.parseFloat(line.substring(14));
                        csvOutput.write(entries[0] + "," + entries[1] + "," + score + "\n");
                        reviews++;
                        entries = new int[entries.length];
                    }
                }
            }
            finally{
                //Close the input stream
                is.close();
                csvOutput.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        try {
            DataModel model = new FileDataModel(new File(outputFile));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            this.recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        }
        catch(TasteException e){
            e.printStackTrace();
        }
    }
}
