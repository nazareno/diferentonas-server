#!/usr/bin/env bash

set -e

# Destroi e cria um BD local limpo.

psql -c "drop database diferentonas;"
psql -c "create database diferentonas;"