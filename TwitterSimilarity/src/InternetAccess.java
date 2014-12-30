import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;

public class InternetAccess {
	// Obtain or Invalidate a bearer token
	public static Response_Obtain_a_bearer_token Obtain_or_Invalidating_a_bearer_token(String Method, String targetURL,
			String urlParameters) {
		String Consumer_Key = "FillYourOwnValueHere";
		String Consumer_Secret = "FillYourOwnValueHere";
		String Bearer_token_credentials = Consumer_Key + ":" + Consumer_Secret;
		String Base64_Bearer_token_credentials = "";
		try {
			Base64.Encoder base64encoder = Base64.getEncoder();
			Base64_Bearer_token_credentials = base64encoder.encodeToString(Bearer_token_credentials.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		URL url;
		HttpURLConnection connection = null;
		try {
			// Create connection
			url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(Method);
			connection.setRequestProperty("Host", "api.twitter.com");
			connection.setRequestProperty("User-Agent", "UlyssesTwitterApps");
			connection.setRequestProperty("Authorization", "Basic " + Base64_Bearer_token_credentials);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Accept-Encoding", "gzip");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			// Get response. Directly change Json to object here and return the
			// object because the way to send request to access token is fix.
			// However, the way to access different user information is
			// flexible, so in that function I will return a string and do not
			// change Json to object directly in the function.
			InputStream is = new GZIPInputStream(connection.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			Gson gson = new Gson();
			Response_Obtain_a_bearer_token ROabt = gson.fromJson(br, Response_Obtain_a_bearer_token.class);
			return ROabt;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	// for retrieve data from twitter REST api.
	public static String excute(Response_Obtain_a_bearer_token token, String Method, String targetURL,
			String urlParameters) {
		URL url;
		HttpURLConnection connection = null;
		try {
			// Create connection
			url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(Method);
			connection.setRequestProperty("Host", "api.twitter.com");
			connection.setRequestProperty("User-Agent", "UlyssesTwitterApps");
			connection.setRequestProperty("Authorization", token.token_type + " " + token.access_token);
			connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Accept-Encoding", "gzip");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			// Send request
			if (Method.equals("GET")) {
				connection.setDoOutput(false);
			} else if (Method.equals("POST")) {
				DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
				wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();
			}
			// Get Response
			InputStream is = null;
			try {
				is = new GZIPInputStream(connection.getInputStream());
			} catch (IOException exception) {
				is = new GZIPInputStream(connection.getErrorStream());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				response.append(line);
			}
			br.close();
			// return a string and do not change Json to object directly in
			// this function.
			return response.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	/*
	 * The method below is for obtain Oauth1 token. They are useless in this
	 * time, but if we want to extend or program to more general cases and
	 * provide more functions such as POST a twitter message, we need Oauth1
	 * token. (I just test how Oauth1 works so the code below is a little mess.)
	 */

	private static String encode(String value) {
		String encoded = "";
		try {
			encoded = URLEncoder.encode(value, "UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		String sb = "";
		char focus;
		for (int i = 0; i < encoded.length(); i++) {
			focus = encoded.charAt(i);
			if (focus == '*') {
				sb += "%2A";
			} else if (focus == '+') {
				sb += "%20";
			} else if (focus == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7'
					&& encoded.charAt(i + 2) == 'E') {
				sb += '~';
				i += 2;
			} else {
				sb += focus;
			}
		}
		return sb.toString();
	}

	private static String generateSignature(String signatueBaseStr, String oAuthConsumerSecret, String oAuthTokenSecret) {
		byte[] byteHMAC = null;
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec spec;
			if (null == oAuthTokenSecret) {
				String signingKey = encode(oAuthConsumerSecret) + '&';
				spec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
			} else {
				String signingKey = encode(oAuthConsumerSecret) + '&' + encode(oAuthTokenSecret);
				spec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
			}
			mac.init(spec);
			byteHMAC = mac.doFinal(signatueBaseStr.getBytes());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Base64.Encoder base64encoder = Base64.getEncoder();
		return base64encoder.encodeToString(byteHMAC).trim();
	}

	public static String Obtain_a_access_token(String Method, String targetURL, String urlParameters) {
		String Consumer_Key = "FillYourOwnValueHere";
		String Consumer_Secret = "FillYourOwnValueHere";
		String Access_Token = "FillYourOwnValueHere";
		String Access_Token_Secret = "FillYourOwnValueHere";
		URL url;
		HttpURLConnection connection = null;
		try {

			Map<String, String> AuthKeyValues = new TreeMap<String, String>();

			String[] urlParameters_array = urlParameters.split("&");
			for (String urlpara : urlParameters_array) {
				String[] urlparakeyandvalue = urlpara.split("=");
				if (urlparakeyandvalue.length == 2)
					AuthKeyValues.put(urlparakeyandvalue[0], urlparakeyandvalue[1]);
			}

			AuthKeyValues.put("oauth_consumer_key", Consumer_Key);
			AuthKeyValues.put("oauth_token", Access_Token);
			// AuthKeyValues.put("oauth_callback", "oob");

			String uuid_string = UUID.randomUUID().toString();
			uuid_string = uuid_string.replaceAll("-", "");
			String oauth_nonce = uuid_string;
			AuthKeyValues.put("oauth_nonce", oauth_nonce);
			AuthKeyValues.put("oauth_signature_method", "HMAC-SHA1");
			AuthKeyValues.put("oauth_timestamp", String.valueOf(System.currentTimeMillis() / 1000));
			AuthKeyValues.put("oauth_version", "1.0");
			int count = 0;
			String parameterforsignature = "";

			for (Entry<String, String> entry : AuthKeyValues.entrySet()) {
				// System.out.println(entry.getKey());
				parameterforsignature += encode(entry.getKey()) + "=" + encode(entry.getValue());
				count++;
				if (count < AuthKeyValues.size())
					parameterforsignature += "&";
			}
			System.out.println("parameterforsignature=" + parameterforsignature);
			String signatueBaseStr = Method + "&" + encode(targetURL) + "&" + encode(parameterforsignature);
			System.out.println("signatueBaseStr=" + signatueBaseStr);
			String oauth_signature = generateSignature(signatueBaseStr, Consumer_Secret, null);
			System.out.println("oauth_signature=" + oauth_signature);
			System.out.println("oauth_signature=" + URLEncoder.encode(oauth_signature, "UTF-8"));
			AuthKeyValues.put("oauth_signature", URLEncoder.encode(oauth_signature, "UTF-8"));

			count = 0;
			String parameterforauth = "";
			for (Entry<String, String> entry : AuthKeyValues.entrySet()) {
				parameterforauth += entry.getKey() + "=" + "\"" + entry.getValue() + "\"";
				count++;
				if (count < AuthKeyValues.size())
					parameterforauth += ",";
			}

			System.out.println("parameterforauth=" + parameterforauth);

			// Create connection
			url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(Method);
			connection.setRequestProperty("Host", "api.twitter.com");
			connection.setRequestProperty("User-Agent", "UlyssesTwitterApps");
			connection.setRequestProperty("Authorization", "OAuth  " + parameterforauth);
			System.out.println("OAuth " + parameterforauth);
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				response.append(line);
			}
			br.close();
			String[] responsepara = response.toString().split("&");
			for (String repara : responsepara) {
				String[] parakeyandvalue = repara.split("=");
				if (parakeyandvalue.length == 2) {
					if (parakeyandvalue[0].equals("oauth_token")) {
						return parakeyandvalue[1];
					}
				}
			}
			return null;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public static String excutenoauth(String Method, String targetURL, String urlParameters) {
		URL url;
		HttpURLConnection connection = null;
		try {
			// Create connection
			url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(Method);
			connection.setRequestProperty("Host", "api.twitter.com");
			connection.setRequestProperty("User-Agent", "UlyssesTwitterApps");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Accept-Encoding", "gzip");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			// Get Response
			InputStream is = new GZIPInputStream(connection.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				response.append(line);
			}
			br.close();
			// return a string and do not change Json to object directly in this
			// function.
			return response.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

}
