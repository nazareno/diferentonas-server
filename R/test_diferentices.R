library(dplyr)
library(testthat)

source("R/diferentices-lib.R")

emas = data.frame(NM_MUNICIPIO_PROPONENTE = "Emas", 
                  UF_PROPONENTE = "PB",
                  ANO_CONVENIO = "2013",           
                  cod7 = 1,                  
                  funcao.imputada = c("f1", "f2"),
                  nome = "emas",
                  total = c(10, 20))

cabedelo = data.frame(NM_MUNICIPIO_PROPONENTE = "Cabedelo", 
                      UF_PROPONENTE = "PB",
                      ANO_CONVENIO = "2013",           
                      cod7 = 2, 
                      nome = "cabedelo",
                      funcao.imputada = c("f1"),
                      total = c(1))

lucena = data.frame(NM_MUNICIPIO_PROPONENTE = "Lucena", 
                    UF_PROPONENTE = "PB",
                    ANO_CONVENIO = "2013",           
                    cod7 = 3,                  
                    funcao.imputada = c("f2", "f3"),
                    nome = "lucena",
                    total = c(10, 40))

alguns.convenios = rbind(emas, cabedelo, lucena)

context("Expande totais por área")

test_that("Expansão contém todos os níveis", {
  expandido = expande_convenios(alguns.convenios)
  expect_equal(length(expandido), 7)
  expect_equal(NROW(expandido), 9)
  expandido %>% select(total) %>% sum() %>% expect_equal(81)
  expandido %>% filter(nome == "cabedelo") %>% select(total) %>% sum() %>% expect_equal(1)
})

expandido = expande_convenios(alguns.convenios)
vizinhos.t = data.frame(origem = levels(as.factor(alguns.convenios$cod7)), 
                        V1 = c(2, 3, 1), 
                        V2 = c(3, 1, 2))

test_that("DF de comparação é corretamente montado", {
  df = cria_df_comparacao(1, expandido, vizinhos.t, col_origem = 1, cols_vizinhos = 2:3)
  expect_equal(length(df), 6)
  expect_equal(NROW(df), 9)
  df[with(df, cod7 == 1 & funcao.imputada == "f1"), "total"] %>% expect_equal(data.frame(total = 10))
  expect_equal(df[with(df, cod7 == 1 & funcao.imputada == "f1"), ]$zscore, 1.149932, tolerance = 1e-4)
  expect_equal(df[with(df, cod7 == 1 & funcao.imputada == "f2"), ]$zscore, 1, tolerance = 1e-4)
  expect_equal(df[with(df, cod7 == 2 & funcao.imputada == "f2"), ]$zscore, -1, tolerance = 1e-4)
  expect_equal(df[with(df, cod7 == 3 & funcao.imputada == "f2"), ]$zscore, 0, tolerance = 1e-4)
})

test_that("Scores ok.", {
  scores = cria_dados_score(1, expandido, vizinhos.t, col_origem = 1, cols_vizinhos = 2:3)
  expect_equal(length(scores), 5)
  expect_equal(NROW(scores), 3)
  expect_equal(scores[with(scores, origem == 1 & funcao.imputada == "f2"), ]$zscore, 1, tolerance = 1e-4)
  expect_equal(scores[with(scores, origem == 1 & funcao.imputada == "f2"), ]$total, 20)
})

test_that("Scores ok.", {
  scores = computa_scores_para_todos(expandido, vizinhos.t, col_origem = 1, cols_vizinhos = 2:3)
  expect_equal(length(scores), 5)
  expect_equal(NROW(scores), 9)
  # O restante já foi testado.
})