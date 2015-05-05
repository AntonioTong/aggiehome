package main;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


public class DummyRead {
	boolean mode=true,vtgReadErr=true,tmpReadErr=true;
	double data=0;
	int counter=0;
	ArrayList<Double> list=new ArrayList<Double>();
	ArrayList<Double> c=new ArrayList<Double>();
	ArrayList<Double> v=new ArrayList<Double>();
	ArrayList<Double> t=new ArrayList<Double>();
	//ArrayList<Double> data_l= new ArrayList<Double>();
	//List<double> data=new ArrayList<double>();
	public DummyRead(){
		try {
			   FileReader fileA; fileA = new FileReader("data.txt");
        BufferedReader inputPowerread= new BufferedReader(fileA);
			   String lineParam=null;
			try {
			   while((lineParam = inputPowerread.readLine()) != null){
				
				//lineParam = inputPowerread.readLine();
				Scanner s=new Scanner(lineParam);
				
				list.add(s.nextDouble());
				list.add(s.nextDouble());
				c.add(s.nextDouble());
				
				for (int i1=0; i1<15; i1++){
			    v.add(s.nextDouble());
				}
				for (int i2=0; i2<15; i2++){
				t.add(s.nextDouble());
				}
				s.close();
			   }
			   inputPowerread.close();	fileA.close();
			} catch (IOException e) {mode=false; System.out.println("dummy data error occured"); }
			} catch (FileNotFoundException e1) {mode=false;}
		System.out.println("good");
	}
	public AggieHome.Battery GetRead(AggieHome.Battery battery){
		
		// cell vtg
		for (int p = 0; p < battery.nS; p++){
			if (v.get(counter*15+p) < 4.4){battery.cell[p].v=v.get(counter*15+p);}	
			else {vtgReadErr=false;} //if voltage read is wrong, keep the old reading, and send out error signal;
		}
		battery.vMax = battery.cell[0].v;battery.vMin= battery.cell[0].v; battery.vPack=0;
		for (int i1 = 0; i1 < battery.nS; i1++){
			battery.vPack=battery.vPack+battery.cell[i1].v;
			if (battery.vMin > battery.cell[i1].v){battery.vMin= battery.cell[i1].v;}
			if (battery.vMax < battery.cell[i1].v){battery.vMax= battery.cell[i1].v;}
		}
		// cell tmp
		for (int p = 0; p < battery.nS; p++){
	 		//System.out.println(dvalue);
	 		if (t.get(counter*15+p)< 100){battery.cell[p].t = t.get(counter*15+p);;}
	 		else {tmpReadErr=false;}
		}
		battery.tMax = battery.cell[0].t; battery.tMin = battery.cell[0].t;
		for (int i1 = 0; i1 < battery.nS; i1++){
			if (battery.tMin > battery.cell[i1].t){battery.tMin = battery.cell[i1].t;}
			if (battery.tMax < battery.cell[i1].t){battery.tMax = battery.cell[i1].t;}
		}
		// cell crt and bal
		battery.cPack=c.get(counter);
		for (int ie = 0; ie < battery.nS; ie++){
				battery.cell[ie].cold=battery.cell[ie].c;
				battery.cell[ie].c=c.get(counter);
			
			   //dvtg method is not accurate, use a simple temperature method
		      if(battery.cell[ie].t>40){
		    	  battery.cell[ie].b = 1;battery.cell[ie].c=c.get(counter)-2;
		    	  if (battery.cell[ie].c<0){battery.cell[ie].c=0;}}
			  else{battery.cell[ie].b = 0;}
	    }
		
		
		counter=counter+1;
		return battery;
	}
}
