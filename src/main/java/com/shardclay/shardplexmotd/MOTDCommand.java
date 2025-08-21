package com.shardclay.shardplexmotd;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public class MOTDCommand implements SimpleCommand {

    private final ConfigManager configManager;

    public MOTDCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            source.sendMessage(Component.text("Usage: /motd <reload|maintenance|max|fakeplayer>").color(NamedTextColor.RED));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                configManager.reloadConfig();
                source.sendMessage(Component.text("Configuration reloaded.").color(NamedTextColor.GREEN));
                break;
            case "maintenance":
                if (args.length > 1) {
                    boolean maintenance = Boolean.parseBoolean(args[1]);
                    configManager.getConfig().put("maintenance", maintenance);
                    source.sendMessage(Component.text("Maintenance mode set to " + maintenance).color(NamedTextColor.GREEN));
                } else {
                    source.sendMessage(Component.text("Usage: /motd maintenance <true|false>").color(NamedTextColor.RED));
                }
                break;
            case "max":
                if (args.length > 1) {
                    try {
                        int maxPlayers = Integer.parseInt(args[1]);
                        configManager.getConfig().put("maxplayer", maxPlayers);
                        source.sendMessage(Component.text("Max players set to " + maxPlayers).color(NamedTextColor.GREEN));
                    } catch (NumberFormatException e) {
                        source.sendMessage(Component.text("Invalid number.").color(NamedTextColor.RED));
                    }
                } else {
                    source.sendMessage(Component.text("Usage: /motd max <number>").color(NamedTextColor.RED));
                }
                break;
            case "fakeplayer":
                if (args.length > 1) {
                    boolean fakePlayer = Boolean.parseBoolean(args[1]);
                    configManager.getConfig().put("fakeplayer", fakePlayer);
                    source.sendMessage(Component.text("Fake player count set to " + fakePlayer).color(NamedTextColor.GREEN));
                } else {
                    source.sendMessage(Component.text("Usage: /motd fakeplayer <true|false>").color(NamedTextColor.RED));
                }
                break;
            default:
                source.sendMessage(Component.text("Unknown subcommand.").color(NamedTextColor.RED));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 1) {
            return java.util.Arrays.asList("reload", "maintenance", "max", "fakeplayer");
        } else if (invocation.arguments().length == 2) {
            if (invocation.arguments()[0].equalsIgnoreCase("maintenance") || invocation.arguments()[0].equalsIgnoreCase("fakeplayer")) {
                return java.util.Arrays.asList("true", "false");
            }
        }
        return java.util.Collections.emptyList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("shardplexmotd.admin");
    }
}
