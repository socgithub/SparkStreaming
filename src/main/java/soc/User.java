package soc;

import java.util.ArrayList;
import java.util.List;

public class User {
    // soc@gmail.com_firstname_lastname_passwd_mountain view_23_Sport_Movie_Game
    private String userID;
    private String firstName;
    private String lastName;
    private String password;
    private String location;
    private int year;
    private List<String> interest;
    
    public User() {
        interest = new ArrayList<String>();
    }
    public String getFistName() {
        return this.firstName;
    }
    public void setFistName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getUserID() {
        return this.userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public int getYear() {
        return this.year;
    }
    public String getLocation() {
        return this.location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setInterest(String... interests) {
        for(String obj: interests) {
            this.interest.add(obj);
        }
    }
    public List<String> getInterest() {
        return this.interest;
    }
}
