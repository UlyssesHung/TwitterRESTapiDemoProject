class Response_Obtain_a_bearer_token implements Cloneable {
	public String token_type;
	public String access_token;

	// Constructor use the token I took before as a default to initialize
	// The Gson's fromJson method will also use this to initialize and if the
	// Json lack of some field, the value here will be the default value.
	// Also, it is OK to have keys that this class don't have, Gson will just
	// ignore them.
	public Response_Obtain_a_bearer_token() {
		token_type = "bearer";
		access_token = "AAAAAAAAAAAAAAAAAAAAAKEGdAAAAAAA3407LTE7WYekjT5Gvi4Mqjk3FPw%3D9fzzqgoISi62WgsVY2QUNnXlKhTPExCxWYlcVUIjDwDKTZRV2h!";
	}

	// For initialize
	public Response_Obtain_a_bearer_token(String token_type, String access_token) {
		this.token_type = token_type;
		this.access_token = access_token;
	}

	// Clone Method 1. Though this is not the typical Java clone method, I
	// prefer this actually because this way is workable in different languages
	// and more flexible.
	public Response_Obtain_a_bearer_token(Response_Obtain_a_bearer_token item) {
		this.token_type = item.token_type;
		this.access_token = item.access_token;
	}

	// Clone Method 1. Typical Java clone method;
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}

// This class include all the field I saw in Json, including many nested class
// because I just want to try and know how gson works in this situation. In
// other class I will only add the attribute I used in similarity measurement.
class GET_users_lookup implements Cloneable {
	// used both in entitiesclass.urlclass and
	// statusclass.entitiesclass.urlclass
	public class urlsclass {
		public String url;
		public String expanded_url;
		public String display_url;
		public Integer[] indices;
	}

	public long id;
	public String id_str;
	public String name;
	public String screen_name;
	public String location;
	public String profile_location;
	public String description;
	public String url;

	public entitiesclass entities;

	public class entitiesclass {
		public urlclass url;
		public urldescription description;

		public class urlclass {
			public urlsclass[] urls;
		}

		public class urldescription {
			public urlsclass[] urls;
		}

	}

	// public boolean protected;
	public int followers_count;
	public int friends_count;
	public int listed_count;
	public String created_at;// Date

	public int favourites_count;
	public int utc_offset;
	public String time_zone;
	public boolean geo_enabled;
	public boolean verified;
	public int statuses_count;
	public String lang;

	public statusclass status;

	// twitter article
	public class statusclass {
		public String created_at;// Date
		public long id;
		public String id_str;

		public String text;
		public String source;
		public boolean truncated;
		public int in_reply_to_status_id;
		public String in_reply_to_status_id_str;
		public int in_reply_to_user_id;
		public String in_reply_to_user_id_str;
		public String in_reply_to_screen_name;
		public String geo;
		public String coordinates;
		public String place;
		public String contributors;
		public int retweet_count;
		public int favorite_count;

		public entitiesclass entities;

		public class entitiesclass {
			public hashtagsclass[] hashtags;

			// never encounter these two field have values
			// public symbolsclass[] symbols;
			// public user_mentionsclass[] user_mentions;

			public class hashtagsclass {
				public String text;
				public Integer[] indices;
			}

			public urlsclass[] urls;

			public mediaclass[] media;

			public class mediaclass {
				public long id;
				public String id_str;
				public Integer[] indices;

				public String media_url;
				public String media_url_https;
				public String url;
				public String display_url;
				public String expanded_url;

				public String type;

			}

			public boolean favorited;
			public boolean retweeted;
			public boolean possibly_sensitive;
			public String lang;
		}

		public boolean contributors_enabled;
		public boolean is_translator;
		public boolean is_translation_enabled;
		public String profile_background_color;
		public String profile_background_image_url;
		public String profile_background_image_url_https;
		public String profile_background_tile;
		public String profile_image_url;
		public String profile_image_url_https;
		public String profile_banner_url;
		public String profile_link_color;
		public String profile_sidebar_border_color;
		public String profile_sidebar_fill_color;
		public String profile_text_color;

		public boolean profile_use_background_image;
		public boolean default_profile;
		public boolean default_profile_image;

		public String following;
		public String follow_request_sent;
		public String notifications;
	}

	public GET_users_lookup() {
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}

class GET_users_show implements Cloneable {
	public String name;
	public String profile_image_url;
	public String created_at;
	public String location;
	public String profile_image_url_https;
	public int utc_offset;
	public String lang;
	public String profile_background_image_url_https;
	public String time_zone;
	public String description;
	public String profile_background_image_url;
	public String screen_name;

	public GET_users_show() {
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}

class GET_friends_or_followers_ids implements Cloneable {
	public String previous_cursor;
	public String next_cursor;
	public long[] ids;

	public GET_friends_or_followers_ids() {
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
