library(dplyr)
source("R/diferentonas-utils.R")
source("R/load-convenios.R")

# TODO receber parâmetro da linha de comando
message("Carregando convênios")
convprog = load_convenios()

## seleciona e salva apenas os convênios propostos no âmbito municipal
# salvamos filtragens em arquivos intermediários pra facilitar o uso interativo do script
message("Filtrando")
convprog %>%
  filter(ANO_PROPOSTA >= 2013,
         !(TX_SITUACAO %in% c("Proposta/Plano de Trabalho Cancelados", 
                              "Proposta/Plano de Trabalho Rejeitados")), 
         !(TX_ESFERA_ADM_PROPONENTE %in% c("ESTADUAL", "FEDERAL"))) %>%
  select(-ANO_PROPOSTA, 
         -NR_PROPOSTA, 
         -TX_ESFERA_ADM_PROPONENTE, 
         -CD_IDENTIF_PROPONENTE, 
         -65:-51) %>% 
  write.csv(file = "dist/data/convenios-por-municipio-detalhes.csv", row.names = FALSE)

convprog %>%
  filter(ANO_PROPOSTA >= 2013,
         !(TX_SITUACAO %in% c("Proposta/Plano de Trabalho Cancelados", 
                              "Proposta/Plano de Trabalho Rejeitados")), 
         !(TX_ESFERA_ADM_PROPONENTE %in% c("ESTADUAL", "FEDERAL"))) %>%
  select(NM_MUNICIPIO_PROPONENTE, UF_PROPONENTE, VL_REPASSE, NM_ORGAO_SUPERIOR, ANO_CONVENIO) %>%
  group_by(NM_MUNICIPIO_PROPONENTE, UF_PROPONENTE, NM_ORGAO_SUPERIOR, ANO_CONVENIO) %>%
  summarise(total = sum(VL_REPASSE)) %>% 
  write.csv(file = "dist/data/convenios-por-municipio.csv", row.names = FALSE)

## Join:

# 1. Dados dos convênios no SICONV:
convenios = read.csv("dist/data/convenios-por-municipio.csv")
convenios.d = read.csv("dist/data/convenios-por-municipio-detalhes.csv")

# 2. Dados do IBGE e IDH
municipios = read.csv("dist/data/dados2010.csv")
# para pegar as UFs: 
populacao = read.csv2("dist/data/populacao.csv")

# 3. Dados do SIAFI
# Esses não tem função! ?
# TODO receber da linha de comando
arquivo_siafi = "dados-externos/convenios-siafi-em-201605.csv"
siafi <- read.csv(arquivo_siafi, sep=";")
siafi$Número.Convênio = as.integer(as.character(siafi$Número.Convênio))
names(siafi)[1] = "numero.convenio"
siafi = siafi[!duplicated(select(siafi, 1, 5, 7, 9, 13)),]

convenios = convenios %>% 
  mutate(nome = rm_accent(tolower(as.character(NM_MUNICIPIO_PROPONENTE))))
convenios.d = convenios.d %>% 
  mutate(nome = rm_accent(tolower(as.character(NM_MUNICIPIO_PROPONENTE))))

municipios = municipios %>% 
  mutate(nome = rm_accent(tolower(as.character(municipio))))

municipios = inner_join(municipios, 
                        select(populacao, Código, Município), 
                        by = c("cod7" = "Código"))

# TODO só temos códigos para ~5300 municípios. Faltam uns 300 mais para todos do Brasil. Nos SICONV temos ~15 a mais que esse número.
NROW(levels(municipios$municipio))
NROW(levels(convenios$NM_MUNICIPIO_PROPONENTE))
convenios[!(convenios$nome %in% municipios$nome),] %>% 
  select(NM_MUNICIPIO_PROPONENTE) %>% unique() %>% NROW()

m.ids = municipios %>% select(nome, cod7, UF)

joined = inner_join(convenios, m.ids, by = c("nome" = "nome", "UF_PROPONENTE" = "UF"))
joined.d = inner_join(convenios.d, m.ids, by = c("nome" = "nome", "UF_PROPONENTE" = "UF"))
joined.du = unique(joined.d)
# TODO Aqui guardamos apenas a última menção ao convênio. Quando quisermos histórico, rever isso.
joined.du = joined.du[-which(duplicated(joined.du$NR_CONVENIO, fromLast = TRUE)),]

message(paste(NROW(joined.du), " convênios do SICONV"))
summary(joined.du$NR_CONVENIO %in% siafi$numero.convenio)
joined.du.siafi = joined.du %>% left_join(select(siafi, numero.convenio, 6:10), 
                                          by = c("NR_CONVENIO" = "numero.convenio"))

message(paste(NROW(joined.du.siafi), " convênios após cruzar com SIAFI"))
message(paste(sum(is.na(joined.du.siafi$Nome.Funcao)), " convênios sem função orçamentária após cruzar SICONV e SIAFI"))

write.csv(joined, "dist/data/convenios-municipio-ccodigo.csv", row.names = FALSE)
write.csv(joined.du.siafi, "dist/data/convenios-municipio-detalhes-ccodigo.csv", row.names = FALSE)
