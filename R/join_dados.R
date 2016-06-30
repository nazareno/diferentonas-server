library(dplyr, warn.conflicts = FALSE)
library(readr)
source("R/diferentonas-utils.R")

#' 
#' Carrega as iniciativas de um arquivo baixado do siconv e 
#' descarta as partes que não utilizamos no Diferentonas. 
#' 
carrega_e_filtra_convenios = function(arquivo_siconv){
  message(paste("Carregando convênios de", arquivo_siconv))
  convprog = readr::read_csv2(arquivo_siconv)
  convprog = converte_dbls_currency(convprog)
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

#' 
#' Principal função dessa biblioteca. Cruza os dados de SICONV, SIAFI, 
#' IDH e população e produz um dataframe (salvo em arquivo) com os dados 
#' de iniciativas todos que usamos no Diferentonas. 
#' 
cruza_dados = function(arquivo_siconv, 
                       arquivo_siafi,
                       arquivo_idh = "dist/data/dados2010.csv", 
                       arquivo_populacao = "dist/data/populacao.csv"){
  convenios.d = carrega_e_filtra_convenios(arquivo_siconv)
  message(paste("Filtrados são ", NROW(convenios.d), "convênios"))
  # pra facilitar o uso interativo do script:
  # convenios.d %>% write.csv(file = "dist/data/convenios-por-municipio-detalhes.csv", row.names = FALSE)
  # convenios.d = read.csv("dist/data/convenios-por-municipio-detalhes.csv")
  
  # --------------------------------------
  # Primeiro carregar todo mundo
  # --------------------------------------
  
  # 1. Dados dos convênios no SICONV estão já em convenios.d
  
  # 2. Dados do IBGE e IDH
  municipios = read_csv(arquivo_idh)
  message("Carreguei dados do de código e IDH")
  # para pegar as UFs: 
  populacao = read_csv2(arquivo_populacao)
  names(populacao)[1:3] = c("Sigla", "Codigo", "Municipio") # lidar com https://github.com/hadley/dplyr/issues/848
  message("Carreguei dados de UF")
  
  # 3. Dados do SIAFI
  message(paste("Carregando dados do SIAFI de", arquivo_siafi))
  siafi <- read_csv2(arquivo_siafi)
  names(siafi)[1] = "numero.convenio"
  siafi$numero.convenio = as.integer(siafi$numero.convenio)
  siafi = siafi[!duplicated(select(siafi, 1, 13), fromLast = TRUE),]

  # -------------------
  # Cruzar dados
  # -------------------
  
  # Primeiro SICONV x IDH e população
  joined.d = cruza_siconv_idh_populacao(convenios.d, municipios, populacao)
  joined.du = unique(joined.d)
  # Aqui guardamos apenas a última menção ao convênio. 
  # O histórico é feito a partir desses snapshots.
  joined.du = joined.du[-which(duplicated(joined.du$NR_CONVENIO, fromLast = TRUE)),]
  
  message(paste(NROW(joined.du), " convênios do SICONV"))
  
  # -------------------
  # SICONV x SIAFI
  # -------------------
  joined.du.siafi = joined.du %>% left_join(select(siafi, numero.convenio, 6:10), 
                                            by = c("NR_CONVENIO" = "numero.convenio"))
  
  message(paste(NROW(joined.du.siafi), " convênios após cruzar com SIAFI"))
  message(paste(sum(is.na(joined.du.siafi$`Nome Funcao`)), " convênios do SICONV sem função orçamentária especificada no SIAFI"))
  
  # PREENCHER FUNÇÃO PARA CONVÊNIOS AUSENTES DO SIAFI
  joined.siafi.imputado = imputa_funcoes_orcamentarias(joined.du.siafi)
  
  return(joined.siafi.imputado)
}

cruza_siconv_idh_populacao = function(convenios.d, municipios, populacao){
  convenios.d = convenios.d %>% 
    mutate(nome = rm_accent(tolower(as.character(NM_MUNICIPIO_PROPONENTE))))
  municipios = municipios %>% 
    mutate(nome = rm_accent(tolower(as.character(municipio))))
  
  municipios = inner_join(municipios, 
                          select(populacao, Codigo, Municipio), 
                          by = c("cod7" = "Codigo"))
  
  # TODO só temos códigos para ~5300 municípios. Faltam uns 300 mais para todos do Brasil. 
  # Nos SICONV temos ~15 a mais que esse número.
  # NROW(unique(municipios$cod7))
  # NROW(unique(paste(convenios.d$NM_MUNICIPIO_PROPONENTE, convenios.d$UF_PROPONENTE)))
  
  m.ids = municipios %>% select(nome, cod7, UF)
  
  joined.d = inner_join(convenios.d, m.ids, by = c("nome" = "nome", "UF_PROPONENTE" = "UF"))
  return(joined.d)
}

#' 
#' Cria um data.frame com funções orçamentárias para todas as iniciativas, 
#' incluindo as que não tem função orçamentária registrada no SIAFI. Para 
#' isso, considera que todas as iniciativas com recursos originando de um 
#' mesmo ministério têm a função orçamentária mais comum nas iniciativas 
#' daquele ministério. Para os ministérios onde não temos dados para essa 
#' inferência, escolhemos funções junto aos técnicos do MPOG e MJ.
#' 
imputa_funcoes_orcamentarias <- function(joined.du.siafi){
  mapa.funcoes = criar_mapa(joined.du.siafi)
  joined.siafi.imputado = left_join(joined.du.siafi, mapa.funcoes)
  
  joined.siafi.imputado = joined.siafi.imputado %>%
    rowwise() %>%
    mutate(funcao.imputada = ifelse(
      is.na(`Nome Funcao`),
      ifelse(
        is.na(funcao.imputada),
        "Outros",
        as.character(funcao.imputada)
      ),
      as.character(`Nome Funcao`)
    )) %>%
    ungroup() %>% 
    mutate(funcao.imputada = as.factor(funcao.imputada))
  
  return(joined.siafi.imputado)
}

#' Cria um mapa de nomes de ministérios que são órgãos superiores de 
#' convênios no SICONV para nomes de funções orçamentárias do Gov 
#' Federal. 
#' 
#' Para isso, recebe um dataframe que já tem os dados de SICONV e 
#' SIAFI cruzados. Esse df possui pelo menos uma coluna "Nome.funcao"  
#' e uma chamada "NM_ORGAO_SUPERIOR". A primeira do SIAFI e a segunda
#' do SICONV.
criar_mapa = function(df){
  resposta = df %>% 
    filter(!is.na(`Nome Funcao`)) %>%
    group_by(NM_ORGAO_SUPERIOR, `Nome Funcao`) %>% 
    tally() %>% 
    arrange(-n) %>% 
    slice(1) %>% 
    ungroup() %>% 
    select(-n)
  # Lidando com ministérios sem convênio com função em nenhum convênio do SIAFI
  resposta = rbind(resposta, 
                   data_frame(NM_ORGAO_SUPERIOR = c("JUSTICA ELEITORAL", 
                                                    "MINISTERIO DE MINAS E ENERGIA", 
                                                    "MINISTERIO DOS TRANSPORTES",
                                                    "MINISTÉRIO DA PESCA E AQUICULTURA", 
                                                    "MINISTERIO DAS RELACOES EXTERIORES", 
                                                    "MIN.DAS MULH., DA IG.RACIAL E DOS DIR.HUMANOS"), 
                              `Nome Funcao` = c("Administração", 
                                              "Energia", 
                                              "Transporte", 
                                              "Agricultura", 
                                              "Relações Exteriores", 
                                              "Direitos da Cidadania")))
  names(resposta)[2] = "funcao.imputada"
  return(resposta)
}
