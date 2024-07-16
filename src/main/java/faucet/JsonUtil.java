package faucet;

//google gson
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.GsonBuilder;
//google json simple
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
//spark
import spark.ResponseTransformer;
public class JsonUtil {
	public static String toJson(Object object) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		String json = gson.toJson(object);
		System.out.println(json);
		return json;
	}
	public static ResponseTransformer json() {
		return JsonUtil::toJson;
	}
}
