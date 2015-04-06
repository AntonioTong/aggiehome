import java.util.Calendar;


public class EnergyMgmt2 {
	public double power=0.1; // power is the control output, meaning the capacity of the system 
	public int mode=0,modeBefore=0,modeAfter=0,step=2,stepBefore=0,stepAfter=0; //mode is used to switch between difference control types
	public double[] logStart1= new double[15], logEnd1= new double[15]; // logs 
	public double[] logStart2= new double[15], logEnd2= new double[15]; // logs 
	boolean done=false;
	int chCnt=1, dschCnt=1;
	public EnergyMgmt2(){
		for (int i=0;i<15;i++){
		logStart1[i]=0;
		logEnd1[i]=0.3;
		logStart2[i]=0;
		logEnd2[i]=0;	};
	}
	
	public void SetPower(AggieHome home, CANRead canPort){
		switch (step) {
        case 0:  stp0(home); //off time
                 break;
        case 1:  stp1(home); // partial peak before peak time
                 break;
        case 2:  stp2(home); // peak time
                 break;
        case 3:  stp3(home); // partial peak after
                 break;
    }
		if (canPort.mode==false){this.power=0.0;}
	}
	public void stp0(AggieHome home){
		// if battery soc <80% and pv > grid, charge the battery
		if(home.battery.socSuper<0.95 & done==false)
		{
			this.netZero(home);
			double powerDisbute=Math.abs(home.pB-this.power);
			if (this.power<0 |  powerDisbute>500 ){
				this.power=0.05;}
			this.setPower(home,this.power);		
			done=false;
		}
		if((home.battery.socSuper>0.95|home.battery.vMax>3.64) & done==false){
			this.setPower(home,this.power=-1);
			done=true;
		}
		step=0;
		if (home.time.timeHour>=12 & home.time.timeHour<=21){step=1;done=false;}
	}
	public void stp1(AggieHome home){
		if(home.battery.socSuper<0.95  & done==false)
		{
			this.netZero(home);
		//	double powerDisbute=Math.abs(home.pB-this.power);
			if (this.power<0 ){this.power=0.1;}
			this.setPower(home,this.power);
			done=false;
		}
		else if((home.battery.socSuper>0.95|home.battery.vMax>3.64) & done==false){
			this.setPower(home,this.power=-2);
			done=true;
		}
		else
		{   
			done=true;
		    this.setPower(home,this.power=2);
		}
		step=1;
		if (home.time.timeHour>=14){
			this.dayilyYieldSet(home);
			step=2; done=false;
		}
	}
	public void stp2(AggieHome home){
		double soh=0;
		if(home.battery.socSuper>this.logEnd1[0]  & done==false)
		{
		   this.netZero(home);
		    if (this.power>0 ){this.power=-2;}
		   this.setPower(home,this.power);
		   done=false;
		}
		else if((home.battery.socSuper<this.logEnd1[0] | home.battery.vMin<2.8) & done==false)
		{   
			done=true;
		    this.setPower(home,this.power=2);
		}
		else
		{   
			done=true;
		    this.setPower(home,this.power=2);
		}
		step=2;
		if (home.time.timeHour>=22){
			dayilyYieldSet(home);
			step=3;done=false;}
	}
	public void stp3(AggieHome home){
		step=3;
		this.power=3;
		   this.setPower(home,this.power);
		if (home.time.timeHour>=23 | home.time.timeHour<12){
			dayilyYieldStart(home);
			step=0;}
	}
	// setPower applies last safety net of the system
	public void setPower(AggieHome home,double pow){
		    this.power=pow;
		    if (this.power>2000){this.power=2000;}
		    if (this.power<-2000){this.power=-2000;}
			if (home.battery.vMin<2.80 | this.chCnt>1){
				this.power=50;
				this.chCnt=this.chCnt-1;
				if(home.battery.vMin<2.80){this.chCnt=21;}
				}
			if (home.battery.vMax>3.65 | this.dschCnt>1 ){
				this.power=-50;
				this.dschCnt=this.dschCnt-1;
				if(home.battery.vMax>3.6){this.dschCnt=21;}
				}
	}
	// netZero set the power of the EnergyMgmt to zero utility usage
	public void netZero(AggieHome home){
		if(home.pG!=home.pGold){
		    double pow=home.pG*0.2;//+(home.pG-home.pGold)+(home.pP-home.pPold)
		    if(pow>500){pow=500;}
		    if(pow<-500){pow=-500;}
		    this.power=this.power+pow;
		}
	}
	// daily yield calculates 
	// how many pv energy recieved 
	// by battery in a day 
	public void dayilyYieldStart(AggieHome home){
		this.logStart1[0]=home.battery.socSuper;
	}
	public void dayilyYieldSet(AggieHome home){
		double dSoC=home.battery.socSuper-this.logStart1[0];
		this.logStart1[0]=0;
		if (dSoC<0.4 || dSoC>1){
			this.logEnd1[0]=0.3;
		}
		else{
			this.logEnd1[0]=0.5-dSoC/2;
		}
		if(this.logEnd1[0]<0.05){ this.logEnd1[0]=0.05;}
	}
}
