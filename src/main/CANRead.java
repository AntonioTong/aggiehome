package main;
import peak.can.basic.*;

public class CANRead {
	public boolean mode = true;
	boolean vtgReadErr = true;
	boolean tmpReadErr = true;

	public AggieHome ReadCAN(AggieHome home) {

		mode = true;
	
		PCANBasic can = null;
	    TPCANMsg msg = null;
	    TPCANMsg cmdMsg = null;
	    TPCANStatus status = null;
	    TPCANStatus ret;
	    int messageID;
	    double[] volAry;
	    double[] tempAry;
	    volAry = new double[15];
	    tempAry = new double[15];
	    double current = 0.0;
	    can = new PCANBasic();
	   
		try{
			status = can.Initialize(TPCANHandle.PCAN_USBBUS1, TPCANBaudrate.PCAN_BAUD_500K, TPCANType.PCAN_TYPE_NONE, 0, (short) 0);
		}
		catch(Exception e){
			mode = false;
			System.out.println("CAN bus communication initialize error occur.");
		}

		//Initializing the request message and then request the voltage (PID message from BMS)
		try{
		msg = new TPCANMsg();
		cmdMsg = new TPCANMsg();
		 cmdMsg.setID((int)2016);
	    cmdMsg.setLength((byte)8);
	    cmdMsg.setType(TPCANMsg.MSGTYPE_STANDARD);
	    cmdMsg.getData()[0] = (byte)3;  //3
	    cmdMsg.getData()[1] = (byte)16; //10
	    cmdMsg.getData()[2] = (byte)20; //14
	    cmdMsg.getData()[4] = (byte)0;
	    cmdMsg.getData()[5] = (byte)0;
	    cmdMsg.getData()[6] = (byte)0;
	    cmdMsg.getData()[7] = (byte)0;
	    for(int i = 0; i<15; i++)
	    {
	    cmdMsg.getData()[3]=(byte)i;
	    status = can.Write(TPCANHandle.PCAN_USBBUS1, cmdMsg);
	    Thread.sleep(100);
	    }
		 }	
	    catch(Exception e){mode = false;System.out.println("CAN bus unable to send voltage request");}
	//Then try to see if the received side got the voltage request and updating according to the request
		try{
			while(can.Read(TPCANHandle.PCAN_USBBUS1, msg, null) == TPCANStatus.PCAN_ERROR_OK)
	       {           

	            messageID = msg.getID();            
	           if(messageID == 2024)
	            {             
	               

	                       byte volValue = (byte)(msg.getData()[4]);
	                       int value = ((0xFF & volValue));
	                       // Converted to byte first because some of the cell reads negative which is wrong
	                        volAry[(int)msg.getData()[3]] = ((double)value/100.0+2.0);

	               
	            }           
	        }  
		}
		catch(Exception e1){ mode = false; System.out.println("Error updating cell voltage");}
	//Then need to switch the request message from voltage to temperature and request to CAN
		try{
			cmdMsg.getData()[2] = (byte)24;
	       for(int i = 0; i<15; i++)
	    {
	    cmdMsg.getData()[3]=(byte)i;
	    status = can.Write(TPCANHandle.PCAN_USBBUS1, cmdMsg);
	    Thread.sleep(100);
	    }
		 }
		catch(Exception e2){mode = false;System.out.println("CAN bus unable to send temperature request.");}
		// Read the respond message
		try{
			 while(can.Read(TPCANHandle.PCAN_USBBUS1, msg, null) == TPCANStatus.PCAN_ERROR_OK)
	        {

	             messageID = msg.getID();
	            if(messageID == 2024)
	             {

	              tempAry[(int)msg.getData()[3]] =  ((double)msg.getData()[4]+128);

	             }
	           
	         }
		}
		catch(Exception e3){
			mode = false;
			System.out.println("Error updating cell temperature");
		}
//		//Start
//		//Initializing the request message and then request the current (PID message from BMS)
			try{
	                   cmdMsg.getData()[2] = (byte)106; // Changing the data reading to avg current
	                   cmdMsg.getData()[3] = (byte)0;
	                   status = can.Write(TPCANHandle.PCAN_USBBUS1, cmdMsg);
	                   Thread.sleep(100);
	               }
	               catch(Exception e4){mode = false;System.out.println("Can bus unable to send Current request");}
	                try{
			 while(can.Read(TPCANHandle.PCAN_USBBUS1, msg, null) == TPCANStatus.PCAN_ERROR_OK)
	        {

	             messageID = msg.getID();
	            if(messageID == 2024)
	             {
	                       byte msb = (byte)(msg.getData()[4]);
	                       byte lsb = (byte)(msg.getData()[5]);
	                       int value = ((int)msb<<8|(0xFF & lsb));
	                       current = -(double)value/10;  // Because - is Ain and we want it positive
	                
	             }
	           
	         }
		}
		catch(Exception e3){
			mode = false;
			System.out.println("Error updating current");
	//	
	       }
		
		// Then try to see if the received side got the voltage request and
		// updating according to the request
		/*
		 * try{ while(can.Read(TPCANHandle.PCAN_USBBUS1, msg, null) ==
		 * TPCANStatus.PCAN_ERROR_OK) { //System.out.println("ID:\t" +
		 * msg.getID() ); messageID = msg.getID(); if(messageID == 2024) {
		 * double current =-(double)msg.getData()[5]/10 ; //positive for current
		 * in, negative for current out
		 * System.out.println(current+"kenny says"); home.battery.cPack =
		 * current; //double temp = ((double)msg.getData()[2]+128);
		 * //System.out.println(msg.getData()[3]+"\t" + String.format( "%.2f",
		 * vol )+"\t"); } } } catch(Exception e1){ mode = false;
		 * System.out.println("Error updating cell current");}
		 */
		// end
		/* Overall update */
		// cell vtg update
		for (int i = 0; i < home.battery.nS; i++) {

			if (volAry[i] < 4.4) {
				home.battery.cell[i].v = volAry[i];
			}
			// else if(volAry[i]-2.56
			// >2){home.battery.cell[i].v=volAry[i]-2.56;}
			else {
				mode = false;
				System.out.println("Error reading cell vtg." + volAry[i]);
			}
		}
		home.battery.vMax = home.battery.cell[0].v;
		home.battery.vMin = home.battery.cell[0].v;
		home.battery.vPack = 0;
		for (int i = 0; i < home.battery.nS; i++) {
			if (home.battery.vMin > home.battery.cell[i].v) {
				home.battery.vMin = home.battery.cell[i].v;
			}
			if (home.battery.vMax < home.battery.cell[i].v) {
				home.battery.vMax = home.battery.cell[i].v;
			}
			home.battery.vPack = home.battery.vPack + home.battery.cell[i].v;
		}
		// cell tmp
		for (int i = 0; i < home.battery.nS; i++) {
			if (tempAry[i] < 100) {
				home.battery.cell[i].t = tempAry[i];
			} else {
				mode = false;
				System.out.println("Error reading tmp.");
			}
		}
		home.battery.tMax = home.battery.cell[0].t;
		home.battery.tMin = home.battery.cell[0].t;
		for (int i = 0; i < home.battery.nS; i++) {
			if (home.battery.tMin > home.battery.cell[i].t) {
				home.battery.tMin = home.battery.cell[i].t;
			}
			if (home.battery.tMax < home.battery.cell[i].t) {
				home.battery.tMax = home.battery.cell[i].t;
			}
		}
		// Update the current 
		home.battery.cPack = current;
		// balancing status
		for (int i = 0; i < home.battery.nS; i++) {
			if (home.battery.cell[i].t > 40 && home.battery.cell[i].c > 0) {
				home.battery.cell[i].b = 1;
			} else {
				home.battery.cell[i].b = 0;
			}
			if (home.battery.cell[i].b == 1 && home.battery.cPack > 2) {
				home.battery.cell[i].c = home.battery.cPack - 2;
			} else {
				home.battery.cell[i].c = home.battery.cPack;
			}
		}

		can.Uninitialize(TPCANHandle.PCAN_USBBUS1);
		return home;

	}
}
