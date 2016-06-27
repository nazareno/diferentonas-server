sumariza_convenios_para_diferentices = function(df){
  df %>% 
    select(  NM_MUNICIPIO_PROPONENTE, UF_PROPONENTE, funcao.imputada, nome, cod7, VL_REPASSE) %>%
    group_by(NM_MUNICIPIO_PROPONENTE, UF_PROPONENTE, funcao.imputada, nome, cod7) %>%
    summarise(total = sum(VL_REPASSE)) 
}

expande_convenios = function(convenios){
  convenios %>% 
  complete(nesting(NM_MUNICIPIO_PROPONENTE, UF_PROPONENTE, nome, cod7), funcao.imputada, 
           fill = list(total = 0)) %>% unique()
}

cria_df_comparacao <- function(cod, convenios.expandidos, vizinhos.df, col_origem = 12, cols_vizinhos = 13:22) {
  ids = vizinhos.df[vizinhos.df$origem == cod, c(col_origem, cols_vizinhos)]
  cs = convenios.expandidos %>% ungroup() %>% filter(cod7 %in% ids)
  cs$cod7 = droplevels(factor(cs$cod7))

  if(any(is.na(cs$funcao.imputada))){
    stop("Função imputada faltando: ", 
         paste("municipio: ", cs[is.na(cs$funcao.imputada), c("cod7")]))
  }
    
  score = function(x){
    if(sd(x) == 0){
      return(0)
    } else{ 
      return((x - mean(x)) / sd(x))
    }
  }
  
  cs = cs %>% 
    group_by(funcao.imputada) %>% 
    mutate(zscore = score(total), 
           media = mean(total), 
           sd = sd(total)) 
  cs$origem = cod
  return(cs)
}

cria_dados_score = function(id, os.convenios, vizinhos.df, col_origem = 12, cols_vizinhos = 13:22){
  cria_df_comparacao(id, os.convenios, vizinhos.df, col_origem, cols_vizinhos) %>% 
    filter(origem == cod7) %>% 
    select(origem, funcao.imputada, zscore, total, media, sd) 
}

computa_scores_para_todos = function(os.convenios, vizinhos.df, col_origem = 12, cols_vizinhos = 13:22){
  vizinhos.df %>% 
    select(origem) %>% 
    rowwise() %>% 
    do(cria_dados_score(.$origem, os.convenios, vizinhos.df, col_origem, cols_vizinhos))
}

pca_comparacao <- function(convenios, vizinhos, cod) {
  ids = vizinhos[vizinhos$origem == cod, 12:22]
  cs = convenios %>% filter(cod7 %in% ids, ANO_CONVENIO >= 2013)
  cs$cod7 = factor(cs$cod7)
  cs$funcao.imputada = droplevels(cs$funcao.imputada)
  
  cs.w = dcast(select(cs, cod7, funcao.imputada, total), 
               formula = cod7 ~ funcao.imputada, sum)
  
  df = cs.w[, -1]
  row.names(df) = cs$NM_MUNICIPIO_PROPONENTE
  pcs = prcomp(df, scale = TRUE)
  autoplot(pcs, label = TRUE, label.size = 3, shape = FALSE, 
           loadings = TRUE, loadings.colour = 'blue',
           loadings.label = TRUE, loadings.label.size = 3)
  
  pr.var <- pcs$sdev^2
  pve <- pr.var / sum(pr.var)
  df = data.frame(x = 1:NROW(pve), y = cumsum(pve))
  ggplot(df, aes(x = x, y = y)) + 
    geom_point(size = 3) + 
    geom_line() + 
    labs(x='Principal Component', y = 'Cumuative Proportion of Variance Explained')
}
