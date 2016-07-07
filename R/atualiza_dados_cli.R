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
# dados_siconv = "dados-externos/siconv/01_ConveniosProgramas-20160627.csv"
# arquivo_siafi = "dados-externos/convenios-siafi-201607.csv"
# arquivo_vizinhos = "dist/data/vizinhos.euclidiano.csv"
# arquivo_iniciativas_saida = "dist/data/iniciativas-20160705.csv"
# arquivo_diferentices = "dist/data/diferentices-20160705.csv"
# arquivo_historicos = "dist/data/historico-20160705.csv"

# -----------------------------------------------------------
# Organiza os dados do SICONV
# -----------------------------------------------------------
source("R/consolida_siconv.R")
convenios = consolida_convenios(dados_siconv)
historico = carrega_e_limpa_historicos(dados_siconv, convenios$NR_CONVENIO)
write.csv(historico, arquivo_historicos, row.names = FALSE)
message("Históricos dos convênios salvo em ", arquivo_historicos)

# -----------------------------------------------------------
# Cruza dados do SICONF + SIAFI com dados sobre os municípios:
# -----------------------------------------------------------
source("R/join_dados.R")
source("R/traducao_termos.R")
iniciativas = cruza_dados(convenios, arquivo_siafi)
iniciativas = traduz_termos(iniciativas)
write.csv(iniciativas, arquivo_iniciativas_saida, row.names = FALSE)
message("Dados de iniciativa salvos em ", arquivo_iniciativas_saida)

# -----------------------------------------------------------
# Calcula scores de diferentices das cidades
# -----------------------------------------------------------
source("R/diferentices.R")
message("Calculando diferentices a partir de ", arquivo_iniciativas_saida, " e ", arquivo_vizinhos)
diferentices = calcula_diferentices(arquivo_iniciativas_saida, arquivo_vizinhos)
write.csv(diferentices,
          arquivo_diferentices,
          row.names = FALSE)
message("Diferentices salvas em ", arquivo_diferentices)

