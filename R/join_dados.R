source("R/diferentonas-utils.R")

#' 
#' Recebe as iniciativas de um arquivo baixado do siconv e 
#' descarta as partes que não utilizamos no Diferentonas. 
#' 
filtra_convenios = function(convenios) {
  require(dplyr, warn.conflicts = FALSE)
  futile.logger::flog.info("Antes de filtrar: %d convênios", NROW(convenios))
  
  ## seleciona e salva apenas os convênios propostos no âmbito municipal
  futile.logger::flog.info("Filtrando aqueles depois de 2013 e celebrados por municípios")
  t = convenios %>%
    filter(ANO_PROP >= 2013,
           NATUREZA_JURIDICA == "Administração Pública Municipal") %>%
    select(-NATUREZA_JURIDICA)
  
  futile.logger::flog.info(paste("Filtrados são ", NROW(convenios), "convênios"))
  return(t)
}

#' 
#' Principal função dessa biblioteca. Cruza os dados de SICONV, SIAFI, 
#' IDH e população e produz um dataframe (salvo em arquivo) com os dados 
#' de iniciativas todos que usamos no Diferentonas. 
#' 
cruza_dados = function(convenios_siconv, 
                       arquivo_siafi,
                       arquivo_idh = "dist/data/dados2010.csv", 
                       arquivo_populacao = "dist/data/populacao.csv"){
  require(dplyr, warn.conflicts = FALSE)
  require(futile.logger)
  # --------------------------------------
  # Primeiro carregar todo mundo
  # --------------------------------------
  
  # 1. Dados dos convênios no SICONV 
  convenios.d = filtra_convenios(convenios_siconv)
  
  # 2. Dados do IBGE e IDH
  municipios = readr::read_csv(arquivo_idh)
  flog.info("Carreguei dados do de código e IDH")
  # para pegar as UFs: 
  populacao = readr::read_csv2(arquivo_populacao)
  names(populacao)[1:3] = c("Sigla", "Codigo", "Municipio") # lidar com https://github.com/hadley/dplyr/issues/848
  flog.info("Carreguei dados de UF")
  
  # 3. Dados do SIAFI
  flog.info(paste("Carregando dados do SIAFI de", arquivo_siafi))
  siafi <- readr::read_csv2(arquivo_siafi)
  names(siafi)[14] = "numero.convenio"
  siafi$numero.convenio = as.integer(siafi$numero.convenio)

  # -------------------
  # Cruzar dados
  # -------------------
  
  # Primeiro SICONV x IDH e população
  joined.du = cruza_siconv_idh_populacao(convenios.d, municipios, populacao)
  
  flog.info("%d convênios nos dados cruzados SICONV x IBGE", NROW(joined.du))
  
  # -------------------
  # SICONV x SIAFI
  # -------------------
  joined.du.siafi = joined.du %>% left_join(siafi %>% select(numero.convenio, 
                                                             `Nome Programa`,     
                                                             `Codigo Programa`,
                                                             `Nome Funcao`,
                                                             `Codigo Funcao`), 
                                            by = c("NR_CONVENIO" = "numero.convenio"))
  
  flog.info(paste(NROW(joined.du.siafi), " convênios após cruzar com SIAFI"))
  flog.info(paste(sum(is.na(joined.du.siafi$`Nome Funcao`)), " convênios do SICONV sem função orçamentária especificada no SIAFI"))
  
  # PREENCHER FUNÇÃO PARA CONVÊNIOS AUSENTES DO SIAFI
  todos_os_convenios = convenios_siconv %>% left_join(siafi %>% select(numero.convenio, 
                                                                       `Nome Programa`,     
                                                                       `Codigo Programa`,
                                                                       `Nome Funcao`,
                                                                       `Codigo Funcao`), 
                                                      by = c("NR_CONVENIO" = "numero.convenio"))
  joined.siafi.imputado = imputa_funcoes_orcamentarias(joined.du.siafi, todos_os_convenios)
  
  return(joined.siafi.imputado)
}

cruza_siconv_idh_populacao = function(convenios.d, municipios, populacao) {
  require(dplyr, warn.conflicts = FALSE)
  convenios.d = convenios.d %>%
    mutate(nome = rm_accent(tolower(as.character(MUNIC_PROPONENTE))))
  municipios = municipios %>%
    mutate(nome = rm_accent(tolower(as.character(municipio))))
  
  municipios = inner_join(municipios,
                          select(populacao, Codigo, Municipio),
                          by = c("cod7" = "Codigo"))
  
  m.ids = municipios %>% select(nome, cod7, UF)
  
  joined.d = inner_join(convenios.d,
                        m.ids,
                        by = c("nome" = "nome", "UF_PROPONENTE" = "UF"))
  faltou = anti_join(convenios.d,
                     m.ids,
                     by = c("nome" = "nome", "UF_PROPONENTE" = "UF")) %>% select(nome, UF_PROPONENTE) %>% unique()
  if (NROW(faltou) > 0) {
    warning(NROW(faltou),
            " municípios não encontrados nos dados do IBGE: ",
            paste(paste(
              faltou$nome, "-", faltou$UF_PROPONENTE, " "
            )))
  }
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
imputa_funcoes_orcamentarias <- function(joined.du.siafi, todos_os_convenios){
  require(dplyr, warn.conflicts = FALSE)
  mapa.funcoes = criar_mapa(todos_os_convenios)
  joined.siafi.imputado = left_join(joined.du.siafi, mapa.funcoes, by = "DESC_ORGAO_SUP")
  
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
  require(dplyr, warn.conflicts = FALSE)
  
  resposta = df %>% 
    filter(!is.na(`Nome Funcao`)) %>%
    group_by(DESC_ORGAO_SUP, `Nome Funcao`) %>% 
    tally() %>% 
    arrange(-n) %>% 
    slice(1) %>% 
    ungroup() %>% 
    select(-n)
  # Lidando com ministérios sem convênio com função em nenhum convênio do SIAFI
  sem_convenios = data_frame(DESC_ORGAO_SUP = c("JUSTICA ELEITORAL", 
                                                "MINISTERIO DE MINAS E ENERGIA", 
                                                "MINISTERIO DOS TRANSPORTES",
                                                "MINISTÉRIO DA PESCA E AQUICULTURA", 
                                                "MINISTERIO DAS RELACOES EXTERIORES", 
                                                "MIN.DAS MULH., DA IG.RACIAL E DOS DIR.HUMANOS", 
                                                "MINISTERIO DO TRABALHO E EMPREGO"), 
                             `Nome Funcao` = c("Administração", 
                                               "Energia", 
                                               "Transporte", 
                                               "Agricultura", 
                                               "Relações Exteriores", 
                                               "Direitos da Cidadania", 
                                               "Trabalho"))
  resposta = rbind(resposta, filter(sem_convenios, !(DESC_ORGAO_SUP %in% resposta$DESC_ORGAO_SUP)))
  names(resposta)[2] = "funcao.imputada"
  return(resposta)
}
