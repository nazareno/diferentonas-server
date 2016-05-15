currency2double <- function(x){
  x = as.character(x) %>% substr(4, 400); 
  x = gsub(",", ".", gsub("\\.", "", x));
  as.double(x)
}; 

load_convenios = function(siconvfile = "dados-externos/siconv//01_ConveniosProgramas.csv"){
  # Dados do SICONV
  convprog <- read.csv(siconvfile, sep=";")
  convprog = convprog %>% 
    dplyr::mutate(VL_GLOBAL = currency2double(VL_GLOBAL), 
           VL_REPASSE = currency2double(VL_REPASSE), 
           VL_CONTRAPARTIDA_TOTAL = currency2double(VL_CONTRAPARTIDA_TOTAL))
}