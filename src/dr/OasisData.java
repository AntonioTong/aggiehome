package dr;


public class OasisData {

	private double value;
	private String opr_date;
	private String int_num;
	private String int_start_gmt;
	private String int_end_gmt;
	// REPORT_DATA
	// DATA_ITEM -> target name = LMP_PRC
	// OPR_DATE
	// INTERVAL_NUM
	// INTERVAL_START_GMT
	// INTERVAL_END_GMT
	// VALUE <- interested value
	
	public OasisData(double value, String opr_date, String int_num,
			String int_start_gmt, String int_end_gmt) {
		
		this.value = value;
		this.opr_date = opr_date;
		this.int_num = int_num;
		this.int_start_gmt = int_start_gmt;
		this.int_end_gmt = int_end_gmt;
	}
	
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	public String getOpr_date() {
		return opr_date;
	}
	public void setOpr_date(String opr_date) {
		this.opr_date = opr_date;
	}
	public String getInt_num() {
		return int_num;
	}
	public void setInt_num(String int_num) {
		this.int_num = int_num;
	}
	public String getInt_start_gmt() {
		return int_start_gmt;
	}
	public void setInt_start_gmt(String int_start_gmt) {
		this.int_start_gmt = int_start_gmt;
	}
	public String getInt_end_gmt() {
		return int_end_gmt;
	}
	public void setInt_end_gmt(String int_end_gmt) {
		this.int_end_gmt = int_end_gmt;
	}

	@Override
	public String toString() {
		return "OasisData [value=" + value + ", opr_date=" + opr_date
				+ ", int_num=" + int_num + ", int_start_gmt=" + int_start_gmt
				+ ", int_end_gmt=" + int_end_gmt + "]";
	}

	
}
