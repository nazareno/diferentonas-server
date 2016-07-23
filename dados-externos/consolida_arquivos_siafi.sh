#!/usr/bin/env bash

if [[ $# -lt 1 ]]; then
    echo "Uso: $0 <arquivo1> <arquivo2> ..."
fi

echo "Número Convênio;Modalidade Aplicação;Fonte-Finalidade;Nome Favorecido;Codigo Acao;Nome Programa;Codigo Programa;Nome Sub Funcao;Codigo Sub Funcao ;Nome Funcao;Codigo Funcao;Nome Municipio;Codigo SIAFI Municipio;Sigla Unidade Federação"
# Seleciona colunas e remove duplicatas
awk -F'\t' 'BEGIN{OFS = ";"};
            NR > 1 && $(17) ~ /[0-9]/ && !(a[$(17)]) {a[$(17)] += 1; print $(17), $(16), $(15), $(14), $(10), $9, $8, $7, $6, $5, $4, $3, $2, $1}' $*
