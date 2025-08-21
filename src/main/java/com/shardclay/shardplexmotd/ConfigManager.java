package com.shardclay.shardplexmotd;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class ConfigManager {

    private final Logger logger;
    private final Path dataDirectory;
    private Map<String, Object> config;

    @Inject
    public ConfigManager(Logger logger, @DataDirectory Path dataDirectory) {
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        loadConfig();
    }

    private void loadConfig() {
        try {
            File dataDir = dataDirectory.toFile();
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File configFile = new File(dataDir, "config.yml");
            if (!configFile.exists()) {
                logger.info("Config file not found, creating a default one.");
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    Files.copy(in, configFile.toPath());
                }
            }
            try (InputStream in = new FileInputStream(configFile)) {
                Yaml yaml = new Yaml();
                config = yaml.load(in);
            }
        } catch (IOException e) {
            logger.error("Could not load config.yml", e);
        }
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void reloadConfig() {
        loadConfig();
    }
}
