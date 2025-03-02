package webapp.runner.launch;

import org.apache.naming.ResourceRef;
import org.redisson.Redisson;
import org.redisson.codec.FstCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;

public class JndiRedissonFactory implements ObjectFactory {

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {

        System.out.println("------------------------->JndiRedissonFactory.getObjectInstance");
        System.out.println("obj = " + obj);

        try {
            return buildClient(obj);
        } catch (Exception e) {
            NamingException ex = new NamingException();
            ex.initCause(e);
            throw ex;
        }
    }

    private Object buildClient(Object obj) {

        ResourceRef resourceRef = (ResourceRef) obj;

        String redisUriString;
        if (System.getenv("REDIS_URL") == null && System.getenv("REDISTOGO_URL") == null && System.getenv("REDISCLOUD_URL") == null) {
            System.out.println("WARNING: using redis session store, but the required environment variable isn't set.");
            System.out.println("Redis session store is configured with REDIS_URL, REDISTOGO_URL or REDISCLOUD_URL");
        } else {
            if (System.getenv("REDIS_URL") != null) {
                redisUriString = System.getenv("REDIS_URL");
            } else if (System.getenv("REDISTOGO_URL") != null) {
                redisUriString = System.getenv("REDISTOGO_URL");
            } else {
                redisUriString = System.getenv("REDISCLOUD_URL");
            }

            URI redisUri = URI.create(redisUriString);
            URI redisUriWithoutAuth;
            try {
                // https://github.com/redisson/redisson/issues/2370
                redisUriWithoutAuth = new URI(redisUri.getScheme(), null, redisUri.getHost(), redisUri.getPort(), redisUri.getPath(), redisUri.getQuery(), redisUri.getFragment());
            } catch (URISyntaxException e) {
                System.out.printf("WARNING: could not write redis configuration for %s\n", redisUri);
                return null;
            }

            Config config = new Config();
            SingleServerConfig serverConfig = config.useSingleServer()
                    .setAddress(redisUriWithoutAuth.toString())
                    .setConnectionPoolSize(Integer.valueOf((String) resourceRef.get("sessionStorePoolSize").getContent()))
                    .setConnectionMinimumIdleSize(Integer.valueOf((String) resourceRef.get("sessionStorePoolSize").getContent()))
                    .setTimeout(Integer.valueOf((String) resourceRef.get("sessionStoreOperationTimout").getContent()));

            config.setCodec(new FstCodec());

            if (redisUri.getUserInfo() != null) {
                serverConfig.setPassword(redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(":") + 1));
            }

            return Redisson.create(config);
        }
        return null;
    }

}
