import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CollectStreamAPI {
	static Properties prop;
	static Connection c = null;
	static ResultSet resultSet = null;
	static ResultSet rs = null;
	static Statement st = null;
	static PreparedStatement statement = null;
	static int count = 0;
	static String index;
	String News_id;
	ArrayList<String> list;
	ArrayList<String> noun_list;
	FileWriter fw;
	BufferedWriter bw;

	void getConnection() {
		try {
			if (c == null) {
				prop = loadProperties();
				Class.forName("org.sqlite.JDBC");
				String dbPath = prop.getProperty("dbpath");
				c = DriverManager.getConnection("jdbc:sqlite:" + dbPath
						+ "/RumorTool2.db");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Properties loadProperties() throws FileNotFoundException,
			IOException {
		Properties prop = new Properties();
		InputStream in = CollectStreamAPI.class
				.getResourceAsStream("/resources/db.properties");
		prop.load(in);
		return prop;
	}

	public static Properties loadAccessProperties()
			throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		InputStream in = CollectStreamAPI.class
				.getResourceAsStream("/resources/access_tokens.properties");
		prop.load(in);
		return prop;
	}

	public void entryPoint(String param_word) {
		try {

			Properties accessProp = loadAccessProperties();
			String this_got = param_word;
			System.out.println(this_got);
			String[] params = this_got.split("\n");
			News_id = params[params.length - 2];

			index = params[params.length - 1];
			String credentials_temp = accessProp.getProperty(index);
			String credentials = credentials_temp.substring(1,
					credentials_temp.length() - 1);

			String[] creds = credentials.split(",");
			String OAuthConsumerKey = creds[0];
			String OAuthConsumerSecret = creds[1];
			String AccessToken = creds[2];
			String AccessTokenSecret = creds[3];

			TwitterFactory twitterFactory;
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setOAuthConsumerKey(OAuthConsumerKey);
			cb.setOAuthConsumerSecret(OAuthConsumerSecret);
			cb.setOAuthAccessToken(AccessToken);
			cb.setOAuthAccessTokenSecret(AccessTokenSecret);
			TwitterStream twitterStream = new TwitterStreamFactory(cb.build())
					.getInstance();
			StatusListener listener = new StatusListener() {

				@Override
				public void onException(Exception arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onDeletionNotice(StatusDeletionNotice arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onScrubGeo(long arg0, long arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onStatus(Status status) {

					try {
						// gets Twitter handle
						if (count == 500) {
							if (rs != null) {
								rs.close();
							}
							if (st != null) {
								st.close();
							}
							if (c != null) {
								c.close();
							}
							System.exit(1);
						}

						User user = status.getUser();
						String content = status.getText();
						content = content.toLowerCase();

						Combinations com = new Combinations(noun_list);
						Double myd = 0.45 * noun_list.size();

						List<List<LinkedList>> resultList = com
								.getCombinations();
						int i = 0;
						String s1;

						while (i < resultList.size()) {

							s1 = new String("");
							List<LinkedList> s = resultList.get(i);
							if (s.size() == (myd.intValue())) {
								int j = 0;

								s1 = s1 + ".*";

								while (j < s.size()) {

									s1 = s1 + s.get(j) + ".*";
									j++;
								}

								Pattern p = Pattern.compile(s1);
								Matcher m = p.matcher(content);

								if (m.find()) {
									String username = status.getUser()
											.getScreenName();

									String name = status.getUser().getName();

									String profileLocation = user.getLocation();
									long tweetId = status.getId();

									String getid = "select * from USERS where Twitter_handle='"
											+ username + "'";

									getConnection();
									st = c.createStatement();
									rs = st.executeQuery(getid);

									if (!rs.next()) {

										String sql = "insert into USERS values(?,?,?,?,?,?)";
										statement = c.prepareStatement(sql);
										statement.setString(1, username);
										statement.setString(2, name);
										statement.setString(3, profileLocation);
										statement.setInt(4,
												user.getFollowersCount());
										statement.setInt(5,
												user.getFriendsCount());
										statement.setInt(6,
												user.getStatusesCount());
										statement.executeUpdate();
									}

									getid = "select * from tweets where Tweet_ID='"
											+ Long.toString(tweetId) + "'";
									getConnection();
									st = c.createStatement();
									rs = st.executeQuery(getid);
									if (!rs.next()) {
										System.out.println(username);
										System.out.println(name);
										System.out.println(profileLocation);
										System.out.println(tweetId);
										System.out.println(content + "\n");
										String tweet_time = status
												.getCreatedAt().toString();
										System.out.println(tweet_time + "\n");
										int retweet_count = Long.valueOf(
												status.getRetweetCount())
												.intValue();
										String retweet_status = "No";
										if (retweet_count != 0) {
											retweet_status = "Yes";
										}
										URLEntity[] urls = status
												.getURLEntities();
										StringBuilder s2 = new StringBuilder();
										for (URLEntity url : urls) {
											s2.append(url.getURL());
										}
										String urllist = s2.toString();
										HashtagEntity[] hte = status
												.getHashtagEntities();
										StringBuilder s3 = new StringBuilder();
										for (HashtagEntity ht : hte) {
											s3.append(ht.getText());
										}
										String hashtaglist = s3.toString();
										String sql = "insert into tweets values(?,?,?,?,?,?,?,?,?,?,?)";
										statement = c.prepareStatement(sql);
										statement.setString(1,
												Long.toString(tweetId));
										statement.setString(2, News_id);
										statement.setString(3, content);
										statement.setString(4, username);
										statement.setString(5, tweet_time);
										statement.setString(6, retweet_status);
										statement.setString(7, urllist);
										statement.setString(8, hashtaglist);
										statement.setString(9, "bla");
										statement.setInt(10, retweet_count);
										statement.setString(11, "bla");
										statement.executeUpdate();

									}

									break;
								}

							}
							i++;

						}

						count++;

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				@Override
				public void onTrackLimitationNotice(int arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onStallWarning(StallWarning arg0) {
					// TODO Auto-generated method stub

				}

			};
			FilterQuery fq = new FilterQuery();
			this_got = param_word;
			params = this_got.split("\n");
			News_id = params[params.length - 1];
			System.out.println("News Id: " + News_id + "\n");
			list = new ArrayList<String>();
			noun_list = new ArrayList<String>();
			String[] nouns = { "NNP", "NNS", "NN", "NNPS" };
			boolean found = false;
			for (String pms : params) {
				String[] pm = pms.split(":");
				if (pm.length == 1)
					break;
				if (!pm[0].toLowerCase().equals("viral"))
					list.add(pm[0].toLowerCase());
				found = false;
				for (String nn : nouns) {
					found = pm[1].equals(nn);
					if (found)
						break;
				}
				if ((found) && (!pm[0].toLowerCase().equals("viral")))
					noun_list.add(pm[0].toLowerCase());
			}

			String[] keywords = noun_list.toArray(new String[noun_list.size()]);
			fq.track(keywords);

			twitterStream.addListener(listener);
			twitterStream.filter(fq);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

