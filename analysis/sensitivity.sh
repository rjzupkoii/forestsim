#!/bin/bash

for ndx in `seq 1500 500 3500`; do

  # Write the updated settings
  rm settings.ini
  printf "[settings]\nlogging=$ndx\n" >> settings.ini
    
  # Run the model
  for mode in none discount agglomeration; do  
    java -jar ForestSim.jar --$mode -quiet
    mkdir -p results/$ndx/$mode
    mv out/$mode/*.csv results/$ndx/$mode
    rm -rf out 
  done
done

