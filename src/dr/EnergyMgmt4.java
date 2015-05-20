package dr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import main.AggieHome;
import main.CANRead;

public class EnergyMgmt4 {

	private boolean dataStatus; // only need to retrieve data once for one day,
	private boolean tmrDataStatus; // true = data got
	private boolean safeToCharge; // indicate if battery is fully charged or not
	private boolean safeToDischarge; // indicate if battery cannot be further
										// discharge
	private BatteryState bs;
	private PriceState ps;
	private double peakPrice;
	private boolean done;
	private double price;

	// private int testHour;
	private double power;
	private int chCnt, dschCnt;
	ArrayList<ReturnValue> priceList; // = new ArrayList<ReturnValue>();
	ArrayList<ReturnValue> priceListTmr;

	public EnergyMgmt4() {
		dataStatus = false;
		tmrDataStatus = false;
		safeToCharge = false;
		safeToDischarge = false;
		priceList = new ArrayList<ReturnValue>();
		priceListTmr = new ArrayList<ReturnValue>();
		bs = BatteryState.NORMAL;
		// testHour = 0;
		power = 0.0;
		chCnt = 1;
		dschCnt = 1;
		done = false;
		ps = PriceState.OFFPEAK;
	}

	public enum BatteryState {
		FULL, NORMAL, EMPTY, UNKNOWN
	}

	public enum PriceState {
		PEAK, OFFPEAK
	}

	public void batteryStateUpdate(AggieHome home) {
		// update the three scenarios;
		if ((home.battery.vMax > 3.6)) {
			bs = BatteryState.FULL;
		} else if ((home.battery.vMin < 2.9)) {
			bs = BatteryState.EMPTY;
		} else {
			bs = BatteryState.NORMAL;
		}

	}

	public void batteryResponse() {
		switch (bs) {
		case FULL:
			System.out.println("Battery is full");
			safeToCharge = false;
			safeToDischarge = true;
			break;
		case NORMAL:
			System.out.println("Battery is normal");
			safeToCharge = true;
			safeToDischarge = true;

			break;
		case EMPTY:
			System.out.println("uh oh battery is empty");
			safeToCharge = true;
			safeToDischarge = false;
			break;
		default:
			System.out.println("Warning: battery status unknown");
			safeToCharge = false;
			safeToDischarge = false;
			break;

		}
	}

	public double findThreshold() {
		ArrayList<ReturnValue> tmp = new ArrayList<ReturnValue>(priceList);
		Collections.sort(tmp);
		double thPrice = 40;
		if (tmp.get(5).getValue() > thPrice) {
			return tmp.get(5).getValue();
		}
		if (tmp.get(1).getValue() < thPrice) {
			return tmp.get(1).getValue();
		}
		return thPrice;
	}

	public boolean overPrice(int hrNow) {
		// if current time's price is over a limit, return yes
		// if not return false
		// getCurrentHour
		int time = priceList.get(hrNow).getHour();
		price = priceList.get(hrNow).getValue();
		System.out.println("Time is: " + time + " The price now is " + price);
		if (price >= peakPrice) {
			// System.out.println("So expensive.....");
			return true;
		} else {
			// System.out.println("ehh.. cheap electricity");
			return false;
		}

	}

	public void offpeak(AggieHome home) {
		System.out.println("Now using the offpeak time algo");
//		if(bs == BatteryState.EMPTY){
//			done = true;
//			System.out.println("Off peak, but battery empty");
//			this.setPower(home,this.power = 2);
//		}
		if (bs != BatteryState.FULL & !done) {
			this.netZero(home);
			System.out.println("offpeak charging");
//			if (this.power < 0) {
//				this.power = 0.05;
//			}
			this.setPower(home, this.power);
		} else if (bs == BatteryState.FULL | done) {
			System.out.println("Battery done charging");
			done = true;
			this.setPower(home, this.power = -1);
		} else {
			done = true;
			this.setPower(home, this.power = -1);
		}
		if (overPrice(new Date().getHours())) {
			ps = PriceState.PEAK;
			done = false;
		}
	}

	public void peak(AggieHome home) {
		System.out.println("Now using the peak time algo");
		if (bs != BatteryState.EMPTY & !done) {
			System.out.println("Battery discharging");
			this.netDischarge(home);
			if (this.power > 0) {
				this.power = -2;
			}
			this.setPower(home, this.power);
			done = false;
		} else if (bs == BatteryState.EMPTY | done) {
			System.out.println("Battery done discharging");
			done = true;
			this.setPower(home, this.power = 2);

		} else {
			done = true;
			this.setPower(home, this.power = 2);
		}
		if (!overPrice(new Date().getHours())) {
			ps = PriceState.OFFPEAK;
			done = false;
		}
	}

	public void peakOffpeak(AggieHome home, CANRead canPort) {
		switch (ps) {
		case PEAK:
			peak(home);
			break;
		case OFFPEAK:
			offpeak(home);
			break;
		}
		if (canPort.mode == false) {
			this.power = 0.0;
		}

	}

	public void SetPower(AggieHome home, CANRead canPort) {
		run();
		batteryStateUpdate(home);
		peakOffpeak(home, canPort);
	}

	public int dateCount(String day) {
		if (day.equals("today")) {
			System.out.println("Today's data is called");
			return 0;
		} else if (day.equals("tomorrow")) {
			System.out.println("Tomorrow's data is called");
			return 1;
		}
		return 0;
	}

	public void retriveData(String day) { //string day
		Date dt = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.add(Calendar.DATE, dateCount(day)); //dateCount(day)
		dt = c.getTime();

		Connection connection = new Connection();
		connection.getDR(dt);
		xmlGet xmlGet = new xmlGet();
		xmlGet.run();
		List odList = xmlGet.getOdList();

		for (int i = 0; i < odList.size(); i++) {
			ReturnValue tmp = xmlGet.getValueTime(i);
			if (day.equals("today")) {
				priceList.add(tmp);
			} else if (day.equals("tomorrow")) {
				priceListTmr.add(tmp);
			}
		}

	}

	public void setPower(AggieHome home, double pow) { // AggieHome home,double
														// pow
		this.power = pow;
		if (this.power > 2000) {
			this.power = 2000;
		}
		if (this.power < -2000) {
			this.power = -2000;
		}
		if (home.battery.vMin < 2.80 | this.chCnt > 1) {
			this.power = 200;
			this.chCnt = this.chCnt - 1;
			if (home.battery.vMin < 2.80) {
				this.chCnt = 21;
			}
		}
		if (home.battery.vMax > 3.65 | this.dschCnt > 1) {
			this.power = -200;
			this.dschCnt = this.dschCnt - 1;
			if (home.battery.vMax > 3.6) {
				this.dschCnt = 21;
			}
		}
	}

	// netZero set the power of the EnergyMgmt to zero utility usage
	public void netDischarge(AggieHome home) {
		if (home.pG != home.pGold) {
			// double
			// pow=home.pG*0.2;//+(home.pG-home.pGold)+(home.pP-home.pPold)
			double pow = (home.pG - 500) * 0.2; // new
			if (pow > 500) {
				pow = 500;
			}
			if (pow < -500) {
				pow = -500;
			}
			this.power = this.power + pow;
			if (this.power > -500) {
				this.power = -500; // new
			}
		}
	}

	public void netZero(AggieHome home) {
		if (home.pG != home.pGold) {
			double pow = home.pG * 0.3;// +(home.pG-home.pGold)+(home.pP-home.pPold)
			// double pow = (500 - home.pG) * 0.2; // new
			if (pow > 500) {
				pow = 500;
			}
			if (pow < -500) {
				pow = -500;
			}
			this.power = this.power + pow;
		}
	}

	public void printData(ArrayList<ReturnValue> list) {
		for (int i = 0; i < list.size(); i++) {
			System.out.println("For time = " + (list.get(i).getHour())
					+ " price is: " + list.get(i).getValue());
		}
	}

	public void checkData(int curHour) {
		if (!isDataStatus()) {
			retriveData("today");
			System.out.println("Data retriving");
			peakPrice = findThreshold();
			printData(priceList);
			dataStatus = true;
		} else {
			System.out.println("Data already exists");
		}
		if (curHour >= 16 & !tmrDataStatus) { // new Date().getHours()
			System.out.println("New day data is retriving");
			retriveData("tomorrow");
			
			if (!priceListTmr.isEmpty()) {
				
				tmrDataStatus = true;
				System.out.println("Tomorrow's data is valid");
				printData(priceListTmr);
			}
		}
		if (curHour == 0 & tmrDataStatus) { // new Date().getHours()
			System.out.println("New day, data reset");

			priceList.clear();
			priceList = new ArrayList<ReturnValue>(priceListTmr);
			priceListTmr.clear();
			printData(priceList);
			peakPrice = findThreshold();
			tmrDataStatus = false;

		}
	}

	public void run() {
		checkData(new Date().getHours());
		System.out.println("Threshold price is: " + peakPrice);
		// batteryResponse();
		// testHour = (testHour + 1)%24;
		// suggest to run once every hour
	}

	public boolean isDataStatus() {
		return dataStatus;
	}

	public boolean isSafeToCharge() {
		return safeToCharge;
	}

	public boolean isSafeToDischarge() {
		return safeToDischarge;
	}

	public BatteryState getBs() {
		return bs;
	}

	public double getPeakPrice() {
		return peakPrice;
	}

	public boolean isTmrDataStatus() {
		return tmrDataStatus;
	}

	public boolean isDone() {
		return done;
	}

	public double getPower() {
		return power;
	}

	public PriceState getPs() {
		return ps;
	}

	public double getPrice() {
		return price;
	}

}
