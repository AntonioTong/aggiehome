import java.io.IOException;

import smaGson.SMA;
import xml.XmlRead;

class MultiThread {
    boolean flag = false;
    AggieHome home=new AggieHome();
	//EnergyMgmt control= new EnergyMgmt();
	//SerialRead serialPort=new SerialRead();
	CANRead canPort=new CANRead();
	DummyRead dummy=new DummyRead();
	PrintOut printOut=new PrintOut();
	WriteToFile writeToFile=new WriteToFile();
	EnergyMgmt1 energyMgmt1 = new EnergyMgmt1();
	EnergyMgmt2 energyMgmt2 = new EnergyMgmt2();// normal routing
	EnergyMgmt3 energyMgmt3 = new EnergyMgmt3();// normal routing
	//generate classes for sma read/write, 
	SMA smaPort2 = new SMA();
	//and xml file read
	XmlRead obviusPort=new XmlRead();
	WriteToDB writeToDB = new WriteToDB();
	
	public synchronized void BatteryThread() {
    	home.time.SetTime();
    	// get measurement canbus, current, voltage, temperature
 	    home=canPort.ReadCAN(home);
 	    // step 3. update soc/soh, update power management states
 	    home.battery.SetBatteryUpdate();
 	    //System.out.println("Thread BATTERY");
        notify();
    }
    public synchronized void HouseThread() {
    	// set power old variale for ph pb pv pg
 		home.setPowerOld();
 		// get measurement from sma, pv power and battery current
 		smaPort2.getSMAPV();
 	    home.pP=smaPort2.pvBat.getPv();
 	    // get measurement from obvius panel, house power
 	    //home.battery=dummy.GetRead(home.battery);
 	    obviusPort.getMeasure();
 	    home.pG=obviusPort.va; // negative being back feeding
 	    home.pB=home.battery.cPack*home.battery.vPack;
	    home.pH=home.pP-home.pB-home.pG;
	    energyMgmt3.SetPower(home,canPort); //here sets power
	    smaPort2.setParameters(energyMgmt3.power);
//    	System.out.println("Thread HOUSE");
        notify();
    }
    public synchronized void CloudThread() throws IOException {
    	// step 4. variables print out & data logging
	    printOut.SetPrint(home,canPort,energyMgmt3);
	    writeToFile.SetDatalog(home, energyMgmt3);
	    writeToDB.setDataBase(home);
	    //System.out.println("Thread CLOUD");
        notify();
    }
}

class T1 implements Runnable {
	MultiThread m;

    public T1(MultiThread m1) {
        this.m = m1;
        new Thread(this, "BatteryThread").start();
    }

    public void run() {
    	while (true) {
   		 try {Thread.sleep(1000);}catch (InterruptedException e){e.printStackTrace();} 
            m.BatteryThread();
        }
    }
}

class T2 implements Runnable {
	MultiThread m;

    public T2(MultiThread m1) {
        this.m = m1;
        new Thread(this, "HouseThread").start();
    }

    public void run() {
    	while (true) {
   		 try {Thread.sleep(60000);}catch (InterruptedException e){e.printStackTrace();} 
            m.HouseThread();
        }
    }
}
class T3 implements Runnable {
	MultiThread m;

    public T3(MultiThread m1) {
        this.m = m1;
        new Thread(this, "CloudThread").start();
    }

    public void run() {
    	while (true) {
   		 try {Thread.sleep(600000);}catch (InterruptedException e){e.printStackTrace();} 
            try {
				m.CloudThread();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
}

public class TestThread {
    public static void main(String[] args) {
    	MultiThread m = new MultiThread();
        new T1(m);
        new T2(m);
        new T3(m);
    }
}