package com.rei.stats.gsampler.jdbc

import groovy.grape.Grape

class JdbcDriver {
    String className
    String groupId
    String artifactId
    String version
    
    JdbcDriver(className, gav) {
        this.className = className
        def parts = gav.split(':')
        if (parts.size() != 3) {
            throw new IllegalArgumentException("GAV ($gav) must be in the format 'group:artifact:version'")
        }
        groupId = parts[0]
        artifactId = parts[1]
        version = parts[2]
    }
    
    void register() {
        def cl = new GroovyClassLoader()
        Grape.grab(group: groupId, module: artifactId, version: version, classLoader:cl)
        ClassLoader.systemClassLoader.addURL(cl.URLs[0])
        Class.forName(className, true, cl)
    }
}
