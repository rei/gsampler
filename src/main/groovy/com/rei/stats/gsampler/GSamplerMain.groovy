package com.rei.stats.gsampler;

public class GSamplerMain {
    
    public static void main(String[] args) {
        def cli = new CliBuilder(usage: 'gsampler <options>\n')
        cli.with {
            h(longOpt: 'help', 'Prints usage information.')
            t(longOpt: 'test', 'read config and a single sample from all samplers, print errors')
            c(longOpt: 'config', args: 1, 'path to config file, relative to working directory')
        }
        
        def options = cli.parse(args)
        
        if(options == null || options.h) {
            cli.usage()
            System.exit(1)
        }

        def engine = getEngine(options.config)
        if (options.test) {
            engine.test()
        } else {
            engine.start()
        }
        
        while (true) { sleep(1000) }
    }

    private static GSamplerEngine getEngine(configOption) {
        def configFile = configOption ? new File(configOption) : new File('config/gsampler.groovy')
        return new GSamplerEngine(configFile.absoluteFile.toPath())
    }    
}
