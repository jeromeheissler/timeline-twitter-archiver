package controllers;

import lib.models.ClassifiedTweetModel;
import play.mvc.*;

import views.html.*;

public class ResultController  extends Controller  {

	public static Result visu() {
		return ok(visu.render(ClassifiedTweetModel.finder.all()));
	}
	
}
