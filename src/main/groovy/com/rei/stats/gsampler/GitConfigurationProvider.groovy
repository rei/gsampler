package com.rei.stats.gsampler

import java.nio.file.Files
import java.nio.file.Path

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.OpenSshConfig
import org.eclipse.jgit.transport.URIish
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.jcraft.jsch.Session

class GitConfigurationProvider implements ConfigurationProvider {
    private static final Logger logger = LoggerFactory.getLogger(GitConfigurationProvider.class);

    public static final String DEFAULT_CONFIG_PATH = 'gsampler.groovy'

    private ConfigurationLoader configLoader = new ConfigurationLoader()

    private String repoUrl
    private Path configDir
    private String configPath

    GitConfigurationProvider(String repoUrl, Path configDir, String configPath = DEFAULT_CONFIG_PATH) {
        this.repoUrl = repoUrl
        this.configDir = configDir
        this.configPath = configPath
    }

    @Override
    Configuration loadConfiguration(GSamplerEngine engine) {
        JschConfigSessionFactory.setInstance(new JschConfigSessionFactory() {
            protected void configure(OpenSshConfig.Host hc, Session session) {
                session.setConfig('StrictHostKeyChecking', 'false')
            }
        })

        if (Files.exists(configDir.resolve('.git'))) {
            def repo = Git.open(configDir.toFile())
            def remoteUrl = new URIish(repoUrl)
            if (repo.remoteList().call().find { it.URIs.contains(remoteUrl) }) {
                logger.info("configuration already checked out, pulling latest")
                try {
                    repo.pull().call()
                } catch (Exception e) {
                    logger.warn("unable to pull latest config, re-using existing configuration", e)
                }
            } else {
                logger.info("configuration origin url differs from existing config, deleting...")
                configDir.deleteDir()
                cloneRepo()
            }

        } else {
            cloneRepo()
        }
        return configLoader.loadConfiguration(configDir.resolve(configPath).toFile())
    }

    private void cloneRepo() {
        logger.info("cloning config from url ${repoUrl}")
        Git.cloneRepository().setDirectory(configDir.toFile()).setURI(repoUrl).call()
    }

    @Override
    void shutdown() {

    }
}
