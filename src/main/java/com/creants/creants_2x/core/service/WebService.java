package com.creants.creants_2x.core.service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.MultivaluedMap;

import com.creants.creants_2x.core.util.AppConfig;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import net.sf.json.JSONObject;

/**
 * @author LamHM
 *
 */
public class WebService {
	private static final String KEY = "1|WqRVclir6nj4pk3PPxDCzqPTXl3J";
	private static final String LICENSE_URL = "http://license4j-muheroes.1d35.starter-us-east-1.openshiftapps.com/";
	// private static final String LICENSE_URL = "http://localhost:9393/";
	private static WebService instance;


	public static WebService getInstance() {
		if (instance == null) {
			instance = new WebService();
		}
		return instance;
	}


	private WebService() {
	}


	public String verify(String token) {
		WebResource webResource = Client.create().resource(AppConfig.getGraphApi() + "/internal/" + "verify");

		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("key", KEY);
		formData.add("token", token);

		ClientResponse response = webResource.accept(APPLICATION_JSON).post(ClientResponse.class, formData);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		}

		return response.getEntity(String.class);
	}


	public String linkFb(String fbToken) {
		WebResource webResource = Client.create().resource(AppConfig.getGraphApi() + "/internal/" + "fb");

		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("app_id", String.valueOf(1));
		formData.add("fb_token", fbToken);

		ClientResponse response = webResource.accept(APPLICATION_JSON).post(ClientResponse.class, formData);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		}

		return response.getEntity(String.class);
	}


	public String checkValid(String svid) {
		String entity = "";
		try {
			WebResource webResource = Client.create().resource(LICENSE_URL);
			MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
			String vmid = getVMID();
			String hostAddress = getSystemIP();
			formData.add("mid", vmid);
			formData.add("svid", svid);
			formData.add("ip", hostAddress);

			ClientResponse response = webResource.accept(APPLICATION_JSON).post(ClientResponse.class, formData);
			entity = response.getEntity(String.class);
			if (response.getStatus() != 200) {
				System.exit(0);
			}

			JSONObject jo = JSONObject.fromObject(entity);
			if (!jo.getString("vmid").equals(hostAddress)) {
				System.exit(0);
			}
		} catch (Exception e) {
			System.exit(0);
		}

		return entity;
	}


	public static String getVMID() {
		String s = (new java.rmi.dgc.VMID()).toString();
		return s.substring(0, s.length() - 6);
	}


	public String getUser(String uid, String key) {
		WebResource webResource = Client.create().resource(AppConfig.getGraphApi() + "/internal/" + "user");
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("uid", uid);

		ClientResponse response = webResource.accept(TEXT_PLAIN).header("key", KEY).post(ClientResponse.class,
				formData);

		if (response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		}

		return response.getEntity(String.class);
	}

	class License {
		private String vmip;


		public String getVmip() {
			return vmip;
		}


		public void setVmip(String vmip) {
			this.vmip = vmip;
		}


		@Override
		public String toString() {
			return "{vmid:" + vmip + "}";
		}

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
