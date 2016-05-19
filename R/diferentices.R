library(ggplot2)
library(reshape2)
library(tidyr)
library(dplyr)
source("R/diferentices-lib.R")

convenios = read.csv("dist/data/convenios-municipio-detalhes-ccodigo.csv")
vizinhos = read.csv("dist/data/vizinhos.euclidiano.csv")

convenios  = convenios %>% 
  select(NM_MUNICIPIO_PROPONENTE, UF_PROPONENTE, VL_REPASSE, funcao.imputada, ANO_CONVENIO, nome, cod7) %>%
  group_by(NM_MUNICIPIO_PROPONENTE, UF_PROPONENTE, funcao.imputada, ANO_CONVENIO, nome, cod7) %>%
  summarise(total = sum(VL_REPASSE)) 

# cs %>% 
#   ggplot(aes(x = funcao.imputada, fill = funcao.imputada, weight = total)) + 
#   geom_bar() + 
#   facet_grid(NM_MUNICIPIO_PROPONENTE ~ .) + coord_flip()

pca_comparacao <- function(convenios, vizinhos, cod) {
  ids = vizinhos[vizinhos$origem == cod, 12:22]
  cs = convenios %>% filter(cod7 %in% ids, ANO_CONVENIO >= 2013)
  cs$cod7 = factor(cs$cod7)
  cs$funcao.imputada = droplevels(cs$funcao.imputada)
  
  cs.w = dcast(select(cs, cod7, funcao.imputada, total), 
               formula = cod7 ~ funcao.imputada, sum)
  
  df = cs.w[, -1]
  row.names(df) = cs$NM_MUNICIPIO_PROPONENTE
  pcs = prcomp(df, scale = TRUE)
  autoplot(pcs, label = TRUE, label.size = 3, shape = FALSE, 
           loadings = TRUE, loadings.colour = 'blue',
           loadings.label = TRUE, loadings.label.size = 3)
  
  pr.var <- pcs$sdev^2
  pve <- pr.var / sum(pr.var)
  df = data.frame(x = 1:NROW(pve), y = cumsum(pve))
  ggplot(df, aes(x = x, y = y)) + 
    geom_point(size = 3) + 
    geom_line() + 
    labs(x='Principal Component', y = 'Cumuative Proportion of Variance Explained')
}

convenios.e = expande_convenios(convenios)

t = convenios.e %>% 
  group_by(NM_MUNICIPIO_PROPONENTE, UF_PROPONENTE, nome, cod7, ANO_CONVENIO) %>% 
  summarise(total = sum(total))
t$funcao.imputada = "TOTAL GERAL"

convenios.e = rbind(convenios.e, t)

# essa chamada demora!
resultado = computa_scores_para_todos(convenios.e, vizinhos)

r2 = resultado[resultado$total > 0, ] 
write.csv(resultado, "dist/data/diferencas-cidades-tudo.csv", row.names = FALSE)
write.csv(r2, "dist/data/diferencas-cidades.csv", row.names = FALSE)

# Explorando:  
x = cria_df_comparacao(convenios, vizinhos, 2503209)

x %>%
  ggplot(aes(x = funcao.imputada, y = total, colour = NM_MUNICIPIO_PROPONENTE)) + 
  geom_point() + theme_bw() + coord_flip()

x %>%
  ggplot(aes(x = funcao.imputada, y = zscore, colour = NM_MUNICIPIO_PROPONENTE)) + 
  geom_point() + theme_bw() + coord_flip()


# Emas : 2505907
