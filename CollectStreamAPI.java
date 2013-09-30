import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
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

public class CollectStreamAPI {
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
	
	public void entryPoint(String param_word) {
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
		TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
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
					if (count==5)
						System.exit(1);
					
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
		                count++;
					}
					
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
		String this_got = param_word;
	    String[] params = this_got.split("\n");
	    String News_id = params[params.length-1];
	    ArrayList<String> list = new ArrayList<String>();
	    for (String pms : params) {
	    String[] pm = pms.split(":");
	    list.add(pm[0]);
	    }
	    //ArrayList<String> list = new ArrayList<String>(Arrays.asList(params));
	    list.remove(params.length-1);
	    String[] keywords = list.toArray(new String[list.size()]);
	    System.out.println("java:\n");
        fq.track(keywords);

        twitterStream.addListener(listener);
        twitterStream.filter(fq);  
       
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
