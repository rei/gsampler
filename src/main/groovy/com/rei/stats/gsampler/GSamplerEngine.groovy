package com.rei.stats.gsampler

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.rei.stats.gsampler.console.ConsoleWriter

class GSamplerEngine {
    private static Logger logger = LoggerFactory.getLogger(GSamplerEngine)

    public final LocalDateTime started = LocalDateTime.now()
    private Path runsDir
    private ScheduledExecutorService executor

    Configuration config

    final SamplerStats selfStats = new SamplerStats()
    final ConcurrentMap<String, Object> errors = new ConcurrentHashMap<>()
    private GSamplerAdminServer adminServer
    private ConfigurationProvider configProvider

    GSamplerEngine(ConfigurationProvider configProvider, Path homeDir) {
        this.configProvider = configProvider
        this.runsDir = homeDir.resolve('runs') // configfile/../runs

    }
    
    void start() {
        reloadConfig(true)
        adminServer = new GSamplerAdminServer(this)
        adminServer.start()

        logger.info('sampler engine started!')
    }
    
    void test() {
        reloadConfig(false)
        config.writers = [new ConsoleWriter()]
        
        config.samplers.each { Sampler s ->
            println "testing sampler ${s.id}:"
            performSampling(s.id, s.namePrefix, s.reader)
        }

        if (selfStats.failedSamples.get() > 0) {
            throw new IllegalArgumentException("one or more samples failed! " + errors)
        }
    }
    
    void stop() {
        configProvider.shutdown()
        adminServer.stop()
    }

    void printSelfStats() {
        logger.info(selfStats.toString())
    }
    
    void reloadConfig(boolean schedule) {
        try {
            config = configProvider.loadConfiguration(this)
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
        if (!Files.exists(runsDir)) {
            Files.createDirectory(runsDir) //ensure data dir exists
        }
        new File(runsDir.toFile(), "${id}.lastRun").text = System.currentTimeMillis() as String
        selfStats.lastRan[id] = new Date()
    }
    
    long readLastRun(id) {
        def f = new File(runsDir.toFile(), "${id}.lastRun")
        return f.exists() ? f.text as long : 0
    }
}
