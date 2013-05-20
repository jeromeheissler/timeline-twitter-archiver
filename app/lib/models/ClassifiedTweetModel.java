package lib.models;

import java.util.Map;

import leodagdag.play2morphia.Model;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Reference;

@Entity("classifiedTweet")
public class ClassifiedTweetModel extends Model {

	public ObjectId id;
	public String text;
			
	@Reference
	public TweetModel tweet;

	@Reference
	public Map<ClassifiedTweetModel, Integer> lstTweetClass;
	
	public static Finder<ObjectId, ClassifiedTweetModel> finder = new Finder<ObjectId, ClassifiedTweetModel>(ObjectId.class, ClassifiedTweetModel.class);
	
}
