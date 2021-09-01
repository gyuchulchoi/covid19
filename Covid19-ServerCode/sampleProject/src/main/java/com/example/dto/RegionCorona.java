package com.example.dto;
 
public class RegionCorona {
 
    private String update_time;
    private String country_name;
    private int new_case;
    private int total_case;
    private int recovered;
    private int death;
    private float percentage;
    private int new_fcase;
    private int new_ccase;
    
    public String getUpdateTime() {
        return update_time;
    }
    
    public void setUpdateTime(String update_time) {
        this.update_time = update_time;
    }
    
    public String getCountryName() {
        return country_name;
    }
    
    public void setCountryName(String country_name) {
        this.country_name = country_name;
    }
 
    public int getNewCase() {
        return new_case;
    }
 
    public void setNewCase(int new_case) {
        this.new_case = new_case;
    }
    
    public int getTotalCase() {
        return total_case;
    }
    
    public void setTotalCase(int total_case) {
        this.total_case = total_case;
    }
 
    public int getRecovered() {
        return recovered;
    }
    public void setRecovered(int recovered) {
        this.recovered = recovered;
    }
    
    public int getDeath() {
        return death;
    }
    public void setDeath(int death) {
        this.death = death;
    }
    public float getPercentage() {
        return percentage;
    }
    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
    public int getNewFCase() {
        return new_fcase;
    }
    public void setNewFCase(int new_fcase) {
        this.new_fcase = new_fcase;
    }
    public int getNewCCase() {
        return new_ccase;
    }
    public void setNewCCase(int new_ccase) {
        this.new_ccase = new_ccase;
    }
}