#!/usr/bin/env bash
#
# Pega os dados do SIAFI dos convênios do governo federal.
# Esses dados têm a função orçamentária das transferências dos convênios, que são descrições compreensíveis pelo
# cidadão. Alguém da CGU nos disse que essa é a categoria pela qual eles descrevem os gastos dos municípios pros
# cidadãos quando precisam.
#
# Em maio de 2016, os dados baixados são 2.7G

# comando curl para pegar um arquivo:
# curl 'http://arquivos.portaldatransparencia.gov.br/downloads.asp?a=2016&m=01&consulta=Transferencias' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8' -H 'Referer: http://www.portaltransparencia.gov.br/downloads/mensal.asp?c=Transferencias' -H 'Connection: keep-alive' --compressed

set -u
set -e

mkdir -p transparenciabrasil
cd transparenciabrasil


curl -o  'http://arquivos.portaldatransparencia.gov.br/downloads.asp?a=2016&m=05&d=08&consulta=Convenios' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8' -H 'Referer: http://www.portaltransparencia.gov.br/downloads/snapshot.asp?c=Convenios' -H 'Upgrade-Insecure-Requests: 1' -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.94 Safari/537.36' --compressed



ano_atual=`date +'%Y'`
mes_atual=`date +'%m'`
for ano in `seq 2013 $ano_atual`; do
    echo "ano: " $ano;
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
            echo "baixando mês " $mes
            curl 'http://arquivos.portaldatransparencia.gov.br/downloads.asp?a='$ano'&m='$mes'&consulta=Transferencias' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8' -H 'Referer: http://www.portaltransparencia.gov.br/downloads/mensal.asp?c=Transferencias' -H 'Connection: keep-alive' --compressed > transferencias-$ano-$mes.zip
        else
            echo "já tem mês " $mes
        fi
    done
done

echo "pronto. dados do siafi baixados em " transparenciabrasil

for f in *.zip; do
    unzip -n $f
done

echo "Convertendo todo mundo para UTF-8"
for f in *.csv; do
    if [[ ! -e utf8-$f ]]; then
         iconv -t UTF-8 -f ISO-8859-15 $f > utf8-$f
    fi
done

echo "Fim. Os .zip e .csv que não iniciam com 'utf8-' podem ser descartados"
# para apagar:
# rm transparenciabrasil/*zip
# rm rm transparenciabrasil/201*

echo "Consolidando o que precisamos em um arquivo só"
ofn='transferencias-siafi-em-'${ano_atual}${mes_atual}'.csv'
of='../'$ofn
echo "Número Convênio;Modalidade Aplicação;Fonte-Finalidade;Nome Favorecido;Codigo Acao;Nome Programa;Codigo Programa;Nome Sub Funcao;Codigo Sub Funcao ;Nome Funcao;Nome Funcao;Nome Municipio;Codigo SIAFI Municipio;Sigla Unidade Federação" > $of
# Seleciona colunas e remove duplicatas
awk -F'\t' 'BEGIN{OFS = ";"};
            NR > 1 && $(17) != "" && !($0 in a) {a[$0]; print $(17), $(16), $(15), $(14), $(10), $9, $8, $7, $6, $5, $5, $3, $2, $1}' utf8-*_Transferencias.csv >> $of
cd -

echo "Resultado em " $ofn