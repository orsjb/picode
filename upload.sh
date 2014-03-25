#!/bin/bash

D="$( dirname "$0" )"
echo ${D}

scp ${D}/src/Main.java pi@raspberrypi:main/
scp -r ${D}/audio pi@raspberrypi:
