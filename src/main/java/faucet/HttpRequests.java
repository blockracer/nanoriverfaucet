package faucet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
//java
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;



public class HttpRequests {

	public static JsonObject jsonObject2;
	public static final String url = "https://www.google.com/recaptcha/api/siteverify";
	public static final String secret = "sercet";
	private final static String USER_AGENT = "Mozilla/5.0";
	public static StringBuffer response2;

public static boolean sendRequest(String destination) throws IOException {
    String amount = "10000000000000000000000000";

    // Create an HttpClient with default settings
    CloseableHttpClient httpClient = HttpClients.createDefault();

    // Define the URL you want to send the POST request to
    String sendUrl = "https://secret.nanoriver.io/send";

    //String sendUrl = "http://[::1]:7076/faucetsend";

    // Create an HttpPost object with the URL
    HttpPost httpPost = new HttpPost(sendUrl);

    // Add headers to the request
    httpPost.addHeader("User-Agent", USER_AGENT);
    httpPost.addHeader("Accept-Language", "en-US,en;q=0.5");
    httpPost.addHeader("auth", "secret");

    // Create JSON object for the POST request
    JsonObject jsonRequest = Json.createObjectBuilder()
            .add("amount", amount)
            .add("destination", destination)
            .build();

    // Convert JSON object to string
    StringEntity entity = new StringEntity(jsonRequest.toString(), ContentType.APPLICATION_JSON);

    // Set the JSON entity to the request
    httpPost.setEntity(entity);

    try {
        // Execute the request and get the response
        CloseableHttpResponse response = httpClient.execute(httpPost);

        // Check if the response code is successful (HTTP 200 OK)
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            // Parse the response content as a JSON string
            String jsonResponse = EntityUtils.toString(response.getEntity());

            // Use Gson to parse the JSON string into a JsonObject
            //JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse));
            //JsonObject jsonObject = jsonReader.readObject();
	    //System.out.println(jsonObject);
            //jsonReader.close();

            // Print the result
            //System.out.println(jsonObject);

            return true;
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try {
            // Close the response to release resources
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    return false;
}
	


	public static String getBalanceRequest(String account) throws IOException {
		try{
		String sendUrl = "https://secret.nanoriver.io/getbalance";
		//String sendUrl = "http://[::1]:7076/getbalance";
		URL obj2 = new URL(sendUrl);
		HttpURLConnection con2 = (HttpURLConnection) obj2.openConnection();

		// add reuqest header
		con2.setRequestMethod("GET");
		con2.setRequestProperty("User-Agent", USER_AGENT);
		con2.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		con2.setRequestProperty("auth", "secret");

		// Send post request
		con2.setDoOutput(true);
		DataOutputStream wr2 = new DataOutputStream(con2.getOutputStream());

		Map<String, String> parameters = new HashMap<>();
		parameters.put("account", account);

		wr2.writeBytes(ParameterStringBuilder.getParamsString(parameters));
		wr2.flush();
		wr2.close();
		
		int responseCode2 = con2.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + sendUrl);
		System.out.println("Response Code : " + responseCode2);

		BufferedReader in2 = new BufferedReader(new InputStreamReader(
				con2.getInputStream()));
		String inputLine2;
		response2 = new StringBuffer();

		while ((inputLine2 = in2.readLine()) != null) {
			response2.append(inputLine2);
		}
		in2.close();

		// print result
		System.out.println(response2.toString());
		
		//parse JSON response and return 'success' value
		JsonReader jsonReader2 = Json.createReader(new StringReader(response2.toString()));
		jsonObject2 = jsonReader2.readObject();
		jsonReader2.close();
		
		//return jsonObject.getString("hash");
		System.out.println(jsonObject2);

		return jsonObject2.getString("balance");
		}catch(Exception e){
			e.printStackTrace();
			return response2.toString();
		}
	}
	public static boolean verifyV3(String gRecaptchaResponse) throws IOException {
    if (gRecaptchaResponse == null || "".equals(gRecaptchaResponse)) {
        return false;
    }

    try {
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        // Add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        // Set the secret key and v3 action
        String postParams = "secret=" + secret + "&response=" + gRecaptchaResponse;

        // Send post request
        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.writeBytes(postParams);
            wr.flush();
        }

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + postParams);
        System.out.println("Response Code : " + responseCode);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            // Print result
            System.out.println(response.toString());

            // Parse JSON response and return 'success' value
            JsonReader jsonReader = Json.createReader(new StringReader(response.toString()));
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();

            // Assuming the structure of the v3 response JSON
            return jsonObject.getBoolean("success");
        }
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}






}
