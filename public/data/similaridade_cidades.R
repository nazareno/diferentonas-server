require("proxy")
library("FastKNN")

acha_vizinhos = function(dist10, k, dados2010){
  n = dim(dist10)[1]
  nn = matrix(0, n, k) # n x k
  for (i in 1:n)
    nn[i,] = k.nearest.neighbors(i, dist10, k = k)
  df = as.data.frame(nn)
  neighours = df %>% 
    mutate(n1 = dados2010$cod7[V1], 
           n1.nome = paste(dados2010$municipio[V1], dados2010$UF[V1]),
           n2 = dados2010$cod7[V2], 
           n2.nome = paste(dados2010$municipio[V2], dados2010$UF[V2]),
           n3 = dados2010$cod7[V3], 
           n3.nome = paste(dados2010$municipio[V3], dados2010$UF[V3]),
           n4 = dados2010$cod7[V4], 
           n5 = dados2010$cod7[V5], 
           n6 = dados2010$cod7[V6])
  neighours$origem = dados2010$cod7
  neighours$origem.nome = paste(dados2010$municipio, dados2010$UF)
  neighours
}

dados2010 <- read.csv("public/data/dados2010.csv", header=T)

quantitativos = dados2010 %>% 
  select(6:9) %>% 
  mutate_each(funs(scale))

k=6
  
#distancia euclidiana
dist10 <- as.matrix(dist(quantitativos, method="euclidean"))
vizinhos.euclides = acha_vizinhos(dist10, k, dados2010)

#distancia com cos
dist10.cos <- as.matrix(dist(quantitativos, method="cosine"))
vizinhos.cosseno = acha_vizinhos(dist10.cos, k, dados2010)

vizinhos.euclides %>% 
  select(7, 9, 11, 13:16, 17, 8, 10, 12) %>% 
  write.csv("public/data/vizinhos.euclidiano.csv", row.names = FALSE)

vizinhos.cosseno %>% 
  select(7, 9, 11, 13:16, 17, 8, 10, 12) %>% 
  write.csv("public/data/vizinhos.cosseno.csv", row.names = FALSE)

library(jsonlite)
toJSON(select(neighours, origem.nome, origem))
