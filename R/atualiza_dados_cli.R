#!/usr/bin/env Rscript
args = commandArgs(trailingOnly=TRUE)
if(length(args) < 3){
  stop("Informe caminho para o arquivo de convênios / iniciativas e arquivo de saida")
}
arquivo_siconv = args[1]
arquivo_siafi = args[2]
arquivo_saida = args[3]
source("R/join_dados.R")
cruza_dados(arquivo_siconv, arquivo_siafi, arquivo_saida)
message(paste("Dados salvos em", arquivo_saida))

# arquivo_siconv = "dados-externos/siconv/01_ConveniosProgramas-20160513.csv"
# arquivo_siafi = "dados-externos/convenios-siafi-em-201605.csv"
# arquivo_saida = "x"
