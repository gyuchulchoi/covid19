package com.example.dto;

public class WorldCorona {
	private String update_time;
    private String nation_name;
    private int new_case;
    private int total_case;
    private int total_recovered;
    private int total_death;
    private int new_death;
    
    public String getUpdateTime() {
        return update_time;
    }
    
    public void setUpdateTime(String update_time) {
        this.update_time = update_time;
    }
    
    public String getNationName() {
        return nation_name;
    }
    
    public void setNationName(String nation_name) {
        this.nation_name = nation_name;
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
 
    public int getTotalRecovered() {
        return total_recovered;
    }
    public void setTotalRecovered(int total_recovered) {
        this.total_recovered = total_recovered;
    }
    
    public int getTotalDeath() {
        return total_death;
    }
    public void setTotalDeath(int total_death) {
        this.total_death = total_death;
    }
    public int getNewDeath() {
        return new_death;
    }
    public void setNewDeath(int new_death) {
        this.new_death = new_death;
    }
}
