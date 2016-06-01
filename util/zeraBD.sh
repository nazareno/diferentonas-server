#!/usr/bin/env bash

set -e

# Destroi e cria um BD local limpo.
db_name="diferentonas"

psql -d $db_name -c 'drop schema public cascade;
create schema public;
create extension cube;
create extension earthdistance;'

echo "BD " $db_name " limpo"
