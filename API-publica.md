API Diferentona Aberta
----------------

## URL base

Todos os endpoints abaixo estão descritos a partir de:

http://150.165.85.28/api

## Cidade

* `GET      /cidades` : Lista de todas as cidades conhecidas   
* `GET      /cidade/:id` : Detalhes de uma cidade pelo id   
* `GET      /cidade/:id/similares` : Cidades similares a uma dada cidade, considerando IDH e população.   
* `GET      /cidade/:id/iniciativas` : Iniciativas de uma cidade   

## Iniciativa

* `GET      /iniciativas/:id` : Detalhes de uma iniciativa a partir do id.
* `GET      /iniciativas/:id/similares` : Lista de iniciativas semelhantes em tema e proximidade.
