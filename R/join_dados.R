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

convenios = read.csv("public/data/convenios-por-municipio.csv")
municipios = read.csv("public/data/dados2010.csv")
# para pegar as UFs: 
populacao = read.csv2("public/data/populacao.csv")

convenios = convenios %>% 
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

head(joined)

