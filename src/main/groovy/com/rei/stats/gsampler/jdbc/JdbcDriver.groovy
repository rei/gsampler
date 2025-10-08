package com.rei.stats.gsampler.jdbc

import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact

import com.rei.aether.Aether

class JdbcDriver {

    String className
    Artifact driverArtifact
    String mvnRepo

    JdbcDriver(className, gav, mvnRepo) {
        this.mvnRepo = mvnRepo
        this.className = className
        driverArtifact = new DefaultArtifact(gav)
    }

    void register() {
        Aether aether = mvnRepo != null ? Aether.builder().setDefaultRemoteRepo(mvnRepo).setTempLocalRepo().build()
                                        : Aether.fromMavenSettings()

        ClassLoader parent = Thread.currentThread().getContextClassLoader() ?: ClassLoader.getSystemClassLoader()
        GroovyClassLoader gcl = new GroovyClassLoader(parent)

        aether.resolveDependencies(driverArtifact).each { a ->
            gcl.addURL(a.file.toURI().toURL())
        }

        Class.forName(className, true, gcl)


        Thread.currentThread().setContextClassLoader(gcl)
    }
}
