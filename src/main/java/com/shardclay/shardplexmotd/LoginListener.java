package com.shardclay.shardplexmotd;

import com.shardclay.shardplexmotd.utils.GradientUtil;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Map;

public class LoginListener {

    private final ConfigManager configManager;
    private final ShardplexMOTD plugin;

    public LoginListener(ShardplexMOTD plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        Map<String, Object> config = configManager.getConfig();

        if ((boolean) config.get("maintenance")) {
            if (!player.hasPermission("shardplexmotd.team")) {
                List<String> kickMessageList = (List<String>) ((Map<String, Object>) config.get("messages")).get("maintenance");
                Component kickMessage = Component.join(Component.text("\n"), GradientUtil.parse(kickMessageList));
                player.disconnect(kickMessage);
            }
        } else if (plugin.getServer().getPlayerCount() >= (int) config.get("maxplayer")) {
            if (!player.hasPermission("shardplexmotd.bypass")) {
                List<String> kickMessageList = (List<String>) ((Map<String, Object>) config.get("messages")).get("fullserver");
                Component kickMessage = Component.join(Component.text("\n"), GradientUtil.parse(kickMessageList));
                player.disconnect(kickMessage);
            }
        }
    }
}
