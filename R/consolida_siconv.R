
consolida_convenios <- function(data_dir){
  require("dplyr", warn.conflicts = F)
  require("futile.logger")
  # Convenios
  flog.info("lendo arquivo de convênios do siconv")
  convenios_siconv = readr::read_csv2(paste0(data_dir, "/siconv_convenio.csv"))
  names(convenios_siconv)[1] = "NR_CONVENIO" # Há um caracter desconhecido vindo início do arquivo
  # Propostas (contém o proponente)
  flog.info("lendo arquivo de propostas do siconv")
  propostas = readr::read_csv2(paste0(data_dir, "/siconv_proposta.csv"))
  names(propostas)[1] = "ID_PROPOSTA" # Há um caracter desconhecido vindo início do arquivo
  convenios_siconv = left_join(convenios_siconv,
                               propostas,
                               by = c("ID_PROPOSTA"))
  flog.info("convênios carregados")
  return(convenios_siconv)
}

carrega_e_limpa_historicos <- function(data_dir, ids_conhecidos){
  require("dplyr", warn.conflicts = F)
  futile.logger::flog.info("lendo arquivo de histórico das propostas")
  historico = readr::read_csv2(paste0(data_dir, "/siconv_historico_situacao.csv"))
  names(historico)[1] = "ID_PROPOSTA" # Há um caracter desconhecido vindo início do arquivo
  historico = historico %>% 
    filter(NR_CONVENIO %in% ids_conhecidos) %>% 
    unique() %>% 
    mutate(data = lubridate::dmy(DIA_HISTORICO_SIT)) %>% 
    arrange(COD_HISTORICO_SIT, data)
  return(historico)
}