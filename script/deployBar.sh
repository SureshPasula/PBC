#!/bin/bash -x
# This file is copyright of Prolifics 2011. All rights reserved. You are not permitted to copy, modify, or redistribute this file.
# See the readme.txt file for further terms and conditions.

echo mqsideploy.sh $*

# Prepare the parameters

# The BAR file that will be created
export BARNAME=$1

# Local IIB_BROKER
export IIB_BROKER=$2

# IIB execution group
export IIB_EXEC_GRP=$3

# The home for the IIT binaries
export IIT_BIN=$4 

# The home for the IIB binaries
export IIB_BIN=$5

# The PBC root
export PBC_ROOT=$6

# Remote broker host
export IIB_REMOTEBROKER_HOST=$7

# Remove broker port
export IIB_REMOTEBROKER_PORT=$8

# Remote broker queue manager
export IIB_REMOTEBROKER_QMGR=$9

shift

# Switch setting
export IIB_CLEAN_DEPLOY_SWITCH=$9

shift

# Switch setting
export IIB_TIMEOUT_SWITCH=$9

shift

# Switch setting
export USER=$9

shift

# Switch setting
export TRACE_FILE=$9

echo This script is copyright of Prolifics 2011. All rights reserved. You are not permitted to make copy, modify, or redistribute it.
echo Broker Archive Command Line Deployment
echo --------------------------------------------------
echo BAR name=$BARNAME
echo IIB broker=$IIB_BROKER
echo IIB remote broker host name=$IIB_REMOTEBROKER_HOST
echo IIB remote broker port=$IIB_REMOTEBROKER_PORT
echo IIB remote broker queue mgr=$IIB_REMOTEBROKER_QMGR
echo IIB execution group=$IIB_EXEC_GRP
echo IIB clean deploy switch=$IIB_CLEAN_DEPLOY_SWITCH
echo IIB timeout switch=$IIB_TIMEOUT_SWITCH
echo IIT binaries=$IIT_BIN
echo IIB binaries=$IIB_BIN
echo PBC root=$PBC_ROOT
echo USER=$USER
echo TRACE_FILE=$TRACE_FILE
echo ---------------------------------------------------

# Set the MQSI profile
. $IIB_BIN/mqsiprofile

chmod 777 $PBC_ROOT/logs

if [ -z $IIB_BROKER ]
then
	echo "$IIB_BIN/mqsideploy -i $IIB_REMOTEBROKER_HOST -p $IIB_REMOTEBROKER_PORT -q $IIB_REMOTEBROKER_QMGR -e $IIB_EXEC_GRP -a $BARNAME $IIB_CLEAN_DEPLOY_SWITCH  $IIB_TIMEOUT_SWITCH -v $TRACE_FILE"
	su -c "$IIB_BIN/mqsideploy -i $IIB_REMOTEBROKER_HOST -p $IIB_REMOTEBROKER_PORT -q $IIB_REMOTEBROKER_QMGR -e $IIB_EXEC_GRP -a $BARNAME $IIB_CLEAN_DEPLOY_SWITCH  $IIB_TIMEOUT_SWITCH -v $TRACE_FILE" -s /bin/sh $USER
else
	echo "$IIB_BIN/mqsideploy $IIB_BROKER -e $IIB_EXEC_GRP -a $BARNAME $IIB_CLEAN_DEPLOY_SWITCH $IIB_TIMEOUT_SWITCH -v $TRACE_FILE"
	su -c "$IIB_BIN/mqsideploy $IIB_BROKER -e $IIB_EXEC_GRP -a $BARNAME $IIB_CLEAN_DEPLOY_SWITCH $IIB_TIMEOUT_SWITCH -v $TRACE_FILE" -s /bin/sh $USER
fi