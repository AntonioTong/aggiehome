package dr;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import xml.XmlRead.CustomAuthenticator;
public class xmlGet {
	Document dom;
	List<OasisData> odList;
	TimeHandle t = new TimeHandle();

	public xmlGet() {

		odList = new ArrayList<OasisData>();
	}

	public void run() {
		parseXml();
		parseDoc();
		printData();
	}

	public void parseXml() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse("oasis.xml");
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void parseDoc() {
		Element doc = dom.getDocumentElement();
		// System.out.println("Information of AggieVillage");
		// Loop through the list
		NodeList nl = doc.getElementsByTagName("REPORT_DATA");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the employee element
				Element el = (Element) nl.item(i);
				String data_item = getTextValue(el, "DATA_ITEM");
				if (data_item.equals("LMP_PRC")) {
					OasisData od = getOD(el);

					odList.add(od);
				}

			}
		}

	}

	private String getTextValue(Element ele, String tagName) {
		// TODO Auto-generated method stub
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}
		return textVal;
	}

	private double getDoubleValue(Element ele, String tagName) {
		return Double.parseDouble(getTextValue(ele, tagName));
	}

	private OasisData getOD(Element ele) {
		double value = getDoubleValue(ele, "VALUE");
		String opr_date = getTextValue(ele, "OPR_DATE");
		String int_num = getTextValue(ele, "INTERVAL_NUM");
		String int_start_gmt = getTextValue(ele, "INTERVAL_START_GMT");
		String int_end_gmt = getTextValue(ele, "INTERVAL_END_GMT");

		OasisData od = new OasisData(value, opr_date, int_num, int_start_gmt,
				int_end_gmt);
		return od;
	}

	private void printData() {
		Iterator it = odList.iterator();
		while (it.hasNext()) {
			it.next().toString();
			//System.out.println(it.next().toString());
		}
	}

	public List getOdList() {
		return odList;
	}

	public ReturnValue getValueTime(int hour) {
		ReturnValue r = null;
		for (OasisData i : odList) {
			int tmp = (t.parseTime(i.getInt_start_gmt()).getHours() + 17)%24;
			if (hour == tmp) {
				r = new ReturnValue(i.getValue(), hour);
			}
		}

		return r;
	}

}