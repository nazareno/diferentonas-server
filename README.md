[![Build Status](https://travis-ci.org/nazareno/diferentonas-server.svg?branch=master)](https://travis-ci.org/nazareno/diferentonas-server)

# Diferentonas 

Só minha cidade que investe verba federal assim? 

Usamos dados dos convênios que os municípios celebram com o governo federal e a sua execução, cruzados com dados socioeconômicos sobre os municípios do Brasil para examinar no que uma cidade é diferentona com relação aos convênios que celebrou com o governo federal. Melhor explicado aqui: https://vimeo.com/162919268 .

Este repositório tem nosso servidor. Um cliente Ionic existe [neste outro repositório](https://github.com/luizaugustomm/diferentonas-client). Você pode acessar [uma versão html do app atual na web](https://luizaugustomm.github.io/diferentonas-client), também. Deixe usuário e senha em branco, e lembre que o leiaute foi feito para a tela de um celular. 

## Participe

O Diferentonas está em pleno desenvolvimento. Mantemos o nossos planos nos issues do github, inclusive com um roadmap.

A pilha de tecnologias que usamos para prover a API RESTful do servidor é: 

* Play Framework (2.5) com JPA / Hibernate 
* Postgres. 

O pré-processamento dos dados para o BD é feito em R + Bash.
 
## Para rodar o servidor

Você precisará de um postgres instalado na sua máquina, com usuário e senha padrão. Crie nele um bd chamado diferentonas.

Rode `./activator run` e seu servidor escutará na porta 9000. Os endpoints REST são os expostos em `conf/routes`.

Caso você queira colocar seu servidor no Heroku, a configuração está pronta no `/Procfile`.

## Testes automáticos

Java: `./activator test`

R: `Rscript R/test_diferentices.R`

## Para gerar os dados de criação ou atualização do BD a partir dos dados públicos

É preciso ter os pacotes R necessários instalados. No terminal R: 

```
pks = c("tidyr", "dplyr", "reshape2")
install.packages(pks, dependencies = TRUE))
```

Com os pacotes, no bash:

```
cd dados-externos
./cria_snapshot_completo.sh 
```

O script criará os dois arquivos necessários para povoar ou atualizar o BD: um com as iniciativas dos municípios e outro com os scores de diferentices calculados. 
