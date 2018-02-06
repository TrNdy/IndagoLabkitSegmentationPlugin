#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/travis-build.sh
sh travis-build.sh $encrypted_bd2ebf2194b9_key $encrypted_bd2ebf2194b9_iv
