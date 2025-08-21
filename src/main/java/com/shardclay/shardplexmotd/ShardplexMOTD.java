package com.shardclay.shardplexmotd;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "shardplexmotd",
        name = "ShardplexMOTD",
        version = "1.1",
        description = "A Velocity MOTD plugin.",
        authors = {"Shardclay"}
)
public class ShardplexMOTD {

    private final ProxyServer server;
    private final Logger logger;

    private final ConfigManager configManager;

    @Inject
    public ShardplexMOTD(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.configManager = new ConfigManager(logger, dataDirectory);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
                server.getEventManager().register(this, new ServerListPingListener(configManager));
        
        server.getEventManager().register(this, new LoginListener(this, configManager));
        server.getCommandManager().register("motd", new MOTDCommand(configManager));

        logger.info("ShardplexMOTD has been enabled!");
    }

    public ProxyServer getServer() {
        return server;
    }
}
