#!/usr/bin/env Rscript
args = commandArgs(trailingOnly = TRUE)
if (length(args) != 6) {
  stop(
    "Uso: atualiza_dados_cli.R <diretório dados SICONV> <arquivo SIAFI> <arquivo de vizinhos> <arquivo de saida de iniciativas> <arquivo de saida de diferentices> <arquiv de saida de histórico>"
  )
}
dados_siconv = args[1] # diretório com dados baixados e descompactados
arquivo_siafi = args[2]
arquivo_vizinhos = args[3]
arquivo_iniciativas_saida = args[4]
arquivo_diferentices = args[5]
arquivo_historicos = args[6]

# Debug / uso interativo: 
# dados_siconv = "dados-externos/siconv-20160628"
# arquivo_siafi = "dados-externos/convenios-siafi-201607.csv"
# arquivo_vizinhos = "dist/data/vizinhos.euclidiano.csv"
# arquivo_iniciativas_saida = "dist/data/iniciativas-20160728.csv"
# arquivo_diferentices = "dist/data/diferentices-20160728.csv"
# arquivo_historicos = "dist/data/historico-20160728.csv"

library(futile.logger)

# -----------------------------------------------------------
# Organiza os dados do SICONV
# -----------------------------------------------------------
source("R/consolida_siconv.R")
convenios = consolida_convenios(dados_siconv)
# TODO Por agora não estamos usando arquivos de histórico (criar issue)
# historico = carrega_e_limpa_historicos(dados_siconv, convenios$NR_CONVENIO)
# write.csv(historico, arquivo_historicos, row.names = FALSE)
# flog.info("Históricos dos convênios salvo em %s", arquivo_historicos)

# -----------------------------------------------------------
# Cruza dados do SICONF + SIAFI com dados sobre os municípios:
# -----------------------------------------------------------
source("R/join_dados.R")
source("R/traducao_termos.R")
iniciativas = cruza_dados(convenios, arquivo_siafi)
iniciativas = traduz_termos(iniciativas)
write.csv(iniciativas, arquivo_iniciativas_saida, row.names = FALSE)
flog.info("Dados de iniciativa salvos em %s", arquivo_iniciativas_saida)

# -----------------------------------------------------------
# Calcula scores de diferentices das cidades
# -----------------------------------------------------------
source("R/diferentices.R")
flog.info("Calculando diferentices a partir de %s e %s", arquivo_iniciativas_saida, arquivo_vizinhos)
diferentices = calcula_diferentices(arquivo_iniciativas_saida, arquivo_vizinhos)
write.csv(diferentices,
          arquivo_diferentices,
          row.names = FALSE)
flog.info("Diferentices salvas em %s", arquivo_diferentices)

