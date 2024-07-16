package faucet;

//java
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.lang.StringBuilder;
import java.util.Properties; 
import java.math.BigInteger;
import java.io.IOException;



//apache http components
import org.apache.http.NameValuePair;

//javax
import javax.mail.Session; 
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication; 
import javax.mail.Message;
import javax.mail.internet.MimeBodyPart; 
import javax.mail.Multipart; 
import javax.mail.Transport; 
import javax.mail.internet.MimeMessage; 
import javax.mail.internet.InternetAddress; 
import javax.mail.internet.MimeMultipart; 
import javax.mail.internet.AddressException; 
import javax.mail.MessagingException; 

//nanoweb
import static faucet.HttpRequests.getBalanceRequest;
import static faucet.HttpRequests.sendRequest;

public class Tools {

		public static Map<String, String> toMap(List<NameValuePair> pairs) {
			Map<String, String> map = new HashMap<>();
			for(int i=0; i<pairs.size(); i++){
				NameValuePair pair = pairs.get(i);
				map.put(pair.getName(), pair.getValue());
			}
			return map;
		}

		public static boolean checkDry() throws IOException {
			String balance = getBalanceRequest("nano_14hns4kgbbaf6f953qkcnh1qq1hu4tg9ig5fqrhsj6q6z7outdaj1fwutsty");	
			BigInteger faucetBalance = new BigInteger(balance);
			//check if equal or greater than 1000000000000000000000000000
			BigInteger minimum = new BigInteger("1000000000000000000000000000");
			if(faucetBalance.compareTo(minimum) >= 0) {
				return false;
			}
			return true;
		}


		public static String getCode() {
			String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
			StringBuilder salt = new StringBuilder();
			Random rnd = new Random();
			while (salt.length() < 7) { // length of the random string.
			    int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			    salt.append(SALTCHARS.charAt(index));
			}
			String saltStr = salt.toString();
			return saltStr;
		}
		public static boolean validateAccount(String account) {
			//if the first 5 chars or 3 chars is nano or xrb continue
			if(account.equals("nano_1xc1soodwjzypreh4jzme4de15p8ooe9fxreg9pgidm33btyzrnck7nxxpa9")) {
				return false;
			}
			char[] nano = new char[4];
			if(account.length() >= 64) {
				account.getChars(0, 4, nano, 0);

				if(nano[0] == 'n') {
					if(nano[1] == 'a') {
						if(nano[2] == 'n') {
							if(nano[3] == 'o') {
								if(account.length() == 65) {
									return true;
								}
							}
						}

					}
				}
				if(nano[0] == 'x') {
					if(nano[1] == 'r') {
						if(nano[2] == 'b') {
							if(account.length() == 64) {
								return true;
							}
						}
					}
		
				}
			}
			return false;
		}


}
