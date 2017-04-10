package com.rei.stats.gsampler.exec

import com.rei.stats.gsampler.StatValue
import com.rei.stats.gsampler.StatsReader

class ExecStatsReader implements StatsReader {
    private MetricParser parser
    private def cmd    private long timeout;
    
    ExecStatsReader(cmd, long timeout, MetricParser parser) {
        this.parser = parser
        this.cmd = cmd
        this.timeout = timeout;
    }
    
    @Override
    public Map<String, StatValue> read() {
        Process p = cmd.execute()
        def output = new StringBuilder()
        p.consumeProcessOutputStream(output)
        p.waitForOrKill(timeout)
        return parser.parse(output.toString())        
    }
}
