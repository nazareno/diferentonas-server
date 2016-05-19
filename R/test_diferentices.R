library(dplyr)
library(testthat)

source("R/diferentices-lib.R")

emas = data.frame(NM_MUNICIPIO_PROPONENTE = "Emas", 
                  UF_PROPONENTE = "PB",
                  cod7 = 1,                  
                  funcao.imputada = c("f1", "f2"),
                  nome = "emas",
                  total = c(10, 20))

cabedelo = data.frame(NM_MUNICIPIO_PROPONENTE = "Cabedelo", 
                      UF_PROPONENTE = "PB",
                      cod7 = 2, 
                      nome = "cabedelo",
                      funcao.imputada = c("f1"),
                      total = c(1))

lucena = data.frame(NM_MUNICIPIO_PROPONENTE = "Lucena", 
                    UF_PROPONENTE = "PB",
                    cod7 = 3,                  
                    funcao.imputada = c("f2", "f3"),
                    nome = "lucena",
                    total = c(10, 40))

cabrobro = data.frame(NM_MUNICIPIO_PROPONENTE = "Cabrobro", 
                    UF_PROPONENTE = "PB",
                    cod7 = 4,                  
                    funcao.imputada = c("f4"),
                    nome = "cabrobro",
                    total = c(1))

riacho.doce = data.frame(NM_MUNICIPIO_PROPONENTE = "Riacho Doce", 
                         UF_PROPONENTE = "PB",
                         cod7 = 5,                  
                         funcao.imputada = c("f4"),
                         nome = "riacho doce",
                         total = c(1))

alguns.convenios = rbind(emas, cabedelo, lucena)

context("Expande totais por área")

test_that("Expansão contém todos os níveis", {
  expandido = expande_convenios(alguns.convenios)
  expect_equal(length(expandido), 6)
  expect_equal(NROW(expandido), 9)
  expandido %>% select(total) %>% sum() %>% expect_equal(81)
  expandido %>% filter(nome == "cabedelo") %>% select(total) %>% sum() %>% expect_equal(1)
})

test_that("Sumarização por função", {
  docsv = read.csv("R/test-data/para-diferentices.csv")
  expect_equal(sum(duplicated(docsv[, c("funcao.imputada", "nome")])), 1)
  outros.convenios = sumariza_convenios_para_diferentices(docsv)
  expect_equal(sum(duplicated(outros.convenios[, c("funcao.imputada", "nome")])), 0)
})

expandido = expande_convenios(alguns.convenios)
vizinhos.t = data.frame(origem = levels(as.factor(alguns.convenios$cod7)), 
                        V1 = c(2, 3, 1), 
                        V2 = c(3, 1, 2))

test_that("DF de comparação é corretamente montado", {
  df = cria_df_comparacao(1, expandido, vizinhos.t, col_origem = 1, cols_vizinhos = 2:3)
  expect_equal(length(df), 9)
  expect_equal(NROW(df), 9)
  df[with(df, cod7 == 1 & funcao.imputada == "f1"), "total"] %>% expect_equal(data.frame(total = 10))
  expect_equal(df[with(df, cod7 == 1 & funcao.imputada == "f1"), ]$zscore, 1.149932, tolerance = 1e-4)
  expect_equal(df[with(df, cod7 == 1 & funcao.imputada == "f2"), ]$zscore, 1, tolerance = 1e-4)
  expect_equal(df[with(df, cod7 == 2 & funcao.imputada == "f2"), ]$zscore, -1, tolerance = 1e-4)
  expect_equal(df[with(df, cod7 == 3 & funcao.imputada == "f2"), ]$zscore, 0, tolerance = 1e-4)
})


test_that("DF de comparação em caso que estava com bug", {
  mais.convenios = rbind(emas, cabrobro, riacho.doce)
  expandido2 = expande_convenios(mais.convenios)
  vizinhos.t2 = data.frame(origem = levels(as.factor(mais.convenios$cod7)), 
                           V1 = c(4, 5, 1), 
                           V2 = c(5, 1, 4))
  df = cria_df_comparacao(1, expandido2, vizinhos.t2, col_origem = 1, cols_vizinhos = 2:3)
  expect_true(all(df$zscore != 0))
  expect_true(all(df$media != 0))
  expect_true(any(df$total == 0))
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

test_that("Com dados reais", {
  outros.convenios = read.csv("R/test-data/para-diferentices.csv") %>% 
    sumariza_convenios_para_diferentices()
  expandido = expande_convenios(outros.convenios)
  expect_equal(NROW(expandido), 8)
  vizinhos.t2 = data.frame(origem = c(5200050, 3100104), V1 = c(3100104, 5200050))
  df = computa_scores_para_todos(expandido, vizinhos.t2, col_origem = 1, cols_vizinhos = 2)
})
