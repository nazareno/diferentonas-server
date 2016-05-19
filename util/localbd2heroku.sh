#!/usr/bin/env bash

# DESTRÓI BD no Heroku e o substitui pelo BD 'diferentonas' local.
# Isso é útil para acelerar o processo de deploy no heroku,
# já que popular um BD vazio lá com cidades e iniciativas
# demora mais de 60s, o que faz com que o heroku cancele o deploy.

set -e

echo "DESTRUINDO BD atual no Heroku e enviando o local para lá"
heroku pg:reset DATABASE
heroku pg:push diferentonas DATABASE_URL --app diferentonas

echo "Reiniciando todos os dynos"
heroku restart
    