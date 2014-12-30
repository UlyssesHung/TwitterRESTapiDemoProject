import ij.process.ColorProcessor;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;

import de.lmu.ifi.dbs.jfeaturelib.LibProperties;
import de.lmu.ifi.dbs.jfeaturelib.features.Histogram;

public class FeatureExtraction {
	// Feature 1 and 2: friends and followers ids
	// Simply compare the ids
	public static int[][] friends_or_followers_ids(GET_friends_or_followers_ids[] idsobjectarray) {
		int[][] idsobject_features = new int[2][];
		try {
			if (idsobjectarray[0] != null && idsobjectarray[1] != null) {
				Set<Long> base_friends_ids = new TreeSet<Long>();
				for (GET_friends_or_followers_ids friends : idsobjectarray) {
					for (long l : friends.ids) {
						base_friends_ids.add(l);
					}
				}
				Long[] base_friends_ids_array = new Long[base_friends_ids.size()];
				base_friends_ids_array = base_friends_ids.toArray(base_friends_ids_array);
				for (int i = 0; i < idsobjectarray.length; i++) {
					idsobject_features[i] = FeatureExtraction.compared_ids(base_friends_ids_array,
							idsobjectarray[i].ids);
					// if something go wrong or this user has no friends.
					// we still give friends features some weight.
					if (idsobject_features[i] == null || idsobject_features[i].length == 0)
						idsobject_features[i] = new int[30];
				}
			} else {
				for (int i = 0; i < idsobjectarray.length; i++) {
					idsobject_features[i] = new int[30];
				}
			}
			return idsobject_features;
		} catch (Exception ex) {
			ex.printStackTrace();
			for (int i = 0; i < idsobject_features.length; i++) {
				idsobject_features[i] = new int[30];
			}
			return idsobject_features;
		}
	}

	// Used by Feature 1
	public static int[] compared_ids(Long[] base, long[] target_ids) {
		int[] feature_array = new int[base.length];
		Set<Long> target_ids_set = new HashSet<Long>();
		for (Long id : target_ids) {
			target_ids_set.add(id);
		}
		for (int i = 0; i < base.length; i++) {
			if (target_ids_set.contains(base[i]))
				feature_array[i] = 1;
			else
				feature_array[i] = 0;
		}
		return feature_array;
	}

	// Feature 3: profile_image_url
	// Return the scaled distribution instead of absolute value.
	// If the size of image is different, scaled distribution can
	// deal with this problem and give a more general histogram.
	public static double[] Image(String profile_image_url) {
		if (profile_image_url == null || profile_image_url.trim().isEmpty()) {
			return new double[30];
		}
		double Max_value = 0;
		List<Double> featurelist = new ArrayList<Double>();
		BufferedImage img = null;
		try {
			URL url = new URL(profile_image_url);
			img = ImageIO.read(url);
			ColorProcessor image = new ColorProcessor(img);
			List<String> HISTOGRAMS_TYPEList = new ArrayList<String>(Arrays.asList("Red", "Green", "Blue"));
			for (String Type : HISTOGRAMS_TYPEList) {
				LibProperties prop = LibProperties.get();
				prop.setProperty(LibProperties.HISTOGRAMS_BINS, 10);

				prop.setProperty(LibProperties.HISTOGRAMS_TYPE, Type);
				Histogram descriptor = new Histogram();
				descriptor.setProperties(prop);
				descriptor.run(image);
				List<double[]> features = descriptor.getFeatures();
				for (double[] feature : features) {
					for (double f : feature) {
						if (f > Max_value) {
							Max_value = f;
						}
						featurelist.add(f);
					}
				}
			}
			double[] feature_array = new double[featurelist.size()];
			for (int i = 0; i < featurelist.size(); i++) {
				feature_array[i] = featurelist.get(i) / Max_value;
			}
			return feature_array;
		} catch (Exception ex) {
			ex.printStackTrace();
			return new double[30];
		}
	}

	// Feature 4: description of user profile
	// There are two cases in description
	// If the language is English, we just split sentence by word and use
	// unigram technique to generate features.
	// If not, we split sentence by characters and use unigram technique to
	// generate features.
	public static int[] Description(String base, String description) {
		try {
			if (base == null || base.trim().isEmpty())
				return new int[30];
			boolean base_isEnglish = isEnglish(base);
			boolean description_isEnglish = isEnglish(description);
			if (base_isEnglish && description_isEnglish) {
				return Unigramfeature(base, description, true);
			} else if (!base_isEnglish && !description_isEnglish) {
				return Unigramfeature(base, description, false);
			} else {
				// Different languages, return empty
				return Unigramfeature(base, "", base_isEnglish);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return new int[30];
		}
	}

	// Use by Feature 4
	// According to the percentage of English characters to determine whether
	// this is sentence English or not.
	public static boolean isEnglish(String sentense) {
		boolean isEnglish = true;
		int countAllchar = 0;
		int countEnglishchar = 0;
		if (sentense != null && !sentense.trim().isEmpty()) {
			char[] base_charArray = sentense.toCharArray();
			for (char a : base_charArray) {
				if ((int) a < 256) {// English Only
					countEnglishchar++;
				}
				countAllchar++;
			}
			if (((countEnglishchar / (double) countAllchar) > (9.0 / 10))
					|| ((countEnglishchar / (double) countAllchar) > ((countAllchar - 2) / (double) countAllchar))) {
				isEnglish = true;
			} else
				isEnglish = false;
		}
		return isEnglish;
	}

	// Use by Feature 4 and Feature 5, a simple unigram feature
	public static int[] Unigramfeature(String base, String target_sentense, boolean isEnglish) {
		if (isEnglish) {
			// TreeSet can output element by order.
			Set<String> base_wordset = new TreeSet<String>();
			// Only English word and numbers can be the "words" we defined.
			for (String word : base.replaceAll("[^A-Za-z0-9\\s]", "").split(" ")) {
				if (!word.trim().isEmpty() && !base_wordset.contains(word)) {
					base_wordset.add(word);
				}
			}
			int[] feature_array = new int[base_wordset.size()];
			String[] base_word_array = new String[base_wordset.size()];
			base_word_array = base_wordset.toArray(base_word_array);

			if (target_sentense != null && !target_sentense.trim().isEmpty()) {
				// HashSet perform very well on searching for specific element.
				Set<String> target_sentense_wordset = new HashSet<String>();
				for (String word : target_sentense.replaceAll("[^A-Za-z0-9\\s]", "").split(" ")) {
					if (!word.trim().isEmpty() && !target_sentense_wordset.contains(word)) {
						target_sentense_wordset.add(word);
					}
				}

				for (int i = 0; i < base_word_array.length; i++) {
					if (target_sentense_wordset.contains(base_word_array[i])) {
						feature_array[i] = 1;
					} else
						feature_array[i] = 0;
				}
			}
			// System.out.println(Arrays.toString(base_word_array));
			return feature_array;
		} else {
			// Only non-space characters can be the "character" we defined
			char[] base_charArray = base.replaceAll("[\\r\\n\\t\\s]", "").toCharArray();
			Set<Character> base_charset = new TreeSet<Character>();
			for (char character : base_charArray) {
				base_charset.add(character);
			}
			int[] feature_array = new int[base_charset.size()];
			Character[] base_charset_array = new Character[base_charset.size()];
			base_charset_array = base_charset.toArray(base_charset_array);

			if (target_sentense != null && !target_sentense.trim().isEmpty()) {
				char[] target_sentense_charArray = target_sentense.toCharArray();
				Set<Character> target_sentense_charset = new HashSet<Character>();
				for (char character : target_sentense_charArray) {
					target_sentense_charset.add(character);
				}

				for (int i = 0; i < base_charset_array.length; i++) {
					if (target_sentense_charset.contains(base_charset_array[i]))
						feature_array[i] = 1;
					else
						feature_array[i] = 0;
				}
			}
			// System.out.println(Arrays.toString(base_charset_array));
			return feature_array;
		}
	}

	// Feature 5: other features in GET_user_show;
	// This method actually is a little ugly because it only for this case.
	// The more general setting should collect a lot of user data and encode
	// every attribute used in this method then produce features for each
	// user. However, in this case, we simply compare the attribute of each
	// user, if users have same value for the attribute, set both their features
	// 1. If not, set one 1 and the other one 0.
	public static int[][] user_show(GET_users_show[] user_show_array) {
		int[][] other_show_features = new int[2][];
		try {
			String base_name = "", base_location = "", base_lang = "", base_time_zone = "";
			for (int i = 0; i < user_show_array.length; i++) {
				base_name += user_show_array[i].name + " ";
				base_location += user_show_array[i].location + " ";
				base_lang += user_show_array[i].lang + " ";
				base_time_zone += user_show_array[i].time_zone + " ";
			}
			for (int i = 0; i < user_show_array.length; i++) {
				other_show_features[i] = FeatureExtraction.Description(base_name, user_show_array[i].name);
				other_show_features[i] = ArrayUtils.addAll(other_show_features[i],
						FeatureExtraction.Description(base_location, user_show_array[i].location));
				other_show_features[i] = ArrayUtils.addAll(other_show_features[i],
						FeatureExtraction.Description(base_lang, user_show_array[i].lang));
				other_show_features[i] = ArrayUtils.addAll(other_show_features[i],
						FeatureExtraction.Description(base_time_zone, user_show_array[i].time_zone));
			}
			return other_show_features;
		} catch (Exception ex) {
			ex.printStackTrace();
			for (int i = 0; i < other_show_features.length; i++) {
				other_show_features[i] = new int[30];
			}
			return other_show_features;
		}
	}
}
