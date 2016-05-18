#!/usr/bin/env bash

set -e

# Destroi e cria um BD local limpo.
db_name="diferentonas"

dropdb $db_name
createdb $db_name

echo $db_name "database is clean!"
