#!/bin/bash

# This script should be run from within the Osmosis/bin folder if you have not added Osmosis to your environment

#input_file="/Users/daddy30000/14_Mobility_Sim/GoogleDrive/SmartBay/data/new_networks/california-latest.osm.pbf"
#output_file="/Users/daddy30000/14_Mobility_Sim/GoogleDrive/LBL/Test_Network/SF_Geary.osm"
input_file="/Users/daddy30000/dev/beam/application/sf-light/r5/sf-light.osm.pbf"
output_file="/Users/daddy30000/dev/beam/application/sf-light/r5/sf-light.osm"
## Geary boundaries
#top=37.783559
#left=-122.4759167
#bottom=37.779177
#right=-122.458279
top=37.909727
left=-122.662198
bottom=37.694680
right=-122.334262
# reject_list=residential

####
## NOTE: only one of the following two blocks must be run. Comment out the other one!
##

# Clipping to bounding box with all roads
# sh osmosis --rb file=$input_file \
# --bounding-box top=$top left=$left bottom=$bottom right=$right completeWays=false --used-node --wb $output_file

# Clipping to bounding box with road filtering
#sh osmosis --rb file=$input_file \
#--tf reject-ways highway=$reject_list --bounding-box top=$top left=$left bottom=$bottom right=$right completeWays=true --used-node --wx  $output_file


# Writing to xml
sh osmosis --rb file=$input_file --wx $output_file