#!/usr/bin/env bash
#
# Cria os arquivos necessários para atualizar o BD com os dados disponíveis
# hoje nas fontes que utilizamos.
#
set -e
set -u

# arquivo com cidades semelhantes, por enquanto estático:
ARQUIVO_VIZINHOS=dist/data/vizinhos.euclidiano.csv

hoje=`date +'%Y%m%d'`
# Dados SICONV ------------------
mkdir siconv-$hoje
./get_dados_siconv.sh siconv-$hoje
# Dados SIAFI  ------------------
./get_dados_siafi.sh 'convenios-siafi-'${hoje}'.csv'
# Calcula o que queremos --------
cd ..
f1=/tmp/iniciativas-${hoje}.csv
f2=/tmp/diferentices-${hoje}.csv
f3=/tmp/historico-${hoje}.csv
./R/atualiza_dados_cli.R dados-externos/siconv-$hoje dados-externos/convenios-siafi-${hoje}.csv $ARQUIVO_VIZINHOS $f1 $f2
mv $f1 $f2 $f3 dist/data/
cd -
