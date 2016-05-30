#wget ftp://geoftp.ibge.gov.br/organizacao_territorial/localidades/Google_KML/BR%20Localidades%202010%20v1.kml -O coordenadas.kml
echo "\"COD.IBGE\",\"LONGITUDE\",\"LATITUDE\",\"ALTITUDE\"" > coordenadas.csv
awk -F '[<>]' '$2~/CD_CATEGORIA|CD_GEOCODMU|coordinates/ {print $3}' coordenadas.kml | tail -n+17 | paste -d ',' - - - | awk -F ',' 'BEGIN { OFS = ","} $2 == "05" {print $1, $3, $4, $5}' >> coordenadas.csv
rm *.kml
