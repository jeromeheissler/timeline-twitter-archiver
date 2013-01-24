package controllers;

import lib.models.TwitterModel;
import lib.models.UserModel;
import play.Play;
import play.libs.Json;
import play.mvc.*;

import org.codehaus.jackson.JsonNode;
import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;

public class TwitterController extends Controller {
	
	private static String apiKey = Play.application().configuration().getString("twitter.apiKey");
	private static String apiSecret = Play.application().configuration().getString("twitter.apiSecret");
	
	public static String login(){
		try {
			OAuthService service = new ServiceBuilder()
				.provider(TwitterApi.class)
				.callback("http://localhost:9000/twitter/callback")
				.apiKey(apiKey)
				.apiSecret(apiSecret)
				.build();
			  
			Token requestToken = service.getRequestToken();
			String url = service.getAuthorizationUrl(requestToken);
		  
			return url;
		} catch (Exception e) {
			play.Logger.error(e.getMessage());
			return "";
		}
	}
	
	public static Result callback(){
		OAuthService service = new ServiceBuilder()
			.provider(TwitterApi.class)
			.callback("http://localhost:9000/twitter/callback")
			.apiKey(apiKey)
			.apiSecret(apiSecret)
			.build();
		  
		Verifier v = new Verifier(request().queryString().get("oauth_verifier")[0]);
		Token requestToken = new Token(request().queryString().get("oauth_token")[0],request().queryString().get("oauth_verifier")[0]);
		  
		Token accessToken = service.getAccessToken(requestToken, v);
		String token = accessToken.getToken();
		String tokenSecret = accessToken.getSecret();
		//session().put("twitter_token",token);
		//session().put("twitter_token_secret", tokenSecret);
		  
		OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.twitter.com/1/account/verify_credentials.json");
		service.signRequest(accessToken, request);
		Response response = request.send();
		JsonNode result = Json.parse(response.getBody());
		
		UserModel user = new UserModel(result.get("id").asText());
		user.insert();
		
		TwitterModel twitter = new TwitterModel();
		twitter.setUser(user);
		twitter.setToken(token);
		twitter.setTokenSecret(tokenSecret);
		twitter.insert();
			  
		response().setCookie("oauthState","1");
		session().put("twitterId", result.get("id").asText());
		
		return redirect("/loadtimeline");
	}
}