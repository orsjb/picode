#!/bin/bash

DIR=`dirname $0`
cd ${DIR}/..
/usr/bin/java -cp build/picode.jar dynamic.DynamoPI > stdout