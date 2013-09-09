import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;

public class CollectTweets {
	public static void main(String[] args) throws TwitterException {
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
                User user = status.getUser();
                
                // gets Username
                String username = status.getUser().getScreenName();
                System.out.println(username);
                String profileLocation = user.getLocation();
                System.out.println(profileLocation);
                long tweetId = status.getId(); 
                System.out.println(tweetId);
                String content = status.getText();
                System.out.println(content +"\n");

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
	    
        String keywords[] = {"obama","supports","muslims"};

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
}
