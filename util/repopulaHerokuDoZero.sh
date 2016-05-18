#!/usr/bin/env bash

set -e

# Zera seu BD, povoa ele (por hora rodando os testes de unidade) e espelha no Heroku

cd "${BASH_SOURCE%/*}"/.. || exit
./util/zeraBD.sh
./activator test
echo "BD agora está populado como efeito dos testes"
./util/localbd2heroku.sh
cd -