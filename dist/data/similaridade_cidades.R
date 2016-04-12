require("proxy")
library("FastKNN")
library("dplyr")

acha_vizinhos = function(dist10, k, dados2010){
  n = dim(dist10)[1]
  nn = matrix(0, n, k) # n x k
  for (i in 1:n)
    nn[i,] = k.nearest.neighbors(i, dist10, k = k)
  df = as.data.frame(nn)
  neighours = df %>% 
    mutate_each(funs(nome = paste(dados2010$municipio[.], dados2010$UF[.])))
  names(neighours) <- paste0(names(neighours), ".nome")
  neighours$origem.nome = paste(dados2010$municipio, dados2010$UF)
  neighours$origem = dados2010$cod7
  neighbours.cod = df %>% 
    mutate_each(funs(nome = paste(dados2010$cod7[.])))
  neighours = cbind(neighours, neighbours.cod)
  neighours
}

dados2010 <- read.csv("dist/data/dados2010.csv", header=T)

quantitativos = dados2010 %>% 
  select(6:9) %>% 
  mutate(pop = log(pop)) %>% 
  mutate_each(funs(scale))

k=10
  
#distancia euclidiana
dist10 <- as.matrix(dist(quantitativos, method="euclidean"))
vizinhos.euclides = acha_vizinhos(dist10, k, dados2010)

#distancia com cos
dist10.cos <- as.matrix(dist(quantitativos, method="cosine"))
vizinhos.cosseno = acha_vizinhos(dist10.cos, k, dados2010)

vizinhos.euclides %>% 
  write.csv("dist/data/vizinhos.euclidiano.csv", row.names = FALSE)

vizinhos.cosseno %>% 
  write.csv("dist/data/vizinhos.cosseno.csv", row.names = FALSE)

# library(jsonlite)
# write(toJSON(select(neighours, origem.nome, origem)), "todasascidades.json")
