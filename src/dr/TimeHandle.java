package dr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class TimeHandle {
	
	public String getDate(Date date){
		SimpleDateFormat sdf1 = new SimpleDateFormat("YYYYMMdd'T'HH':00-0000'",
				Locale.ENGLISH);
		sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		StringBuilder dateStr = new StringBuilder(sdf1.format(date));
		
		return dateStr.toString();
	}
	
	public Date parseTime(String dateStr) {
		DateFormat format = new SimpleDateFormat(
				"YYYY-MM-DD'T'HH:'00:00-00:00'");

		Date date = null;
		try {
			date = format.parse(dateStr);

		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		return date;
	}


}
