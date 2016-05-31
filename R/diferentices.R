library(ggplot2)
library(reshape2)
library(tidyr)
library(dplyr)
source("R/diferentices-lib.R")

convenios = read.csv("dist/data/convenios-municipio-detalhes-ccodigo.csv")
vizinhos = read.csv("dist/data/vizinhos.euclidiano.csv")

convenios  = sumariza_convenios_para_diferentices(convenios)

# cs %>% 
#   ggplot(aes(x = funcao.imputada, fill = funcao.imputada, weight = total)) + 
#   geom_bar() + 
#   facet_grid(NM_MUNICIPIO_PROPONENTE ~ .) + coord_flip()

convenios.e = expande_convenios(convenios)

t = convenios.e %>% 
  group_by(NM_MUNICIPIO_PROPONENTE, UF_PROPONENTE, nome, cod7) %>% 
  summarise(total = sum(total))
t$funcao.imputada = "TOTAL GERAL"

convenios.e = rbind(convenios.e, t)

# essa chamada demora!
resultado = computa_scores_para_todos(convenios.e, vizinhos)

r2 = resultado[resultado$media != 0, ] 
write.csv(resultado, "dist/data/diferencas-cidades-tudo.csv", row.names = FALSE)
write.csv(r2, "dist/data/diferencas-cidades.csv", row.names = FALSE)

# Explorando:  
x = cria_df_comparacao(convenios, vizinhos, 2503209)
  
x %>%
  ggplot(aes(x = funcao.imputada, y = total, colour = NM_MUNICIPIO_PROPONENTE)) + 
  geom_point() + theme_bw() + coord_flip()

resultado %>%
  ggplot(aes(x = funcao.imputada, y = zscore)) + 
  geom_violin(alpha = .4) + 
  geom_point(size = .2, alpha = .2, position = position_jitter(width = .1)) + 
  theme_bw() + 
  coord_flip()


# Emas : 2505907
