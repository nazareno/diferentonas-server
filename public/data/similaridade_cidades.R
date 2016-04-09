require("proxy")

dados2010 <- read.csv("/home/iara/diferentonas-server/public/data/dados2010.csv", header=T)

dados2010$pop <- (dados2010$pop - min(dados2010$pop))/(max(dados2010$pop) - min(dados2010$pop))

#distancia euclidiana
dist10 <- dist(dados2010[,3:7], method="euclidean")
df <- data.frame(as.matrix(dist10), row.names = dados2010$cod)
colnames(df) = dados2010$cod

#distancia com cos
dist10 <- dist(dados2010[,3:7], method="cosine")
df <- data.frame(as.matrix(dist10), row.names = dados2010$cod)
colnames(df) = dados2010$cod

