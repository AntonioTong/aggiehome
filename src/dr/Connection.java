package dr;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

public class Connection {
	
	public String getDate(Date date){
		SimpleDateFormat sdf1 = new SimpleDateFormat("YYYYMMdd'T'HH':00-0000'",
				Locale.ENGLISH);
		sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		StringBuilder dateStr = new StringBuilder(sdf1.format(date));
		
		return dateStr.toString();
	}
	@SuppressWarnings("deprecation")
	public void getDR(Date dt) {
		
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.add(Calendar.DATE, 1);
		Date dtPlusOne = c.getTime();
		
		
		String dateNow = getDate(new Date(dt.getYear(),dt.getMonth(),dt.getDate(),0,0)); // time now
		String dateSixHr = getDate(new Date(dtPlusOne.getYear(),dtPlusOne.getMonth(),dtPlusOne.getDate(),0,0)); // could change to 24 for 24 hrs ahead 
		//System.currentTimeMillis() + 48 * 3600 * 1000)
		System.out.println("The GMT time now is" + dateNow);
		System.out.println("The time in 24 hr is" + dateSixHr);

		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("queryname", "PRC_LMP"));
		qparams.add(new BasicNameValuePair("startdatetime", dateNow.toString()));
		qparams.add(new BasicNameValuePair("enddatetime", dateSixHr.toString()));
		qparams.add(new BasicNameValuePair("version", "1"));
		qparams.add(new BasicNameValuePair("market_run_id", "DAM"));
		qparams.add(new BasicNameValuePair("node", "WSCRMNO_1_N201"));
		URI uri = null;
		HttpResponse response = null;
		try {

			uri = URIUtils.createURI("http", "oasis.caiso.com/oasisapi", -1,
					"/SingleZip", URLEncodedUtils.format(qparams, "UTF-8"),
					null);
			System.out.println(uri.toString());
			HttpClient isoclient = HttpClientBuilder.create().build();
			HttpGet httpget = new HttpGet(uri);

			// step2 send request and get response
			response = isoclient.execute(httpget);
			InputStream in2 = response.getEntity().getContent();
			FileOutputStream out2 = new FileOutputStream("oasis.zip");
			byte[] b = new byte[1024];
			int count;
			while ((count = in2.read(b)) > 0) {
				out2.write(b, 0, count);
			}
			out2.close();
			in2.close();

			// http://www.google.com/search?q=httpclient&btnG=Google+Search&aq=f&oq=

			// step 3 parse to xml

			// create a buffer to improve copy performance later.
			byte[] buffer = new byte[2048];
			// open the zip file stream
			InputStream theFile = new FileInputStream("oasis.zip");
			ZipInputStream stream = new ZipInputStream(theFile);

			try {
				// now iterate through each item in the stream. The get next
				// entry call will return a ZipEntry for each file in the
				// stream
				ZipEntry entry;
				while ((entry = stream.getNextEntry()) != null) {
					String s = String.format("Entry: %s len %d added %TD",
							entry.getName(), entry.getSize(),
							new Date(entry.getTime()));
					//System.out.println(s);

					// Once we get the entry from the stream, the stream is
					// positioned read to read the raw data, and we keep
					// reading until read returns 0 or less.
					FileOutputStream output = null;
					try {
						output = new FileOutputStream("oasis.xml");
						int len = 0;
						while ((len = stream.read(buffer)) > 0) {
							output.write(buffer, 0, len);
						}
					} finally {
						// we must always close the output file
						if (output != null)
							output.close();
					}
				}
			} finally {
				// we must always close the zip file.
				stream.close();
			}

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
		}
		for (int p = 0; p < 1; p++) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
}
