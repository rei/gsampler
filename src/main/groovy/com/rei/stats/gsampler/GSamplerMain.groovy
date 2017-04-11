package com.rei.stats.gsampler

import static com.rei.stats.gsampler.FilesystemConfigurationProvider.DEFAULT_CONFIG_FILE
import static com.rei.stats.gsampler.GitConfigurationProvider.DEFAULT_CONFIG_PATH

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GSamplerMain {
    private static final Logger logger = LoggerFactory.getLogger(GSamplerMain.class);
    
    static void main(String[] args) {
        def cli = new CliBuilder(usage: 'gsampler <options>\n')
        cli.with {
            h(longOpt: 'help', 'Prints usage information.')
            t(longOpt: 'test', 'read config and a single sample from all samplers, print errors')
            c(longOpt: 'config', args: 1, 'path to config file, relative to working directory or repo root (if config-repo-url set)')
            _(longOpt: 'config-repo-url', args: 1, 'git repo url to config repo')
        }
        
        def options = cli.parse(args)
        
        if(options == null || options.h) {
            cli.usage()
            System.exit(1)
        }

        Path homeDir = Paths.get('.')
        def configProvider = getConfigProvider(options, homeDir.resolve('config'))
        def engine = new GSamplerEngine(configProvider, homeDir)
        if (options.test || System.env['TEST'] != null) {
            logger.info("test flag specified, running test of each configured sampler")
            engine.test()
        } else {
            engine.start()
            while (true) { sleep(1000) }
        }
    }

    private static ConfigurationProvider getConfigProvider(options, Path configDir) {
        if (options.'config-repo-url' || System.env['CONFIG_REPO_URL']) {
            logger.info("config repo url specified, using GitConfigurationProvider")
            def repoUrl = options.'config-repo-url' ?: System.env['CONFIG_REPO_URL']
            def configPath = options.config || System.env['CONFIG_FILE'] ?
                    (options.config ?: System.env['CONFIG_FILE'])
                    : DEFAULT_CONFIG_PATH

            return new GitConfigurationProvider(repoUrl, configDir, configPath)
        }
        return new FilesystemConfigurationProvider(configDir.resolve(DEFAULT_CONFIG_FILE))
    }
}
