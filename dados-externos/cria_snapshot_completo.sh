#!/bin/bash
#
# Cria os arquivos necessários para atualizar o BD com os dados disponíveis
# hoje nas fontes que utilizamos.
#
set -e
set -u

# arquivo com cidades semelhantes, por enquanto estático:
ARQUIVO_VIZINHOS=dist/data/vizinhos.euclidiano.csv

# Data dos dados do SICONV
data_dados_siconv=`curl -sI 'http://portal.convenios.gov.br/images/docs/CGSIS/csv/siconv_convenio.csv.zip' | grep 'Last-Modified' | cut -d' ' -f 2- | awk 'BEGIN{
   m=split("Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec",d,"|")
   for(o=1;o<=m;o++){
      date[d[o]]=sprintf("%02d",o)
    }
}
{
  {print $4 date[$3] $2}
}'`

# Saídas que geraremos
f1=/tmp/iniciativas-${data_dados_siconv}.csv
f2=/tmp/diferentices-${data_dados_siconv}.csv
f3=/tmp/historico-${data_dados_siconv}.csv

# A menos que existam
if [[ -f $f1 && -f $f2 && -f $f3 ]]; then
  echo "[`date`] Não há dados novos publicados no portal de convênios. Data mais recente: " $data_dados_siconv
  exit 0
fi

# Dados SICONV ------------------
output_siconv='siconv-'$data_dados_siconv # diretório
mkdir -p $output_siconv
echo "[`date`] Baixando dados do siconv para $output_siconv"
./get_dados_siconv.sh $output_siconv
# Dados SIAFI  ------------------
hoje=`date +'%Y%m'`
output_siafi='convenios-siafi-'${hoje}'.csv'
if [ ! -e $output_siafi ]; then
  echo "[`date`] Baixando dados do siafi para $output_siafi"
  ./get_dados_siafi.sh $output_siafi
else
  echo "Já temos os dados mais recentes do SIAFI"
fi
# Calcula o que queremos --------
cd ..
./R/atualiza_dados_cli.R dados-externos/$output_siconv dados-externos/$output_siafi $ARQUIVO_VIZINHOS $f1 $f2 $f3
# Deu tudo certo, copia para dist/data
mv $f1 $f2 $f3 dist/data/
echo "[`date`] Dados movidos para dist/data/ : " $f1 " " $f2 " " $f3
cd -

# TODO apagar $output_siconv

echo "[`date`] Fim da aquisição e pré-processamento de dados do siconv"
