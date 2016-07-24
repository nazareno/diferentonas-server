# Instruções para deploy

Testadas em uma máquina Ubuntu 14.04.

## Dependências

**R**

```
sudo echo 'deb http://cran.rstudio.com/bin/linux/ubuntu trusty/' > /etc/apt/sources.list.d/diferentonas.list
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys E084DAB9
sudo apt-get update
sudo apt-get -y install r-base

sudo apt-get -y install libcurl4-openssl-dev
sudo R -e 'install.packages(c("dplyr", "reshape2", "ggplot2", "tidyr", "readr", "FastKNN", "testthat", "lubridate", "futile.logger"), repos = "http://cran.rstudio.com/")'
```

**Java**

```
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get -y install oracle-java8-installer
```

**Outros**

```
sudo apt-get -y install git
```

## Diferentonas

```
git clone http://github.com/nazareno/diferentonas-server
cd diferentonas-server

```
