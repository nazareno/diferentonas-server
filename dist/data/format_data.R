#Formata as tabelas e separa os dados por anos

idhm<- read.csv("idhm-completo.csv", header=T,sep=";", dec=",",encoding="latin1")

# remove linha do total do Brasil
idhm <- idhm[2:nrow(idhm), ]

# filtra colunas importantes
idhm <- idhm %>% select(c(2, 5, 8, 11, 14))
colnames(idhm) <- c("cod6", "idhm","idhm_renda","idhm_longev","idhm_edu")


populacao <- read.csv("populacao.csv", header=T,sep=";") %>% select(c(1,2,3,8))
colnames(populacao) <- c("UF","cod7","municipio","pop")
populacao <- populacao %>% mutate(cod6 = substr(cod7, 1, nchar(cod7)-1))

data <- merge(idhm, populacao, by=("cod6"))

coordenadas <- read.csv("coordenadas.csv", header = T)
colnames(coordenadas) <- c("cod7","long","lat","alt")

data <- merge(data, coordenadas, by=("cod7"))

data <- data[, c("cod6", "cod7", "municipio", "UF", "idhm","idhm_renda","idhm_longev","idhm_edu","pop","lat","long","alt")]

write.csv(data,"dados2010.csv",row.names = F)
