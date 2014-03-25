#!/bin/bash

for i in [3..20] 
do
	scp -r ../bin pi@10.0.1.${i}:/home/pi/pisound/
done