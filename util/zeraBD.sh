#!/usr/bin/env bash

set -e

# Destroi e cria um BD local limpo.
db_name="diferentonas"

dropdb $db_name
createdb $db_name

echo "BD " $db_name " limpo"
