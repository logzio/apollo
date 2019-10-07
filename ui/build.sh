#!/usr/bin/env bash

set -ev

cd ui
    docker run --rm -v $PWD:/data digitallyseamless/nodejs-bower-grunt npm install &> /dev/null
    docker run --rm -v $PWD:/data digitallyseamless/nodejs-bower-grunt bower install &> /dev/null
    docker run --rm -v $PWD:/data digitallyseamless/nodejs-bower-grunt grunt build --force
cd -

0
