package faucet;
//spark
import spark.staticfiles.*;
import static spark.Spark.*;

//apache log4j
import org.apache.log4j.Logger;

//java
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.json.Json;
import javax.json.JsonObject;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.nio.charset.Charset;
import java.sql.DriverManager;
import java.util.Random;
import java.io.StringReader;

//apache httpcomponents
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

//faucet
//import faucet.SparkUtils;
import static faucet.Tools.toMap;
import static faucet.JsonUtil.json;
//import static faucet.HttpRequests.verify;
import static faucet.HttpRequests.verifyV3;
import static faucet.HttpRequests.sendRequest;
import static faucet.Tools.validateAccount;
import static faucet.Tools.checkDry;


//sql2o
import org.sql2o.*;

import spark.utils.SparkUtils;


public class Main {

	public static Logger logger = Logger.getLogger(Main.class);
	public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	public static List<String> ipList = new ArrayList<>();

	public static List<String> accountList = new ArrayList<>();

	public static Map<String, Long> ipReset = new ConcurrentHashMap<>();

	public static Map<String, Long> fpReset = new ConcurrentHashMap<>();

	public static Map<String, Long> accountReset = new ConcurrentHashMap<>();

	public static List<String> fingerprintList = new ArrayList<>();

	public static void main (String[] args) {

		scheduler.scheduleAtFixedRate(() -> {
            		// Clear the list
            		fingerprintList.clear();
            		System.out.println("List cleared at " + System.currentTimeMillis());

			for (Map.Entry<String, Long> entry : ipReset.entrySet()) {
                        	String ip = entry.getKey();
                        	long nextResetTime = entry.getValue();
                        	long currentTime = System.currentTimeMillis();

                        	if(currentTime > nextResetTime) {
                                	//remove from map
                                	ipReset.remove(ip);
                       		 }
			}
			for (Map.Entry<String, Long> entry : fpReset.entrySet()) {
                        	String fp = entry.getKey();
                        	long nextResetTime = entry.getValue();
                        	long currentTime = System.currentTimeMillis();

                        	if(currentTime > nextResetTime) {
                                	//remove from map
                                	fpReset.remove(fp);
                        	}
			}

        	}, 0, 10, TimeUnit.SECONDS);

		externalStaticFileLocation("/home/user/javaProjects/private_repo/faucet/src/main/resources");
		port(4114);
		//ipAddress("0.0.0.0");
		//SparkUtils.createServerWithRequestLog(logger);

		get("/ip", (request, response) -> {

			String ip2 = request.headers("CF-Connecting-IP");
			System.out.println(ip2);
			String result = ip2.substring(ip2.lastIndexOf(',') + 1).trim();
			return result;
		});
		post("/send", (request, response) -> {
			response.type("text/plain");

			System.out.println("Received JSON data: " + request.body());
			// Parse JSON data from the request body using javax.json.JsonObject
            		JsonObject json = Json.createReader(new StringReader(request.body())).readObject();

            		// Retrieve values from JSON
            		String destination = json.containsKey("destination") ? json.getString("destination") : null;
            		String fingerprint = json.containsKey("fingerprint") ? json.getString("fingerprint") : null;
            		String gRecaptchaResponse = json.containsKey("g-recaptcha-response") ? json.getString("g-recaptcha-response") : null;

			System.out.println(gRecaptchaResponse);
			System.out.println(destination);

    			// Retrieve values from JSON

			System.out.println(gRecaptchaResponse);
		//	boolean captchaResult = verify(gRecaptchaResponse);
			boolean check = verifyV3(gRecaptchaResponse);
			String res;
			if(check == true) {
				boolean checkValidAccount = validateAccount(destination);
				if(checkValidAccount == true) {

					//check if faucet is dry
					Boolean dry = checkDry();

					if(dry == false) {

						//check is account has been paid before

							//check ip
							String ip = request.headers("CF-Connecting-IP");
							//find ip in array

							for (Map.Entry<String, Long> entry : accountReset.entrySet()) {
                        					String accountFound = entry.getKey();

                        					long accountResetTime = entry.getValue();

								long currentTime = System.currentTimeMillis();

								long timeRemaining = accountResetTime - currentTime; 	
								long toMinutes = timeRemaining / (60 * 1000);

								if(accountFound.equals(destination)) {
									System.out.println("destination found");
                							return "Please wait " + toMinutes + " minutes.";
									
								}

                       		 			}

							for (Map.Entry<String, Long> entry : fpReset.entrySet()) {
                        					String fpfound = entry.getKey();

                        					long fpResetTime = entry.getValue();

								long currentTime = System.currentTimeMillis();

								long timeRemaining = fpResetTime - currentTime; 	
								long toMinutes = timeRemaining / (60 * 1000);

								if(ip.equals(fpfound)) {
									System.out.println("fingerprint found");
                							return "Please wait " + toMinutes + " minutes.";
									
								}


                       		 			}
							for (Map.Entry<String, Long> entry : ipReset.entrySet()) {
                        					String ipfound = entry.getKey();
                        					long ipResetTime = entry.getValue();

								long currentTime = System.currentTimeMillis();

								long timeRemaining = ipResetTime - currentTime; 	
								long toMinutues = timeRemaining / (60 * 1000);

								if(ip.equals(ipfound)) {
									System.out.println("ip found");
                							return "Please wait " + toMinutues + " minutes.";
									
								}


                       		 			}

								sendRequest(destination);
								ipList.add(ip);
								fingerprintList.add(fingerprint);
								//BlockHash hash = new BlockHash(obj.getString("hash"));nano_1xno5fdkdrbxrwkdhmjp4ah9oxg49qkdy3d491ge3tywyqyt87ubwygq8m7k
								long currentTime = System.currentTimeMillis();
								long nextReset = currentTime + 600000;	

								ipReset.put(ip, nextReset);
								fpReset.put(fingerprint, nextReset);


								res = "Nano has been sent.";
								return res;
					}
					res = "Faucet is dry!";
					return res;
				}
				res = "Not a nano address!";
				return res;

			}
			res = "Captcha verification failed!";
			return res;	
		});


	}
}




