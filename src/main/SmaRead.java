package main;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;


public class SmaRead {
	boolean mode=true;
	// this class establishes a function to read and write to Sma webbox, 
	// the ReadBox reads data by given 
	// battery measurement update	
	 public AggieHome ReadPort(AggieHome home, double power){
		  mode=true;
		 //read port reads pv, battery crt, house demand data
		 //assign data for pB pP pH pG (representing power battery/pv/house/grid)
		   //setpP
		   try {
				DefaultHttpClient httpClient = new DefaultHttpClient(); //construct a httpClient
			    HttpPost smaPost = new HttpPost("http://192.168.30.22:3334/rpc"); //assign httpClient address to the sunnywebbox
			    // json code and header to read the sunnyboy data
				StringEntity RPC = new StringEntity("RPC={\"version\":\"1.0\",\"proc\":\"GetProcessData\",\"id\":\"5\",\"format\":\"JSON\",\"passwd\":\"a289fa4252ed5af8e3e9f9bee545c172\",\"params\":{\"devices\":[{\"key\":\"WR30U09E:2002225636\",\"channels\":[\"Pac\"]}]}}");
				RPC.setContentType("application/json"); smaPost.setEntity(RPC);	HttpResponse response = httpClient.execute(smaPost);
				// read the response
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
				// the pattern and matcher reads a specific line in the output string
				String output ;	Pattern p = Pattern.compile("(\"name\":\"Pac\",\"value\":\")(-?[\\d\\.]+)");
				while ((output = br.readLine()) != null) {    Matcher matcherPV=p.matcher(output);
					while (matcherPV.find()) {home.pP=Double.parseDouble(matcherPV.group(2));}
				}
				httpClient.getConnectionManager().shutdown();} 
		       catch (MalformedURLException e) {e.printStackTrace();} 
		       catch (IOException e) {mode=false;System.out.println("read pv power error occured"); }
		   //set pB
		   	   home.pB=home.battery.cPack*home.battery.vPack;
		   //set pG
		   // read the value of house energy from smartpanel
			double obvius=0;
		    try {
			   FileReader fileA; fileA = new FileReader("loadvalue.txt");
           BufferedReader inputPowerread= new BufferedReader(fileA);
			   String lineParam;
			try {
			   lineParam = inputPowerread.readLine();
			   obvius= Double.parseDouble(lineParam);
			   inputPowerread.close();	fileA.close();
			} catch (IOException e) {mode=false; System.out.println("power house error occured"); }
			} catch (FileNotFoundException e1) {mode=false;System.out.println("power house error occured"); }
			home.pG=-obvius;
		   //set pH
			home.pH=home.pG+home.pP-home.pB;
     return home;
	 }
	 public void WritePort(AggieHome home, double power){
		 //write port sets battery charge/discharge current
		   		// body for set battery current
		             //x*240 = 48 * cur = 1000
		            double inverterCurrent=-power/240-0.2; //(0.4 is the corruent cost for operating the inverter)
					try {	 
						DefaultHttpClient httpClient = new DefaultHttpClient(); //construct a httpClient
					    HttpPost smaPost = new HttpPost("http://192.168.30.22:3334/rpc"); //assign httpClient address to the sunnywebbox
					    // json code and header to read the sunnyboy data
					    String command ="RPC={\"version\":\"1.0\",\"proc\":\"SetParameter\",\"id\":\"5\",\"format\":\"JSON\",\"passwd\":\"a289fa4252ed5af8e3e9f9bee545c172\",\"params\":{\"devices\":[{\"key\":\"SI6048UH:1260017397\",\"channels\":[{\"meta\":\"FedInCurAtCom\",\"value\":\""+String.format("%.2f", inverterCurrent)+"\"}]}]}}";
						StringEntity RPC = new StringEntity(command);
						RPC.setContentType("application/json"); 
				        // set the body of httppose
						smaPost.setEntity(RPC);
				        // get response of httppose
						HttpResponse response = httpClient.execute(smaPost);
						httpClient.getConnectionManager().shutdown();
					 } 
					catch (MalformedURLException e) {e.printStackTrace();} 
					catch (IOException e) {mode=false;System.out.println("write to sma error occured");}
	 }
}
