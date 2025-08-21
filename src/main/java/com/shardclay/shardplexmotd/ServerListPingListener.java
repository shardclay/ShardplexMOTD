package com.shardclay.shardplexmotd;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.shardclay.shardplexmotd.utils.GradientUtil;
import com.velocitypowered.api.util.Favicon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ServerListPingListener {

    private final ConfigManager configManager;

    public ServerListPingListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Subscribe
    public void onServerListPing(ProxyPingEvent event) {
        final InboundConnection connection = event.getConnection();
        ServerPing.Builder ping = event.getPing().asBuilder();
        Map<String, Object> config = configManager.getConfig();

        boolean maintenance = (boolean) config.get("maintenance");

        Map<String, Object> motdConfig = (Map<String, Object>) config.get("motd");
        if (maintenance) {
            String top = (String) ((Map<String, Object>) motdConfig.get("maintenance")).get("top");
            String bottom = (String) ((Map<String, Object>) motdConfig.get("maintenance")).get("bottom");
            String playerName = connection.getRemoteAddress().toString();
            ping.description(GradientUtil.parse(top.replace("%player%", playerName) + "\n" + bottom.replace("%player%", playerName)));
        } else {
            List<String> topList = (List<String>) ((Map<String, Object>) motdConfig.get("normal")).get("top");
            List<String> bottomList = (List<String>) ((Map<String, Object>) motdConfig.get("normal")).get("bottom");
            String top = topList.get(ThreadLocalRandom.current().nextInt(topList.size()));
            String bottom = bottomList.get(ThreadLocalRandom.current().nextInt(bottomList.size()));
            String playerName = connection.getRemoteAddress().toString();
            ping.description(GradientUtil.parse(top.replace("%player%", playerName) + "\n" + bottom.replace("%player%", playerName)));
        }

        int maxPlayers = (int) config.get("maxplayer");
        ping.maximumPlayers(maxPlayers);

        if ((boolean) config.get("fakeplayer")) {
            ping.onlinePlayers(ThreadLocalRandom.current().nextInt(0, maxPlayers + 1));
        } else {
            ping.onlinePlayers(event.getPing().getPlayers().get().getOnline());
        }

        Map<String, Object> versionConfig = (Map<String, Object>) config.get("versionname");
        List<String> versionLeftList = (List<String>) versionConfig.get("left");
        String versionLeft = versionLeftList.get(ThreadLocalRandom.current().nextInt(versionLeftList.size()));

        String versionRight;
        if (maintenance) {
            versionRight = (String) ((Map<String, Object>) versionConfig.get("right")).get("maintenance");
        } else {
            versionRight = (String) ((Map<String, Object>) versionConfig.get("right")).get("normal");
            versionRight = versionRight.replace("%players%", String.valueOf(event.getPing().getPlayers().get().getOnline()));
            versionRight = versionRight.replace("%maxplayers%", String.valueOf(maxPlayers));
            versionRight = versionRight.replace("%teamplayers%", "0"); // TODO: Implement team player count
        }

        ping.version(new ServerPing.Version(-1, LegacyComponentSerializer.legacySection().serialize(GradientUtil.parseLegacy(versionLeft).append(Component.text("                                                                                                    ")).append(GradientUtil.parseLegacy(versionRight)))));

        List<String> hoverLines = (List<String>) versionConfig.get("hover");
        ping.samplePlayers(hoverLines.stream().map(line -> new ServerPing.SamplePlayer(LegacyComponentSerializer.legacySection().serialize(GradientUtil.parseLegacy(line.replace("%player%", connection.getRemoteAddress().toString()))), UUID.randomUUID())).toArray(ServerPing.SamplePlayer[]::new));

        try {
            File iconFile = new File(configManager.getDataDirectory().toFile(), maintenance ? (String) ((Map<String, Object>) config.get("favicon")).get("maintenance") : (String) ((Map<String, Object>) config.get("favicon")).get("normal"));
            if (iconFile.exists()) {
                ping.favicon(Favicon.create(iconFile.toPath()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        event.setPing(ping.build());
    }
}
