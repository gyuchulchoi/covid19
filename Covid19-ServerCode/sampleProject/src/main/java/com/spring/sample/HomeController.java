package com.spring.sample;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.ModelAndView;

import com.example.service.CoronaService;
import com.example.dao.CoronaDAO;
import com.example.dto.RegionCorona;
import com.example.dto.WorldCorona;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 * Handles requests for the application home page.
 */
@Controller
@ContextConfiguration(locations= {"file:src/min/webapp/WEB-INF/spring/**/root-context.xml"})
public class HomeController {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	@Inject
    private CoronaService service;
    @Inject
    private CoronaDAO dao;
	
	@Inject 
    private DataSource ds;
	
	@Inject
    private SqlSessionFactory sqlFactory;
	
	static XSSFRow row;
	
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    @SuppressWarnings("unused")
	private static final String CREDENTIALS_FILE_PATH = "\\credentials.json";
    
    @RequestMapping(value = "/dbTest.do", method = RequestMethod.GET)
    public void testConnection() throws Exception {
        try(Connection con = ds.getConnection()) {
            System.out.println("Connection : " + con + "\n");
            System.out.println("Mybatis Connection -------" + sqlFactory);
            try(SqlSession sqlSession = sqlFactory.openSession() ) {
                System.out.println("세션 연결");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/worldDbSelect.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
    public String worldDbConnection(HttpServletRequest request) throws Exception {
    	
    	String update_time = request.getParameter("update_time");
    	String nation_name = request.getParameter("nation_name");
    	List<Map<String,String>> worldCorona;
    	if(nation_name != null && update_time != null)
    		 worldCorona = service.selectWorldInfor(update_time,nation_name);
    	else if(nation_name == null)
    		worldCorona = service.selectWorldTimeInfor(update_time);
    	else
    		worldCorona = service.selectWorldNationInfor(nation_name);
    	
    	JSONArray jsonArray = new JSONArray();
    	
    	for (Map<String, String> map : worldCorona) {
    		jsonArray.add(convertMapToJson(map));
    	}
    	
    	return jsonArray.toString();
    }
    
    @ResponseBody
	@RequestMapping(value = "/population.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
	public String Population(HttpServletRequest request) throws IOException {
        Document doc = Jsoup.connect("https://superkts.com/population/sum_sido/").get();        
        String country_name = request.getParameter("country_name");
        Elements ele = doc.select("main");
        Elements tables = ele.select("table");
        Elements selec = tables.select("tbody");
        Elements tr = selec.select("tr");
        int to = 0;
        String s="";
        JSONObject jsonData = new JSONObject();
        JSONArray dataArray = new JSONArray(); // 국가의 Jason 정보를 담을 Array 선언
        for(int i=1; i<tr.size(); i++) {
        	//jsonData = new JSONObject();
        	s = tr.eq(i).select("td").eq(2).text();
        	if(i==1)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("gangwon",to);
        	}
        	else if(i==2)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("gyeonggi",to);
        	}
        	else if(i==3)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("gyeongnam",to);
        	}
        	else if(i==4)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("gyeongbuk",to);
        	}
        	else if(i==5)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("gwangju",to);
        	}
        	else if(i==6)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("deagu",to);
        	}
        	else if(i==7)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("daejeon",to);
        	}
        	else if(i==8)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("busan",to);
        	}
        	else if(i==9)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("seoul", to);
        	}
        	else if(i==10)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("sejong",to);
        	}
        	else if(i==11)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("ulsan",to);
        	}
        	else if(i==12)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("incheon",to);
        	}
        	else if(i==13)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("jeonnam",to);
        	}
        	else if(i==14)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("jeonbuk",to);
        	}
        	else if(i==15)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("jeju",to);
        	}
        	else if(i==16)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("chungnam",to);
        	}
        	else if(i==17)
        	{
        		s = s.replace(",", "");
        		to = Integer.parseInt(s);
        		jsonData.put("chungbuk",to);
        	}
        }
        
        //dataArray.add(jsonData);
        return jsonData.toString();
    }
    @ResponseBody
	@RequestMapping(value = "/csvToJson.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
	public String csvToJson(HttpServletRequest request) throws IOException {
        Document doc = Jsoup.connect("https://github.com/jooeungen/coronaboard_kr/blob/master/kr_regional_daily.csv").get();        
        String country_name = request.getParameter("country_name");
        Elements tables = doc.select("table");
        
        Elements selec = tables.select("tbody");
        Elements tr = selec.select("tr");
        JSONArray dataArray = new JSONArray(); // 국가의 Jason 정보를 담을 Array 선언
        
        String s = "";
        String current_name = "";
        
        if(country_name.equals("seoul"))
        	country_name = "서울";
        else if(country_name.equals("busan"))
        	country_name = "부산";
        else if(country_name.equals("daegu"))
        	country_name = "대구";
        else if(country_name.equals("incheon"))
        	country_name = "인천";
        else if(country_name.equals("gwangju"))
        	country_name = "광주";
        else if(country_name.equals("sejong"))
        	country_name = "세종";
        else if(country_name.equals("daejeon"))
        	country_name = "대전";
        else if(country_name.equals("ulsan"))
        	country_name = "울산";
        else if(country_name.equals("gyeonggi"))
        	country_name = "경기";
        else if(country_name.equals("gangwon"))
        	country_name = "강원";
        else if(country_name.equals("chungbuk"))
        	country_name = "충북";
        else if(country_name.equals("chungnam"))
        	country_name = "충남";
        else if(country_name.equals("jeonbuk"))
        	country_name = "전북";
        else if(country_name.equals("jeonnam"))
        	country_name = "전남";
        else if(country_name.equals("gyeongbuk"))
        	country_name = "경북";
        else if(country_name.equals("gyeongnam"))
        	country_name = "경남";
        else if(country_name.equals("jeju"))
        	country_name = "제주";
        else if(country_name.equals("검역"))
        	country_name = "quarantine";
       
        
        for(int i = 0; i < tr.size(); i++) {
        	JSONObject jsonData = new JSONObject();
        	for(int j=0; j<6; j++) {
        		current_name = tr.eq(i).select("td").eq(2).text();
        		if(j == 1 && current_name.equals(country_name))        //날짜
        		{
        			s = tr.eq(i).select("td").eq(j).text();
        			if(s == null || s == "")
        				s = "0";
        			jsonData.put("update_time", s);
        		}
        		else if(j == 2 && current_name.equals(country_name)) {
        			s = tr.eq(i).select("td").eq(j).text();
        			if(s == null || s == "")
        				s = "0";
        			jsonData.put("country_name", s);
        		}
        		else if(j == 3 && current_name.equals(country_name)) {
        			s = tr.eq(i).select("td").eq(j).text();
        			if(s == null || s == "")
        				s = "0";
        			jsonData.put("confirmed", s);
        		}
        		else if(j == 4 && current_name.equals(country_name)) {
        			s = tr.eq(i).select("td").eq(j).text();
        			if(s == null || s == "")
        				s = "0";
        			jsonData.put("death", s);
        		}
        		else if(j == 5 && current_name.equals(country_name)) {
        			s = tr.eq(i).select("td").eq(j).text();
        			if(s == null || s == "")
        				s = "0";
        			jsonData.put("released", s);
        		}
        	}
        	if(current_name.equals(country_name))
        		dataArray.add(jsonData);
        }
    	return dataArray.toString();
	}
    
    @ResponseBody
    @RequestMapping(value = "regionDbSelect.do/", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
    public String regionDbConnection(HttpServletRequest request) throws Exception {
    	String update_time = request.getParameter("update_time");
    	String country_name = request.getParameter("country_name");
    	List<Map<String,String>> regionCorona = service.selectRegionInfor(update_time, country_name);
    	if(country_name != null && update_time != null)
    		regionCorona = service.selectRegionInfor(update_time, country_name);
    	else if(country_name == null)
    		regionCorona = service.selectRegionTimeInfor(update_time);
    	else
    		regionCorona = service.selectRegionCountryInfor(country_name);
    	JSONArray jsonArray = new JSONArray();

    	for (Map<String, String> map : regionCorona) {
    		jsonArray.add(convertMapToJson(map));
    	}
    	return jsonArray.toString();
    }

    @SuppressWarnings({ "unchecked" })

    public static JSONObject convertMapToJson(Map<String, String> map) {

    	JSONObject json = new JSONObject();

    	for (Map.Entry<String, String> entry : map.entrySet()) {

    		String key = entry.getKey();

    		Object value = entry.getValue();

    		// json.addProperty(key, value);

    		json.put(key, value);

    	}

    	return json;

    }

	private static String get(String apiUrl, Map<String, String> requestHeaders) {
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBody(con.getInputStream());
            } else { // 에러 발생
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }
	
	private static HttpURLConnection connect(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection) url.openConnection();
        } catch(MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch(IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }
	
	private static String readBody(InputStream body) {
        InputStreamReader streamReader = new InputStreamReader(body);

        try(BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch(IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }
	
	@ResponseBody
	@RequestMapping(value = "/totalCorona.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
	public String totalCorona(HttpServletRequest request) {
		String apiKey = "98138b3b3cdd13084777fd0bd6adc6432";
		
        String apiURL = "http://api.corona-19.kr/korea/?serviceKey=" + apiKey ; // xml 결과

        Map<String, String> requestHeaders = new HashMap<>();
        String responseBody = get(apiURL,requestHeaders);
		
		return responseBody;
	}
	
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/regionCoronaData.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
	public String regionCoronaData(HttpServletRequest request) throws ParseException {
	
		String apiKey = "98138b3b3cdd13084777fd0bd6adc6432";
        String apiURL = "http://api.corona-19.kr/korea/country/new/?serviceKey=" + apiKey ; // xml 결과

        Map<String, String> requestHeaders = new HashMap<>();
        String responseBody = get(apiURL,requestHeaders);
        
        String apiURL1 = "http://api.corona-19.kr/korea/?serviceKey=" + apiKey ; // xml 결과
        Map<String, String> requestHeaders1 = new HashMap<>();
        String responseBody1 = get(apiURL1,requestHeaders1);
        JSONParser jsonParse1 = new JSONParser(); 
        JSONObject jsonObj1 = (JSONObject) jsonParse1.parse(responseBody1);
        String updateTime = (String) jsonObj1.get("updateTime");
        RegionCorona regionCorona = new RegionCorona();
        JSONParser jsonParse = new JSONParser(); 
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        
        Date date = new Date();
        String nowTime = format.format(date);
        
        JSONObject jsonObj = (JSONObject) jsonParse.parse(responseBody);
        JSONObject seoul = (JSONObject) jsonObj.get("seoul");
        JSONObject busan = (JSONObject) jsonObj.get("busan");
        JSONObject daegu = (JSONObject) jsonObj.get("daegu");
        JSONObject incheon = (JSONObject) jsonObj.get("incheon");
        JSONObject gwangju = (JSONObject) jsonObj.get("gwangju");
        JSONObject daejeon = (JSONObject) jsonObj.get("daejeon");
        JSONObject ulsan = (JSONObject) jsonObj.get("ulsan");
        JSONObject sejong = (JSONObject) jsonObj.get("sejong");
        JSONObject gyeonggi = (JSONObject) jsonObj.get("gyeonggi");
        JSONObject gangwon = (JSONObject) jsonObj.get("gangwon");
        JSONObject chungbuk = (JSONObject) jsonObj.get("chungbuk");
        JSONObject chungnam = (JSONObject) jsonObj.get("chungnam");
        JSONObject jeonbuk = (JSONObject) jsonObj.get("jeonbuk");
        JSONObject jeonnam = (JSONObject) jsonObj.get("jeonnam");
        JSONObject gyeongbuk = (JSONObject) jsonObj.get("gyeongbuk");
        JSONObject gyeongnam = (JSONObject) jsonObj.get("gyeongnam");
        JSONObject jeju = (JSONObject) jsonObj.get("jeju");
        JSONObject quarantine = (JSONObject) jsonObj.get("quarantine");
        jsonObj.remove("resultMessage");
        jsonObj.remove("resultCode");
        jsonObj.remove("korea");
        
        regionInfor(regionCorona, seoul, "seoul",nowTime);
        regionInfor(regionCorona, busan, "busan",nowTime);
        regionInfor(regionCorona, daegu, "deagu",nowTime);
        regionInfor(regionCorona, incheon, "incheon",nowTime);
        regionInfor(regionCorona, gwangju, "gwangju",nowTime);
        regionInfor(regionCorona, daejeon, "daejeon",nowTime);
        regionInfor(regionCorona, ulsan, "ulsan",nowTime);
        regionInfor(regionCorona, sejong, "sejong",nowTime);
        regionInfor(regionCorona, gyeonggi, "gyeonggi",nowTime);
        regionInfor(regionCorona, gangwon, "gangwon",nowTime);
        regionInfor(regionCorona, chungbuk, "chungbuk",nowTime);
        regionInfor(regionCorona, chungnam, "chungnam",nowTime);
        regionInfor(regionCorona, jeonbuk, "jeonbuk",nowTime);
        regionInfor(regionCorona, jeonnam, "jeonnam",nowTime);
        regionInfor(regionCorona, gyeongbuk, "gyeongbuk",nowTime);
        regionInfor(regionCorona, gyeongnam, "gyeongnam",nowTime);
        regionInfor(regionCorona, jeju, "jeju",nowTime);
        regionInfor(regionCorona, quarantine, "quarantine",nowTime);
        return "regionCorona";
	}
	
	
	
	public void regionInfor(RegionCorona allRegion, JSONObject region,String regionName, String updateTime) {
		allRegion.setUpdateTime(updateTime);
		allRegion.setCountryName(regionName);
		allRegion.setDeath(Integer.parseInt(region.get("death").toString().replace(",", "")));
		allRegion.setTotalCase(Integer.parseInt(region.get("totalCase").toString().replace(",", "")));
		allRegion.setNewCase(Integer.parseInt(region.get("newCase").toString().replace(",", "")));
		allRegion.setNewFCase(Integer.parseInt(region.get("newFcase").toString().replace(",", "")));
		allRegion.setNewCCase(Integer.parseInt(region.get("newCcase").toString().replace(",", "")));
		if(regionName.equals("quarantine") == true)
			allRegion.setPercentage(0);
		else 
			allRegion.setPercentage(Float.parseFloat(region.get("percentage").toString().replace(",", "")));
		allRegion.setRecovered(Integer.parseInt(region.get("recovered").toString().replace(",", "")));
		dao.insertRegion(allRegion);
		
	}
	public JSONObject regionStoI(JSONObject region, String regionName) {
		int death = Integer.parseInt(region.get("death").toString().replace(",", ""));
		int totalCase = Integer.parseInt(region.get("totalCase").toString().replace(",", ""));
		int newCase = Integer.parseInt(region.get("newCase").toString().replace(",", ""));
		int newFCase = Integer.parseInt(region.get("newFcase").toString().replace(",", ""));
		int newCCase = Integer.parseInt(region.get("newCcase").toString().replace(",", ""));
		float percentage = 0;
		if(regionName.equals("quarantine") == true)
			percentage = 0;
		else
			percentage = Float.parseFloat(region.get("percentage").toString().replace(",", ""));
		int recovered = Integer.parseInt(region.get("recovered").toString().replace(",", ""));
		region.put("death",Integer.toString(death));
		region.put("totalCase",Integer.toString(totalCase));
		region.put("newCase",Integer.toString(newCase));
		region.put("newFcase", Integer.toString(newFCase));
		region.put("newCcase",Integer.toString(newCCase));
		region.put("percentage",Float.toString(percentage));
		region.put("recovered", Integer.toString(recovered));
		
		return region;
	}
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/regionCorona.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
	public String regionCorona(HttpServletRequest request) throws ParseException {
		String apiKey = "98138b3b3cdd13084777fd0bd6adc6432";
		
        String apiURL = "http://api.corona-19.kr/korea/country/new/?serviceKey=" + apiKey ; // xml 결과

        Map<String, String> requestHeaders = new HashMap<>();
        String responseBody = get(apiURL,requestHeaders);
        
        String apiURL1 = "http://api.corona-19.kr/korea/?serviceKey=" + apiKey ; // xml 결과
        Map<String, String> requestHeaders1 = new HashMap<>();
        String responseBody1 = get(apiURL1,requestHeaders1);
        JSONParser jsonParse1 = new JSONParser(); 
        JSONObject jsonObj1 = (JSONObject) jsonParse1.parse(responseBody1);
        String updateTime = (String) jsonObj1.get("updateTime");
        
        JSONParser jsonParse = new JSONParser(); 
        
        //JSONParse에 json데이터를 넣어 파싱한 다음 JSONObject로 변환한다. 
        JSONObject jsonObj = (JSONObject) jsonParse.parse(responseBody);
        JSONObject seoul = (JSONObject) jsonObj.get("seoul");
        JSONObject busan = (JSONObject) jsonObj.get("busan");
        JSONObject daegu = (JSONObject) jsonObj.get("daegu");
        JSONObject incheon = (JSONObject) jsonObj.get("incheon");
        JSONObject gwangju = (JSONObject) jsonObj.get("gwangju");
        JSONObject daejeon = (JSONObject) jsonObj.get("daejeon");
        JSONObject ulsan = (JSONObject) jsonObj.get("ulsan");
        JSONObject sejong = (JSONObject) jsonObj.get("sejong");
        JSONObject gyeonggi = (JSONObject) jsonObj.get("gyeonggi");
        JSONObject gangwon = (JSONObject) jsonObj.get("gangwon");
        JSONObject chungbuk = (JSONObject) jsonObj.get("chungbuk");
        JSONObject chungnam = (JSONObject) jsonObj.get("chungnam");
        JSONObject jeonbuk = (JSONObject) jsonObj.get("jeonbuk");
        JSONObject jeonnam = (JSONObject) jsonObj.get("jeonnam");
        JSONObject gyeongbuk = (JSONObject) jsonObj.get("gyeongbuk");
        JSONObject gyeongnam = (JSONObject) jsonObj.get("gyeongnam");
        JSONObject jeju = (JSONObject) jsonObj.get("jeju");
        JSONObject quarantine = (JSONObject) jsonObj.get("quarantine");
        System.out.println();
        
        jsonObj.remove("resultMessage");
        jsonObj.remove("resultCode");
        jsonObj.remove("korea");
        
        seoul.put("lat", "37.566535");
        seoul.put("lng", "126.9779692");
        busan.put("lat", "35.1795543");
        busan.put("lng", "129.0756416");
        daegu.put("lat", "35.8714354");
        daegu.put("lng", "128.601445");
        incheon.put("lat", "37.4562557");
        incheon.put("lng", "126.7052062");
        gwangju.put("lat", "35.1595454");
        gwangju.put("lng", "126.8526012");
        daejeon.put("lat", "36.3504119");
        daejeon.put("lng", "127.3845475");
        ulsan.put("lat", "35.5383773");
        ulsan.put("lng", "129.3113596");
        sejong.put("lat", "36.4800984");
        sejong.put("lng", "127.2890354");
        gyeonggi.put("lat", "37.41379999999999");
        gyeonggi.put("lng", "127.5183");
        gangwon.put("lat", "37.8228");
        gangwon.put("lng", "128.1555");
        chungbuk.put("lat", "36.8");
        chungbuk.put("lng", "127.7");
        chungnam.put("lat", "36.5184");
        chungnam.put("lng", "126.8");
        jeonbuk.put("lat", "35.71750000000001");
        jeonbuk.put("lng", "127.153");
        jeonnam.put("lat", "34.8679");
        jeonnam.put("lng", "126.991");
        gyeongbuk.put("lat", "36.4919");
        gyeongbuk.put("lng", "128.8889");
        gyeongnam.put("lat", "35.4606");
        gyeongnam.put("lng", "128.2132");
        jeju.put("lat", "33.4890113");
        jeju.put("lng", "126.4983023");
        seoul = regionStoI(seoul, "seoul");
        busan = regionStoI(busan, "busan");
        daegu = regionStoI(daegu, "daegu");
        gwangju = regionStoI(gwangju, "gwangju");
        daejeon = regionStoI(daejeon,"daejeon");
        ulsan = regionStoI(ulsan, "ulsan");
        sejong = regionStoI(sejong, "sejong");
        gyeonggi = regionStoI(gyeonggi,"gyeonggi");
        gangwon = regionStoI(gangwon,"gangwon");
        chungbuk = regionStoI(chungbuk, "chungbuk");
        chungnam = regionStoI(chungnam, "chungnam");
        jeonbuk = regionStoI(jeonbuk, "jeonbuk");
        jeonnam = regionStoI(jeonnam, "jeonnam");
        gyeongbuk = regionStoI(gyeongbuk, "gyeongbuk");
        gyeongnam = regionStoI(gyeongnam, "gyeongnam");
        jeju = regionStoI(jeju, "jeju");
        
        responseBody = jsonObj.toJSONString();
        @SuppressWarnings("resource")
		XSSFWorkbook xlsWb = new XSSFWorkbook();
        Sheet sheet1 = xlsWb.createSheet("코로나 현황");
        CellStyle cellStyle = xlsWb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER); //가운데 정렬
        
        Row row = null;
        
        row = sheet1.createRow(0);
        row.createCell(0).setCellValue("완치자수");
        row.createCell(1).setCellValue("신규확진환자수");
        row.createCell(2).setCellValue("사망자");
        row.createCell(3).setCellValue("전일대비증감-해외유입");
        row.createCell(4).setCellValue("전일대비증감-지역발생");
        row.createCell(5).setCellValue("발생률");
        row.createCell(6).setCellValue("시도명(지역명)");
        row.createCell(7).setCellValue("확진환자수");
        row.createCell(8).setCellValue("날짜");
        sheet1.setColumnWidth(0, 3000);
        sheet1.setColumnWidth(1, 4000);
        sheet1.setColumnWidth(2, 2500);
        sheet1.setColumnWidth(3, 5500);
        sheet1.setColumnWidth(4, 5500);
        sheet1.setColumnWidth(5, 2500);
        sheet1.setColumnWidth(6, 4000);
        sheet1.setColumnWidth(7, 3000);
        
        regionExcelCellvalue(seoul, row, 1, sheet1, updateTime);
        regionExcelCellvalue(busan, row, 2, sheet1, updateTime);
        regionExcelCellvalue(daegu, row, 3, sheet1, updateTime);
        regionExcelCellvalue(incheon, row, 4, sheet1, updateTime);
        regionExcelCellvalue(gwangju, row, 5, sheet1, updateTime);
        regionExcelCellvalue(daejeon, row, 6, sheet1, updateTime);
        regionExcelCellvalue(ulsan, row, 7, sheet1, updateTime);
        regionExcelCellvalue(sejong, row, 8, sheet1, updateTime);
        regionExcelCellvalue(gyeonggi, row, 9, sheet1, updateTime);
        regionExcelCellvalue(gangwon, row, 10, sheet1, updateTime);
        regionExcelCellvalue(chungbuk, row, 11, sheet1, updateTime);
        regionExcelCellvalue(chungnam, row, 12, sheet1, updateTime);
        regionExcelCellvalue(jeonbuk, row, 13, sheet1, updateTime);
        regionExcelCellvalue(jeonnam, row, 14, sheet1, updateTime);
        regionExcelCellvalue(gyeongbuk, row, 15, sheet1, updateTime);
        regionExcelCellvalue(gyeongnam, row, 16, sheet1, updateTime);
        regionExcelCellvalue(jeju, row, 17, sheet1, updateTime);
        regionExcelCellvalue(quarantine, row, 18, sheet1, updateTime);
        row.createCell(0).setCellValue((String) gyeongnam.get("recovered"));
        //로컬폴더에 파일 생성
        HttpSession session = request.getSession(); 
        String root_path = session.getServletContext().getRealPath("");
    	String filePath = root_path + "\\excel.xlsx";
        fileCreate(filePath, xlsWb);
        
		return responseBody;
	}
	
	public void regionExcelCellvalue(JSONObject region, Row row, int index, Sheet sheet1, String updateTime) {
		row = sheet1.createRow(index);
		row.createCell(0).setCellValue((String) region.get("recovered"));
        row.createCell(1).setCellValue((String) region.get("newCase"));
        row.createCell(2).setCellValue((String) region.get("death"));
        row.createCell(3).setCellValue((String) region.get("newFcase"));
        row.createCell(4).setCellValue((String) region.get("newCcase"));
        row.createCell(5).setCellValue((String) region.get("percentage") + "%");
        row.createCell(6).setCellValue((String) region.get("countryName"));
        row.createCell(7).setCellValue((String) region.get("totalCase"));
        row.createCell(8).setCellValue(updateTime);
	}
	
	public boolean fileDel(String filePath) {
		File file = new File(filePath); 
		if(file.exists()) { 
			if(file.delete()) { 
				System.out.println("파일삭제 성공"); 
			} else { 
				System.out.println("파일삭제 실패");
			} 
		} else { 
			System.out.println("파일이 존재하지 않습니다."); 
		}
		return true;
	}
	
	public boolean fileCreate(String filePath, XSSFWorkbook xlsWb) {
		// 출력 파일 위치및 파일명 설정
        FileOutputStream fos;
        File file=null;
        try {
            	file = new File(filePath);
            	
    			fos = new FileOutputStream(file);
    			xlsWb.write(fos);
                 
    			fos.close();
                System.out.println("파일생성 완료");
				
                return true;
        } catch(Exception e) {
            e.printStackTrace();
        }
		return true;
	}
    
    @RequestMapping(value="/download.do", produces="text/plain;charset=UTF-8")
    public void fileDownload(HttpServletRequest request, HttpServletResponse response) {
    	HttpSession session = request.getSession(); 
    	String root_path = session.getServletContext().getRealPath("");
    	String fileName = "excel.xlsx";

        File file = null;
        InputStream is = null;
        OutputStream os = null;
     
        String mimetype = "application/x-msdownload";
        response.setContentType(mimetype);
     
        try {
            setDisposition(fileName, request, response);
      
            file = new File(root_path + "\\excel.xlsx");
            is = new FileInputStream(file);
            os = response.getOutputStream();
            
            byte b[] = new byte[(int) file.length()];
            int leng = 0;
      
            while((leng = is.read(b)) > 0){
                os.write(b,0,leng);
            }
      
            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    void setDisposition(String filename, HttpServletRequest request, HttpServletResponse response) throws Exception {
	    String browser = getBrowser(request);
	    String dispositionPrefix = "attachment; filename=";
	    String encodedFilename = null;
	 
	    if(browser.equals("MSIE")) {
	        encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
	    } else if(browser.equals("Firefox")) {
	        encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
	    } else if(browser.equals("Opera")) {
	        encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
	    } else if(browser.equals("Chrome")) {
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < filename.length(); i++) {
	        	char c = filename.charAt(i);
	        	if (c > '~') {
	        		sb.append(URLEncoder.encode("" + c, "UTF-8"));
	        	} else {
	        		sb.append(c);
	        	}
	        }
	        encodedFilename = sb.toString();
	    } else {
	        throw new IOException("Not supported browser");
	    }
	    response.setHeader("Content-Disposition", dispositionPrefix + encodedFilename);
	 
	    if("Opera".equals(browser)) {
	        response.setContentType("application/octet-stream;charset=UTF-8");
	    }
	}
    
    private String getBrowser(HttpServletRequest request) {
        String header = request.getHeader("User-Agent");
        if(header.indexOf("MSIE") > -1) {
             return "MSIE";
        } else if(header.indexOf("Chrome") > -1) {
             return "Chrome";
        } else if(header.indexOf("Opera") > -1) {
             return "Opera";
        } else if(header.indexOf("Firefox") > -1) {
             return "Firefox";
        } else if(header.indexOf("Mozilla") > -1) {
             if(header.indexOf("Firefox") > -1) {
                  return "Firefox";
             } else {
                  return "MSIE";
             }
        }
        return "MSIE";
   }
    
    /**
	 * Simply selects the home view to render by returning its name.
     * @throws IOException 
	 */
    @ResponseBody
	@RequestMapping(value = "/worldCoronaData.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
	public String worldCoronaData(HttpServletRequest request) throws IOException {
    	Document doc = Jsoup.connect("https://www.worldometers.info/coronavirus/").get();        
        Elements tables = doc.select("#main_table_countries_today");
        Elements th = tables.select("thead").select("tr").select("th");
        Elements selec = tables.select("tbody").eq(0);
        Elements tr = selec.select("tr");
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        
        Date date = new Date();
        String nowTime = format.format(date);
        
        JSONArray dataArray = new JSONArray(); // 국가의 Jason 정보를 담을 Array 선언
        
        String s = "";
        String name ="";
        String time = "";
        int n1=0;
        int n2=0;
        int n3=0; 
        int n4=0;
        int n5=0;
		WorldCorona worldCorona = new WorldCorona();
        for(int i = 0; i < tr.size(); i++) {
        	if(i > 7) {
        		for(int j = 1; j < 7; j++) {
	        		try {
        				s = tr.eq(i).select("td").get(j).text(); //s[i][1] => 나라이름
        				System.out.println(i + " " + j);
        				if(j == 1) {
        					name = s;
        					System.out.println(s);
        				}
        				else if(j == 2) {
        					s = s.replace(",", "");
        					s = s.replace("+", "");
        					s = s.replace("N\\/A", "");
        					s = s.replace("N/A", "");
        					int to;
        					if(s.equals("")) {
        						to = 0;
        					}
        					else {
        						to = Integer.parseInt(s);
        					}
        					n1 = Integer.parseInt(s);
        				}
        				else if(j == 3) {
        					s = s.replace(",", "");
        					s = s.replace("+", "");
        					s = s.replace("N\\/A", "");
        					s = s.replace("N/A", "");
        					int to = 0;
        					if(s.equals("")) {
        					//	to = 0;
        					}
        					else {
        						to = Integer.parseInt(s);
        					}
        					n2 = to;
        				}
        				else if(j == 4) {
        					s = s.replace(",", "");
        					s = s.replace("+", "");
        					s = s.replace("N\\/A", "");
        					s = s.replace("N/A", "");
        					System.out.println(s);
        					int to;
        					if(s.equals("")) {
        						to = 0;
        					}
        					else {
        						to = Integer.parseInt(s);
        					}
        					n3 = to;
        				}
        				else if(j == 5) {
        					s = s.replace(",", "");
        					s = s.replace("+", "");
        					s = s.replace("N\\/A", "");
        					s = s.replace("N/A", "");
        					int to;
        					if(s.equals("")) {
        						to = 0;
        					}
        					else {
        						to = Integer.parseInt(s);
        					}
        					n4 = to;
        				}
        				else if(j == 6) {
        					s = s.replace(",", "");
        					s = s.replace("+", "");
        					s = s.replace("N\\/A", "");
        					s = s.replace("N/A", "");
        					int to;
        					if(s.equals("")) {
        						to = 0;
        					}
        					else {
        						to = Integer.parseInt(s);
        					}
        					n5 = to;
        				}
        			}
	        		
	        		catch(IndexOutOfBoundsException e) {
	        			e.printStackTrace();
	        		}
	        	}
	            worldCorona.setUpdateTime(nowTime);
	            worldCorona.setNationName(name);
	            worldCorona.setNewDeath(n4);
	            worldCorona.setTotalCase(n1);
	            worldCorona.setTotalDeath(n3);
	            worldCorona.setTotalRecovered(n5);
	            worldCorona.setNewCase(n2);
	            dao.insertWorld(worldCorona);
        	}
        }
        
    	return "worldCorona";
    }
    
	@ResponseBody
	@RequestMapping(value = "/worldCorona.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
	public String worldCorona(HttpServletRequest request) throws IOException {
        Document doc = Jsoup.connect("https://www.worldometers.info/coronavirus/").get();        
        Elements tables = doc.select("#main_table_countries_today");
        Elements th = tables.select("thead").select("tr").select("th");
        Elements selec = tables.select("tbody").eq(0);
        Elements tr = selec.select("tr");
        
        JSONArray dataArray = new JSONArray(); // 국가의 Jason 정보를 담을 Array 선언
        
        String s = "";
        String name;
        String time = "";
        int n1;
        int n2;
        int n3; 
        int n4;
        int n5;
        
        for(int i = 0; i < tr.size(); i++) {
        	if(i > 7) {
        		JSONObject jsonData = new JSONObject(); //한 국가 정보 들어갈 JSONObject
        		for(int j = 1; j < 7; j++) {
	        		try {
        				s = tr.eq(i).select("td").get(j).text(); //s[i][1] => 나라이름
        				
        				if(j == 1) {
        					jsonData.put("Name",s);
        					name = s;
        					//worldCorona.setNationName(s);
        				}
        				else if(j == 2) {
        					s = s.replace(",", "");
        					int to = Integer.parseInt(s);
        					jsonData.put("totalCases", to);
        					//worldCorona.setTotalCase(to);
        				}
        				else if(j == 3) {
        					s = s.replace(",", "");
        					s = s.replace("+", "");
        					s = s.replace("N\\/A", "");
        					s = s.replace("N/A", "");
        					int to;
        					if(s.equals("")) {
        						to = 0;
        					}
        					else {
        						to = Integer.parseInt(s);
        					}
        					jsonData.put("newCases", to);
        					
        				}
        				else if(j == 4) {
        					s = s.replace(",", "");
        					s = s.replace("+", "");
        					s = s.replace("N\\/A", "");
        					s = s.replace("N/A", "");
        					int to;
        					if(s.equals("")) {
        						to = 0;
        					}
        					else {
        						to = Integer.parseInt(s);
        					}
        					jsonData.put("totalDeaths",to);
        				}
        				else if(j == 5) {
        					s = s.replace(",", "");
        					s = s.replace("+", "");
        					s = s.replace("N\\/A", "");
        					s = s.replace("N/A", "");
        					int to;
        					if(s.equals("")) {
        						to = 0;
        					}
        					else {
        						to = Integer.parseInt(s);
        					}
        					jsonData.put("newDeaths", to);
        				}
        				else if(j == 6) {
        					s = s.replace(",", "");
        					s = s.replace("+", "");
        					s = s.replace("N\\/A", "");
        					s = s.replace("N/A", "");
        					int to;
        					if(s.equals("")) {
        						to = 0;
        					}
        					else {
        						to = Integer.parseInt(s);
        					}
        					jsonData.put("totalRecovered", to);
        				}
        			}
	        		
	        		catch(IndexOutOfBoundsException e) {
	        			e.printStackTrace();
	        		}
	        	}
        		
	        	dataArray.add(jsonData);
        	}
        }
    	return dataArray.toString();
	}
	
	@ResponseBody
	@RequestMapping(value = "/worldCoronaCheck.do", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")  //코로나 총 인원 정리
	public String worldCoronaCheck(HttpServletRequest request) throws IOException {
	    Document doc = Jsoup.connect("https://www.worldometers.info/coronavirus/").get();
	    Elements mainData = doc.select("div.content-inner");
	    Elements data1 = mainData.select("div.maincounter-number").eq(0).select("span");
	    Elements data2 = mainData.select("div.maincounter-number").eq(1).select("span");
	    Elements data3 = mainData.select("div.maincounter-number").eq(2).select("span");
	    Elements tables = doc.select("#main_table_countries_yesterday");
        Elements selec = tables.select("tbody").eq(0);
        Elements tr = selec.select("tr");
        JSONArray dataArray = new JSONArray();
        JSONObject jsonData = new JSONObject();
        String oldAllVirus = "";
        String oldAllDeaths = "";
        String oldAllRecovered = "";
        String allVirus = "";
	    String allDeaths = "";
	    String allRecovered = "";
        int oldV = 0;
        int oldD = 0;
        int oldR = 0;
        int virus = 0;
        int deaths = 0;
        int recovered = 0;
        
        oldAllVirus = tr.eq(7).select("td").get(2).text();
        oldAllDeaths = tr.eq(7).select("td").get(4).text();
        oldAllRecovered = tr.eq(7).select("td").get(6).text();
        
        oldV = Integer.parseInt(oldAllVirus.replace(",", ""));
		oldD = Integer.parseInt(oldAllDeaths.replace(",", ""));
		oldR = Integer.parseInt( oldAllRecovered.replace(",", ""));
     
	    allVirus = data1.text();
	    allDeaths = data2.text();
	    allRecovered = data3.text();
	   
		virus = Integer.parseInt(allVirus.replace(",", ""));
	    jsonData.put("allVirus", virus);
	    
		deaths = Integer.parseInt(allDeaths.replace(",", ""));
	    jsonData.put("allDeaths", deaths);
	    
		recovered = Integer.parseInt(allRecovered.replace(",", ""));
	    jsonData.put("allRecovered", recovered);
	    
	    jsonData.put("changeVirus", virus - oldV);
	    jsonData.put("changeDeaths", deaths - oldD);
	    jsonData.put("changeRecovered",recovered - oldR);
	    
		return jsonData.toString();
	}
	
	/**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    @SuppressWarnings("unused")
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, HttpServletRequest request) throws IOException {
    	HttpSession session = request.getSession(); 
        String root_path = session.getServletContext().getRealPath("");
    	String filePath = root_path + "credentials.json";
        // Load client secrets.
        InputStream in = new FileInputStream(filePath);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + filePath);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
	
	@RequestMapping(value = "/googleDrive.do", method = RequestMethod.GET)
	public void googleDrive(HttpServletRequest request) throws IOException, GeneralSecurityException {
		// Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, request))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setPageSize(20)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<com.google.api.services.drive.model.File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (com.google.api.services.drive.model.File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
	}
	
}
