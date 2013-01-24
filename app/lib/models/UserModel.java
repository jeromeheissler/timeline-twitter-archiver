package lib.models;

import java.util.ArrayList;
import java.util.List;

import leodagdag.play2morphia.Model;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;
import com.google.code.morphia.query.Query;

@Entity("user")
public class UserModel extends Model {

	@Id
	private ObjectId id;
	private String pseudo;
	private String twitterId;
	@Reference
	private List<UserModel> following;
	@Reference
	private List<UserModel> follower;
	
	protected UserModel(){}
	
	public UserModel(String twitterId)	{
		this.twitterId = twitterId;
		follower = new ArrayList<UserModel>();
		following = new ArrayList<UserModel>();
	}
	
	public static Model.Finder<ObjectId, UserModel> finder = new Model.Finder<ObjectId, UserModel>(
			ObjectId.class, UserModel.class);
	public static UserModel findByTwitterId(String id)	{
		Query<UserModel> query = finder.getDatastore().createQuery(UserModel.class).field("twitterId").equal(id);
		return query.get();
	}
	public String getPseudo() {
		return pseudo;
	}
	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}
	public String getTwitterId() {
		return twitterId;
	}
	
	public List<UserModel> follower()	{
		return follower;
	}
	public List<UserModel> following()	{
		return following;
	}
	
	public void addFollower(UserModel e)	{
		follower.add(e);
	}
	
	public void addFollow(UserModel e)	{
		following.add(e);
	}

}
