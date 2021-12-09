package webapp.runner.launch;

import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.redisson.tomcat.JndiRedissonSessionManager;

public class RedisSessionStore extends SessionStore {

    /**
     * Configures Redis session manager
     *
     * @param commandLineParams Arguments map
     * @param ctx               Tomcat context
     */
    @Override
    public void configureSessionStore(CommandLineParams commandLineParams, Context ctx) {
        System.out.println("★★★★★★★★Using redis session store: org.redisson.tomcat.RedissonSessionManager");

        register(commandLineParams, ctx);

        JndiRedissonSessionManager jndiRedissonSessionManager = new JndiRedissonSessionManager();
        jndiRedissonSessionManager.setJndiName("bean/redisson");

        ctx.setManager(jndiRedissonSessionManager);
    }

    private void register(CommandLineParams commandLineParams, Context ctx) {
        ContextResource resource = new ContextResource();
        resource.setName("bean/redisson");
        resource.setProperty("factory", "webapp.runner.launch.JndiRedissonFactory");
        resource.setProperty("sessionStorePoolSize", commandLineParams.sessionStorePoolSize.toString());
        resource.setProperty("sessionStorePoolSize", commandLineParams.sessionStorePoolSize.toString());
        resource.setProperty("sessionStoreOperationTimout", commandLineParams.sessionStoreOperationTimout.toString());
        resource.setAuth("Container");
        resource.setCloseMethod("shutdown");
        ctx.getNamingResources().addResource(resource);
    }
}
