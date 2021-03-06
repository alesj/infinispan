package org.infinispan.rest;

import java.io.IOException;
import java.nio.file.Path;

import org.infinispan.counter.EmbeddedCounterManagerFactory;
import org.infinispan.counter.impl.manager.EmbeddedCounterManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.rest.cachemanager.RestCacheManager;
import org.infinispan.rest.configuration.AuthenticationConfiguration;
import org.infinispan.rest.configuration.RestServerConfiguration;
import org.infinispan.rest.framework.ResourceManager;
import org.infinispan.rest.framework.RestDispatcher;
import org.infinispan.rest.framework.impl.ResourceManagerImpl;
import org.infinispan.rest.framework.impl.RestDispatcherImpl;
import org.infinispan.rest.resources.CacheManagerResource;
import org.infinispan.rest.resources.CacheResourceV2;
import org.infinispan.rest.resources.ClusterResource;
import org.infinispan.rest.resources.CounterResource;
import org.infinispan.rest.resources.LoginResource;
import org.infinispan.rest.resources.MetricsResource;
import org.infinispan.rest.resources.RedirectResource;
import org.infinispan.rest.resources.SearchAdminResource;
import org.infinispan.rest.resources.ServerResource;
import org.infinispan.rest.resources.StaticContentResource;
import org.infinispan.rest.resources.TasksResource;
import org.infinispan.rest.resources.XSiteResource;
import org.infinispan.server.core.AbstractProtocolServer;
import org.infinispan.server.core.ServerManagement;
import org.infinispan.server.core.transport.NettyInitializers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;

/**
 * REST Protocol Server.
 *
 * @author Sebastian Łaskawiec
 */
public class RestServer extends AbstractProtocolServer<RestServerConfiguration> {
   private ServerManagement server;
   private RestDispatcher restDispatcher;
   private RestCacheManager<Object> restCacheManager;
   private InvocationHelper invocationHelper;

   public RestServer() {
      super("REST");
   }

   @Override
   public ChannelOutboundHandler getEncoder() {
      return null;
   }

   @Override
   public ChannelInboundHandler getDecoder() {
      return null;
   }

   @Override
   public ChannelInitializer<Channel> getInitializer() {
      return new NettyInitializers(getRestChannelInitializer());
   }

   /**
    * Returns Netty Channel Initializer for REST.
    *
    * @return Netty Channel Initializer for REST.
    */
   public RestChannelInitializer getRestChannelInitializer() {
      return new RestChannelInitializer(this, transport);
   }

   RestDispatcher getRestDispatcher() {
      return restDispatcher;
   }

   @Override
   public void stop() {
      if (restCacheManager != null) {
         restCacheManager.stop();
      }
      AuthenticationConfiguration auth = configuration.authentication();
      if (auth.enabled()) {
         try {
            auth.authenticator().close();
         } catch (IOException e) {
         }
      }
      super.stop();
   }

   public void setServer(ServerManagement server) {
      this.server = server;
   }

   @Override
   protected void startInternal(RestServerConfiguration configuration, EmbeddedCacheManager cacheManager) {
      this.configuration = configuration;
      AuthenticationConfiguration auth = configuration.authentication();
      if (auth.enabled()) {
         auth.authenticator().init(this);
      }
      super.startInternal(configuration, cacheManager);
      restCacheManager = new RestCacheManager<>(cacheManager, this::isCacheIgnored);

      invocationHelper = new InvocationHelper(restCacheManager,
            (EmbeddedCounterManager) EmbeddedCounterManagerFactory.asCounterManager(cacheManager),
            configuration, server, getExecutor());

      String restContext = configuration.contextPath();
      String rootContext = "/";
      ResourceManager resourceManager = new ResourceManagerImpl();
      resourceManager.registerResource(restContext, new CacheResourceV2(invocationHelper));
      resourceManager.registerResource(restContext, new CounterResource(invocationHelper));
      resourceManager.registerResource(restContext, new CacheManagerResource(invocationHelper));
      resourceManager.registerResource(restContext, new XSiteResource(invocationHelper));
      resourceManager.registerResource(restContext, new SearchAdminResource(invocationHelper));
      resourceManager.registerResource(restContext, new TasksResource(invocationHelper));
      resourceManager.registerResource(rootContext, new MetricsResource());
      Path staticResources = configuration.staticResources();
      if (staticResources != null) {
         Path console = configuration.staticResources().resolve("console");
         resourceManager.registerResource(rootContext, new StaticContentResource(staticResources, "static"));
         resourceManager.registerResource(rootContext, new StaticContentResource(console, "console"));
         resourceManager.registerResource(rootContext, new StaticContentResource(console, "console/welcome"));
         resourceManager.registerResource(rootContext, new RedirectResource(rootContext, rootContext + "console/welcome", true));
      }
      if (server != null) {
         resourceManager.registerResource(restContext, new ServerResource(invocationHelper));
         resourceManager.registerResource(restContext, new ClusterResource(invocationHelper));
         resourceManager.registerResource(restContext, new LoginResource(invocationHelper, rootContext + "console/", rootContext + "console/forbidden.html"));
      }
      this.restDispatcher = new RestDispatcherImpl(resourceManager);
   }
}
