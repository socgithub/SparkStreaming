package soc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Movie {
    // movie name_tag1_tag2_tag3
    private String name;
    private List<String> tags;
    public int sport;
    public int movie;
    public int travel;
    public int reading;
    public int tv_shows;
    public int game;
    public int music;
    public int technology;

    public Movie() {
        tags = new ArrayList<String>();
    }
    
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setTags(String... tag) {
        for(String obj: tag) {
            this.tags.add(obj);
        }
    }
    public List<String> getInterest() {
        return this.tags;
    }
}
