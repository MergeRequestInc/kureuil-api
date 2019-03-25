#!/bin/sh

## This tutorial should be launch only after installing the PostgreSQL version you want
##
##

VAR='kureuil'

runuser -l postgres -c "createuser -d $VAR"
createdb -O $VAR $VAR
psql -d kureuil -c "ALTER USER $VAR WITH ENCRYPTED PASSWORD '$VAR'"
psql -d kureuil -c "CREATE EXTENSION pgcrypto"

## You can manualy login in with the following command line: psql -U $VAR -d $VAR -h localhost
