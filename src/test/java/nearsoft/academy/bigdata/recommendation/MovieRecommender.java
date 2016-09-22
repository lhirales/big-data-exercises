package nearsoft.academy.bigdata.recommendation;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.math3.linear.SymmLQ;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.opencsv.CSVWriter;

import static nearsoft.academy.bigdata.recommendation.Utilities.decompressGzipFile;

/**
 * Created by liver on 19/09/16.
 */
public class MovieRecommender {
    private UserBasedRecommender recommender;

    public MovieRecommender(String path) throws IOException, TasteException {

        setupDatabase(path);
//        DataModel model = new FileDataModel(new File(path));
//        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
//        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
//        this.recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

    }

    public int getTotalReviews() {

        int totalReviews = 0;
        return totalReviews;
    }

    public int getTotalProducts() {

        int totalProducts = 0;
        return totalProducts;
    }

    public int getTotalUsers() {

        int totalUsers = 0;
        return totalUsers;
    }


    public List<String> getRecommendationsForUser(String user) {

        List<String> userList= Arrays.asList(user);
        return userList;

    }


    public void setupDatabase(String path) throws IOException {
        String fileTextName = "movies.txt";
        File fileText = new File(fileTextName);
        if(!fileText.exists()) {
            decompressGzipFile(path, fileTextName);
        }

        HashMap<String, Integer> usersMap = new HashMap<String, Integer>();
        HashMap<String, Integer> itemsMap = new HashMap<String, Integer>();
        Integer usersCounter = 0;
        Integer itemsCounter = 0;
        int reviews = 0;

        String outputFile = "movies.csv";

        try{
            // Open the file
            LineIterator it = FileUtils.lineIterator(fileText, "UTF-8");

            String[] entries = new String[3];

            CSVWriter csvOutput = new CSVWriter(new FileWriter(outputFile));

            try{
                //Read File Line By Line
                while (it.hasNext()) {
                    String line = it.nextLine();
                    if(line.contains("product/productId: ")){
                        String item = line.substring(19);
                        if (!itemsMap.containsKey(item)) {
                            itemsCounter++;
                            itemsMap.put(item, itemsCounter);
                            entries[1] = Integer.toString(itemsCounter);
                        }
                        else{
                            entries[1] = Integer.toString(itemsMap.get(item));
                        }
                    } else if (line.contains("review/userId: ")) {
                        String user = line.substring(15);
                        if (!usersMap.containsKey(user)) {
                            usersCounter++;
                            usersMap.put(user, usersCounter);
                            entries[0] = Integer.toString(usersCounter);
                        }
                        else{
                            entries[0] = Integer.toString(usersMap.get(user));
                        }
                    } else if (line.contains("review/score: ")) {
                        String score = line.substring(14);
                        entries[2] = score;
                        csvOutput.writeNext(entries);
                        reviews++;
                        entries = new String[entries.length];
                    }
                }
            }
            finally{
                //Close the input stream
                LineIterator.closeQuietly(it);
                csvOutput.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        System.out.println(reviews);
        System.out.println(itemsMap.size());
        System.out.println(usersMap.size());
    }
}
