package main;
import jssc.SerialPort;
import jssc.SerialPortException;

public class SerialRead {
	// this class establishes a function to read serial port, 
	// the ReadProt sub function takes battery class as input and updates value of v,c,t,b for cells and vct values for the pack
	// then the function return the very same class.
	// battery measurement update	
    	boolean mode=true, vtgReadErr=true, tmpReadErr=true;
    	SerialPort serialPort = new SerialPort("/dev/ttyUSB1");
    	
    	public AggieHome.Battery ReadPort(AggieHome.Battery battery){
    		mode=true;
    		try {
    			serialPort.openPort();
    			serialPort.setParams(57600, 8, 1, 0);//Set params.
    		} catch (SerialPortException e1) {
    			mode=false;System.out.println("open serial port error occured");
    		}//Open serial port
    		String str1 = "", str2 = "", str3 = "";
			int N = 2999,  a = 0, i = 0, j = 0;
			String[] Arr; 	Arr = new String[3000];		
		    try {
	        byte[] byt=serialPort.readBytes(3000);
	        for(i=0;i<3000;i++){Arr[i]=Byte.toString(byt[i]);}
		    }catch (Exception e) {
		    	mode=false;System.out.println("read from serial port error occured");
		    }
		  
		    // read information includes v,c,t,b and     
		    for (int i2 =0; i2 < 2000;i2++){
	    		  if (Arr[i2+3].equals("27")&& Arr[i2+4].equals("91") && Arr[i2+5].equals("72") ){N = i2+2;break;}
	    		  }
	    		  
	    		  vtgReadErr=true;
	    		  tmpReadErr=true;
	    		  if (N!=2999) {
	    			    try {
	    			  	short s = (short) Integer.parseInt(ASCII(Arr[N + 3 + 17])+ASCII(Arr[N + 3 + 18])+ASCII(Arr[N + 3 + 19])+ASCII(Arr[N + 3 + 20]),16);
	    			  	// batteryPack.crtPack= s*(-0.1); current is no longer obtain from serial read
	    			  	// cell vtg
	    				for (int p = 0; p < battery.nS; p++){
	    					double dvalue = Integer.parseInt(ASCII(Arr[N+115+2*(p+1)-1])+ASCII(Arr[N+115+2*(p+1)]),16);
	    					if (dvalue*0.01+2 < 4.4){battery.cell[p].v=dvalue*0.01+2;}	
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
	    			 		double dvalue= Integer.parseInt(ASCII(Arr[N+628+2*(p+1)-1])+ASCII(Arr[N+628+2*(p+1)]),16);
	    			 		//System.out.println(dvalue);
	    			 		if (dvalue-128 < 100){battery.cell[p].t = dvalue-128;}
	    			 		else {tmpReadErr=false;}
	    				}
	    				battery.tMax = battery.cell[0].t; battery.tMin = battery.cell[0].t;
	    				for (int i1 = 0; i1 < battery.nS; i1++){
	    					if (battery.tMin > battery.cell[i1].t){battery.tMin = battery.cell[i1].t;}
	    					if (battery.tMax < battery.cell[i1].t){battery.tMax = battery.cell[i1].t;}
	    				}
	    				// cell crt and bal
	    				for (int ie = 0; ie < battery.nS; ie++){
	    					  //if(batteryPack.vtgPack>51 &&((batteryPack.batteryCell[ie].vtg - batteryPack.vtgMin>0.05)||(-batteryPack.batteryCell[ie].vtg + batteryPack.vtgMax> 0.05)||batteryPack.batteryCell[ie].vtg > 3.4)){
	    					  // dvtg method is not accurate, use a simple temperature method
	    				      if(battery.cell[ie].t>40){battery.cell[ie].b = 1;}
	    					  else{battery.cell[ie].b = 0;}
	    			    }
	    				} catch (NumberFormatException e) {mode=false;System.out.println("update mes from serial port error occured");}
	    		  }            
	    		  
		    try {
		    	serialPort.closePort();
    		} catch (SerialPortException e1) {
    			mode=false;System.out.println("close serial port error occured");
    		}//Open serial port
		    return battery;
    	}
    	
    	public String Hex_to_binary(char c)
    	{
    		String binary=""; 
            switch (c)
            {
                    case '0' :binary = "0000";break;   case '1' :binary =  "0001";break;    case '2' :binary =  "0010";break;
                    case '3' :binary =  "0011";break;  case '4' :binary =  "0100";break;    case '5' :binary =  "0101";break;
                    case '6' :binary =  "0110";break;  case '7' :binary =  "0111";break;    case '8' :binary =  "1000";break;
                    case '9' :binary =  "1001";break;  case 'A' :binary =  "1010";break;    case 'B' :binary =  "1011";break;
                    case 'C' :binary =  "1100";break;  case 'D' :binary =  "1101";break;    case 'E' :binary =  "1110";break;
                    case 'F' :binary =  "1111";break;  default: break;
           }
           return binary;
    	}
    	public String ASCII(String c)
    	{
    		String binary=""; 
            switch (c)
            {       case "0" :binary = "NUL";break;case "1" :binary = "SOH";break;case "2" :binary = "STX";break;
                    case "3" :binary = "ETX";break;case "4" :binary = "EOT";break;case "5" :binary = "ENQ";break;
                    case "6" :binary = "ACK";break;case "7" :binary = "BEL";break;case "8" :binary = "BS";break;
                    case "9" :binary = "HT";break;case "10" :binary = "LF";break;case "11" :binary = "VT";break;
                    case "12" :binary = "FF";break;case "13" :binary = "CR";break;case "14" :binary = "SO";break;
                    case "15" :binary = "SI";break;case "16" :binary = "DLE";break;case "17" :binary = "DC1";break;
                    case "18" :binary = "DC2";break;case "19" :binary = "DC3";break;case "20" :binary = "DC4";break;
                    case "21" :binary = "NAK";break;case "22" :binary = "SYN";break;case "23" :binary = "ETB";break;
                    case "24" :binary = "CAN";break;case "25" :binary = "EM";break;case "26" :binary = "SUB";break;
                    case "27" :binary = "ESC";break;case "28" :binary = "FS";break;case "29" :binary = "GS";break;
                    case "30" :binary = "RS";break;case "31" :binary = "US";break;
                    case "32" :binary = " ";break;case "33" :binary = "!";break;case "34" :binary = "\"";break;
                    case "35" :binary = "#";break;case "36" :binary = "$";break;case "37" :binary = "%";break;
                    case "38" :binary = "&";break;case "39" :binary = "'";break;case "40" :binary = "(";break;
                    case "41" :binary = ")";break;case "42" :binary = "*";break;case "43" :binary = "+";break;
                    case "44" :binary = ",";break;case "45" :binary = "-";break;case "46" :binary = ".";break;
                    case "47" :binary = "/";break;
                    case "48" :binary = "0";break;case "49" :binary = "1";break;
                    case "50" :binary = "2";break;case "51" :binary = "3";break;case "52" :binary = "4";break;
                    case "53" :binary = "5";break;case "54" :binary = "6";break;case "55" :binary = "7";break;
                    case "56" :binary = "8";break;case "57" :binary = "9";break;
                    case "58" :binary = ":";break;                
                    case "59" :binary = ";";break;case "60" :binary = "<";break;case "61" :binary = "=";break;
                    case "62" :binary = ">";break;case "63" :binary = "?";break;case "64" :binary = "@";break;
                    case "65" :binary = "A";break;case "66" :binary = "B";break;case "67" :binary = "C";break;
                    case "68" :binary = "D";break;case "69" :binary = "E";break;case "70" :binary = "F";break;
                    case "71" :binary = "G";break;case "72" :binary = "H";break;case "73" :binary = "I";break;
                    case "74" :binary = "J";break;case "75" :binary = "K";break;case "76" :binary = "L";break;
                    case "77" :binary = "M";break;case "78" :binary = "N";break;case "79" :binary = "O";break;
                    case "80" :binary = "P";break;case "81" :binary = "Q";break;case "82" :binary = "R";break;
                    case "83" :binary = "S";break;case "84" :binary = "T";break;case "85" :binary = "U";break;
                    case "86" :binary = "V";break;case "87" :binary = "W";break;case "88" :binary = "X";break;
                    case "89" :binary = "Y";break;case "90" :binary = "Z";break;case "91" :binary = "[";break;
                    case "92" :binary = "\\";break;case "93" :binary = "]";break;case "94" :binary = "^";break;
                    case "95" :binary = "_";break;case "96" :binary = "`";break;case "97" :binary = "a";break;
                    case "98" :binary = "b";break;case "99" :binary = "c";break;
                    default: binary = "0"; break;
           }
           return binary;
    	}
    	public String convert_hex_to_str( String str )
    	{
            String binary_str = "" ;
            for(int i = 0; i < str.length(); i++)
            {binary_str = binary_str + Hex_to_binary(str.charAt(i));}
            return binary_str;
    	}	    	

}
		
