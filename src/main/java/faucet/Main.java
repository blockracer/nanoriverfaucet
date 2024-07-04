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
//import static faucet.HttpRequests.verify;
import static faucet.HttpRequests.verifyV3;
import static faucet.HttpRequests.sendRequest;
import static faucet.Tools.validateAccount;
import static faucet.Tools.checkDry;


import spark.utils.SparkUtils;


public class Main {

	public static Logger logger = Logger.getLogger(Main.class);
	public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	public static List<String> ipList = new ArrayList<>();

	public static void main (String[] args) {

		scheduler.scheduleAtFixedRate(() -> {
            		// Clear the list
            		ipList.clear();
            		System.out.println("List cleared at " + System.currentTimeMillis());
        	}, 0, 5, TimeUnit.MINUTES);

		externalStaticFileLocation("/home/server-admin/javaProjects/private_repo/faucet/src/main/resources");
		port(4114);

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
            		String gRecaptchaResponse = json.containsKey("g-recaptcha-response") ? json.getString("g-recaptcha-response") : null;
			System.out.println(gRecaptchaResponse);
			System.out.println(destination);

    			// Retrieve values from JSON

			System.out.println(gRecaptchaResponse);
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
							for (String str : ipList) {
            							if (str.equals(ip) && !str.equals("144.137.220.73")) {
                							System.out.println("ip found");
                							return "please wait 5 minutes";
            							}
							}


								sendRequest(destination);
								ipList.add(ip);
								res = "Nano has been sent";
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




