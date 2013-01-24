package lib.models;

import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;
import com.google.code.morphia.query.Query;

import leodagdag.play2morphia.Model;
import org.bson.types.ObjectId;

@Entity("socialTwitter")
public class TwitterModel extends Model {
	@Id
	private ObjectId id;
	@Reference
	private UserModel user;
	private String twitterRequest;
	private String token;
	private String tokenSecret;
	
	private static Model.Finder<ObjectId, TwitterModel> finder = new Model.Finder<ObjectId, TwitterModel>(
			ObjectId.class, TwitterModel.class);

	public static List<TwitterModel> all() {
		return finder.all();
	}
	
	public static TwitterModel findByTwitterUser(UserModel user){
		Query<TwitterModel> query = finder.getDatastore().createQuery(TwitterModel.class).field("user").equal(user);
		return query.get();
	}
	
	public TwitterModel()	{
	}
	
	public ObjectId id()	{
		return id;
	}

	public static TwitterModel byId(ObjectId id) {
		return finder.byId(id);
	}
	
	public UserModel getUser()	{
		return user;
	}
	public void setUser(UserModel user)	{
		this.user = user;
	}
	
	/**
	 * @return twitter ID
	 */
	public String getTwitterId() {
		return user.getTwitterId();
	}

	/**
	 * @return twitter token
	 */
	public String getToken(){
		return token;
	}
	/**
	 * @param twitter token
	 */
	public void setToken(String token){
		this.token = token;
	}
	/**
	 * @return twitter token secret
	 */
	public String getTokenSecret(){
		return tokenSecret;
	}
	/**
	 * @param set the twitter token secret
	 */
	public void setTokenSecret(String token){
		this.tokenSecret = token;
	}
	/**
	 * @return twitter request send to other users
	 */
	public String getRequest(){
		return twitterRequest;
	}
	/**
	 * @param add user ID who has send a request
	 */
	public void addRequest(String id){
		this.twitterRequest = id;
	}
}