import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

appender("FILE", RollingFileAppender) {
  file = "logs/gsampler.log"
  rollingPolicy(TimeBasedRollingPolicy) {
    fileNamePattern = "logs/gsampler.%d{yyyy-MM-dd}.log"
    maxHistory = 10
  }
  encoder(PatternLayoutEncoder) {
    pattern = "%d [%thread] %-5level %logger{60} - %msg%n"
  }
}

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d [%thread] %-5level %logger{60} - %msg%n"
    }
}

root(INFO, System.properties.appender == 'console' ? ["CONSOLE"] : ["FILE"])