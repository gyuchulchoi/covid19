package com.example.service;
 
import java.util.List;
import java.util.Map;

import com.example.dto.WorldCorona;
import com.example.dto.RegionCorona;
 
public interface CoronaService {
    
    public List<Map<String,String>> selectWorldInfor(String update_time, String nation_name) throws Exception;
    public List<Map<String,String>> selectWorldTimeInfor(String update_time) throws Exception;
    public List<Map<String,String>> selectWorldNationInfor(String nation_name) throws Exception;
    
    public List<Map<String, String>> selectRegionInfor(String update_time, String country_name) throws Exception;
    public List<Map<String,String>> selectRegionTimeInfor(String update_time) throws Exception;
    public List<Map<String,String>> selectRegionCountryInfor(String country_name) throws Exception;
}