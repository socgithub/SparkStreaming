package soc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.api.java.JavaSQLContext;
import org.apache.spark.sql.api.java.JavaSchemaRDD;
import org.apache.spark.sql.api.java.Row;

import soc.sw.SemanticWord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SparkSQL {
    public static void recommandMovies(String line) {
        String userID = line.split(":")[1].trim();
        JavaSQLContext sqlCtx = new JavaSQLContext(SparkStreaming.ctx);
        // Create a JavaSchemaRDD from User.json
        JavaSchemaRDD userFromJsonFile = sqlCtx.jsonFile(Properties.USER_JSON);
        userFromJsonFile.printSchema();
        userFromJsonFile.registerTempTable("user");
        //JavaSchemaRDD user = sqlCtx.sql("SELECT interest FROM user WHERE userID = \"Zack\"");
        JavaSchemaRDD user = sqlCtx.sql("SELECT interest FROM user WHERE userID = \"" + userID+ "\"");
        List<String> interestsString = user.map(new Function<Row, String>() {
            public String call(Row row) { return row.get(0).toString(); }
          }).collect();
        
        String[] part = interestsString.get(0).trim().replace("[", "").replace("]", "").split(", ");
        List<String> interests = new ArrayList<String>(Arrays.asList(part));
        
        // Create a JavaSchemaRDD from User.json
        JavaSchemaRDD movieFromJsonFile = sqlCtx.jsonFile(Properties.MOVIE_JSON);
        movieFromJsonFile.printSchema();
        movieFromJsonFile.registerTempTable("movie");
        String orderBy = "(";
        for(int i = 0; i < interests.size(); i ++) {
            orderBy += interests.get(i);
            if(i != interests.size() - 1)
                orderBy += "+";
        }
        orderBy += ")";
        JavaSchemaRDD movie = sqlCtx.sql("SELECT name FROM movie ORDER BY" + orderBy + "DESC LIMIT 5");
        List<String> moviesString = movie.map(new Function<Row, String>() {
            public String call(Row row) { return row.getString(0); }
          }).collect();
        
        try {
            FileWriter fw = new FileWriter(new File(Properties.RECOMMAND_LOG), true);
            fw.write(userID);
            for(String obj: moviesString)
                fw.write("_"+obj);
            fw.write("\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void toDatabase (String line) {
        final GsonBuilder builder = new GsonBuilder();
        final Gson gson = builder.create();
        
        if(line.startsWith("user_created:")) {
            String[] data = line.replace("user_created:", "").trim().split("_");
            // user_created:soc@gmail.com_firstname_lastname_passwd_mountain view_23_Sport_Movie_Game
            User user = new User();
            user.setUserID(data[0]);
            user.setFistName(data[1]);
            user.setLastName(data[2]);
            user.setPassword(data[3]);
            user.setLocation(data[4]);
            user.setYear(Integer.parseInt(data[5]));
            user.setInterest(data[6].toLowerCase(), data[7].toLowerCase(), data[8].toLowerCase());
            
            final String json = gson.toJson(user);
            
            try {
                File file = new File(Properties.USER_JSON);
                file.getParentFile().mkdirs();
                FileWriter fw = new FileWriter(file, true);
                fw.write(json+"\n");
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        else if(line.startsWith("movie_created:")) {
            String[] data = line.replace("movie_created:", "").split("_");
            // movie_created:movie name_tag1_tag2_tag3
            Movie movie = new Movie();
            movie.setName(data[0]);
            movie.setTags(data[1].toLowerCase(), data[2].toLowerCase(), data[3].toLowerCase());
            movie.sport = (int)SemanticWord.getTagSimilarityScore("sport", new Vector<String>(movie.getInterest()));
            movie.movie = (int)SemanticWord.getTagSimilarityScore("movie", new Vector<String>(movie.getInterest()));
            movie.travel = (int)SemanticWord.getTagSimilarityScore("travel", new Vector<String>(movie.getInterest()));
            movie.reading = (int)SemanticWord.getTagSimilarityScore("reading", new Vector<String>(movie.getInterest()));
            movie.tv_shows = (int)SemanticWord.getTagSimilarityScore("tv_shows", new Vector<String>(movie.getInterest()));
            movie.game = (int)SemanticWord.getTagSimilarityScore("game", new Vector<String>(movie.getInterest()));
            movie.music = (int)SemanticWord.getTagSimilarityScore("music", new Vector<String>(movie.getInterest()));
            movie.technology = (int)SemanticWord.getTagSimilarityScore("technology", new Vector<String>(movie.getInterest()));
            
            final String json = gson.toJson(movie);
            try {
                File file = new File(Properties.MOVIE_JSON);
                file.getParentFile().mkdirs();
                FileWriter fw = new FileWriter(file, true);
                fw.write(json+"/");
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
    }
    public static void main(String[] args) {
        //user_login:soc@gmail.com
        //toDatabase("user_created:soc@gmail.com_firstname_lastname_passwd_mountain view_23_Sport_Movie_Game");
        //toDatabase("movie_created:Inception_Nolan_Fiction_Popular");
    }
}
