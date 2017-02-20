status.antigos <- c("",
                    "Proposta Aprovada e Plano de Trabalho Complementado em Análise",
                    "Proposta Aprovada e Plano de Trabalho Complementado enviado para Análise",
                    "Proposta Aprovada e Plano de Trabalho em Análise",
                    "Proposta Aprovada e Plano de Trabalho em Complementação",
                    "Proposta/Plano de Trabalho Aprovados",
                    "Proposta/Plano de Trabalho Aprovado",
                    "Proposta/Plano de Trabalho complementado em Análise",
                    "Proposta/Plano de Trabalho complementado envida para Análise",
                    "Proposta/Plano de Trabalho em Análise",
                    "Proposta/Plano de Trabalho em Complementação",
                    "Proposta/Plano de Trabalho enviado para Análise",
                    "Assinatura Pendente Registro TV Siafi",
                    "Assinado",
                    "Em execução",
                    "Aguardando Prestação de Contas",
                    "Prestação de Contas enviada para Análise",
                    "Prestação de Contas em Análise",
                    "Prestação de Contas em Complementação",
                    "Prestação de Contas Aprovada",
                    "Prestação de Contas Aprovada com Ressalvas",
                    "Prestação de Contas Rejeitada", 
                    "Convênio Anulado", 
                    "Inadimplente")

status.novos <- c("Não informado",
                  rep("Não iniciada", 13),
                  "Em andamento",
                  rep("Concluída, segundo a prefeitura", 4),
                  "Aprovada pelo Governo Federal",
                  "Aprovada pelo Governo Federal com ressalvas",
                  "Rejeitada pelo Governo Federal", 
                  "Anulado",
                  "Inadimplente")

traduz_termos = function(iniciativas){
  library(futile.logger)
  library(dplyr, warn.conflicts = FALSE)
  flog.info("Traduzindo termos, formatando strings")
  status <- tibble(SIT_CONVENIO = status.antigos, TX_STATUS = status.novos)
  ## TODO Não entendo porque o left_join não funciona mas o merge sim. 
  # Gerando um exemplo mínimo com dput, passa a funcionar...
  # novas.iniciativas <- left_join(iniciativas, status, by = "SIT_CONVENIO")
  novas.iniciativas = merge(iniciativas, status, by = "SIT_CONVENIO", all.x = TRUE)
  
  # Tornando as palavras capitalizadas
  modificar.palavra <- function(palavra) {
    if (nchar(palavra) > 3)
      return(paste(toupper(substr(palavra, 1, 1)), tolower(substr(palavra, 2, nchar(palavra))), sep = ""))
    return(tolower(palavra))
  }
  
  capitalizar <- function(frase) {
    palavras <- unlist(strsplit(as.character(frase), " "))
    return(paste(sapply(palavras, modificar.palavra), collapse = " "))
  }
  
  novas.iniciativas = novas.iniciativas %>% 
    mutate(OBJETO_PROPOSTA = ifelse(is.na(OBJETO_PROPOSTA), 
                                    "Sem descrição cadastrada", 
                                    OBJETO_PROPOSTA))
  novas.iniciativas$OBJETO_PROPOSTA <- sapply(novas.iniciativas$OBJETO_PROPOSTA, capitalizar)
  novas.iniciativas$DESC_ORGAO_SUP <- sapply(novas.iniciativas$DESC_ORGAO_SUP, capitalizar)
  
  return(novas.iniciativas)
}
