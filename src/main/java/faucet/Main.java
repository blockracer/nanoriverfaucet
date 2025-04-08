package faucet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.google.gson.Gson;

//spark
import spark.staticfiles.*;
import static spark.Spark.*;
import spark.Session;

//apache log4j
import org.apache.log4j.Logger;

//java
//
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.json.Json;
import javax.json.JsonObject;
import java.util.Map;

import java.util.HashMap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
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

        public static String baselineHash; 
	public static List<String> accountList = new ArrayList<>();

	//public static List<String> initialSessionList = new ArrayList<>();
	//public static List<String> initialSyncedSessionList = Collections.synchronizedList(initialSessionList);
	
	public static List<String> sessionList = new ArrayList<>();

	public static Map<String, Long> ipReset = new ConcurrentHashMap<>();

	public static Map<String, Long> fpReset = new ConcurrentHashMap<>();

	public static Map<String, Long> accountReset = new ConcurrentHashMap<>();

	public static Map<String, Long> initialFpMap = new ConcurrentHashMap<>();

	public static List<String> fingerprintList = new ArrayList<>();


	private static String calculateHash(String filePath) throws IOException, NoSuchAlgorithmException {
        // Read HTML content from file
        String htmlContent = readHtmlFile(filePath);

        // Calculate hash of the content
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(htmlContent.getBytes());
        byte[] digest = md.digest();

        // Convert byte array to hex string
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    // Method to read HTML file content
    private static String readHtmlFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] htmlBytes = Files.readAllBytes(path);
        return new String(htmlBytes);
    }


	public static void main (String[] args) throws IOException, NoSuchAlgorithmException {


		baselineHash = calculateHash("/path/to/secret.html");

		scheduler.scheduleAtFixedRate(() -> {
            		// Clear the list
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
			for (Map.Entry<String, Long> entry : accountReset.entrySet()) {
                        	String account = entry.getKey();
                        	long nextResetTime = entry.getValue();
                        	long currentTime = System.currentTimeMillis();

                        	if(currentTime > nextResetTime) {
                                	//remove from map
                                	accountReset.remove(account);
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
			for (Map.Entry<String, Long> entry : initialFpMap.entrySet()) {
                        	String intialFp = entry.getKey();
				long removeTime = entry.getValue();
                        	long currentTime = System.currentTimeMillis();

                        	if(currentTime > removeTime) {
                                	//remove from map
                                	initialFpMap.remove(intialFp);
                        	}
			}


        	}, 0, 10, TimeUnit.SECONDS);

		externalStaticFileLocation("/path/to/resources");
		port(4114);
		//ipAddress("0.0.0.0");
		//SparkUtils.createServerWithRequestLog(logger);

		get("/ip", (request, response) -> {

			String ip2 = request.headers("CF-Connecting-IP");
			System.out.println(ip2);
			String result = ip2.substring(ip2.lastIndexOf(',') + 1).trim();
			return result;
		});
		post("/initialfp", (request, response) -> {

			response.type("application/json");
			Session userSession = request.session(true);
            		JsonObject json = Json.createReader(new StringReader(request.body())).readObject();
            		String initialFp = json.containsKey("initialFp") ? json.getString("initialFp") : null;
			long currentTime = System.currentTimeMillis();
			long removeTime = currentTime + 3600000;

			Map<String, String> map = new HashMap<>();
			//check if key exists
			if(!initialFpMap.containsKey(initialFp)) {
				//add entry
				initialFpMap.put(initialFp, removeTime);

				String result = "{\"result\":true}";
				return result;

			}

			String result = "{\"result\":true}";
			return result;


		});

	


		post("/send", (request, response) -> {
			response.type("text/plain");

				Thread.sleep(500);

				Session session = request.session(true);
            			String userId = session.id();

				 for (String id : sessionList) {
					 if(userId.equals(id)) {
						return "rate limit exceeded, wait 10 seconds";
					}
        			}
				sessionList.add(userId);


			System.out.println("Received JSON data: " + request.body());
			// Parse JSON data from the request body using javax.json.JsonObject
            		JsonObject json = Json.createReader(new StringReader(request.body())).readObject();

            		// Retrieve values from JSON
            		String destination = json.containsKey("destination") ? json.getString("destination") : null;

            		String clientHash = json.containsKey("hash") ? json.getString("hash") : null;

            		String fingerprint = json.containsKey("fingerprint") ? json.getString("fingerprint") : null;
            		String gRecaptchaResponse = json.containsKey("g-recaptcha-response") ? json.getString("g-recaptcha-response") : null;

			System.out.println(gRecaptchaResponse);
			System.out.println(destination);

			System.out.println("client hash: " + clientHash );
			System.out.println("server hash: " +  baselineHash);


			//check if hashes match
			if(!baselineHash.equals(clientHash)) { 

				scheduleRemoval(userId);
				return "Page modification detected";

			}
			if(!initialFpMap.containsKey(fingerprint)){
				scheduleRemoval(userId);
				return "fingerprint detection failed";
			}


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
									scheduleRemoval(userId);
									System.out.println("destination found");
                							return "Please wait " + toMinutes + " minute(s).";
									
								}

                       		 			}

							for (Map.Entry<String, Long> entry : fpReset.entrySet()) {
                        					String fpfound = entry.getKey();

                        					long fpResetTime = entry.getValue();

								long currentTime = System.currentTimeMillis();

								long timeRemaining = fpResetTime - currentTime; 	
								long toMinutes = timeRemaining / (60 * 1000);

								if(ip.equals(fpfound)) {
									scheduleRemoval(userId);
									System.out.println("fingerprint found");
                							return "Please wait " + toMinutes + " minute(s).";
									
								}


                       		 			}
							for (Map.Entry<String, Long> entry : ipReset.entrySet()) {
                        					String ipfound = entry.getKey();
                        					long ipResetTime = entry.getValue();

								long currentTime = System.currentTimeMillis();

								long timeRemaining = ipResetTime - currentTime; 	
								long toMinutues = timeRemaining / (60 * 1000);

								if(ip.equals(ipfound)) {
									scheduleRemoval(userId);
									System.out.println("ip found");
                							return "Please wait " + toMinutues + " minute(s).";
									
								}


                       		 			}

								sendRequest(destination);
								ipList.add(ip);
								fingerprintList.add(fingerprint);
								accountList.add(destination);
								//BlockHash hash = new BlockHash(obj.getString("hash"));nano_1xno5fdkdrbxrwkdhmjp4ah9oxg49qkdy3d491ge3tywyqyt87ubwygq8m7k
								long currentTime = System.currentTimeMillis();
								long nextReset = currentTime + 1200000;	

								ipReset.put(ip, nextReset);
								fpReset.put(fingerprint, nextReset);
								accountReset.put(destination, nextReset);


								scheduleRemoval(userId);
								res = "Nano has been sent.";
								return res;
					}
					scheduleRemoval(userId);
					res = "Faucet is dry!";
					return res;
				}


				scheduleRemoval(userId);
				res = "Not a nano address!";
				return res;

			}
			//sessionList.remove(userId);
			scheduleRemoval(userId);
			res = "Captcha verification failed!";
			return res;	
		});

		


	}
 	// Method to calculate hash of a file
	// Helper method to check if a user can submit based on rate limit

	 private static void scheduleRemoval(String userId) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        // Schedule the removal after 10 seconds
        executorService.schedule(() -> {
            if (sessionList.contains(userId)) {
                sessionList.remove(userId);
                System.out.println("Item '" + userId + "' removed after 10 seconds.");
            } else {
                System.out.println("Item '" + userId + "' not found in the list.");
            }
            executorService.shutdown();
        }, 10, TimeUnit.SECONDS);
	}
}




