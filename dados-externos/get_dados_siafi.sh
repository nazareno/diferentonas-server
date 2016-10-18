#!/usr/bin/env bash
#
# Pega os dados do SIAFI dos convênios do governo federal.
# Esses dados têm a função orçamentária das transferências dos convênios, que são descrições compreensíveis pelo
# cidadão. Alguém da CGU nos disse que essa é a categoria pela qual eles descrevem os gastos dos municípios pros
# cidadãos quando precisam.
#
# Em maio de 2016, os dados baixados são 2.7G

# comando curl para pegar um arquivo:
# curl 'http://arquivos.portaldatransparencia.gov.br/downloads.asp?a=2016&m=01&consulta=Transferencias' -H 'Accept: text/html,application/xhtmlxml,application/xml;q=0.9,image/webp,*/*;q=0.8' -H 'Referer: http://www.portaltransparencia.gov.br/downloads/mensal.asp?c=Transferencias' -H 'Connection: keep-alive' --compressed

set -u
set -e

ofn=$1

mkdir -p siafi
cd siafi

ano_atual=`date +'%Y'`
mes_atual=`date +'%m'`
for ano in `seq 2011 $ano_atual`; do
    echo "[$(date)] ano: " $ano;
    for mes in `seq 1 12`; do
        if [ $ano == $ano_atual ] && [ $mes == $(( $mes_atual - 1 )) ]; then
            break
        fi

        # formata mes
        if [ $mes -le 9 ]; then
            mes=0$mes # 01, 02, etc
        fi
        # pega o arquivo
        if [[ ! -e  utf8-${ano}${mes}_Transferencias.csv ]]; then
            echo "[$(date)] baixando mês " $mes
            curl -o transferencias-$ano-$mes.zip 'http://arquivos.portaldatransparencia.gov.br/downloads.asp?a='$ano'&m='$mes'&consulta=Transferencias' -H 'Accept: text/html,application/xhtmlxml,application/xml;q=0.9,image/webp,*/*;q=0.8' -H 'Referer: http://www.portaltransparencia.gov.br/downloads/mensal.asp?c=Transferencias' -H 'Connection: keep-alive' --compressed
        else
            echo "[$(date)] já tem mês " $mes
        fi
    done
done

echo "[$(date)] pronto. dados do siafi baixados em " siafi

for f in *.zip; do
    [ -f "$f" ] || break
    unzip -n $f
done

echo "[$(date)] Convertendo todo mundo para UTF-8"
for f in 20*.csv; do
    [ -f "$f" ] || break
    if [[ ! -e utf8-$f ]]; then
         iconv -t UTF-8 -f ISO-8859-15 $f > utf8-$f
    fi
done

# Os .zip e .csv que não iniciam com 'utf8-' podem ser descartados
rm -f transferencias-*zip
rm -f 201*csv

echo "[$(date)] Consolidando em um só arquivo"

of='../'$ofn
../consolida_siafi.R utf8-*_Transferencias.csv > $of
cd -

echo "[$(date)] Resultado em " $ofn
