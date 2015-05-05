package dr;


public class ReturnValue implements Comparable<ReturnValue> {
	double value;

	int hour;

	ReturnValue(double value, int hour) {
		this.value = value;
		this.hour = hour;
	}

	public int getHour() {
		return hour;
	}

	public double getValue() {
		return value;
	}

	@Override
	public int compareTo(ReturnValue o) {
		double comparePrice = ((ReturnValue)o).getValue();
		
		return (int) (comparePrice - this.value);
	}
	
	public String toString(){
		return hour + " " + value + "\n";
	}

}