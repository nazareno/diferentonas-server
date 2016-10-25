# Instruções para deploy

Testadas em uma VM Ubuntu 16.04.

## Nó do BD

Funcionamos com postgres 9.1+

```
sudo apt-get -y update
sudo apt-get -y upgrade
sudo update-locale LC_ALL=en_US.UTF-8 LANG=en_US.UTF-8
sudo apt-get install postgresql

# em /etc/postgresql/VERSION/main/postgresql.conf
listen_addresses = '*'

sudo -u postgres psql template1
> ALTER USER postgres with encrypted password 'your_password';
> CREATE USER diferentonas with encrypted password 'your_password';
> CREATE DATABASE diferentonas;
> GRANT ALL privileges ON DATABASE diferentonas TO diferentonas;

# em /etc/postgresql/9.1/main/pg_hba.conf
local   all         postgres                          md5
host    diferentonas    diferentonas    0.0.0.0/0               md5

sudo service postgresql restart

# para testar nas máquinas que acessarão
sudo apt install postgresql-client-9.5
psql -h <HOST OU IP> -U diferentonas -W

# extensões que precisamos
psql -U postgres diferentonas -W -c 'create extension cube; create extension earthdistance;'
```

## Nós da API

**R**

```
echo 'deb http://cran.rstudio.com/bin/linux/ubuntu trusty/' | sudo tee /etc/apt/sources.list.d/diferentonas.list
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys E084DAB9
sudo apt-get -y update
sudo apt-get -y upgrade
sudo update-locale LC_ALL=en_US.UTF-8 LANG=en_US.UTF-8

sudo apt-get -y install libcurl4-openssl-dev

sudo apt-get -y install r-base
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
sudo apt-get -y install zip
```

**Diferentonas**

Você precisará de um arquivo de configuração para seu ambiente de produção.
Para isso crie uma cópia de `conf/prod.conf` e o edite. Você o utilizará em
todas os nós web.

Para preparar a distribuição a partir do código mais recente:

```
git clone http://github.com/nazareno/diferentonas-server
cd diferentonas-server
./activator clean compile dist
# Isso gera o arquivo diferentonas-server-1.0-SNAPSHOT.zip
```

Para iniciar o diferentonas em cada nó:

```
unzip path-para-o-zip/diferentonas-server-1.0-SNAPSHOT.zip
cd diferentonas-server-1.0-SNAPSHOT
./bin/diferentonas-server -J-server -Dconfig.file=path-para-o-conf/producao.conf -Dlogger.resource=logback-prod.xml
```
