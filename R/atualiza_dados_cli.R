#!/usr/bin/env Rscript
args = commandArgs(trailingOnly = TRUE)
if (length(args) != 5) {
  stop(
    "Uso: atualiza_dados_cli.R <arquivo de iniciativas SICONV> <arquivo SIAFI> <arquivo de vizinhos> <arquivo de saida de iniciativas> <arquivo de saida de diferentices>"
  )
}
arquivo_siconv = args[1]
arquivo_siafi = args[2]
arquivo_vizinhos = args[3]
arquivo_saida = args[4]
arquivo_diferentices = args[5]

# Debug / uso interativo: 
# arquivo_siconv = "dados-externos/siconv/01_ConveniosProgramas-20160627.csv"
# arquivo_siafi = "dados-externos/convenios-siafi-20160627.csv"
# arquivo_vizinhos = "dist/data/vizinhos.euclidiano.csv"
# arquivo_saida = "dist/data/iniciativas-20160627.csv-2"
# arquivo_diferentices = "dist/data/diferentices-20160627.csv"

# -----------------------------------------------------------
# Cruza dados do SICONF + SIAFI com dados sobre os municípios:
# -----------------------------------------------------------
source("R/join_dados.R")
source("R/traducao_termos.R")
iniciativas = cruza_dados(arquivo_siconv, arquivo_siafi)
iniciativas = traduz_termos(iniciativas)
write.csv(iniciativas, arquivo_saida, row.names = FALSE)
message(paste("Dados de iniciativa salvos em", arquivo_saida))

# -----------------------------------------------------------
# Calcula scores de diferentices das cidades
# -----------------------------------------------------------
source("R/diferentices.R")
message(paste("Calculando diferentices a partir de", arquivo_saida, "e", arquivo_vizinhos))
diferentices = calcula_diferentices(arquivo_saida, arquivo_vizinhos)
write.csv(diferentices,
          arquivo_diferentices,
          row.names = FALSE)
message(paste("Diferentices salvas em", arquivo_diferentices))

# arquivo_siconv = "dados-externos/siconv/01_ConveniosProgramas-20160513.csv"
# arquivo_siafi = "dados-externos/convenios-siafi-em-201605.csv"
# arquivo_saida = "x"
