package dr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import xml.XmlRead.CustomAuthenticator;

// http://oasis.caiso.com/oasisapi/SingleZip?queryname=PRC_LMP&startdatetime=20150408T07:00-0000&enddatetime=20150409T07:00-0000&version=1&market_run_id=DAM&node=WSCRMNO_1_N201
// http://oasis.caiso.com/oasisapi/SingleZip?queryname=PRC_LMP&startdatetime=20150409T06:00-0000&enddatetime=20150409T07:00-0000&version=1&market_run_id=DAM&node=WSCRMNO_1_N201
//http://oasis.caiso.com/oasisapi/SingleZip?queryname=PRC_LMP&startdatetime=20150409T00:00-0000&enddatetime=20150410T00:00-0000&version=1&market_run_id=DAM&node=WSCRMNO_1_N201
// http://oasis.caiso.com/oasisapi/SingleZip?node=WSCRMNO_1_N201&enddatetime=20150409T03%3A00-0000&market_run_id=DAM&queryname=PRC_LMP&version=1&startdatetime=20150409T00%3A00-0000
public class DRCaiso2 {
	
	@SuppressWarnings("deprecation")
	public void getDR() {
		
		// step 2 send request to get the binary file
		// step 3 extract binary to get xml
		// step 4 parse xml
            // test the code
		// step 1 build url
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddThh24:miZ");
		 Date date = new Date();
  	   	StringBuilder  dateNow= new StringBuilder( sdf.format( date ) );
  	   	System.out.println(date);
  	   
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		    qparams.add(new BasicNameValuePair("queryname", "PRC_LMP"));
		    qparams.add(new BasicNameValuePair("startdatetime", "20150308T00:00-0000"));
		    qparams.add(new BasicNameValuePair("enddatetime", "20150309T00:00-0000"));
		    qparams.add(new BasicNameValuePair("version", "1"));
		    qparams.add(new BasicNameValuePair("market_run_id", "DAM"));
		    qparams.add(new BasicNameValuePair("node", "WSCRMNO_1_N201"));
		    URI uri = null;
		    HttpResponse response=null;
			try {
				
					uri = URIUtils.createURI("http", "oasis.caiso.com/oasisapi", -1, "/SingleZip",
							URLEncodedUtils.format(qparams, "UTF-8"), null);
			HttpClient isoclient=HttpClientBuilder.create().build();
		    HttpGet httpget = new HttpGet(uri);
		    
		    //step2 send request and get response
			response = isoclient.execute(httpget);
		      InputStream in2 = response.getEntity().getContent();
		      FileOutputStream out2 = new FileOutputStream("oasis2.zip");
		      byte[] b = new byte[1024];
		        int count;
		        while ((count = in2.read(b)) > 0) {
		            out2.write(b, 0, count);
		        }
		      out2.close();
		      in2.close();
		    
		    //http://www.google.com/search?q=httpclient&btnG=Google+Search&aq=f&oq=
			
		// step 3 parse to xml


		      
		        // create a buffer to improve copy performance later.
		        byte[] buffer = new byte[2048];
		        // open the zip file stream
		        InputStream theFile = new FileInputStream("oasis2.zip");
		        ZipInputStream stream = new ZipInputStream(theFile);

		        try
		        {
		            // now iterate through each item in the stream. The get next
		            // entry call will return a ZipEntry for each file in the
		            // stream
		            ZipEntry entry;
		            while((entry = stream.getNextEntry())!=null)
		            {
		                String s = String.format("Entry: %s len %d added %TD",
		                                entry.getName(), entry.getSize(),
		                                new Date(entry.getTime()));
		                System.out.println(s);

		                // Once we get the entry from the stream, the stream is
		                // positioned read to read the raw data, and we keep
		                // reading until read returns 0 or less.
		                FileOutputStream output = null;
		                try
		                {
		                    output = new FileOutputStream("oasis.xml");
		                    int len = 0;
		                    while ((len = stream.read(buffer)) > 0)
		                    {
		                        output.write(buffer, 0, len);
		                    }
		                }
		                finally
		                {
		                    // we must always close the output file
		                    if(output!=null) output.close();
		                }
		            }
		        }
		        finally
		        {
		            // we must always close the zip file.
		            stream.close();
		        }  
		      
			
			} 
			catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e1) {
			}
		for (int p = 0; p <1; p++){
			 try {Thread.sleep(3000);}catch (InterruptedException e){e.printStackTrace();} 
	         
		}
    }	
}
