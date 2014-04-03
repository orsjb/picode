#!/bin/bash

# get the MAC address to use as hostname

NEWHOST=`cat /sys/class/net/wlan0/address | sed s/://g`
OLDHOST=`cat /etc/hostname`

# correct format of hostname (pisound-<MAC>)

NEWHOST=pisound-${NEWHOST}

# reboot with correct hostname if required

if [ "$NEWHOST" != "$OLDHOST" ] 
then
	echo "Changing hostname to format pisound-<MAC>. This will require a reboot."
	echo $NEWHOST > hostname
	sudo mv hostname /etc/
	sudo reboot 
fi

# move to the correct dir for running java

DIR=`dirname $0`
cd ${DIR}/..

# choose what to run

# /usr/bin/java -cp build/picode.jar dynamic.DynamoPI > stdout &
# /usr/bin/java -cp build/picode.jar synch.Synchronizer > stdout &