package main;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.util.JSON;


public class WriteToDB {
	
	public void setDataBase(AggieHome home) throws IOException{
		
		String jsonSetParObj = jsonGen(home);
		
		DBObject data = (DBObject)JSON.parse(jsonSetParObj);
		//System.out.println(jsonSetParObj);
		MongoClientURI uri = new MongoClientURI("mongodb://gsfUser:gsfUser@ds030827.mongolab.com:30827/aggievillage");
		try{
			MongoClient mongoClient = new MongoClient(uri);
			DB db = mongoClient.getDB(uri.getDatabase());
			System.out.println("Connect to database successfully");
			DBCollection testJson = db.getCollection("calibration");
			testJson.insert(data);
//			DBCursor cursor = testJson.find();
//			int i=1;
//	         while (cursor.hasNext()) { 
//	            System.out.println("Inserted Document: "+i); 
//	            DBObject doc = cursor.next();
//	            String json1 = doc.toString();
//	            System.out.println(json1); 
//	            
//	            i++;
//	         }
		}catch(Exception e){
			System.out.println("Failed");
		}
	}
	public static String jsonGen(AggieHome home) throws IOException{
		Database d1 = new Database();
		Date date = new Date();
		
		d1.setTime(date.toString());
		
		Overall o1 = new Overall();
		o1.setcPack(String.format("%.2f", home.battery.cPack));
		o1.settMax(String.format("%.2f", home.battery.tMax));
		o1.settMin(String.format("%.2f", home.battery.tMin));
		o1.setvMax(String.format("%.2f", home.battery.vMax));
		o1.setvMin(String.format("%.2f", home.battery.vMin));
		o1.setvPack(String.format("%.2f", home.battery.vPack));
		
		d1.setOverall(o1);
		
		Power p1 = new Power();
		p1.setpB(String.format("%.1f", home.pB));
		p1.setpG(String.format("%.1f", home.pG));
		p1.setpH(String.format("%.1f", home.pH));
		p1.setpP(String.format("%.1f", home.pP));
		
		d1.setPower(p1);
		List<CellIndv> col = new ArrayList<CellIndv>();
		for(int i = 0 ; i < home.battery.nS;i++){
			CellIndv c = new CellIndv();
			c.setCellNumber(String.format("%d",i));
			c.setCellCur(String.format("%.4f", home.battery.cell[i].c));
			c.setCellSocC(String.format("%.4f", home.battery.cell[i].socC));
			c.setCellVol(String.format("%.4f", home.battery.cell[i].v));
			c.setCellTemp(String.format("%.4f", home.battery.cell[i].t));
			c.setCellBalance(String.format("%.4f", home.battery.cell[i].b));
			c.setCellSohC(String.format("%.4f", home.battery.cell[i].sohC));
			c.setCellSohP(String.format("%.4f", home.battery.cell[i].sohP));
			col.add(c);
		}
		
		
		d1.setCellCollect(col);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(d1);
	}
}
