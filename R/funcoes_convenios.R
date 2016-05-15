library(dplyr)

transferencias <- read.delim("dados-externos/transparenciabrasil/utf8-201603_Transferencias.csv")
transferencias = transferencias %>% filter(! is.na(Número.Convênio))
