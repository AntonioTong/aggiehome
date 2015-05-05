package main;

public class EnergyMgmt1 {
	double power=0; // power is the control output, meaning the capacity of the system 
	int mode=0,modeBefore=0,modeAfter=0,step=0,stepBefore=0,stepAfter=0; //mode is used to switch between difference control types
	double[] logStart1= new double[15], logEnd1= new double[15]; // logs 
	double[] logStart2= new double[15], logEnd2= new double[15]; // logs 
	public EnergyMgmt1(){
		for (int i=0;i<15;i++){
		logStart1[i]=0;
		logEnd1[i]=0;
		logStart2[i]=0;
		logEnd2[i]=0;	};
	}
	
	public void SetPower(AggieHome home){
		switch (step) {
        case 0:  stp0(home);
                 break;
        case 1:  stp1(home);
                 break;
        case 2:  stp2(home);
                 break;
        case 3:  stp3(home);
                 break;
    }
	}
	public void stp0(AggieHome home){
		power=-1000;
		step=0;
//		inverterCrt=inverterCrt + deltaPV + deltaInverterCrt;
		if (home.battery.vMin<2.4&home.battery.vMin>2){step=1;}
	}
	public void stp1(AggieHome home){
		power=1000;
		step=1;
		if (home.battery.vMax>3.65&home.battery.vMax<4.2){
			for (int i=0;i<15;i++){
				logStart1[i]= home.battery.cell[i].socC;
				logStart2[i]= home.battery.cell[i].ahDsc;};
			step=2;}
	}
	public void stp2(AggieHome home){
		double soh=0;
		
		step=2;
		//battery only discharge
		double batterypower=
		power=1000;
		if (home.battery.vMin<2.4&home.battery.vMin>2){
			for (int i=0;i<15;i++){
				logEnd1[i]=logStart1[i]- home.battery.cell[i].socC;// compare soc difference
				logEnd2[i]=logStart2[i]- home.battery.cell[i].ahDsc;// compare charge throughput
				soh=(logEnd2[i])/(logEnd1[i]+0.01);
				if (soh>36 || soh<5) soh=36;
				home.battery.cell[i].ekf.theta.setEntry(4, 0, soh ) ;  //update soh for each battery
			};
			step=3;}
	}
	public void stp3(AggieHome home){
		step=3;
		power=0;
	}
}
