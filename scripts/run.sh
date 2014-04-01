#!/bin/bash

# get the MAC address to use as hostname
NEWHOST=`cat /sys/class/net/wlan0/address | sed s/://g`
OLDHOST=`cat /etc/hostname`

NEWHOST=pisound-${NEWHOST}

if [ "$NEWHOST" != "$OLDHOST" ] 
then
	echo $NEWHOST > hostname
	sudo mv hostname /etc/
	sudo reboot
fi

DIR=`dirname $0`
cd ${DIR}/..
/usr/bin/java -cp build/picode.jar dynamic.DynamoPI > stdout