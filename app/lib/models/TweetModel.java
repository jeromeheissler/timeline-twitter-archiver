package lib.models;

import leodagdag.play2morphia.Model;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;

@Entity("tweet")
public class TweetModel extends Model {

	@Id
	private ObjectId id;
	private String originalJson;
	private String text;
	private String created_at;
	private String id_str;
	@Reference
	private UserModel user;
	
	public TweetModel(JsonNode json)	{
		originalJson = json.toString();

		if(json.has("text"))
			text = json.get("text").asText();
		if(json.has("created_at"))
			created_at = json.get("created_at").asText();
		if(json.has("id_str"))
			id_str = json.get("id_str").asText();
		if(json.has("user"))	{
			if( json.get("user").has("id_str") )	{
				UserModel user = UserModel.findByTwitterId(json.get("user").get("id_str").asText());
				if(user == null)	{
					user = new UserModel(json.get("user").get("id_str").asText());
					user.setPseudo(json.get("user").get("screen_name").asText());
					user.insert();
					this.user = user;
				}else
					this.user = user;
			}
		}
	}
	
	public ObjectId id()	{
		return id;
	}
	
	public String toString()	{
		return originalJson;
	}
	
	public ObjectNode simpleJson()	{
		ObjectNode ret = Json.newObject();
		ret.put("id_str", id_str);
		ret.put("author", user.getPseudo());
		ret.put("message", text);
		ret.put("date", created_at);
		return ret;
	}

	public String getText() {
		return text;
	}
	
	public String getCreatedAt()	{
		return created_at;
	}
	
	public String twitter_id()	{
		return id_str;
	}
	
	public String user_id()	{
		return user.getTwitterId();
	}
}
