package com.example.dao;
 
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
 
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Repository;
import com.example.dto.WorldCorona;
import com.example.dto.RegionCorona;
 
@Repository
public class CoronaDAOImpl implements CoronaDAO {
 
    @Inject
    private SqlSession sqlSession;
    
    private static final String Namespace = "com.example.mapper.memberMapper";
    
    
    @Override
    public List<Map<String, String>> selectRegionInfor(RegionCorona regionCorona) throws Exception {
    	
        return sqlSession.selectList(Namespace+".selectRegionInfor", regionCorona);
    }
    @Override
    public List<Map<String,String>> selectTimeRegionInfor(RegionCorona regionCorona) throws Exception {
    	
    	return sqlSession.selectList(Namespace+".selectTimeRegionInfor",regionCorona);
    }
    
    @Override
    public List<Map<String,String>> selectCountryRegionInfor(RegionCorona regionCorona) throws Exception {
    	
    	return sqlSession.selectList(Namespace+".selectCountryRegionInfor",regionCorona);
    }
    @Override
    public List<Map<String,String>> selectWorldInfor(WorldCorona worldCorona) throws Exception {
    	
    	return sqlSession.selectList(Namespace+".selectWorldInfor",worldCorona);
    }
    
    @Override
    public List<Map<String,String>> selectTimeWorldInfor(WorldCorona worldCorona) throws Exception {
    	
    	return sqlSession.selectList(Namespace+".selectTimeWorldInfor",worldCorona);
    }
    
    @Override
    public List<Map<String,String>> selectNationWorldInfor(WorldCorona worldCorona) throws Exception {
    	
    	return sqlSession.selectList(Namespace+".selectNationWorldInfor",worldCorona);
    }
    @Override
    public void insertRegion(RegionCorona vo){
 
        sqlSession.insert(Namespace+".insertRegion", vo);
    }
 
    @Override
    public void insertWorld(WorldCorona vo){
 
        sqlSession.insert(Namespace+".insertWorld", vo);
    }

}
