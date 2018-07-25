#!/bin/bash

# run the command
if [[ "${SPARK_TYPE}" == "master" ]]; then
    # run master
    /spark/sbin/start-master.sh &
    sparkPid=$!
elif [[ "${SPARK_TYPE}" == "slave" ]]; then
    # run slave with options
    /spark/sbin/start-slave.sh "${SPARK_MASTERS}" &
    sparkPid=$!
fi

# wait for spark to exit
wait ${sparkPid}
