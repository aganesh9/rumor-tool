import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CollectSearchAPI {
	static Properties prop;
	static Connection c = null;
	static ResultSet resultSet = null;
	static PreparedStatement statement = null;
	static int count = 0;
	ArrayList<String> list;
	ArrayList<String> noun_list;
    static String index;
	static Connection getConnection() {
		try {
			if (c == null) {
				prop = loadDBProperties();
				Class.forName("org.sqlite.JDBC");
				String dbPath = prop.getProperty("dbpath");
				c = DriverManager.getConnection("jdbc:sqlite:" + dbPath
						+ "/RumorTool.db");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	public static Properties loadDBProperties() throws FileNotFoundException,
			IOException {
		Properties prop = new Properties();
		InputStream in = CollectStreamAPI.class
				.getResourceAsStream("/resources/db.properties");
		prop.load(in);
		return prop;
	}
	
	public static Properties loadAccessProperties() throws FileNotFoundException,
			IOException {
		Properties prop = new Properties();
		InputStream in = CollectStreamAPI.class
				.getResourceAsStream("/resources/access_tokens.properties");
		prop.load(in);
		return prop;
	}

	public void entryPoint(String param_word) {
		try {
			
			TwitterFactory twitterFactory;
			ConfigurationBuilder cb = new ConfigurationBuilder();
			
			Properties accessProp = loadAccessProperties();
			//String id = "2" ; 
			
			String this_got = param_word;
			System.out.println(this_got);
			String[] params = this_got.split("\n");
			String News_id = params[params.length - 2];
			
			index = params[params.length - 1];
			String credentials_temp = accessProp.getProperty(index);
			String credentials = credentials_temp.substring(1, credentials_temp.length()-1);
			
			
			
			String[] creds = credentials.split(",");
			String OAuthConsumerKey = creds[0];
			String OAuthConsumerSecret= creds[1];
			String AccessToken = creds[2];
			String AccessTokenSecret = creds[3];
			/*System.out.println(OAuthConsumerKey);
			System.out.println(OAuthConsumerSecret);
			System.out.println(AccessToken);
			System.out.println(AccessTokenSecret);*/
			
			cb.setOAuthConsumerKey(OAuthConsumerKey);
			cb.setOAuthConsumerSecret(OAuthConsumerSecret);
			cb.setOAuthAccessToken(AccessToken);
			cb.setOAuthAccessTokenSecret(AccessTokenSecret);

			
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

			StringBuilder bigword = new StringBuilder();

			int i;
			for (i = 0; i < keywords.length - 1; i++) {
				bigword.append(keywords[i]);
				bigword.append(" OR ");
			}
			bigword.append(keywords[i]);

			twitterFactory = new TwitterFactory(cb.build());
			Twitter twitter = twitterFactory.getInstance();

			/*Query query = new Query(bigword.toString());
			query.setCount(100); // set tweets per page to 5

			QueryResult result = twitter.search(query);
			List<Status> qrTweets = result.getTweets();*/

			Combinations com = new Combinations(noun_list);
			Double myd =  noun_list.size()*0.5;
			System.out.println("Noun list size: "+noun_list.size());
			System.out.println("List size: "+myd.intValue());
			List<String> list = com.getUniqueCombinations(noun_list.toArray(new String[noun_list.size()]), myd.intValue());
			int ind = 0 ; 
			while(ind < list.size()){
				Query query = new Query(list.get(ind));
				ind++;
				query.setCount(5); // set tweets per page to 5

				QueryResult result = twitter.search(query);
				List<Status> qrTweets = result.getTweets();
				
				//System.exit(1);
				String s1;
				String content;
				for (Status status : qrTweets) {
					content = status.getText();
					i = 0;
					//System.out.println("Size.."+ resultList.size());
					
					User user = status.getUser();
					String username = status.getUser().getScreenName();
					
					String name = status.getUser().getName();
					
					String profileLocation = user.getLocation();
					
					long tweetId = status.getId();
					
					Connection c = getConnection();
					String getid = "select * from USERS where Twitter_handle='"
							+ username + "'";
					Statement st = c.createStatement();
					ResultSet rs = st.executeQuery(getid);
					if (!rs.next()) {
						System.out.println(username);
						//System.out.println(name);
						//System.out.println(profileLocation);
					
						String sql = "insert into USERS values(?,?,?,?,?,?)";
						statement = c.prepareStatement(sql);
						statement.setString(1, username);
						statement.setString(2, name);
						statement.setString(3, profileLocation);
						statement.setInt(4, user.getFollowersCount());
						statement.setInt(5, user.getFriendsCount());
						statement.setInt(6, user.getStatusesCount());
						statement.executeUpdate();
					}
					rs.close();
					st.close();
					getid = "select * from tweets where Tweet_ID='"+Long.toString(tweetId)+"'";
					st = c.createStatement();
					rs = st.executeQuery(getid);
					if (!rs.next()) {
						System.out.println(tweetId);
						System.out.println(content + "\n");
						//System.out.println(News_id + "\n");
						//System.out.println(username + "\n");
						String tweet_time = status.getCreatedAt().toString();
						System.out.println(tweet_time + "\n");
						int retweet_count = Long.valueOf(status.getRetweetCount()).intValue();
						String retweet_status = "No";
						if (retweet_count!= 0) {
							retweet_status="Yes";
						}
						//System.out.println(retweet_status + "\n");
						URLEntity[] urls = status.getURLEntities();
						StringBuilder s2 = new StringBuilder(); 
						for(URLEntity url : urls){
							  s2.append(url.getURL());
							  //System.out.println(url.getURL());
						}
						String urllist = s2.toString();
						HashtagEntity[] hte = status.getHashtagEntities();
						StringBuilder s3 = new StringBuilder();
						for(HashtagEntity ht : hte){
							  s3.append(ht.getText());
							  //System.out.println(ht.getText());
						}
						String hashtaglist = s3.toString();
						//System.out.println("Number of retweets: "+retweet_count + "\n");
						//Status replyStatus = twitter.showStatus(status.getInReplyToStatusId());
						//System.out.println(replyStatus.getText());
						//Status retweetStatus = status.getRetweetedStatus();
						//System.out.println(retweetStatus.get); 
						
					    /*List<Status> statuses = twitter.getRetweets(status.getId());
					    for (Status stat : statuses) {
					    	 System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
					    }*/
						String sql = "insert into tweets values(?,?,?,?,?,?,?,?,?,?,?)";
						statement = c.prepareStatement(sql);
						statement.setString(1, Long.toString(tweetId));
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
						
						rs.close();
						st.close();
						}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}