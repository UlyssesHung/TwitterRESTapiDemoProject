import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;

// This is the entry point of this program.
// In this problem, we use Cosine similarity which output value will between 0~1. 
// 0 means that these two users are totally different and 1 means they are the same user.   

public class TwitterSimilarity {
	public static void main(String args[]) throws IOException {
		// Step 1: Read file
		System.out.println("Step 1: Read file.");
		String[] user_names = new String[2];
		try {
			user_names = ReadNamePair();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Read name from file failed! Use default name satoshihirose and keita.");
			user_names[0] = "renamatsuii";
			user_names[1] = "maishiraishii";
		}

		// Step 2: Obtain token in order to access twitter REST api.
		System.out.println("Step 2: Obtain token in order to access twitter REST api.");
		Response_Obtain_a_bearer_token token = new Response_Obtain_a_bearer_token();
		int count = 0;
		while (count < 5) {
			token = InternetAccess.Obtain_or_Invalidating_a_bearer_token("POST",
					"https://api.twitter.com/oauth2/token", "grant_type=client_credentials");
			if (token != null) {
				System.out.println("get token_type=" + token.token_type + "; access_token=" + token.access_token + "!");
				break;
			} else
				count++;
		}
		if (count >= 5) {
			System.out.println("get token fail, will try to use default token to access!");
			token = new Response_Obtain_a_bearer_token();
		}

		// Step 3: Query user information from Twitter
		System.out.println("Step 3: Query user information from Twitter.");
		GET_users_show[] users_show_array = new GET_users_show[2];
		GET_friends_or_followers_ids[] friends_ids_array = new GET_friends_or_followers_ids[2];
		GET_friends_or_followers_ids[] followers_ids_array = new GET_friends_or_followers_ids[2];

		int[][] friends_features = new int[2][];
		int[][] followers_features = new int[2][];
		double[][] image_features = new double[2][];
		int[][] description_features = new int[2][];
		int[][] other_show_features = new int[2][];
		double[][] allfeatures = new double[2][];

		Gson gson = new Gson();
		for (int i = 0; i < user_names.length; i++) {
			// GET users/show
			String users_show = InternetAccess.excute(token, "GET",
					"https://api.twitter.com/1.1/users/show.json?screen_name=" + user_names[i], "");
			GET_users_show users_show_object = gson.fromJson(users_show, GET_users_show.class);
			users_show_array[i] = users_show_object;

			// GET friends/ids
			String friends_ids = InternetAccess.excute(token, "GET",
					"https://api.twitter.com/1.1/friends/ids.json?count=5000&screen_name=" + user_names[i], "");
			GET_friends_or_followers_ids friends_ids_object = gson.fromJson(friends_ids,
					GET_friends_or_followers_ids.class);
			friends_ids_array[i] = friends_ids_object;

			// GET followers/ids
			String followers_ids = InternetAccess.excute(token, "GET",
					"https://api.twitter.com/1.1/followers/ids.json?count=5000&screen_name=" + user_names[i], "");
			GET_friends_or_followers_ids followers_ids_object = gson.fromJson(followers_ids,
					GET_friends_or_followers_ids.class);
			followers_ids_array[i] = followers_ids_object;
		}

		/*
		 * Step 4: Feature extraction 1. Text: For support different languages,
		 * just simply use Unigram to produce features. I do not use methods
		 * that need training data from different languages, because that will
		 * make this file become too complex. If we want to dealing with this
		 * issue. We need NLP package for every language we want to deal. 2.
		 * Profile Picture 3. Location and other information in Get user/show
		 */
		System.out.println("Step 4: Feature extraction.");
		// Use GET friends/ids to extract friends features
		friends_features = FeatureExtraction.friends_or_followers_ids(friends_ids_array);
		for (int i = 0; i < friends_ids_array.length; i++) {
			allfeatures[i] = ArrayUtils.addAll(allfeatures[i], Doubles.toArray(Ints.asList(friends_features[i])));
		}

		// Use GET followers/ids to extract followers features
		followers_features = FeatureExtraction.friends_or_followers_ids(followers_ids_array);
		for (int i = 0; i < followers_ids_array.length; i++) {
			allfeatures[i] = ArrayUtils.addAll(allfeatures[i], Doubles.toArray(Ints.asList(followers_features[i])));
		}

		// Use users/show profile_image_url to extract image features.
		// Use users/show description to extract description features.
		// To save time, if there is no users_show_array, simply generate some
		// zero features
		if (users_show_array[0] != null && users_show_array[1] != null) {
			for (int i = 0; i < users_show_array.length; i++) {
				image_features[i] = FeatureExtraction.Image(users_show_array[i].profile_image_url);
				description_features[i] = FeatureExtraction.Description(users_show_array[0].description + " "
						+ users_show_array[1].description, users_show_array[i].description);
				// Simple Test:
				// FeatureExtraction.Description("乃木坂46のまいやんこと白石麻衣です",
				// "2ndアルバム１１月１９日発売発売！");
			}
			other_show_features = FeatureExtraction.user_show(users_show_array);
		} else {
			for (int i = 0; i < users_show_array.length; i++) {
				image_features[i] = new double[30];
				description_features[i] = new int[30];
				other_show_features[i] = new int[30];
			}
		}

		for (int i = 0; i < users_show_array.length; i++) {
			allfeatures[i] = ArrayUtils.addAll(allfeatures[i], image_features[i]);
			allfeatures[i] = ArrayUtils.addAll(allfeatures[i], Doubles.toArray(Ints.asList(description_features[i])));
			allfeatures[i] = ArrayUtils.addAll(allfeatures[i], Doubles.toArray(Ints.asList(other_show_features[i])));
		}

		// Step 5: Similarity measurement by Cosine similarity
		System.out.println("Step 5: Similarity measurement by Cosine similarity.");
		double similarity_Consine = Measurement.similarity(allfeatures, "Consine", null);
		System.out.println("similarity_Consine=" + similarity_Consine);
		// This is another similarity measurement method.
		// double similarity_t = Measurement.similarity(allfeatures,
		// "Traditional", null);
		// System.out.println("similarity_t=" + similarity_t);

		// Step 6: Output file
		System.out.println("Step 6: Output file.");
		try {
			Writer bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("similarity.csv"), "UTF-8"));
			if (users_show_array[0] == null || users_show_array[1] == null)
				bw.write(user_names[0] + ", " + user_names[1] + ", " + similarity_Consine + "\n");
			else
				bw.write(users_show_array[0].screen_name + ", " + users_show_array[1].screen_name + ", "
						+ similarity_Consine + "\n");
			bw.close();
		} catch (Exception ex) {
			System.out.println("Output file failed!");
			ex.printStackTrace();
		}
		System.out.println("All done! The result saved to similarity.csv");
	}

	// Read the name pair from CSV file
	static String[] ReadNamePair() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("namepair.csv"));
			String line;
			String[] user_names = new String[2];
			while ((line = br.readLine()) != null) {
				user_names = line.split(",");
			}
			br.close();
			if (user_names == null || user_names.length != 2) {
				System.out.println("illegal username!");
				return null;
			} else {
				List<String> name_list = new ArrayList<String>();
				for (String name : user_names) {
					name_list.add(name.trim());
				}
				return name_list.toArray(user_names);
			}
		} catch (Exception ex) {
			System.out.println("Real file error!");
			ex.printStackTrace();
			return null;
		}
	}
}
