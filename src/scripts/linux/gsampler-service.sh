#!/bin/bash

# This is the user that executes gsampler
RUN_AS=sampler

# The path to the gsampler installation
SAMPLER_APP="/opt/sampler"
LOGS="$SAMPLER_APP/logs"

VERSION=$2
echo $SAMPLER_APP

get_pid() {
    ps auxwww | grep java | grep gsampler | grep -v grep | awk '{print $2}'
}

start() {
	pushd $SAMPLER_APP > /dev/null

	JAVA="java"
	CLASSPATH="$(echo lib/*.jar | tr ' ' ':')"
	MAIN_CLASS=com.rei.stats.gsampler.GSamplerMain
	JAVA_OPTS=

    CMD="$JAVA -cp $CLASSPATH $JAVA_OPTS $MAIN_CLASS"

	echo "Now in $(pwd)"

	if [ ! -e $LOGS ]; then
		echo "Making logs dir"
		mkdir $LOGS
	fi

	echo "$CMD"
        nohup $CMD > $LOGS/console.out 2>&1 &

	popd > /dev/null

        pid=get_pid
	if [ -n "$pid" ]; then
		echo "Started with pid $!"
		return 0
	fi

	echo "Sampler did not start."
	return 3
}

stop() {
	pid=`get_pid`
	if [ -n "$pid" ]; then
	    echo "Stopping pid $pid"
	    kill $pid
	    return $?
	fi
	echo "sampler was not running"
    return 3
}

status() {
    pid=`get_pid`
    if [ -n "$pid" ]; then
        echo "sampler (pid $pid) is running..."
        return 0
    fi
    echo "sampler is stopped"
    return 3
}

upgrade() {
    if [ -n "$VERSION" ]; then
        pushd $SAMPLER_APP > /dev/null
        ZIP_NAME="gsampler-$VERSION-dist.zip"
        wget "$MVN_REPO/com/rei/stats/gsampler/gsampler/$VERSION/$ZIP_NAME"
        stop
        rm -r "$SAMPLER_APP/lib.bak"
        rm -r "$SAMPLER_APP/bin.bak"
        mv "$SAMPLER_APP/lib" "$SAMPLER_APP/lib.bak"
        mv "$SAMPLER_APP/bin" "$SAMPLER_APP/bin.bak"
        unzip $ZIP_NAME
        rm $ZIP_NAME
        start
        return 0
    fi
    echo "version to upgrade not specified"
    return 3
}

case "$1" in
    start)
		start
		exit $?
		;;
    stop)
        stop
        exit $?
        ;;
    restart)
        stop
        sleep 10
        start
        exit $?
        ;;
    status)
    	status
    	exit $?
        ;;
    upgrade)
        upgrade
        exit $?
        ;;
    *)
     echo "Usage: $0 {start|stop|restart|status|upgrade <version>}"
esac
