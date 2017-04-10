package com.rei.stats.gsampler.graphite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {
    public static void main(String[] args) throws IOException {
        new EchoServer().start(Integer.parseInt(args[0]));
    }
    
    private Appendable out = System.out;
    private boolean shutdown;
    
    EchoServer setOutput(Appendable out) {
        this.out = out;
        return this;
    }
    
    void start(int port) throws IOException {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            Socket socket = serverSocket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = null;
            while (!shutdown && (line = reader.readLine()) != null) {
                out.append(line + '\n');
            }
        }
    }
    
    void destroy() {
        shutdown = true;
    }
}
