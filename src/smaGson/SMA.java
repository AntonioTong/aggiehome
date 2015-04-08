package smaGson;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;


public class SMA{
	
	 String json = "JSON";
	 String id = "5";
	 String getPD = "GetProcessData";
	 String setParam = "SetParameter";
	 String passwd = "a289fa4252ed5af8e3e9f9bee545c172";
	 String version = "1.0";
	 public BasicParams requestBasic = new BasicParams();
	 public MeasurementObj pvBat=new MeasurementObj(0,0);
	 public BasicParams basicFedinCur = new BasicParams();
	 public boolean status=false;
	 HttpPost smaPost = new HttpPost("http://192.168.30.22:3334/rpc");
	//HttpPost smaPost = new HttpPost("http://ucdavisvillage.ddns.net:3334/rpc");
	 HttpClient httpClient = new DefaultHttpClient();
    public SMA(){
    	/* Following Set up the basic parameters for the 
    	 * get measurement object, can add more strings for the setProc
    	 * for the appropriate parameters
    	 */

    		 requestBasic.setFormat(json);
    		 requestBasic.setId(id);
    		 requestBasic.setProc(getPD);
    		 requestBasic.setPasswd(passwd);
    		 requestBasic.setVersion(version);
    		 
    	/*	This sends in the object into the function to get
    	 *  the PVpower and bat current in return.
    	 */

    		
    		 
    	/* This creates the parameters object needed to set parameters,
    	 * then the parameters are sent to the function,
    	 * resulting in a boolean that tells if the update is completed.
    	 * True = success; False = fail.
    	 */		 

    		 basicFedinCur.setFormat(json);
    		 basicFedinCur.setId(id);
    		 basicFedinCur.setPasswd(passwd);
    		 basicFedinCur.setProc(setParam);
    		 basicFedinCur.setVersion(version);
    		// test to get a measurement 
    		// getSMAmeasure();
    		 //test to set parameter to 50
    		// setParameters(10);
    		     }  

    /**
     * Sends a string of request to the httpResponse.
     * Refer to 
     * http://hc.apache.org/httpcomponents-core-ga/httpcore/apidocs/org/apache/http/HttpResponse.html
     * for more detail.
     * @param request 
     * @return 
     */
    public void httpResponse(String request) {
    	//This function interacts with the sma sunnywebbox
    	
    	this.json = null;
    	try{
    		StringEntity RPC = new StringEntity("RPC="+request);
    		RPC.setContentType("application/json");
    		
    		smaPost.setEntity(RPC);
    		HttpResponse result = httpClient.execute(smaPost);
    		
    		this.json = EntityUtils.toString(result.getEntity(),"UTF-8");
    		
    		smaPost.reset();
    		
    	
    }	
    	catch (IOException ex) {
    		this.status = false;
    		System.out.println("Unable to get http Response");
    		
        }
    }
    /**
     * Takes in two parameters, an object and a value.
     * Function completes the object to json,
     * sends to httpResponse. And compare if the resulting
     * parameter is the same as intended. 
     * @param basic
     * @param power
     * @return
     */
    public void setParameters(double power){
    	//BasicParams basic, double power
    	float inverterCurrent = (float) ((-power/240.0) -0.3);
    	Channels channels = new Channels();
    	channels.setMeta("FedInCurAtCom");
    	channels.setValue(String.format("%.1f", inverterCurrent));
    	List<Channels> ch = new ArrayList<Channels>();
    	ch.add(channels);
    	RespDevices devices = new RespDevices();
    	devices.setKey("SI6048UH:1260017397");
    	devices.setChannels(ch);
    	List<RespDevices> dev = new ArrayList<RespDevices>();
    	dev.add(devices);
    	ParamsSet params = new ParamsSet();
    	params.setDevices(dev);
    	
    	RequestParSet setParObj = new RequestParSet();
    	setParObj.setFormat(this.basicFedinCur.getFormat());
    	setParObj.setId(this.basicFedinCur.getId());
    	setParObj.setPasswd(this.basicFedinCur.getPasswd());
    	setParObj.setProc(this.basicFedinCur.getProc());
    	setParObj.setVersion(this.basicFedinCur.getVersion());
    	setParObj.setParams(params);
    	
    	Gson gson = new GsonBuilder().setPrettyPrinting().create();
    	String jsonSetParObj = gson.toJson(setParObj);

    	this.httpResponse(jsonSetParObj);
    	Detail data = gson.fromJson(this.json, Detail.class);
    	//System.out.println("The newly set " +channels.getMeta() + "is " + Float.parseFloat(data.getResult().getDevices().get(0).getChannels().get(0).getValue()));
//    	if (Float.parseFloat(data.getResult().getDevices().get(0).getChannels().get(0).getValue())==inverterCurrent){
//    		this.status= true;
//    		
//   		 System.out.println("Setting parameter = " + this.status);
//    	}
//    	else{
//    		this.status= false;
//    		System.out.println("Setting parameter = " +this.status);
//    	}

    	
    }
    /**
     * getSMAmeasure first takes in a parameter object,
     * then it completes the object by adding the
     * devices and channels. Then the object is translate
     * into an json object and sends to httpReponse.
     * The result string is parsed back into a Detail object,
     * and the parameters are returned in an object form.
     * @param basic
     * @return
     */

    public void getSMABatCur(){
    	// This function create request to get the PV power and battery current	  
    	  try{
    	  
    	  
    	  Devices batCurDev = new Devices();
    	  batCurDev.setKey("SI6048UH:1260017397");
    	  List<String> chBatCur = new ArrayList<String>();
    	  chBatCur.add("TotBatCur");
    	  batCurDev.setChannels(chBatCur);
    	  
    	  Params par = new Params();
    	  List<Devices> dev = new ArrayList<Devices>();

    	  dev.add(batCurDev);
    	  par.setDevices(dev);
    	  
    			
    	  RequestPar requestParObj = new RequestPar();
    	  requestParObj.setVersion(this.requestBasic.getVersion());
    	  requestParObj.setProc(this.requestBasic.getProc());
    	  requestParObj.setId(this.requestBasic.getId());
    	  requestParObj.setFormat(this.requestBasic.getFormat());
    	  requestParObj.setPasswd(this.requestBasic.getPasswd());
    	  requestParObj.setParams(par);
    	  
    	  Gson gson = new GsonBuilder().setPrettyPrinting().create();
    	  String jsonRequestParObj1 = gson.toJson(requestParObj);
    	  this.httpResponse(jsonRequestParObj1);
    	  Detail data = gson.fromJson(this.json, Detail.class);
    	  

    	  double batCur = Double.parseDouble(data.getResult().getDevices().get(0).getChannels().get(0).getValue());
    	 
    	  

    	  this.pvBat.setBatCur(batCur);

   		  System.out.println("Bat Cur is: " + pvBat.getBatCur());
    	  }catch (Exception e){
    		  System.out.println("Unable to get BatCur measurement from SMA");
    		  this.status = false;
    	  }
    }
    public void getSMAPV(){
    	// This function create request to get the PV power and battery current	  
    	  try{
    	  Devices pVDev = new Devices();
    	  pVDev.setKey("WR30U09E:2002225636");
    	  List<String> chPv = new ArrayList<String>();
    	  chPv.add("Pac");
    	  pVDev.setChannels(chPv);
    	  
    	  
    	  Params par = new Params();
    	  List<Devices> dev = new ArrayList<Devices>();
    	  dev.add(pVDev);
    	  par.setDevices(dev);
    	  
    			
    	  RequestPar requestParObj = new RequestPar();
    	  requestParObj.setVersion(this.requestBasic.getVersion());
    	  requestParObj.setProc(this.requestBasic.getProc());
    	  requestParObj.setId(this.requestBasic.getId());
    	  requestParObj.setFormat(this.requestBasic.getFormat());
    	  requestParObj.setPasswd(this.requestBasic.getPasswd());
    	  requestParObj.setParams(par);
    	  
    	  Gson gson = new GsonBuilder().setPrettyPrinting().create();
    	  String jsonRequestParObj1 = gson.toJson(requestParObj);
    	  this.httpResponse(jsonRequestParObj1);
    	  try{
    	  Detail data = gson.fromJson(this.json, Detail.class);
    	  double pv = Double.parseDouble(data.getResult().getDevices().get(0).getChannels().get(0).getValue());
     	  
    	  this.pvBat.setPv(pv);
    	  }catch(Exception e2)
    	  {

    		  this.pvBat.setPv(0.0);
    	  }
    	  
    	 
    	  
    	  System.out.println("PV is: " + pvBat.getPv());
    	  }catch (Exception e){
    		  System.out.println("Unable to get measurement from SMA");

    		  this.status = false;
    	  }
    }
    }

