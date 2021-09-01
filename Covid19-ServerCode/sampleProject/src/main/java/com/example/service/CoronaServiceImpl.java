package com.example.service;
 
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
 
import org.springframework.stereotype.Service;
 
import com.example.dao.CoronaDAO;
import com.example.dto.RegionCorona;
import com.example.dto.WorldCorona;
@Service
public class CoronaServiceImpl implements CoronaService {
 
    @Inject
    private CoronaDAO dao;
    private WorldCorona worldVO;
    private RegionCorona regionVO;
    @Override
    public List<Map<String,String>> selectWorldInfor(String update_time, String nation_name) throws Exception {
    	worldVO = new WorldCorona();
    	worldVO.setUpdateTime(update_time);
    	worldVO.setNationName(nation_name);
        return dao.selectWorldInfor(worldVO);
    }
    @Override
    public List<Map<String,String>> selectWorldTimeInfor(String update_time) throws Exception {
    	worldVO = new WorldCorona();
    	worldVO.setUpdateTime(update_time);
        return dao.selectTimeWorldInfor(worldVO);
    }
    @Override
    public List<Map<String,String>> selectWorldNationInfor(String nation_name) throws Exception {
    	worldVO = new WorldCorona();
    	worldVO.setNationName(nation_name);
        return dao.selectNationWorldInfor(worldVO);
    }
    @Override
    public List<Map<String, String>> selectRegionInfor(String update_time, String country_name) throws Exception {
    	regionVO = new RegionCorona();
    	regionVO.setUpdateTime(update_time);
    	regionVO.setCountryName(country_name);
        return dao.selectRegionInfor(regionVO);
    }
    @Override
    public List<Map<String,String>> selectRegionTimeInfor(String update_time) throws Exception {
    	regionVO = new RegionCorona();
    	regionVO.setUpdateTime(update_time);
        return dao.selectTimeRegionInfor(regionVO);
    }
    @Override
    public List<Map<String,String>> selectRegionCountryInfor(String country_name) throws Exception {
    	regionVO = new RegionCorona();
    	regionVO.setCountryName(country_name);
        return dao.selectCountryRegionInfor(regionVO);
    }
}
