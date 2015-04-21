public class PrintOut {
  public void SetPrint(AggieHome home,CANRead canPort,EnergyMgmt3 energyMgmt2){
	  
	  	if (home.mode==true & canPort.mode==true ){
	   	 System.out.format("power %.2f ",energyMgmt2.power);
	   	 System.out.format("step %d ",energyMgmt2.step);
	   	System.out.println(energyMgmt2.done);
	  	 System.out.format("socSet %.2f ",energyMgmt2.logEnd1[0]);
		// time 
	  	 System.out.format("dt %.2f ", home.time.timeDt);
		 System.out.print("\n");
	  	}
		 
		 System.out.format("socMax %.2f ", home.battery.socMax);
		 System.out.format("socMin %.2f ", home.battery.socMin);
		 System.out.format("vMax %.2f ", home.battery.vMax);
		 System.out.format("vMin %.2f ", home.battery.vMin);
		 System.out.format("tMax %.2f ", home.battery.tMax);
		 System.out.format("tMin %.2f ", home.battery.tMin);
		 System.out.format("vPack %.2f ", home.battery.vPack);
		 System.out.format("cPack %.2f ", home.battery.cPack);
		 System.out.print("\n");
		 
		 
		 System.out.format("discharge lmt set as %.2f ", energyMgmt2.logEnd1[0]);
		 System.out.print("\n");
		 
		 System.out.format("socSuper %.2f ", home.battery.socSuper);
		 System.out.format("sohSuper %.2f ", home.battery.sohSuper);
		 System.out.format("pbatt %.2f ", home.pB);
		 System.out.format("ppv %.2f ", home.pP);
		 System.out.format("pgrid %.2f ", home.pG);
		 System.out.format("phouse %.2f ", home.pH);
		 System.out.print("\n");
		//cell
		 for (int p = 0; p < 5; p++){
			 System.out.format("soc %.2f ", home.battery.cell[p].socC);
			 System.out.format("soh %.2f ", home.battery.cell[p].sohC);
			 System.out.format("r %.4f ", home.battery.cell[p].sohP);
		 }
		 System.out.print("\n");
		 for (int p = 5; p < 10; p++){
			 System.out.format("soc %.2f ", home.battery.cell[p].socC);
			 System.out.format("soh %.2f ", home.battery.cell[p].sohC);
			 System.out.format("r %.4f ", home.battery.cell[p].sohP);
		 }
		 System.out.print("\n");
		 for (int p = 10; p < 15; p++){
			 System.out.format("soc %.2f ", home.battery.cell[p].socC);
			 System.out.format("soh %.2f ", home.battery.cell[p].sohC);
			 System.out.format("r %.4f ", home.battery.cell[p].sohP);
		 }
		 System.out.print("\n");System.out.print("\n");System.out.print("\n");
		 System.out.print("\r");
  }
}
