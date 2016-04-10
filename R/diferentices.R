library(ggplot2)
library(reshape2)
library(dplyr)

convenios = read.csv("dist/data/convenios-municipio-ccodigo.csv")
vizinhos = read.csv("dist/data/vizinhos.euclidiano.csv")

ve_conv <- function(convenios, cod) {
  c1 = convenios %>% filter(cod7 == cod, ANO_CONVENIO >= 2013)
  
  c1 %>% 
    slice(1) %>% 
    select(NM_MUNICIPIO_PROPONENTE, UF_PROPONENTE) %>% 
    print()
  
  print(paste("Total: ", sum(c1$total)))
  
  c1 %>% 
    group_by(NM_ORGAO_SUPERIOR) %>% 
    summarise(total = sum(total)) %>% 
    arrange(desc(total))
}

ve_conv(convenios, 2503209)
ve_conv(convenios, 4321329)

# cs %>% 
#   ggplot(aes(x = NM_ORGAO_SUPERIOR, fill = NM_ORGAO_SUPERIOR, weight = total)) + 
#   geom_bar() + 
#   facet_grid(NM_MUNICIPIO_PROPONENTE ~ .) + coord_flip()

pca_comparacao <- function(convenios, vizinhos, cod) {
  ids = vizinhos[vizinhos$origem == cod, 12:22]
  cs = convenios %>% filter(cod7 %in% ids, ANO_CONVENIO >= 2013)
  cs$cod7 = factor(cs$cod7)
  cs$NM_ORGAO_SUPERIOR = droplevels(cs$NM_ORGAO_SUPERIOR)
  
  cs.w = dcast(select(cs, cod7, NM_ORGAO_SUPERIOR, total), 
               formula = cod7 ~ NM_ORGAO_SUPERIOR, sum)
  
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


cria_df_comparacao <- function(cod, convenios, vizinhos) {
  ids = vizinhos[vizinhos$origem == cod, 12:22]
  cs = convenios %>% filter(cod7 %in% ids, ANO_CONVENIO >= 2013)
  cs$cod7 = droplevels(factor(cs$cod7))
  cs$NM_ORGAO_SUPERIOR = droplevels(cs$NM_ORGAO_SUPERIOR)
  cs = cs %>% 
    group_by(NM_ORGAO_SUPERIOR, cod7) %>% 
    summarise(total = sum(total)) %>% 
    mutate(zscore = (total - mean(total)) / sd(total)) 
  cs = cs %>% group_by(NM_ORGAO_SUPERIOR) %>% mutate(media = mean(total)) 
  cs[is.na(cs$zscore), "zscore"] = 1
  cs$origem = cod
  cs
}

resultado = data.frame(origem = c(), NM_ORGAO_SUPERIOR = c(), zscore = c(), total = c(), media = c())
for (id in vizinhos$origem){
  x = cria_df_comparacao(id, convenios, vizinhos) %>% 
  filter(origem == cod7) %>% 
  select(origem, NM_ORGAO_SUPERIOR, zscore, total, media) 
  resultado = rbind(resultado, x)
}

write.csv(resultado, "dist/data/diferencas-cidades.csv", row.names = FALSE)


# Explorando:  
x = cria_df_comparacao(convenios, vizinhos, 2503209)

x %>%
  ggplot(aes(x = NM_ORGAO_SUPERIOR, y = total, colour = NM_MUNICIPIO_PROPONENTE)) + 
  geom_point() + theme_bw() + coord_flip()

x %>%
  ggplot(aes(x = NM_ORGAO_SUPERIOR, y = zscore, colour = NM_MUNICIPIO_PROPONENTE)) + 
  geom_point() + theme_bw() + coord_flip()


# Emas : 2505907
