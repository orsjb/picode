#!/bin/bash

# Run this from host. Assumes that your PIs do not have access to internet. 
# The only things that are updates are scrips/run.sh and build/picode.jar.

RUNDIR=`dirname $0`/..

list_of_pis=`less ${RUNDIR}/config/known_pis`

for piname in ${list_of_pis}
do
	echo Updating PI: ${piname}
	scp ${RUNDIR}/build/picode.jar device@${piname}:git/picode/build/
	scp ${RUNDIR}/scripts/run.sh device@${piname}:git/picode/scripts/
done