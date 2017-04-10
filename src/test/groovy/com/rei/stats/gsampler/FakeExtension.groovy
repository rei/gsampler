package com.rei.stats.gsampler

import com.rei.stats.gsampler.Extension;
import com.rei.stats.gsampler.StatValue;
import com.rei.stats.gsampler.StatsReader;

class FakeExtension implements Extension {
    final String name = 'fake'
    
    StatsReader fakeReader() {
        return [read: { [stat1: new StatValue(10), stat2: new StatValue(1.5)] }] as StatsReader
    }
}
