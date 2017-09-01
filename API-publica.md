API Diferentona Aberta
----------------

## URL base

Todos os endpoints abaixo estão descritos a partir de:

http://150.165.85.28:81/api

## Cidade

* `GET      /cidades` : Lista de todas as cidades conhecidas   
* `GET      /cidade/:id` : Detalhes de uma cidade pelo id   
* `GET      /cidade/:id/similares` : Cidades similares a uma dada cidade, considerando IDH e população.   
* `GET      /cidade/:id/iniciativas` : Iniciativas de uma cidade   

## Iniciativa

* `GET      /iniciativas/:id` : Detalhes de uma iniciativa a partir do id.
* `GET      /iniciativas/:id/similares` : Lista de iniciativas semelhantes em tema e proximidade.

## Dados servidos pela API

Os dados brutos são principalmente dois arquivos que você pode obter em CSV: [um para as iniciativas](https://github.com/nazareno/diferentonas-server/raw/master/dist/data/iniciativas-20170614.csv) e [um que mede quão diferentonas as cidades são](https://raw.githubusercontent.com/nazareno/diferentonas-server/master/dist/data/diferentices-20170614.csv). Além desses dois, temos dados sobre as cidades em si [nesses outros arquivos](https://github.com/nazareno/diferentonas-server/tree/master/dist/data).
