package main;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EnergyMgmtCal {

	private Date startTime;
	private double power;
	private ArrayList<Date> profileTime;
	private int counter;

	// private ArrayList<Date> dischargeSchedule;

	public EnergyMgmtCal() {
		startTime = new Date();
		power = 0.0;
		profileTime = new ArrayList<Date>();
		counter = 0;
		// dischargeSchedule = new ArrayList<Date>();
	}

	public Date getStartTime() {
		return startTime;
	}

	public void charge(AggieHome home) {

		this.power = 1000.0;
		if (home.battery.vMax > 3.6) { // prevent overcharge
			this.power = 0;
		}

	}

	public void discharge(AggieHome home) {
		this.power = -1000.0;
		if (home.battery.vMin < 2.9) { // dont discharge
			this.power = 0;
		}
		// this.dschCnt = 20;
	} // no need for now

	public void rest() {
		this.power = 0.0;

	}

	public void setSchedule() {
		Date scheduleTime = startTime;
		Calendar cal = Calendar.getInstance();
		cal.setTime(scheduleTime);
		profileTime.add(scheduleTime);
		for (int i = 0; i < 15; i++) {
			cal.add(Calendar.MINUTE, 20);
			scheduleTime = cal.getTime();
			profileTime.add(scheduleTime);
			// System.out.println(scheduleTime.toString());

			// System.out.println("second loop");
			cal.add(Calendar.MINUTE, 10);
			scheduleTime = cal.getTime();
			profileTime.add(scheduleTime);
			// System.out.println(scheduleTime.toString());
		}

		// for (int i = 0; i< dischargeSchedule.size(); i++){
		// System.out.println("Rest");
		// System.out.println(dischargeSchedule.get(i).toString());
		// }
		// for (int i = 0; i< chargeSchedule.size();i++){
		// System.out.println("Charge");
		// System.out.println(chargeSchedule.get(i).toString());
		// }
		// for (int i = 0; i < profileTime.size(); i++) {
		// System.out.println(i);
		// System.out.println(profileTime.get(i).toString());
		// }

	}

	public void run(AggieHome home) {
		setSchedule();
		Date timeNow = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(timeNow);
		// for (int j = 0; j < 1500; j++) {
		System.out.println("time is now " + timeNow.toString());
		if (timeNow.after(profileTime.get(counter)) & counter % 2 == 0) {
			System.out.println("charging");
			discharge(home);
			counter++;
		} else if (timeNow.after(profileTime.get(counter)) & counter % 2 != 0) {
			System.out.println("Rest");
			rest();
			counter++;
		}

		cal.add(Calendar.MINUTE, 5);
		timeNow = cal.getTime();
		if (counter > 30) {
			System.out.println("Done");
		}
		// }
	}

	public void SetPower(AggieHome home) {
		run(home);
	}

	public double getPower() {
		return power;
	}

	public ArrayList<Date> getProfileTime() {
		return profileTime;
	}

}
