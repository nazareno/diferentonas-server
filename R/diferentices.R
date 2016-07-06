library(ggplot2)
library(reshape2)
library(tidyr, warn.conflicts=FALSE)
library(dplyr, warn.conflicts=FALSE)
source("R/diferentices-lib.R")

calcula_diferentices = function(arquivo_convenios = "dist/data/convenios-municipio-detalhes-ccodigo.csv", 
                                arquivo_vizinhos = "dist/data/vizinhos.euclidiano.csv"){
  convenios = read.csv(arquivo_convenios)
  vizinhos = read.csv(arquivo_vizinhos)
  
  convenios  = sumariza_convenios_para_diferentices(convenios)
  # cs %>% 
  #   ggplot(aes(x = funcao.imputada, fill = funcao.imputada, weight = total)) + 
  #   geom_bar() + 
  #   facet_grid(NM_MUNICIPIO_PROPONENTE ~ .) + coord_flip()
  message("Adicionando zeros nas áreas não mencionadas")
  convenios.e = expande_convenios(convenios)
  
  t = convenios.e %>% 
    group_by(MUNIC_PROPONENTE, UF_PROPONENTE, nome, cod7) %>% 
    summarise(total = sum(total))
  t$funcao.imputada = "TOTAL GERAL"
  
  convenios.e = rbind(convenios.e, t)
  
  # essa chamada demora!
  message("Computando scores de diferentices")
  resultado = computa_scores_para_todos(convenios.e, vizinhos)
  message("Pronto")
  return(resultado)
}

# r2 = resultado[resultado$media != 0, ] 
# write.csv(r2, "dist/data/diferencas-cidades.csv", row.names = FALSE)

# Explorando:  
# x = cria_df_comparacao(convenios, vizinhos, 2503209)
#   
# x %>%
#   ggplot(aes(x = funcao.imputada, y = total, colour = NM_MUNICIPIO_PROPONENTE)) + 
#   geom_point() + theme_bw() + coord_flip()
# 
# resultado %>%
#   ggplot(aes(x = funcao.imputada, y = zscore)) + 
#   geom_violin(alpha = .4) + 
#   geom_point(size = .2, alpha = .2, position = position_jitter(width = .1)) + 
#   theme_bw() + 
#   coord_flip()

# Id Emas : 2505907
