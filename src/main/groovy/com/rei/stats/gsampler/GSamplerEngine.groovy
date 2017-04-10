package com.rei.stats.gsampler

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

import groovy.json.JsonBuilder

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.rei.stats.gsampler.console.ConsoleWriter
import com.rei.stats.gsampler.util.FileWatcher
import com.rei.stats.gsampler.util.SimpleHttpServer

class GSamplerEngine {
    private static Logger logger = LoggerFactory.getLogger(GSamplerEngine)

    private static final LocalDateTime STARTED = LocalDateTime.now()

    private Path configFile;
    private Path dataDir;
    private ScheduledExecutorService executor
    private ConfigurationLoader configLoader = new ConfigurationLoader()
    Configuration config
    private SimpleHttpServer server = new SimpleHttpServer(2245)
    private FileWatcher configWatcher
    private final SamplerStats selfStats = new SamplerStats()
    final ConcurrentMap<String, Object> errors = new ConcurrentHashMap<>()

    GSamplerEngine(Path configFile) {
        this.configFile = configFile;
        this.dataDir = configFile.parent.parent.resolve('data') // configfile/../data
        configWatcher = new FileWatcher(configFile)
    }
    
    void start() {
        reloadConfig(true)
        registerHttpHandlers()
        configWatcher.onModified { reloadConfig(true) }.start()        
        server.start()
        logger.info('sampler engine started!')
    }
    
    void test() {
        reloadConfig(false)
        config.writers = [new ConsoleWriter()]
        
        config.samplers.each { Sampler s ->
            println "testing sampler ${s.id}:"
            performSampling(s.id, s.namePrefix, s.reader)
        }
    }
    
    void stop() {
        configWatcher.shutdown()
        server.stop()
    }
    
    private void registerHttpHandlers() {
        server.register ('GET', '/') { jsonResponse(getRootData()) }
        server.register ('GET', '/self-stats') { jsonResponse(selfStats) }
        server.register ('GET', '/config') { jsonResponse(getConfigMetadata()) }
        server.register ('GET', '/errors') { jsonResponse(errors) }
        
        server.register ('POST', '/reload-config') {
            try {
                reloadConfig(true)
                return jsonResponse([success: true])
            } catch (Exception e) {
                return jsonResponse([success: false])
            }
        }
        logger.info("registered http handlers")
    }
    
    private def jsonResponse(object) {
        try {
            return [contentType: 'application/json', body: new JsonBuilder(object).toString()]
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    private def getConfigMetadata() {
        return [samplers: config.samplers.collect { [id: it.id, namePrefix: it.namePrefix, readerClass: it.reader.class.name, interval: it.interval, unit: it.unit] },
                writers: config.writers.collect { className: it.class.name }]
    }

    private def getRootData() {
        return [status: "up", started: STARTED.format(DateTimeFormatter.ISO_DATE_TIME),
                endpoints:['/self-stats', '/config', '/errors']]
    }

    public void printSelfStats() {
        logger.info(selfStats.toString())
    }
    
    void reloadConfig(boolean schedule) {
        try {
            config = configLoader.loadConfiguration(configFile.toFile())
        } catch (Exception e) {
            logger.error("failed to parse configuration file:", e)
            errors['config'] = [msg: e.message]
            if (config != null) {
                return // only try to continue using the existing config if we have one
            } else {
                throw e
            }
        }
        
        if (executor != null) {
            logger.warn("shutting down existing executor")
            executor.shutdown()
            executor.awaitTermination(5, TimeUnit.SECONDS)
            executor.shutdownNow()
            logger.info("existing executor shut down")
        }
        
        selfStats.lastConfigReload.set(System.currentTimeMillis())
        
        def poolSize = Math.min(config.samplers.size(), 50)
        
        selfStats.poolSize.set(poolSize)
        
        logger.info("created pool with size $poolSize")
        executor = Executors.newScheduledThreadPool(poolSize)
        
        if (schedule) {
            config.samplers.each { Sampler s ->
                def delay = getInitialDelay(s)
                // we may need to drop to more precise unit if a delay is needed because the delay may be smaller than one unit
                def interval = s.unit.toMillis(s.interval)
                
                logger.info("registering sampler ${s.id} with interval ${interval}ms and delay ${delay}ms")
                executor.scheduleAtFixedRate({ performSampling(s.id, s.namePrefix, s.reader) } as Runnable, delay, interval, TimeUnit.MILLISECONDS);
            }
            
        }
    }
    
    private void performSampling(id, String namePrefix, StatsReader reader) {
        try {
            logger.info("performing sample for sampler: $id")
            def sampled = reader.read().collectEntries { ["${config.globalPrefix}.${namePrefix}.${it.key}", it.value] }
            selfStats.lastSampled.putAll(sampled)
            
            config.writers.each { StatsWriter w ->
                w.writeStats(sampled)
            }
            
            selfStats.totalStats.addAndGet(sampled.size())
            selfStats.samplesTaken.incrementAndGet()
        } catch (Exception e) {
            logger.warn("failure taking sampling for $id", e)
            selfStats.failedSamples.incrementAndGet()
            errors[id] = [id: id, type: e.getClass().name, msg: e.message]
        } finally {
            recordLastRun(id)
        }
    }
    
    long getInitialDelay(Sampler s) {
        def lastRun = readLastRun(s.id)
        def minNextRun = lastRun + s.unit.toMillis(s.interval)
        def now = System.currentTimeMillis() 
        return minNextRun < now ? 0 : minNextRun - now  
    }
    
    void recordLastRun(id) {
        if (!Files.exists(dataDir)) {
            Files.createDirectory(dataDir) //ensure data dir exists
        }
        new File(dataDir.toFile(), "${id}.lastRun").text = System.currentTimeMillis() as String
        selfStats.lastRan[id] = new Date()
    }
    
    long readLastRun(id) {
        def f = new File(dataDir.toFile(), "${id}.lastRun")
        return f.exists() ? f.text as long : 0
    }
}
