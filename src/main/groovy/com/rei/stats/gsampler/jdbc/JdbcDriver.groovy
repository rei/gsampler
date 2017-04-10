package com.rei.stats.gsampler.jdbc

import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact

import com.rei.aether.Aether

class JdbcDriver {
    public static final String MVN_REPOS = 'MVN_REPOS'
    String className
    Artifact driverArtifact

    JdbcDriver(className, gav) {
        this.className = className
        driverArtifact = new DefaultArtifact(gav)
    }
    
    void register() {
        Aether aether = System.env[MVN_REPOS] ? Aether.builder().addRemoteRepo('repo', MVN_REPOS).setTempLocalRepo().build()
                                              : Aether.fromMavenSettings()

        aether.resolveDependencies(driverArtifact).each { a -> ClassLoader.systemClassLoader.addURL(a.file.toURI().toURL()) }

        Class.forName(className, true, new GroovyClassLoader())
    }
}
