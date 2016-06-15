library(dplyr)
source("R/diferentonas-utils.R")
source("R/load-convenios.R")

carrega_e_filtra_convenios = function(arquivo_siconv){
  message(paste("Carregando convênios de", arquivo_siconv))
  convprog = load_convenios(arquivo_siconv)
  message(paste("Carreguei", NROW(convprog), "convênios"))
  
  ## seleciona e salva apenas os convênios propostos no âmbito municipal
  message("Filtrando")
  t = convprog %>%
    filter(ANO_PROPOSTA >= 2013,
           !(TX_SITUACAO %in% c("Proposta/Plano de Trabalho Cancelados", 
                                "Proposta/Plano de Trabalho Rejeitados")), 
           !(TX_ESFERA_ADM_PROPONENTE %in% c("ESTADUAL", "FEDERAL"))) %>%
    select(-ANO_PROPOSTA, 
           -NR_PROPOSTA, 
           -TX_ESFERA_ADM_PROPONENTE, 
           -CD_IDENTIF_PROPONENTE) 
  return(t)
}

cruza_dados = function(arquivo_siconv, 
                       arquivo_siafi,
                       arquivo_saida = "dist/data/convenios-municipio-detalhes-ccodigo.csv"){
  convenios.d = carrega_e_filtra_convenios(arquivo_siconv)
  message(paste("Filtrados são ", NROW(convenios.d), "convênios"))
  # pra facilitar o uso interativo do script:
  # convenios.d %>% write.csv(file = "dist/data/convenios-por-municipio-detalhes.csv", row.names = FALSE)
  # convenios.d = read.csv("dist/data/convenios-por-municipio-detalhes.csv")
  
  ## Join:
  
  # 1. Dados dos convênios no SICONV estão já em convenios.d
  
  # 2. Dados do IBGE e IDH
  municipios = read.csv("dist/data/dados2010.csv")
  message("Carreguei dados do de código e IDH")
  # para pegar as UFs: 
  populacao = read.csv2("dist/data/populacao.csv")
  names(populacao)[1:3] = c("Sigla", "Codigo", "Municipio") # lidar com https://github.com/hadley/dplyr/issues/848
  message("Carreguei dados de UF")
  
  # 3. Dados do SIAFI
  message(paste("Carregando dados do SIAFI de", arquivo_siafi))
  siafi <- read.csv(arquivo_siafi, sep=";")
  siafi$Número.Convênio = as.integer(as.character(siafi$Número.Convênio))
  names(siafi)[1] = "numero.convenio"
  siafi = siafi[!duplicated(select(siafi, 1, 13), fromLast = TRUE),]
  
  convenios.d = convenios.d %>% 
    mutate(nome = rm_accent(tolower(as.character(NM_MUNICIPIO_PROPONENTE))))
  
  municipios = municipios %>% 
    mutate(nome = rm_accent(tolower(as.character(municipio))))
  
  municipios = inner_join(municipios, 
                          select(populacao, Codigo, Municipio), 
                          by = c("cod7" = "Codigo"))
  
  # TODO só temos códigos para ~5300 municípios. Faltam uns 300 mais para todos do Brasil. Nos SICONV temos ~15 a mais que esse número.
  NROW(unique(municipios$cod7))
  NROW(unique(paste(convenios.d$NM_MUNICIPIO_PROPONENTE, convenios.d$UF_PROPONENTE)))
  
  m.ids = municipios %>% select(nome, cod7, UF)
  
  joined.d = inner_join(convenios.d, m.ids, by = c("nome" = "nome", "UF_PROPONENTE" = "UF"))
  joined.du = unique(joined.d)
  # TODO Aqui guardamos apenas a última menção ao convênio. Quando quisermos histórico, rever isso.
  joined.du = joined.du[-which(duplicated(joined.du$NR_CONVENIO, fromLast = TRUE)),]
  
  message(paste(NROW(joined.du), " convênios do SICONV"))
  summary(joined.du$NR_CONVENIO %in% siafi$numero.convenio)
  joined.du.siafi = joined.du %>% left_join(select(siafi, numero.convenio, 6:10), 
                                            by = c("NR_CONVENIO" = "numero.convenio"))
  
  message(paste(NROW(joined.du.siafi), " convênios após cruzar com SIAFI"))
  message(paste(sum(is.na(joined.du.siafi$Nome.Funcao)), " convênios do SICONV sem função orçamentária especificada no SIAFI"))
  
  ### PREENCHER FUNÇÃO PARA CONVÊNIOS AUSENTES DO SIAFI
  criar_mapa = function(df){
    resposta = df %>% 
      filter(!is.na(Nome.Funcao)) %>%
      group_by(NM_ORGAO_SUPERIOR, Nome.Funcao) %>% 
      tally() %>% 
      arrange(-n) %>% 
      slice(1) %>% 
      ungroup() %>% 
      select(-n)
    # Lidando com ministérios sem convênio com função em nenhum convênio do SIAFI
    resposta = rbind(resposta, 
                     data.frame(NM_ORGAO_SUPERIOR = c("JUSTICA ELEITORAL", 
                                                      "MINISTERIO DE MINAS E ENERGIA", 
                                                      "MINISTERIO DOS TRANSPORTES"), 
                                Nome.Funcao = c("Administração", 
                                                "Energia", 
                                                "Transporte")))
    names(resposta)[2] = "funcao.imputada"
    return(resposta)
  }
  
  mapa.funcoes = criar_mapa(joined.du.siafi)
  joined.siafi.imputado = left_join(joined.du.siafi, mapa.funcoes)
  
  joined.siafi.imputado = joined.siafi.imputado %>% 
    rowwise() %>% 
    mutate(funcao.imputada = ifelse(is.na(Nome.Funcao), 
                                    as.character(funcao.imputada), 
                                    as.character(Nome.Funcao))) %>%
    ungroup() %>% 
    mutate(funcao.imputada = as.factor(funcao.imputada))
  
  write.csv(joined.siafi.imputado, arquivo_saida, row.names = FALSE)
}
