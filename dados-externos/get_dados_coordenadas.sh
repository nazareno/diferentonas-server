wget ftp://geoftp.ibge.gov.br/organizacao_territorial/localidades/Google_KML/BR%20Localidades%202010%20v1.kml -O coordenadas.kml
echo "\"COD.IBGE\",\"LONGITUDE\",\"LATITUDE\",\"ALTITUDE\"" > coordenadas.csv
awk -F '[<>]' '$2~/CD_GEOCODMU|NM_LOCALIDADE|NM_MUNICIPIO|NM_UF|coordinates/ {print $3}' coordenadas.kml | tail -n+33 | paste -d ',' - - - - - | awk -F ',' 'BEGIN { OFS = ","} $2 == $4 {print $1, $5, $6, $7}' >> coordenadas.csv
rm *.kml
