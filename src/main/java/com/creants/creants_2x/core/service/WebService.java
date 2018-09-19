package com.creants.creants_2x.core.service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import com.creants.creants_2x.core.util.AppConfig;
import com.creants.creants_2x.core.util.QAntTracer;

import net.sf.json.JSONObject;

/**
 * @author LamHM
 *
 */
public class WebService {
	private static final String KEY = "1|WqRVclir6nj4pk3PPxDCzqPTXl3J";
	private static final String LICENSE_URL = "http://35.187.232.137";
	private static final String GRAPH_URL_INTERNAL;
	private static final int CONNECT_TIMEOUT = 5000;
	private static final int SOCKET_TIMEOUT = 5000;
	private static final int MAX_CONNECTION_POOL = 100;
	private static WebService instance;
	private CloseableHttpClient client;

	static {
		GRAPH_URL_INTERNAL = AppConfig.getGraphApi() + "/internal/";
	}


	public static WebService getInstance() {
		if (instance == null) {
			instance = new WebService();
		}
		return instance;
	}


	private WebService() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		client = HttpClients.custom().setConnectionManager(cm).build();
		cm.setDefaultMaxPerRoute(MAX_CONNECTION_POOL);
	}


	public String verify(String token) {
		return doPostInternal(Form.form().add("key", KEY).add("token", token).build(), "verify");

	}


	public String linkFb(String fbToken) {
		return doPostInternal(Form.form().add("app_id", String.valueOf(1)).add("fb_token", fbToken).build(), "fb");
	}


	private String doPostInternal(List<NameValuePair> formReq, String action) {
		return doPostRequest(formReq, GRAPH_URL_INTERNAL + action);
	}


	public void checkValid(String svid) {
		try {
			String verificationString = doPostRequest(
					Form.form().add("mid", getVMID()).add("svid", svid).add("ip", getSystemIP()).build(), LICENSE_URL);

			JSONObject jo = JSONObject.fromObject(verificationString);
			if (!jo.getString("vmid").equals("test"))
				System.exit(0);
		} catch (Exception e) {
			System.exit(0);
		}

	}


	private String doPostRequest(List<NameValuePair> formReq, String url) {
		int statusCode = -1;
		try {
			Request bodyForm = Request.Post(url).addHeader("Content-Type", "application/x-www-form-urlencoded")
					.connectTimeout(CONNECT_TIMEOUT).socketTimeout(SOCKET_TIMEOUT).bodyForm(formReq);

			HttpResponse httpResponse = Executor.newInstance(client).execute(bodyForm).returnResponse();

			statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == 200)
				return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

		} catch (Exception e) {
			QAntTracer.error(this.getClass(),
					"doPostRequest! url: " + url + ", tracer:" + QAntTracer.getTraceMessage(e));
		}

		throw new RuntimeException("Failed : HTTP error code " + statusCode + ", url:" + url);
	}


	public static String getVMID() {
		String s = (new java.rmi.dgc.VMID()).toString();
		return s.substring(0, s.length() - 6);
	}


	public static String getSystemIP() {
		try {
			String sysIP = "";
			String OSName = System.getProperty("os.name");
			if (OSName.contains("Windows")) {
				sysIP = InetAddress.getLocalHost().getHostAddress();
			} else {
				sysIP = getSystemIP4Linux("eth0");
				if (sysIP == null) {
					sysIP = getSystemIP4Linux("eth1");
					if (sysIP == null) {
						sysIP = getSystemIP4Linux("eth2");
						if (sysIP == null) {
							sysIP = getSystemIP4Linux("usb0");
						}
					}
				}
			}
			return sysIP;
		} catch (Exception E) {
			return null;
		}
	}


	/**
	 * method for get IP of linux System
	 * 
	 * @param name
	 * @return
	 */
	private static String getSystemIP4Linux(String name) {
		try {
			String ip = "";
			NetworkInterface networkInterface = NetworkInterface.getByName(name);
			Enumeration<InetAddress> inetAddress = networkInterface.getInetAddresses();
			InetAddress currentAddress = inetAddress.nextElement();
			while (inetAddress.hasMoreElements()) {
				currentAddress = inetAddress.nextElement();
				if (currentAddress instanceof Inet4Address && !currentAddress.isLoopbackAddress()) {
					ip = currentAddress.toString();
					break;
				}
			}
			if (ip.startsWith("/")) {
				ip = ip.substring(1);
			}
			return ip;
		} catch (Exception E) {
			System.err.println("System Linux IP Exp : " + E.getMessage());
			return null;
		}
	}


	public static void main(String[] args) {
		String user = WebService.getInstance().verify(
				"eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhdXRoMCIsImlkIjoiMzIzIiwiZXhwIjoxNTA2MzExOTc2LCJhcHBfaWQiOiIyIiwidHRsIjo4NjQwMDAwMDB9.fkpS0atvhvHu_OAA-V6ZQqxsZ1Ekgs5-E3W7ANGi89U");
		System.out.println(user.toString());

		System.out.println(getSystemIP());
	}
}
