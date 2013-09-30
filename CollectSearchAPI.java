import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
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
	static Connection getConnection() {
		try {
			if (c==null)
			{
				prop = loadProperties();
				Class.forName("org.sqlite.JDBC");  
	   	        String dbPath = prop.getProperty("dbpath");
	           c = DriverManager.getConnection("jdbc:sqlite:"+ dbPath +"/RumorTool.db");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return c;
	}
	
	public static Properties loadProperties() throws FileNotFoundException, IOException{
		Properties prop = new Properties();
		InputStream in = CollectStreamAPI.class.getResourceAsStream("/resources/db.properties");
		prop.load(in);
		return prop;
	}
	
	public  void entryPoint(String param_word) {
		try {
		
		String OAuthConsumerKey = "xxx";
		String OAuthConsumerSecret = "xxx";

		// This is where you enter your Access Token info
		String AccessToken = "xxx";
		String AccessTokenSecret = "xxx";

		TwitterFactory twitterFactory; 
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey(OAuthConsumerKey);
		cb.setOAuthConsumerSecret( OAuthConsumerSecret );
		cb.setOAuthAccessToken(  AccessToken);
		cb.setOAuthAccessTokenSecret( AccessTokenSecret );
		
	    String this_got = param_word;
	    String[] params = this_got.split("\n");
	    String News_id = params[params.length-1];
	    ArrayList<String> list = new ArrayList<String>();
	    for (String pms : params) {
	    String[] pm = pms.split(":");
	    list.add(pm[0]);
	    }
	   
	    list.remove(params.length-1);
	    String[] keywords = list.toArray(new String[list.size()]);
	    System.out.println("java:\n" + keywords);
	    StringBuilder bigword = new StringBuilder();
		
	    int i;
	    for (i=0;i<keywords.length-1;i++)
	    {
	    	bigword.append(keywords[i]);
	    	bigword.append(" OR ");
	    }
	    bigword.append(keywords[i]);
	    
		twitterFactory = new TwitterFactory(cb.build());
	    Twitter twitter = twitterFactory.getInstance();

	        	Query query = new Query(bigword.toString());
	        	query.setCount(5); // set tweets per page to 5
	            
	            QueryResult result = twitter.search(query);
	            List<Status> qrTweets = result.getTweets();
	        for (Status status : qrTweets) {
	        	User user = status.getUser();
                String username = status.getUser().getScreenName();
                System.out.println(username);
                String name = status.getUser().getName();
                System.out.println(name);
                String profileLocation = user.getLocation();
                System.out.println(profileLocation);
                long tweetId = status.getId(); 
                System.out.println(tweetId);
                String content = status.getText();
                System.out.println(content +"\n");
                
                Connection c = getConnection();
                String getid = "select * from USERS where Twitter_handle='"+username+"'";
                Statement st = c.createStatement();
				ResultSet rs = st.executeQuery(getid);
				if (!rs.next())
				{
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
	        }
	       
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
