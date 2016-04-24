setwd("/Users/nazareno/workspace/diferentonas-server")

rm_accent <- function(str,pattern="all") {
  # Rotinas e funções úteis V 1.0
  # rm.accent - REMOVE ACENTOS DE PALAVRAS
  # Função que tira todos os acentos e pontuações de um vetor de strings.
  # Parâmetros:
  # str - vetor de strings que terão seus acentos retirados.
  # patterns - vetor de strings com um ou mais elementos indicando quais acentos deverão ser retirados.
  #            Para indicar quais acentos deverão ser retirados, um vetor com os símbolos deverão ser passados.
  #            Exemplo: pattern = c("´", "^") retirará os acentos agudos e circunflexos apenas.
  #            Outras palavras aceitas: "all" (retira todos os acentos, que são "´", "`", "^", "~", "¨", "ç")
  if(!is.character(str))
    str <- as.character(str)
  
  pattern <- unique(pattern)
  
  if(any(pattern=="Ç"))
    pattern[pattern=="Ç"] <- "ç"
  
  symbols <- c(
    acute = "áéíóúÁÉÍÓÚýÝ",
    grave = "àèìòùÀÈÌÒÙ",
    circunflex = "âêîôûÂÊÎÔÛ",
    tilde = "ãõÃÕñÑ",
    umlaut = "äëïöüÄËÏÖÜÿ",
    cedil = "çÇ"
  )
  
  nudeSymbols <- c(
    acute = "aeiouAEIOUyY",
    grave = "aeiouAEIOU",
    circunflex = "aeiouAEIOU",
    tilde = "aoAOnN",
    umlaut = "aeiouAEIOUy",
    cedil = "cC"
  )
  
  accentTypes <- c("´","`","^","~","¨","ç")
  
  if(any(c("all","al","a","todos","t","to","tod","todo")%in%pattern)) # opcao retirar todos
    return(chartr(paste(symbols, collapse=""), paste(nudeSymbols, collapse=""), str))
  
  for(i in which(accentTypes%in%pattern))
    str <- chartr(symbols[i],nudeSymbols[i], str)
  
  return(str)
}

## seleciona e salva apenas os convênios propostos no âmbito municipal
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
  filter(!(TX_SITUACAO %in% c("Proposta/Plano de Trabalho Cancelados", 
                              "Proposta/Plano de Trabalho Rejeitados")), 
         !(TX_ESFERA_ADM_PROPONENTE %in% c("ESTADUAL", "FEDERAL"))) %>%
  select(NM_MUNICIPIO_PROPONENTE, UF_PROPONENTE, VL_REPASSE, NM_ORGAO_SUPERIOR, ANO_CONVENIO) %>%
  group_by(NM_MUNICIPIO_PROPONENTE, UF_PROPONENTE, NM_ORGAO_SUPERIOR, ANO_CONVENIO) %>%
  summarise(total = sum(VL_REPASSE)) %>% 
  write.csv(file = "dist/data/convenios-por-municipio.csv", row.names = FALSE)

## Join:

convenios = read.csv("dist/data/convenios-por-municipio.csv")
convenios.d = read.csv("dist/data/convenios-por-municipio-detalhes.csv")
municipios = read.csv("dist/data/dados2010.csv")
# para pegar as UFs: 
populacao = read.csv2("dist/data/populacao.csv")

convenios = convenios %>% 
  mutate(nome = rm_accent(tolower(as.character(NM_MUNICIPIO_PROPONENTE))))
convenios.d = convenios.d %>% 
  mutate(nome = rm_accent(tolower(as.character(NM_MUNICIPIO_PROPONENTE))))

municipios = municipios %>% 
  mutate(nome = rm_accent(tolower(as.character(municipio))))

municipios = inner_join(municipios, 
                        select(populacao, Código, Município), 
                        by = c("cod7" = "Código"))

NROW(levels(municipios$municipio))
NROW(levels(convenios$NM_MUNICIPIO_PROPONENTE))

convenios[!(convenios$nome %in% municipios$nome),] %>% 
  select(NM_MUNICIPIO_PROPONENTE) %>% unique() %>% NROW()

m.ids = municipios %>% select(nome, cod7, UF)

joined = inner_join(convenios, m.ids, by = c("nome" = "nome", "UF_PROPONENTE" = "UF"))
joined.d = inner_join(convenios.d, m.ids, by = c("nome" = "nome", "UF_PROPONENTE" = "UF"))
joined.d = joined.d %>% filter(ANO_CONVENIO >= 2013)
joined.du = unique(joined.d)
joined.du = joined.du[-which(duplicated(joined.du$NR_CONVENIO)),]

write.csv(joined, "dist/data/convenios-municipio-ccodigo.csv", row.names = FALSE)
write.csv(joined.du, "dist/data/convenios-municipio-detalhes-ccodigo.csv", row.names = FALSE)

