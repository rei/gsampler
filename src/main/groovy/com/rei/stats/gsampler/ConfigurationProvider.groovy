package com.rei.stats.gsampler

interface ConfigurationProvider {
    Configuration loadConfiguration(GSamplerEngine engine)
    void shutdown()
}