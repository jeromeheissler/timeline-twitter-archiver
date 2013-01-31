package controllers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import lib.TwitterRestApi;
import lib.UserNotFoundTwitterRestApiException;
import lib.models.TweetModel;
import lib.models.UserModel;

import play.libs.Json;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

	public static DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
	public static Date dmax = null;
	public static String since_id = null;
	
	/**
	 * The index of site
	 */
	public static Result index() {
		if(!session().containsKey("twitterId"))	{	
			String twitOAuth = TwitterController.login();
			play.Logger.debug(twitOAuth);
			return ok(index.render(twitOAuth));
		}else	{
			return redirect("/loadtimeline");
		}
	}
	
	/**
	 * Main page wich load all tweet
	 */
	public static Result loadtimeline()	{
		if(!session().containsKey("twitterId") || UserModel.findByTwitterId(session().get("twitterId")) == null)	{
			session().clear();
			response().setCookie("oauthState","0");
			return redirect("/");
		}
		
		UserModel user = UserModel.findByTwitterId(session().get("twitterId"));
		try {
			JsonNode json = TwitterRestApi.userInfo(user, user.getPseudo());	
			return ok(timeline.render(json.get("name").asText()));
		} catch (UserNotFoundTwitterRestApiException e) {
			play.Logger.error(e.toString());
			session().clear();
			return redirect("/");
		}
	}
	
	public static Result loadTweet()	{
		if(!session().containsKey("twitterId") || UserModel.findByTwitterId(session().get("twitterId")) == null)	{
			session().clear();
			response().setCookie("oauthState","0");
			return redirect("/");
		}
		TweetModel tweet;
		JsonNode json;
		
		ObjectNode ret = Json.newObject();
		ArrayNode array = ret.putArray("tweets");
		
		UserModel user = UserModel.findByTwitterId(session().get("twitterId"));
		
		try {
			if(dmax != null)
				json = TwitterRestApi.homeTimeLine(user, 200, since_id);
			else
				json = TwitterRestApi.homeTimeLine(user, 200);
		} catch (UserNotFoundTwitterRestApiException e) {
			session().clear();
			response().setCookie("oauthState","0");
			play.Logger.error(e.toString());
			return redirect("/");
		}
		
		Map<String, ObjectNode> map = new TreeMap<String, ObjectNode>(new Comparator<String>()
	    {
	        public int compare(String o1,String o2)
	        {
	           return o1.compareTo(o2);
	        }
	    });
		
		Iterator<JsonNode> it = json.getElements();
		while(it.hasNext())	{
			JsonNode node = it.next();
			tweet = new TweetModel(node);
			tweet.insert();
			
			UserModel tweetos = UserModel.findByTwitterId(tweet.user_id());
			if(tweet.user_id() != user.getTwitterId() && tweetos != null)	{
				user.addFollower(tweetos);
			}
			
		    try {
				Date result =  df.parse(tweet.getCreatedAt());
				if(dmax == null)	{
					dmax = result;
					since_id = tweet.twitter_id();
				}
				else if(dmax.before(result))	{
					dmax = result;
					since_id = tweet.twitter_id();
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} 
		    map.put(tweet.twitter_id(), tweet.simpleJson());
			//array.add(tweet.simpleJson());
		}
	
		for(String key : map.keySet())	{
			array.add(map.get(key));
		}

		return ok(ret);

	}

}