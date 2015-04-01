import java.util.*;
import java.text.*;

import org.apache.commons.math3.linear.*;

public class AggieHome {
//////////////////////////////////////////////////**************** CONSTRUCTOR **************///////////////////////////////////
	// constructor for the battery system 
		boolean mode=true;
    	AggieHome.Time time = new AggieHome.Time();
    	AggieHome.Battery battery = new AggieHome.Battery();
    	
    	double pH=0,pB=0,pP=0,pG=0,pHold=0,pBold=0,pPold=0,pGold=0; //(power of house battery pv and grid
    	
    public AggieHome(){
    // construct battery pack
    }  	
//////////////////////////////////////////////////**************** OBJECTS **************///////////////////////////////////
// basic time object////////////////////////////////////////////////////////////////
    public class Time{
    	StringBuilder  timeStringNow= new StringBuilder();
    	Calendar cal = Calendar.getInstance();
    	double timeNow=cal.getTimeInMillis()/1000, timeDt=0;
    	int timeYear, timeMonth, timeDay, timeDayOfYear, timeDayOfWeek, timeHour, timeSecond, timeMinute;
    	public void SetTime(){
    		 SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
	    	   Date date = new Date();
	    	   timeStringNow= new StringBuilder( formatter.format( date ) );
    		//update time and dt
    		double oldtime = timeNow;  timeNow = cal.getTimeInMillis()/1000; timeDt=timeNow-oldtime; 
    		//update the rest time parameters
    		cal.clear();
    		cal.setTime(date);
    		timeYear = cal.get(Calendar.YEAR);  timeMonth = cal.get(Calendar.MONTH);  timeDay = cal.get(Calendar.DAY_OF_MONTH);
    	    timeDayOfYear = cal.get(Calendar.DAY_OF_YEAR);	timeDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);	
    	    timeHour = cal.get(Calendar.HOUR_OF_DAY);   	timeSecond = cal.get(Calendar.SECOND); timeMinute = cal.get(Calendar.MINUTE);
    	}
    }
// power object
    public void setPowerOld(){
    	this.pBold=this.pB;
    	this.pPold=this.pP;
    	this.pHold=this.pH;
    	this.pGold=this.pG;
    }
// battery object//////////////////////////////////////////////////////////////// 
    public class Battery{
    	// battery variables 
    	int nP = 9,nS =15,nW =3; double vPack=48,cPack=0; //(0 to 14)
		double normV=3.2,normCap = 40,vHLmt=3.6, vLLmt=2.8, cHLmt=50, cLLmt=-50, bLmt=3.4; 
		double vMax, vMin, tMax, tMin, socMax, socMin, sohMax, sohMin;
		double socSuper,sohSuper;
		double charge=0,discharge=0;
		// here soh is 1/soh numericalwise
		double soh[]= {1.58, 1.58, 1.58, 1.58, 1.58, 1.58, 
				1.58, 1.58, 1.58, 1.58, 1.58, 1.58, 1.58, 1.58, 1.58};
    	Battery.Cell[] cell=new Battery.Cell[nS];
    	//
    	
    	// create the battery class
    	public Battery(){
            for(int i=0; i<nS; i++){
               	cell[i]= new AggieHome.Battery.Cell(i,soh[i]);
            }
        }
    	public void SetBatteryUpdate(){
    		// update chargeLog
    		if (cPack>0){charge=charge+cPack*time.timeDt/3600;}
    		// update dischargeLog
    		else{discharge=discharge-cPack*time.timeDt/3600;}
    		// update soc
    		socMax=0;
    		socMin=1;
    		sohMax=0;
    		sohMin=1;
    		for(int i=0;i<nS;i++){
    			cell[i].ekf.UpdateEkf(cell[i].v, cell[i].c, cell[i].cold, time.timeDt, cell[i]);
    			cell[i].ekf.UpdateRLS(cell[i].v, cell[i].c, cell[i].cold, time.timeDt, cell[i]);
    			cell[i].socC=cell[i].ekf.x.getEntry(0, 0);
    			if (socMax<cell[i].socC){socMax=cell[i].socC;}
    			if (socMin>cell[i].socC){socMin=cell[i].socC;}
    			
    			// update soh
    			cell[i].sohP=cell[i].ekf.theta.getEntry(1, 0);
    			cell[i].sohC=1.00/(cell[i].ekf.theta.getEntry(3, 0));
    			if (sohMax<cell[i].sohC){sohMax=cell[i].sohC;}
    			if (sohMin>cell[i].sohC){sohMin=cell[i].sohC;}
    			// update charge discharge log
    			cell[i].ahDscold=cell[i].ahDsc; 
    					cell[i].ahChgold=cell[i].ahChg;
    							cell[i].ahChgDscold=cell[i].ahChgDsc;
    			if (cell[i].c>0){
    			cell[i].ahChg=cell[i].ahChg+time.timeDt*cell[i].c/3600; 	
    			}
    			else {
    			cell[i].ahDsc=cell[i].ahDsc-time.timeDt*cell[i].c/3600;	
    			}
    			cell[i].ahChgDsc=cell[i].ahChgDsc+time.timeDt*cell[i].c/3600; 
    		}
    		
    		// update socSuper
    		   // calculated based on all battery bank's soc 
			double socw=0.5*socMax +0.5*socMin;
			double w1= 1/(1+Math.exp(-20*(socw-0.5)));
			socSuper=(w1)*socMax +(1-w1)*socMin;
    		// update sohSuper
    		   // calculated based on all battery bank's soh levels
			
    		sohSuper=sohMin;//*(1-(socMax-socMin));
    		//
    	}
    	
    	// create the cell
    	public class Cell {
    	    Cell(int ind, double soh){
    	    	index=ind; 
    	    	ekf.theta.setEntry(3,0,soh) ;
    	    	}
    	    double isoh;
    	    int index;double v,c,t,cold=0,b=0;
    	    double socV,socC,sohC,sohP;
    	    double ahDsc=0, ahChg=0, ahChgDsc=0, ahDscold=0, ahChgold=0, ahChgDscold=0;
    	    AggieHome.Battery.Cell.EKF ekf = new AggieHome.Battery.Cell.EKF(1);
    	    
    	    public RealMatrix F(RealMatrix x, RealMatrix theta, double uold_mes, double dt_mes){
            	//RealMatrix u=new Array2DRowRealMatrix(new double[][] {{uold_mes}});
            	//RealMatrix dt=new Array2DRowRealMatrix(new double[][] {{dt_mes}});
            	RealMatrix out=new Array2DRowRealMatrix(new double[][] {{x.getEntry(0, 0)+uold_mes*dt_mes/3600/theta.getEntry(4, 0)/battery.nP*theta.getEntry(3, 0)},
            														{Math.exp(-dt_mes)*x.getEntry(1, 0)+
            														uold_mes*theta.getEntry(2, 0)*(1-Math.exp(-dt_mes))}});
    			return out;
            }
            public RealMatrix G(RealMatrix x, RealMatrix theta, double u_mes, double dt_mes){
            	//RealMatrix u=new Array2DRowRealMatrix(new double[][] {{u_mes}});
            	//RealMatrix dt=new Array2DRowRealMatrix(new double[][] {{dt_mes}});
            	RealMatrix out=new Array2DRowRealMatrix(new double[][] {{ OCV(x, theta)+x.getEntry(1, 0)+u_mes*theta.getEntry(1, 0)  }});
    			return out;
            }
            public RealMatrix FuncA(RealMatrix x, RealMatrix theta, double uold_mes, double dt_mes){
            	RealMatrix out=new Array2DRowRealMatrix(new double[][] {{1,0},{0,Math.exp(-dt_mes)}});
    			return out;
            }
            public RealMatrix FuncC(RealMatrix x, RealMatrix theta, double uold_mes, double dt_mes){
            	RealMatrix out=new Array2DRowRealMatrix(new double[][] {{ DOCV(x, theta), 1}});
    			return out;
            }
            public double OCV(RealMatrix x,RealMatrix theta){
            	double out=theta.getEntry(5, 0)*1+ 
        			theta.getEntry(6, 0)*Math.log(0.01+x.getEntry(0, 0))+
        			theta.getEntry(7, 0)*Math.log(1.01-x.getEntry(0, 0))+
        			theta.getEntry(8, 0)*x.getEntry(0, 0)+
        			theta.getEntry(9, 0)*Math.pow(x.getEntry(0, 0),3);
            return 	out;	
            }
            public double DOCV(RealMatrix x,RealMatrix theta){
            	double out=theta.getEntry(5, 0)*0+ 
	    			theta.getEntry(6, 0)*(1/(0.01+x.getEntry(0, 0)))+
	    			theta.getEntry(7, 0)*(-1/(1.01-x.getEntry(0, 0)))+
	    			theta.getEntry(8, 0)+
	    			2*theta.getEntry(9, 0)*Math.pow(x.getEntry(0, 0),2);
            return 	out;	
            }
            public double InvOCV(RealMatrix theta,double ocv_mes){
            	double soc_high=1, soc_low=0, soc=0.5;
            	double ocv;
            	for(int i=0;i<20;i++){
            		ocv=this.OCV(new Array2DRowRealMatrix(new double[][] {{soc}}),theta);
            		if (ocv_mes>ocv){
            			soc_low=soc;soc=soc*0.5+soc_high*0.5;
            		}
            		else{
            			soc_high=soc;soc=soc*0.5+soc_low*0.5;
            		}
            	}
            return 	soc;
            }
            
            public class EKF {
            	RealMatrix x,theta,x_old;
            	RealMatrix y,u,uold,a,b,c,d,vw,vx,vv,l;
            	RealMatrix p,phi;
            	int rlscnt=1;
            	double lambda=0.00001;
            	public EKF(double soh){
            		 p=new Array2DRowRealMatrix(new double[][] {{0.0001,0,0},{0,0.0001,0},{0,0,0.00001}});
            		 phi=new Array2DRowRealMatrix(new double[][] {{1},{0},{0}});
            		 lambda=0.00001;
            		 
            		 theta=new Array2DRowRealMatrix(new double[][] {{3.2},{0.01},{0.01},{soh},{40},{3.584},{0.1743},{-0.04125},{-0.3669},{0.02217}});
            				 										//{3.506},{0.1072},{-0.02725},{-0.3192},{0.1145}});
            		                                               // 0:voltage, 1:ro, 2:rs, 3:soh, 4:cap--ah, 5~9:ocv
            		 x=new Array2DRowRealMatrix(new double[][] {{0.8},{0}});
            		 vx=new Array2DRowRealMatrix(new double[][] {{0.01,0},{0,0.01}});
            		 vw=new Array2DRowRealMatrix(new double[][] {{0.0000001,0},{0,0.0000001}});
            		 l=new Array2DRowRealMatrix(new double[][] {{0.001},{0.001}});
            		 vv=new Array2DRowRealMatrix(new double[][] {{10}});
            		 rlscnt=1;
            	}
            	public void IniRLS(){
            		 p=new Array2DRowRealMatrix(new double[][] {{0.00001,0,0},{0,0.00001,0},{0,0,0.000001}});
           		 	 phi=new Array2DRowRealMatrix(new double[][] {{1},{0},{0}});
           		 	 lambda=0.00001;
            	}
            	public void UpdateEkf(double y_mes, double u_mes, double uold_mes, double dt, AggieHome.Battery.Cell cell){
            		// [x,V_x,L_x]=ekf(f,g,x,theta,y,uold,u,func_A,func_C_x,V_x,V_w,V_v,dt)
            		//func_A = @(x,theta,u,dt)([1,0,0; 0,exp(-dt/theta(5)),0; 0,0,exp(-dt/theta(6))]);
            	    //func_C_x = @(x,theta,u,dt)([theta(11:15)'*[0;1/x(1);-1/(1-x(1));1;3*x(1)^2],);
                     x_old=x;
            		
            		 y=new Array2DRowRealMatrix(new double[][] {{y_mes}});
                     u=new Array2DRowRealMatrix(new double[][] {{u_mes}});
                     uold=new Array2DRowRealMatrix(new double[][] {{uold_mes}});
            	     //time update for the state filter
            		 RealMatrix x1=cell.F(x,theta,uold_mes,dt) ;//%state estimation update
            		 a=cell.FuncA(x,theta,uold_mes,dt);
            	     vx=a.multiply(vx).multiply(a.transpose()).add(vw);
            	     c=cell.FuncC(x,theta,u_mes,dt);
            	     RealMatrix tmp=c.multiply(vx).multiply(c.transpose()).add(vv);
            	     RealMatrix Rx= new CholeskyDecomposition(tmp).getL();
            	     l=vx.multiply(c.transpose()).multiply( new LUDecomposition(Rx).getSolver().getInverse().transpose() );         
            	     RealMatrix y1=cell.G(x,theta,u_mes,dt);
            	     x=x1.add(l.multiply(y.subtract(y1)));
            	     //System.out.print("\n");
            	     //System.out.print( x+" ");
            	     vx=vx.subtract(l.multiply(c).multiply(vx));   
            		
                    /// saturate the value of soc
                    if (x.getEntry(0, 0)>0.999){x.setEntry(0, 0, 0.999);}  
                    if (x.getEntry(0, 0)<0.001){x.setEntry(0, 0, 0.001);}
            	}
            	public void UpdateRLS (double y_mes, double u_mes, double uold_mes, double dt, AggieHome.Battery.Cell cell){
            		// inputs current voltage and ocv, regresses soh and r
            		// the rls method updates the theta
                    //matlab code
                    //cell(i).v(j)=cell(i).vtg(j)-cell(i).x(j-1,2)*exp(-time.dt(j-1))-function_ocv(cell(i).x(j-1,1),0,2);
                    //cell(i).phi(j,1:3)=[function_ocv(cell(i).x(j-1,1),0,1)*cell(i).crt(j-1)*time.dt(j-1)/3600/360,cell(i).crt(j),(1-exp(-time.dt(j-1)))*cell(i).crt(j-1)]; %
                    //[cell(i).theta(j,:),cell(i).p] = rls( cell(i).theta(j-1,:),cell(i).phi(j,1:3),cell(i).v(j),cell(i).lambda,cell(i).p );
                    //[theta,p] = rls( theta,phi,z,lambda,p ) 
            		
            		//first, a rephrased theta_rls will be constructed to perform operation
            		
                    RealMatrix z=new Array2DRowRealMatrix(new double[][] {{y_mes-cell.ekf.x.getEntry(1, 0)*Math.exp(-dt)-cell.OCV(x_old,theta)}} );
                    phi=new Array2DRowRealMatrix(new double[][] {{cell.OCV(x_old,theta)*uold_mes*dt/3600/nP/theta.getEntry(4,0)}, {u_mes}, {uold_mes*(1-Math.exp(-dt))}});
                    RealMatrix iddt=new Array2DRowRealMatrix(new double[][] {{1}});
                    RealMatrix msq=phi.transpose().multiply(phi).add(iddt);
                    RealMatrix theta_rls=new Array2DRowRealMatrix(new double[][] {{theta.getEntry(3, 0)}, {theta.getEntry(1, 0)},{theta.getEntry(2, 0)}});   //rendering the theta that used in the update
                    RealMatrix epsilon=z.subtract(phi.transpose().multiply(theta_rls).scalarMultiply(1/msq.getEntry(0, 0)));//  (z-phi'*theta)/msq;
                    RealMatrix p_dot=p.scalarMultiply(lambda).subtract(p.multiply(phi.multiply(phi.transpose().scalarMultiply(1/msq.getEntry(0, 0)))).multiply(p)); //(phi*phi.transpose()/msq)*p);
                    p=p.add(p_dot);
                    RealMatrix theta_dot=p.scalarMultiply(epsilon.getEntry(0, 0)).multiply(phi);
                    theta_rls=theta_rls.add(theta_dot);
                    // wirte back to theta
                    theta.setEntry(3, 0, theta_rls.getEntry(0, 0));
                    theta.setEntry(1, 0, theta_rls.getEntry(1, 0));
                    theta.setEntry(2, 0, theta_rls.getEntry(2, 0));
                    if (theta.getEntry(1, 0)<0.001) theta.setEntry(1, 0,0.001);
                    if (theta.getEntry(2, 0)<0.001) theta.setEntry(2, 0,0.001);
                    if (theta.getEntry(1, 0)>0.05) theta.setEntry(1, 0,0.1);
                    if (theta.getEntry(2, 0)>0.05) theta.setEntry(2, 0,0.1);
                    
                    rlscnt=rlscnt+1;
                    if (rlscnt==20){
                    	rlscnt=1;
                    	IniRLS();
                    }
                   
            	}
            }
    	}
    }

}
