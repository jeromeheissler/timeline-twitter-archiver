package lib;

import java.util.Map;
import java.util.Set;

import lib.models.TwitterModel;
import lib.models.UserModel;

import org.codehaus.jackson.JsonNode;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import play.Play;
import play.libs.Json;

public abstract class TwitterRestApi {

	private static String apiKey = Play.application().configuration().getString("twitter.apiKey");
	private static String apiSecret = Play.application().configuration().getString("twitter.apiSecret");
	
	private static String restBase = "https://api.twitter.com/1.1/";
	private static String baseSearch = "search/tweets.json?";
	private static String userTimeLine = "statuses/user_timeline.json?include_rts=true";
	private static String homeTimeLine = "https://api.twitter.com/1.1/statuses/home_timeline.json";
	private static String share = "https://api.twitter.com/1/statuses/update.json";
	private static String userInfo = "http://api.twitter.com/1.1/users/show.json";
	
	public static JsonNode search(UserModel user, String query) throws UserNotFoundTwitterRestApiException	{
		Response ret = sendRequest(user, restBase+baseSearch+"q="+query, null);
		return Json.parse(ret.getBody());
	}
	
	public static JsonNode userTimeLine(UserModel user) throws UserNotFoundTwitterRestApiException	{
		Response ret = sendRequest(user, restBase+userTimeLine+"&screen_name="+user, null);
		return Json.parse(ret.getBody());
	}
	public static JsonNode userTimeLine(UserModel user, int count) throws UserNotFoundTwitterRestApiException	{
		Response ret = sendRequest(user, restBase+userTimeLine+"&screen_name="+user+"&count="+count, null);
		return Json.parse(ret.getBody());
	}
	public static JsonNode userTimeLine(UserModel user, int count, String since_id) throws UserNotFoundTwitterRestApiException	{
		Response ret = sendRequest(user, restBase+userTimeLine+"&screen_name="+user+"&count="+count+"&since_id="+since_id, null);
		return Json.parse(ret.getBody());
	}
	
	public static JsonNode homeTimeLine(UserModel user, int count) throws UserNotFoundTwitterRestApiException	{
		Response ret = sendRequest(user, homeTimeLine+"?count="+count, null);
		return Json.parse(ret.getBody());

	}
	
	public static JsonNode homeTimeLine(UserModel user, int count, String since_id) throws UserNotFoundTwitterRestApiException	{
		Response ret = sendRequest(user, homeTimeLine+"?count="+count+"&since_id="+since_id, null);
		return Json.parse(ret.getBody());
	}
	
	public static JsonNode userInfo(UserModel user, String twittername) throws UserNotFoundTwitterRestApiException	{
		Response ret = sendRequest(user, userInfo+"?screen_name="+twittername, null);
		return Json.parse(ret.getBody());
	}
	
	private static Response sendRequest(UserModel user, String uri, Map<String, String> param) throws UserNotFoundTwitterRestApiException	{
		if(user != null){
			TwitterModel twitter = TwitterModel.findByTwitterUser(user);
			if(twitter != null){
				OAuthService service = new ServiceBuilder()
				.provider(TwitterApi.class)
				.apiKey(apiKey)
				.apiSecret(apiSecret)
				.build();
			  
			  Token accessToken = new Token(twitter.getToken(), twitter.getTokenSecret());
			  OAuthRequest request;
			  if(param != null && param.size() > 0)	{
				  request = new OAuthRequest(Verb.POST, uri);
				  Set<String> keyLst = param.keySet();
				  for(String key : keyLst)	{
					  request.addBodyParameter(key, param.get(key));
				  }
			  }
			  else
				  request = new OAuthRequest(Verb.GET, uri);
			  
			  service.signRequest(accessToken, request);
			  Response ret = request.send();
			  //play.Logger.debug(request.getBodyContents().toString());
			  //play.Logger.debug(request.getHeaders().toString());
			  //play.Logger.debug(request.getUrl().toString());
			  return ret;
			}else
				throw new UserNotFoundTwitterRestApiException();
		}else
			throw new UserNotFoundTwitterRestApiException();
	}
	
}
