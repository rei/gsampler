appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%-4relative [%thread] %-5level %logger{35} - %msg %n"
  }
}
root(DEBUG, ["STDOUT"])