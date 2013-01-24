package leodagdag.play2morphia;

import com.google.code.morphia.AbstractEntityInterceptor;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.logging.MorphiaLoggerFactory;
import com.google.code.morphia.logging.slf4j.SLF4JLogrImplFactory;
import com.google.code.morphia.mapping.Mapper;
import com.google.code.morphia.validation.MorphiaValidation;
import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import leodagdag.play2morphia.utils.ConfigKey;
import leodagdag.play2morphia.utils.MorphiaLogger;
import org.apache.commons.lang.StringUtils;
import play.Application;
import play.Configuration;
import play.Plugin;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MorphiaPlugin extends Plugin {

    public static final String VERSION = "0.0.5";

    private static Mongo mongo = null;
    private static Morphia morphia = null;
    private static Datastore ds = null;
    private final Application application;
    private static GridFS gridfs;

    public MorphiaPlugin(Application application) {
        this.application = application;
    }

    @Override
    public void onStart() {
        // Register SLF4JLogrImplFactory as Logger
        // @see http://nesbot.com/2011/11/28/play-2-morphia-logging-error
        MorphiaLoggerFactory.reset();
        MorphiaLoggerFactory.registerLogger(SLF4JLogrImplFactory.class);

        try {
        	Configuration morphiaConf = null;
        	if(play.Play.isTest())	{
        		morphiaConf = Configuration.root().getConfig(ConfigKey.PREFIXTEST);
        	}else	{
        		morphiaConf = Configuration.root().getConfig(ConfigKey.PREFIX);
        	}
            if (morphiaConf == null) {
                throw Configuration.root().reportError(ConfigKey.PREFIX, "Missing Morphia configuration", null);
            }

            MorphiaLogger.debug(morphiaConf);

            String dbName = morphiaConf.getString(ConfigKey.DB_NAME.getKey());
            if (StringUtils.isBlank(dbName)) {
                throw morphiaConf.reportError(ConfigKey.DB_NAME.getKey(), "Missing Morphia configuration", null);
            }

            // Connect to MongoDB
            String seeds = morphiaConf.getString(ConfigKey.DB_SEEDS.getKey());

            if (StringUtils.isNotBlank(seeds)) {
                mongo = connect(seeds);
            } else {
                mongo = connect(
                        morphiaConf.getString(ConfigKey.DB_HOST.getKey()),
                        morphiaConf.getString(ConfigKey.DB_PORT.getKey()));
            }

            morphia = new Morphia();
            // Configure validator
            MorphiaValidation morphiaValidation = new MorphiaValidation();
            morphiaValidation.applyTo(morphia);

			//Check if credentials parameters are present
			String username = morphiaConf.getString(ConfigKey.DB_USERNAME.getKey());
			String password = morphiaConf.getString(ConfigKey.DB_PASSWORD.getKey());
						
			if(StringUtils.isNotBlank(username) ^ StringUtils.isNotBlank(password)){
				throw morphiaConf.reportError(ConfigKey.DB_NAME.getKey(), "Missing username or password", null);
			}
			
            // Create datastore
			if(StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)){
				ds = morphia.createDatastore(mongo, dbName, username, password.toCharArray());
			} else {
            	ds = morphia.createDatastore(mongo, dbName);				
			}


            MorphiaLogger.debug("Datastore [%s] created", dbName);
            // Create GridFS
            String uploadCollection = morphiaConf.getString(ConfigKey.COLLECTION_UPLOADS.getKey());
            if (StringUtils.isBlank(dbName)) {
                uploadCollection = "uploads";
                MorphiaLogger.warn("Missing Morphia configuration key [%s]. Use default value instead [%s]", ConfigKey.COLLECTION_UPLOADS, "uploads");
            }
            gridfs = new GridFS(ds.getDB(), uploadCollection);
            MorphiaLogger.debug("GridFS created", "");
            MorphiaLogger.debug("Add Interceptor...", "");
            morphia.getMapper().addInterceptor(new AbstractEntityInterceptor() {

                @Override
                public void postLoad(final Object ent, final DBObject dbObj, final Mapper mapr) {
                    if (ent instanceof Model) {
                        Model m = (Model) ent;
                        m._post_Load();
                    }
                }
            });
            MorphiaLogger.debug("Classes mapping...", "");
            mapClasses();
            MorphiaLogger.debug("End of initializing Morphia", "");
        } catch (MongoException e) {
            MorphiaLogger.error(e, "Problem connecting MongoDB");
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            MorphiaLogger.error(e, "Problem mapping class");
            throw new RuntimeException(e);
        }
    }

    private void mapClasses() throws ClassNotFoundException {
        // Register all models.Class
        Set<String> classes = new HashSet<String>();
        classes.addAll(application.getTypesAnnotatedWith("models", com.google.code.morphia.annotations.Entity.class));
        classes.addAll(application.getTypesAnnotatedWith("models", com.google.code.morphia.annotations.Embedded.class));
        for (String clazz : classes) {
            MorphiaLogger.debug("mapping class: %1$s", clazz);
            morphia.map(Class.forName(clazz, true, application.classloader()));
        }
    }

    private final static ConcurrentMap<String, Datastore> dataStores = new ConcurrentHashMap<String, Datastore>();

    public static Datastore ds(String dbName) {
        if (StringUtils.isBlank(dbName))
            return ds();
        Datastore ds = dataStores.get(dbName);
        if (null == ds) {
            Datastore ds0 = morphia.createDatastore(mongo, dbName);
            ds = dataStores.putIfAbsent(dbName, ds0);
            if (null == ds) {
                ds = ds0;
            }
        }
        return ds;
    }

    public static Datastore ds() {
        return ds;
    }

    public static GridFS gridFs() {
        return gridfs;
    }

    public static DB db() {
        return ds().getDB();
    }

    private Mongo connect(String seeds) {
        String[] sa = seeds.split("[;,\\s]+");
        List<ServerAddress> addrs = new ArrayList<ServerAddress>(sa.length);
        for (String s : sa) {
            String[] hp = s.split(":");
            if (0 == hp.length) {
                continue;
            }
            String host = hp[0];
            int port = 27017;
            if (hp.length > 1) {
                port = Integer.parseInt(hp[1]);
            }
            try {
                addrs.add(new ServerAddress(host, port));
            } catch (UnknownHostException e) {
                MorphiaLogger.error(e, "Error creating mongo connection to %s:%s", host, port);
            }
        }
        if (addrs.isEmpty()) {
            throw Configuration.root().reportError(ConfigKey.DB_SEEDS.getKey(), "Cannot connect to mongodb: no replica can be connected", null);
        }
        return new Mongo(addrs);
    }

    private Mongo connect(String host, String port) {
        String[] ha = host.split("[,\\s;]+");
        String[] pa = port.split("[,\\s;]+");
        int len = ha.length;
        if (len != pa.length) {
            throw Configuration.root().reportError(ConfigKey.DB_HOST.getKey() + "-" + ConfigKey.DB_PORT.getKey(), "host and ports number does not match", null);
        }
        if (1 == len) {
            try {
                return new Mongo(ha[0], Integer.parseInt(pa[0]));
            } catch (Exception e) {
                throw Configuration.root().reportError(
                        ConfigKey.DB_HOST.getKey() + "-"
                                + ConfigKey.DB_PORT.getKey(),
                        String.format("Cannot connect to mongodb at %s:%s",
                                host, port), e);
            }
        }
        List<ServerAddress> addrs = new ArrayList<ServerAddress>(ha.length);
        for (int i = 0; i < len; ++i) {
            try {
                addrs.add(new ServerAddress(ha[i], Integer.parseInt(pa[i])));
            } catch (Exception e) {
                MorphiaLogger.error(e, "Error creating mongo connection to %s:%s", host, port);
            }
        }
        if (addrs.isEmpty()) {
            throw Configuration.root().reportError(
                    ConfigKey.DB_HOST.getKey() + "-" + ConfigKey.DB_PORT.getKey(), "Cannot connect to mongodb: no replica can be connected",
                    null);
        }
        return new Mongo(addrs);
    }

}
