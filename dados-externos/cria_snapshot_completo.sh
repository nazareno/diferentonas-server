#!/bin/bash
#
# Cria os arquivos necessários para atualizar o BD com os dados disponíveis
# hoje nas fontes que utilizamos.
#
# --datasiconv=<data> faz com que o script use os dados em siconv-<data> em
# lugar de baixar novos dados do siconv. Útil para reprocessar dados.

# arquivo com cidades semelhantes, por enquanto estático:
ARQUIVO_VIZINHOS=dist/data/vizinhos.euclidiano.csv

for i in "$@"
do
case $i in
    --datasiconv=*)
    data_dados_siconv="${i#*=}"
    shift # past argument=value
    ;;
esac
done

if [ -z "$data_dados_siconv" ]; then
  reusando_dados_locais=false
  echo "[`date`] Checando data da atualização mais recente no siconv"
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
else
  echo "Usando data de dados siconv fornecida: " $data_dados_siconv
  reusando_dados_locais=true
fi

# Daqui em diante, na defensiva
set -e
set -u

# Saídas que geraremos
diretorio_saida='dist/data/'
saida_iniciativas=../${diretorio_saida}/iniciativas-${data_dados_siconv}.csv
saida_diferentices=../${diretorio_saida}/diferentices-${data_dados_siconv}.csv
saida_historico=../${diretorio_saida}/historico-${data_dados_siconv}.csv

# A menos que existam
if [[ -f $saida_iniciativas && -f $saida_diferentices && -f $saida_historico ]]; then
  echo "[`date`] Já processamos os dados mais recentes no portal de convênios. Data mais recente: " $data_dados_siconv
  exit 0
fi

# Baixar dados SICONV ------------------
if [ "$reusando_dados_locais" != true ]; then
  output_siconv='siconv-'$data_dados_siconv # diretório
  mkdir -p $output_siconv
  echo "[`date`] Baixando dados do siconv para $output_siconv"
  ./get_dados_siconv.sh $output_siconv
fi
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
f1=/tmp/iniciativas-${data_dados_siconv}.csv
f2=/tmp/diferentices-${data_dados_siconv}.csv
f3=/tmp/historico-${data_dados_siconv}.csv

./R/atualiza_dados_cli.R dados-externos/$output_siconv dados-externos/$output_siafi $ARQUIVO_VIZINHOS $f1 $f2 $f3
# Deu tudo certo, copia para dist/data
# mv $f1 $f2 $f3 ${diretorio_saida}
# Temporariamente decidimos não usar o arquivo de histórico (fica tudo mais rápido assim)
mv $f1 $f2 ${diretorio_saida}
echo "[`date`] Dados movidos para ${diretorio_saida} : " $f1 " " $f2 " " $f3
cd -

# TODO apagar $output_siconv

echo "[`date`] Fim da aquisição e pré-processamento de dados do siconv"
