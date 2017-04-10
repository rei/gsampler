package com.rei.stats.gsampler.util

import java.util.concurrent.Executors

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.sun.net.httpserver.*

@SuppressWarnings("restriction")
class SimpleHttpServer {
    private static Logger logger = LoggerFactory.getLogger(SimpleHttpServer)
    
    private int port;
    private Map handlers = [:]
    private HttpServer server 
    
    SimpleHttpServer(int port) {
        this.port = port;
    }
    
    void register(String method, String path, Closure callback) {
        handlers[path] = new CallbackHandler(method, callback)
    }
    
    void start() {
        server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0)
        handlers.each { path, handler ->
            server.createContext(path, handler)
        }
        server.setExecutor(Executors.newCachedThreadPool())
        server.start()
    }

    void stop() {
        server.stop(0)
    }
    
    private static class CallbackHandler implements HttpHandler {
        def method;
        def callback;

        CallbackHandler(method, callback) {
            this.callback = callback;
            this.method = method;
        }
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                logger.info ("received http request: ${exchange.requestMethod} ${exchange.requestURI}")
                if (exchange.requestMethod.equalsIgnoreCase(method)) {
                    def response = callback()
                    exchange.responseHeaders.set('Content-Type', response?.contentType ?: 'text/plain')
                    def bytes = response.body.bytes
                    exchange.sendResponseHeaders(200, bytes.size())
                    exchange.responseBody << bytes
                } else {
                    exchange.sendResponseHeaders(404, 0)
                }
            } catch (Throwable t) {
                logger.info("error processing request!", t)
                exchange.sendResponseHeaders(500, 0)
                exchange.responseBody << "${t.class}: ${t.message}".bytes
            } finally {
                exchange.responseBody.close()
            }
        }
        
    }
}
