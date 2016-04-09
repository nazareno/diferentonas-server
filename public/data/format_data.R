#Formata as tabelas e separa os dados por anos

idhm<- read.csv("idhm-completo.csv", header=T,sep=";", dec=",",encoding="latin1")
idhm <- idhm[2:nrow(idhm), ]

idhm[,1]<-as.character(idhm[,1])
idhm$UF <- substr(idhm[,1],nchar(idhm[,1])-2,nchar(idhm[,1])-1)
idhm[,1] <- substr(idhm[,1],1,nchar(idhm[,1])-5)

populacao <- read.csv("populacao.csv", header=T,sep=";")
colnames(populacao) <- c("UF","COD.IBGE","Lugar","POP1991","POP1996","POP2000","POP2007","POP2010","x")

populacao$COD.IBGE <- substr(populacao$COD.IBGE, 1, nchar(populacao$COD.IBGE)-1)

merge_data <- merge(idhm, populacao, by="COD.IBGE")

idhm91 <- as.data.frame(cbind(merge_data$Lugar,merge_data$COD.IBGE.x,
                              merge_data$IDHM..1991.,merge_data$IDHM.Renda..1991.,merge_data$IDHM.Longevidade..1991.,
                              merge_data$IDHM.Educação..1991.,merge_data$POP1991))

colnames(idhm91) <- c("municipio","cod","idhm","idhm_renda","idhm_longev","idhm_edu","pop")

idhm00 <- as.data.frame(cbind(merge_data$Lugar,merge_data$COD.IBGE.x,
                              merge_data$IDHM..2000.,merge_data$IDHM.Renda..2000.,merge_data$IDHM.Longevidade..2000.,
                              merge_data$IDHM.Educação..2000.,merge_data$POP2000))

colnames(idhm00) <- c("municipio","cod","idhm","idhm_renda","idhm_longev","idhm_edu","pop")

idhm10 <- as.data.frame(cbind(as.character(merge_data$COD.IBGE),merge_data$Lugar.x,
                              merge_data$IDHM..2000.,merge_data$IDHM.Renda..2010.,merge_data$IDHM.Longevidade..2010.,
                              merge_data$IDHM.Educação..2010.,merge_data$POP2010))

colnames(idhm10) <- c("cod","municipio","idhm","idhm_renda","idhm_longev","idhm_edu","pop")

write.csv(idhm10,"dados2010.csv",row.names = F)
