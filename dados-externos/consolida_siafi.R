#!/usr/bin/env Rscript

args = commandArgs(trailingOnly = TRUE)
if (length(args) < 1) {
  stop(
    "Uso: consolida_siafi.R <arquivo1> [<arquivo2> ...]"
  )
}

le_um = function(arquivo_siafi, df_ja_lido = NULL){
  #write(arquivo_siafi, stderr())
  library(readr)
  library(dplyr, warn.conflicts = F)
  bruto = suppressWarnings(read_tsv(arquivo_siafi, 
                                    col_types = cols(.default = col_character(), 
                                                     `Número Convênio` = "i")))
  filtrado = bruto %>%
    select(1:10, 14:17)
  
  deve_incluir = function(numero, ja_lido){
    if(is.null(ja_lido)){
      return(TRUE)
    } else {
      return(numero != "" & !(numero %in% ja_lido$`Número Convênio`))
    }
  }
  
  filtrado = filtrado %>%
    filter(deve_incluir(`Número Convênio`, df_ja_lido)) %>%
    group_by(`Número Convênio`) %>%
    slice(1) %>% 
    ungroup() %>% 
    return()
}

resultado = le_um(args[1])
for (arquivo in args[-1]) {
  resultado = rbind(resultado, le_um(arquivo, resultado))
}

write.csv2(resultado, "")

warnings()