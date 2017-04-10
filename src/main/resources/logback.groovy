import static ch.qos.logback.classic.Level.DEBUG

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
root(INFO, ["FILE"])