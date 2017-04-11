package com.rei.stats.gsampler

import java.nio.file.Path

import com.rei.stats.gsampler.util.FileWatcher

class FilesystemConfigurationProvider implements ConfigurationProvider {
    public static final DEFAULT_CONFIG_FILE = 'gsampler.groovy'

    private Path configFile
    private FileWatcher configWatcher

    private ConfigurationLoader configLoader = new ConfigurationLoader()

    FilesystemConfigurationProvider(Path configFile) {
        this.configFile = configFile
    }

    @Override
    Configuration loadConfiguration(GSamplerEngine engine) {
        if (configWatcher == null) {
            configWatcher = new FileWatcher(configFile)
            configWatcher.onModified { engine.reloadConfig(true) }.start()
            configWatcher.start()
        }
        return configLoader.loadConfiguration(configFile.toFile())
    }

    @Override
    void shutdown() {
        if (configWatcher != null) {
            configWatcher.shutdown()
        }
    }
}
