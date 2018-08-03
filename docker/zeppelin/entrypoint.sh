#!/bin/bash

# run the command
/zeppelin/bin/zeppelin.sh &
zeppelinPid=$!

# wait for spark to exit
wait ${zeppelinPid}
