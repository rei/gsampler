package com.rei.stats.gsampler

import java.time.format.DateTimeFormatter

import groovy.json.JsonBuilder

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.rei.stats.gsampler.util.SimpleHttpServer

class GSamplerAdminServer {
    private static final Logger logger = LoggerFactory.getLogger(GSamplerAdminServer.class);

    private GSamplerEngine engine
    private SimpleHttpServer server = new SimpleHttpServer(2245)

    GSamplerAdminServer(GSamplerEngine engine) {
        this.engine = engine
    }

    void start() {
        registerHttpHandlers()
        server.start()
    }

    void stop() {
        server.stop()
    }

    private void registerHttpHandlers() {
        server.register ('GET', '/') { jsonResponse(getRootData()) }
        server.register ('GET', '/self-stats') { jsonResponse(engine.selfStats) }
        server.register ('GET', '/config') { jsonResponse(getConfigMetadata()) }
        server.register ('GET', '/errors') { jsonResponse(engine.errors) }

        server.register ('POST', '/reload-config') {
            try {
                engine.reloadConfig(true)
                return jsonResponse([success: true])
            } catch (Exception e) {
                return jsonResponse([success: false])
            }
        }
        logger.info("registered http handlers")
    }

    private static jsonResponse(object) {
        try {
            return [contentType: 'application/json', body: new JsonBuilder(object).toString()]
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private getConfigMetadata() {
        return [samplers: engine.config.samplers.collect { [id: it.id, namePrefix: it.namePrefix, readerClass: it.reader.class.name, interval: it.interval, unit: it.unit] },
                writers: engine.config.writers.collect { className: it.class.name }]
    }

    private getRootData() {
        return [status: "up", started: engine.started.format(DateTimeFormatter.ISO_DATE_TIME),
                endpoints:['/self-stats', '/config', '/errors']]
    }
}
