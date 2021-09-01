package com.example.dao;
 
import java.util.List;
import java.util.Map;

import com.example.dto.WorldCorona;
import com.example.dto.RegionCorona;
 
public interface CoronaDAO {
	
	public List<Map<String, String>> selectRegionInfor(RegionCorona regionCorona) throws Exception;
	public List<Map<String, String>> selectTimeRegionInfor(RegionCorona regionCorona) throws Exception;
	public List<Map<String, String>> selectCountryRegionInfor(RegionCorona regionCorona) throws Exception;
	
	public List<Map<String, String>> selectWorldInfor(WorldCorona worldCorona) throws Exception;
	public List<Map<String, String>> selectTimeWorldInfor(WorldCorona worldCorona) throws Exception;
	public List<Map<String, String>> selectNationWorldInfor(WorldCorona worldCorona) throws Exception;
    public void insertRegion(RegionCorona vo);
    public void insertWorld(WorldCorona vo);
}
 