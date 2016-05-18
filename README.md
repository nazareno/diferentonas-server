[![Build Status](https://travis-ci.org/nazareno/diferentonas-server.svg?branch=master)](https://travis-ci.org/nazareno/diferentonas-server)

# Diferentonas 

Só minha cidade que investe verba federal assim? 

Usamos dados dos convênios que os municípios celebram com o governo federal e a sua execução, cruzados com dados socioeconômicos sobre os municípios do Brasil para examinar no que uma cidade é diferentona com relação aos convênios que celebrou com o governo federal. Melhor explicado aqui: https://vimeo.com/162919268 .

Este repositório tem nosso servidor. Um cliente Ionic existe aqui: https://github.com/luizaugustomm/diferentonas-client . Você pode acessar uma versão html do app atual aqui: https://luizaugustomm.github.io/diferentonas-client . Deixe usuário e senha em branco. E lembre que o leiaute foi feito para a tela de um celular. 

## Contribua com a gente

O Diferentonas está em pleno desenvolvimento. Mantemos o nossos planos nos issues do github, inclusive com um roadmap.

Por hora a pilha do servidor é Play Framework (2.5) usando JPA / Hibernate para interagir com um BD Postgres e prover uma API RESTful. A configuração para deploy no Heroku está feita também. O pré-processamento dos dados para o BD é feito em R.

## Para rodar o servidor

Você precisará de um postgres instalado na sua máquina, com usuário e senha padrão. Crie nele um bd chamado diferentonas.

Rode `./activator run` e seu servidor escutará na porta 9000. Os endpoints REST são os expostos em `conf/routes`.

## Para gerar os dados do BD a partir dos dados públicos

(WIP)

```
cd dados-externos
./get_dados_siconv.sh # as vezes o download falha
./get_dados_siafi.sh # demora
cd -
Rscript R/join_dados.R # depende de R instalado
```
