#!/bin/bash

# Script to autorun on device

############### NETWORK STUFF ################

#### NOTE: recommended not to do this in the same script. We now assume another script does this and is autorun before this.

#update our Wifi credentials
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
${DIR}/set-wifi-credentials.pl

# get the MAC address to use as hostname

NEWHOST=`cat /sys/class/net/wlan0/address | sed s/://g`
OLDHOST=`cat /etc/hostname`

echo current hostname: ${OLDHOST}
echo wlan0 mac address: ${NEWHOST}

#fall back to eth0 if wlan0 is empty
if [ -n "$NEWHOST"]
then
	echo Unable to resolve wlan0, resorting to eth0
	NEWHOST=`cat /sys/class/net/eth0/address | sed s/://g`
	echo eth0 mac address: ${NEWHOST}
fi

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

############### THE MAIN APP ################

# move to the correct dir for running java (one level above where this script is)

DIR=`dirname $0`
cd ${DIR}/..

# Run the main app
# args are bufSize (8192), sample rate (22050), bits (16), input channels (0), output channels (1), autostart (true)

#NOTE: if using HIFIBERRY then you want OUTS=2 and SR=44100, else it fails. HIFIBERRY must have a stereo line.

BUF=4096
SR=22050
BITS=16
INS=0
OUTS=1 
AUTOSTART=true

/usr/bin/sudo /usr/bin/java -cp build/picode.jar -Xmx215m device.DeviceMain $BUF $SR $BITS $INS $OUTS $AUTOSTART  > stdout &

############## BONUS FEATURE #################
## Also run the code app (but wait a bit first)
sleep 10
/usr/bin/sudo /usr/bin/java -cp build/picode.jar compositions.bowls2015.BowlsGameMain &
############## ------------- #################

### Various old or test scripts ###
# /usr/bin/sudo /usr/bin/java -cp build/picode.jar test.MiniMUTest $BUF $SR $BITS $INS $OUTS  > stdout &
# /usr/bin/sudo /usr/bin/java -cp build/picode.jar test.PI4JTest > stdout &
# libs/minimulib/minimu9-ahrs -b /dev/i2c-1 | /usr/bin/sudo /usr/bin/java -cp build/picode.jar test.PrintStdIn $BUF $SR $INS $OUTS > stdout &
# echo ~ > stdout &
# libs/minimulib/minimu9-ahrs -b /dev/i2c-1 > stdout &
# /usr/bin/sudo /usr/bin/java -cp build/picode.jar dynamic.DynamoPI $BUF $SR $BITS $INS $OUTS > stdout &
# /usr/bin/sudo /usr/bin/java -cp build/picode.jar synch.Synchronizer $BUF $SR $BITS $INS $OUTS > stdout &
####################################

# Finally, run the network-monitor.sh script to keep WiFi connection alive
#  if we have a wlan0

if [ -d /sys/class/net/wlan0 ]
then
	/usr/bin/sudo scripts/network-monitor.sh > netstatus &
fi
