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
    static PreparedStatement statement = null; 
    static int count = 0;
    ArrayList<String> list;
    ArrayList<String> noun_list;
    FileWriter fw;
    BufferedWriter bw;
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
					if (count==1000)
						System.exit(1);
					
					User user = status.getUser();
	               
	                String content = status.getText();
	                content = content.toLowerCase();
	                
	                Connection c = getConnection();
	                
	                Combinations com = new Combinations(noun_list);
	                Double myd =  0.45* noun_list.size();
	                
	        	    List<List<LinkedList>> resultList =  com.getCombinations();
	        		int i = 0 ; 
	        		//System.out.println("Entering...");
	        		String s1;
	        		while(i < resultList.size()){
	        			s1 = new String("");
	        			List<LinkedList>  s = resultList.get(i);
	        			if(s.size() == (myd.intValue())){
	        				int j = 0 ;
	        				
	        				s1 = s1 + ".*";
	        				//s1 = "";
	        				while(j < s.size()) {
	        					//s1.append(s.get(j));
	        					//s1.append(".*");
	        					s1 = s1 + s.get(j) + ".*";
	        					j++;
	        				}
	        				
	        				Pattern p = Pattern.compile(s1);
	        				Matcher m = p.matcher(content);
	        				//System.out.println("Pattern: "+s1+ "\n");
	        				
	        				if (m.find())
	        				{
	        					 String username = status.getUser().getScreenName();
	        		             System.out.println(username);
	        		             String name = status.getUser().getName();
	        		             System.out.println(name);
	        		             String profileLocation = user.getLocation();
	        		             System.out.println(profileLocation);
	        		             long tweetId = status.getId(); 
	        		             System.out.println(tweetId);
	        		                
	        					String getid = "select * from USERS where Twitter_handle='"+username+"'";
	        	                Statement st = c.createStatement();
	        					ResultSet rs = st.executeQuery(getid);
	        					if (!rs.next())
	        					{
	        						System.out.println(content +"\n");
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
	        					break;
	        				}
	        				//System.out.println("***" + s1 + "#####"); 
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
		String this_got = param_word;
	    String[] params = this_got.split("\n");
	    String News_id = params[params.length-1];
	    System.out.println("News Id: "+ News_id + "\n");
	    list = new ArrayList<String>();
	    noun_list = new ArrayList<String>();
	    String[] nouns = {"NNP", "NNS", "NN", "NNPS"};
	    boolean found=false;
	    for (String pms : params) {
	    String[] pm = pms.split(":");
	    if (pm.length ==1)
	    	break;
	    if (!pm[0].toLowerCase().equals("viral"))
	    	list.add(pm[0].toLowerCase());
	    found = false;
	    for (String nn : nouns) {
	    	found = pm[1].equals(nn);
	    	if (found)
	    		break;
	    }
	    if ((found)&&(!pm[0].toLowerCase().equals("viral")))
	    	noun_list.add(pm[0].toLowerCase());
	    }
	    
	    String[] keywords = noun_list.toArray(new String[noun_list.size()]);
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
