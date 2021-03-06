#!/bin/bash

# util funcs
is_running() {
    local pidFile=$1
    if ! [ -f $pidFile ]; then
	return 1
    fi
    local pid
    pid=$(cat $pidFile)
    if ! ps -p $pid 1>/dev/null 2>&1; then
        echo "Warn: a PID file was detected, removing it."
        rm -f $pidFile
        return 1
    fi
    return 0        
}

waitpid() {
    local pid=$1 timeout=$2 
    { [ -z "$pid" ] || [ -z "$timeout" ]; } && return 10
    local t=0
    while ps -p $pid 1>/dev/null 2>&1; do
        sleep 1
        t=$((t + 1))
        if [ $t -eq $timeout ]; then
            return 1
        fi
        echo -n "."
    done
    return 0
}

forceStopIfNecessary(){
    local pidFile=$1
    if ! [ -f $pidFile ]; then
        return 0
    fi
    local pid
    pid=$(cat $pidFile)
    kill $pid
    waitpid $pid 30 .
    if [ $? -ne 0 ]; then
        echo
        echo "Warn: failed to stop $2 in 30 seconds, sending SIGKILL"
        kill -9 $pid
        sleep 1
    fi
    echo "stopped"
    rm -f $pidFile
}

waitElasticsearch() {
  echo "Checking if Elasticsearch is up and running ..."
  # Try to connect on Elasticsearch port 9200
  elasticsearch_status=0
  retries=1

  exec 6<>/dev/tcp/localhost/9200 || elasticsearch_status=1
  exec 6>&- # close output connection
  exec 6<&- # close input connection

  while (( retries < 6 && elasticsearch_status != 0 )); do
    echo "Elasticsearch doesn't reply to requests on port 9200. Sleeping for a while and trying again... retry ${retries}"

    elasticsearch_status=0

    # Sleep for a while
    sleep 10s

    exec 6<>/dev/tcp/localhost/9200 || elasticsearch_status=1
    exec 6>&- # close output connection
    exec 6<&- # close input connection

    ((retries++))
  done

  if [ $elasticsearch_status -ne 0 ]; then
    echo "/!\ ERROR: Elasticsearch startup has ended with errors"
    exit 1;
  else
    echo "Elasticsearch startup completed successfully --- OK"
  fi
}

waitKibana() {
  echo "Checking if Kibana is up and running ..."
  # Try to connect on Kibana port 5601
  kibana_status=0
  retries=1

  exec 6<>/dev/tcp/localhost/5601 || kibana_status=1
  exec 6>&- # close output connection
  exec 6<&- # close input connection

  while (( retries < 6 && kibana_status != 0 )); do
    echo "Kibana doesn't reply to requests on port 5601. Sleeping for a while and trying again... retry ${retries}"

    kibana_status=0

    # Sleep for a while
    sleep 10s

    exec 6<>/dev/tcp/localhost/5601 || kibana_status=1
    exec 6>&- # close output connection
    exec 6<&- # close input connection

    ((retries++))
  done

  if [ $kibana_status -ne 0 ]; then
    echo "/!\ ERROR: Kibana startup has ended with errors"
    exit 1;
  else
    echo "Kibana startup completed successfully --- OK"
  fi
}
