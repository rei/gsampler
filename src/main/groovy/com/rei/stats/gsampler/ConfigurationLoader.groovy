package com.rei.stats.gsampler

import java.util.concurrent.TimeUnit

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ConfigurationLoader {
    private static Logger logger = LoggerFactory.getLogger(ConfigurationLoader)

    Configuration loadConfiguration(File file) {
        logger.info("loading configuration from ${file}")

        def extensions = loadExtensions(getClass().classLoader)
        
        Configuration config = new Configuration()
        
        def bindings = extensions.collectEntries { [it.key, { Closure clos -> clos.delegate = it.value; clos()}] }
        
        bindings.sampler = { String id, StatsReader reader, String namePrefix, long interval, TimeUnit unit = TimeUnit.SECONDS ->
            config.samplers.add(new Sampler(id: id, reader: reader, namePrefix: namePrefix, interval: interval, unit: unit))
        }
        
        bindings.writer = { StatsWriter writer -> config.writers.add(writer) }
        bindings.globalPrefix = { prefix -> config.globalPrefix = prefix }
        
        try {
            def importCustomizer = new ImportCustomizer().addStaticStars(TimeUnit.class.name)
            def compilerConfig = new CompilerConfiguration().addCompilationCustomizers(importCustomizer)
            
            new GroovyShell(new Binding(bindings), compilerConfig).run(file, [])
            return config
        } catch (Exception e) {
            throw new InvalidConfigurationException(e)
        }
    }
    
    private Map<String, Extension> loadExtensions(cl) {
         return ServiceLoader.load(Extension, cl).collectEntries { [it.name, it] }
    }
    
    static class InvalidConfigurationException extends RuntimeException {
        InvalidConfigurationException(Throwable cause) {
            super(cause)
        }
    }
}
