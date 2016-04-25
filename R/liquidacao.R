library(reshape2)

liquidacao.lido <- read.csv("../hackfest-mj/data/16_Documento_Liquidacao.csv", sep=";")
liquidacao = liquidacao.lido %>% 
  filter(ANO_CONVENIO >= 2013) %>% 
  mutate_each(funs(currency2double), VL_BRUTO_DL, VL_LIQUIDO_DL) %>% 
  unique() # Há muitos documentos duplicados!

#######
# JOIN com ids do IBGE
#######
municipios = read.csv("dist/data/dados2010.csv")
# para pegar as UFs: 
populacao = read.csv2("dist/data/populacao.csv")
liquidacao = liquidacao %>% 
  mutate(nome = rm_accent(tolower(as.character(NM_MUNICIPIO_CONVENENTE))))
municipios = municipios %>% 
  mutate(nome = rm_accent(tolower(as.character(municipio))))
municipios = inner_join(municipios, 
                        select(populacao, Código, Município), 
                        by = c("cod7" = "Código"))

# Ficarão sem Id do IBGE:
liquidacao[!(liquidacao$nome %in% municipios$nome),] %>% 
  select(NM_MUNICIPIO_CONVENENTE) %>% unique() 

m.ids = municipios %>% select(nome, cod7, UF)
joined = inner_join(liquidacao, m.ids, by = c("nome" = "nome", "UF_CONVENENTE" = "UF"))
liquidacao = joined

liquidacao$modalidade = colsplit(string=liquidacao$NR_PROCESSO_DE_COMPRA, pattern="-", names=c("Part1", "Part2"))["Part2"]
liquidacao$modalidade = as.factor(liquidacao$modalidade$Part2)
liquidacao$dispensa = grepl("DISPENSA", liquidacao$modalidade)
## Fim do Join

## Análise das diferentonas nos beneficiários
jp = liquidacao %>% 
  filter(#cod7 %in% c(2507507, 2408102, 4113700), 
         NR_PROCESSO_DE_COMPRA != "",
         TX_ESFERA_ADM_CONVENENTE == "MUNICIPAL",
         nchar(as.character(CD_IDENTIF_FAVORECIDO_DL)) == 14,
         !(TP_DOCUMENTO_LIQUIDACAO %in% c("DIÁRIAS", 
                                          "RECIBO DE PAGAMENTO A AUTONOMO", 
                                          "FOLHA DE PAGAMENTO")))

por.municipio = jp %>% 
  group_by(NM_MUNICIPIO_CONVENENTE, cod7) %>% 
  summarise(total.pjs = sum(VL_BRUTO_DL), documentos.liquidacao = n())

dispensas = jp %>% 
  group_by(NM_MUNICIPIO_CONVENENTE, cod7, dispensa) %>% 
  summarise(total = sum(VL_BRUTO_DL)) %>% 
  mutate(prop = total / sum(total))  %>% 
  ungroup() %>% 
  select(-total) %>% 
  dcast(NM_MUNICIPIO_CONVENENTE + cod7 ~ dispensa, value.var = "prop", fill = 0)
names(dispensas)[3:4] = c("licitacao", "dispensa")

por.municipio = full_join(por.municipio, dispensas)

beneficiarios = jp %>% 
  group_by (NM_MUNICIPIO_CONVENENTE, cod7, NM_IDENTIF_FAVORECIDO_DL) %>% 
  summarise(recebido = sum(VL_BRUTO_DL)) %>% 
  arrange(-recebido) %>%
  summarise(beneficiarios.pjs = n(), 
            acima10K = sum(recebido > 1e4), 
            acima1M = sum(recebido > 1e6), 
            top10pc.quanto = sum(recebido[1:max(1,n()/10)]) / sum(recebido))

por.municipio = full_join(por.municipio, beneficiarios)

summary(por.municipio)
por.municipio %>% filter(documentos.liquidacao > 20) %>% ggpairs(alpha = .7)

write.csv(por.municipio, file = "dist/data/beneficiarios-pj-por-municipio.csv", row.names = FALSE)
