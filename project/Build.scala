import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "timeline-twitter-archiver"
    val appVersion      = "1.0.0"

    val appDependencies = Seq(
      "com.google.code.morphia"    % "morphia"               % "1.00-SNAPSHOT",
      "com.google.code.morphia"    % "morphia-logging-slf4j" % "0.99",
      "com.google.code.morphia"    % "morphia-validation"    % "0.99",
      "commons-lang" 				% "commons-lang" 		 % "2.3",
      
      "org.mongodb" % "mongo-java-driver" % "2.8.3",
      "de.flapdoodle.embedmongo" % "de.flapdoodle.embedmongo" % "1.11",
      "commons-io" % "commons-io" % "2.4",
      "org.scribe" % "scribe" % "1.1.2"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here     
      resolvers ++= Seq(
    	    DefaultMavenRepository, 
    	    Resolvers.morphiaRepository, 
    	    Resolvers.mongoDBRepository) 
    )
    
    object Resolvers {
      val morphiaRepository = "Morphia repository" at "http://morphia.googlecode.com/svn/mavenrepo/"
      val mongoDBRepository = "MongoDb Java Driver Repository" at "http://repo1.maven.org/maven2/org/mongodb/mongo-java-driver/"
	}

}
