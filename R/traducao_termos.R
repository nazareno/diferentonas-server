

iniciativas <- read.csv('../dist/data/convenios-municipio-detalhes-ccodigo.csv')

status.antigos <- c('',
                    'Proposta Aprovada e Plano de Trabalho Complementado em Análise',
                    'Proposta Aprovada e Plano de Trabalho Complementado enviado para Análise',
                    'Proposta Aprovada e Plano de Trabalho em Análise',
                    'Proposta Aprovada e Plano de Trabalho em Complementação',
                    'Proposta/Plano de Trabalho Aprovados',
                    'Proposta/Plano de Trabalho complementado em Análise',
                    'Proposta/Plano de Trabalho complementado envida para Análise',
                    'Proposta/Plano de Trabalho em Análise',
                    'Proposta/Plano de Trabalho em Complementação',
                    'Proposta/Plano de Trabalho enviado para Análise',
                    'Assinado',
                    'Em execução',
                    'Aguardando Prestação de Contas',
                    'Prestação de Contas enviada para Análise',
                    'Prestação de Contas em Análise',
                    'Prestação de Contas em Complementação',
                    'Prestação de Contas Aprovada',
                    'Prestação de Contas Aprovada com Ressalvas',
                    'Prestação de Contas Rejeitada')

status.novos <- c('Não informado',
                  rep('Não iniciada', 11),
                  'Em andamento',
                  rep('Concluída, segundo a prefeitura', 4),
                  'Aprovada pelo Governo Federal',
                  'Aprovada pelo Governo Federal com ressalvas',
                  'Rejeitada pelo Governo Federal')


status <- cbind(TX_SITUACAO = status.antigos, TX_STATUS = status.novos)
novas.iniciativas <- merge(iniciativas, status)


# Tornando as palavras capitalizadas

modificar.palavra <- function(palavra) {
  if (nchar(palavra) > 3)
    return(paste(toupper(substr(palavra, 1, 1)), tolower(substr(palavra, 2, nchar(palavra))), sep = ''))
  return(tolower(palavra))
}

capitalizar <- function(frase) {
  palavras <- unlist(strsplit(as.character(frase), ' '))
  return(paste(sapply(palavras, modificar.palavra), collapse = ' '))
}

novas.iniciativas$TX_OBJETO_CONVENIO <- sapply(novas.iniciativas$TX_OBJETO_CONVENIO, capitalizar)
novas.iniciativas$NM_ORGAO_SUPERIOR <- sapply(novas.iniciativas$NM_ORGAO_SUPERIOR, capitalizar)
novas.iniciativas$NM_ORGAO_CONCEDENTE <- sapply(novas.iniciativas$NM_ORGAO_CONCEDENTE, capitalizar)
novas.iniciativas$NM_PROGRAMA <-  sapply(novas.iniciativas$NM_PROGRAMA, capitalizar)


# Transformando in_aditivo em boolean

novas.iniciativas$IN_ADITIVO_SN <- with(novas.iniciativas, ifelse(IN_ADITIVO_SN == 'S', 1, 0))

write.csv(novas.iniciativas, '../dist/data/iniciativas-detalhadas.csv', row.names = FALSE)
