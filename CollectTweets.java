import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

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

public class CollectTweets {
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
		String path = System.getProperty("user.dir") + System.getProperty("file.separator");
		prop.load(new FileReader(path+ "/src/resources/db.properties"));
		return prop;
	}
	
	public static void main(String[] args) {
		try {
		
		String OAuthConsumerKey = "x4tPFBBC3KxDnVaHBvsRQ";
		String OAuthConsumerSecret = "X0pgrtasZhgl2jasfi2IczJkMvirljb46PAcOsPmEM";

		// This is where you enter your Access Token info
		String AccessToken = "1840289263-3Gv5plXN8VDn08gQbIdb5FvITTz82scQOYSajdK";
		String AccessTokenSecret = "NMjmYWNd104EVCyMIDCnbLm3BVc4bg4NaH7mIRYRaY";

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
	    
        String keywords[] = {"cyrus","miley","suicide"};

        fq.track(keywords);

        twitterStream.addListener(listener);
        twitterStream.filter(fq);  
		/*twitterFactory = new TwitterFactory(cb.build());
	    Twitter twitter = twitterFactory.getInstance();

	    for (int page = 1; page <= 10; page++) {
	        System.out.println("\nPage: " + page);
	        Query query = new Query("#Obama");
	        query.count(100); // set tweets per page to 100
	        query.setSinceId(page);
	        QueryResult res = twitter.search(query); 
	        
	        List<Status> qrTweets = (res).getTweets();

	        // break out of the loop early if there are no more tweets
	        if(qrTweets.size() == 0) break;

	        for(Status t : qrTweets) {
	            System.out.println(t.getId() + " - " + t.getCreatedAt() + ": " + t.getText());
	        }
	    }*/
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
