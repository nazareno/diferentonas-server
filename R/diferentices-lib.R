expande_convenios = function(convenios){
  convenios %>% 
  complete(nesting(NM_MUNICIPIO_PROPONENTE, UF_PROPONENTE, ANO_CONVENIO, nome, cod7), funcao.imputada, 
           fill = list(total = 0)) %>% unique()
}

cria_df_comparacao <- function(cod, convenios.expandidos, vizinhos.df, col_origem = 12, cols_vizinhos = 13:22) {
  ids = vizinhos.df[vizinhos.df$origem == cod, c(col_origem, cols_vizinhos)]
  cs = convenios.expandidos %>% filter(cod7 %in% ids)
  cs$cod7 = droplevels(factor(cs$cod7))
  cs$funcao.imputada = droplevels(cs$funcao.imputada)
  cs = cs %>% 
    group_by(funcao.imputada, cod7) %>% 
    summarise(total = sum(total)) %>% 
    mutate(zscore = (total - mean(total)) / sd(total)) 
  cs = cs %>% group_by(funcao.imputada) %>% mutate(media = mean(total)) 
  cs[is.na(cs$zscore), "zscore"] = 0
  cs$origem = cod
  cs
}

cria_dados_score = function(id, os.convenios, vizinhos.df, col_origem = 12, cols_vizinhos = 13:22){
  cria_df_comparacao(id, os.convenios, vizinhos.df, col_origem, cols_vizinhos) %>% 
    filter(origem == cod7) %>% 
    select(origem, funcao.imputada, zscore, total, media) 
}

computa_scores_para_todos = function(os.convenios, vizinhos.df, col_origem = 12, cols_vizinhos = 13:22){
  vizinhos.df %>% 
    select(origem) %>% 
    rowwise() %>% 
    do(cria_dados_score(.$origem, os.convenios, vizinhos.df, col_origem, cols_vizinhos))
    # group_by(origem) %>% 
    # do(cria_dados_score(.$origem, os.convenios, vizinhos.df))
}