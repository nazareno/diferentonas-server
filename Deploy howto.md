# Instruções para deploy

Testadas em uma VM Ubuntu 16.04.

## Pré-requisitos

Antes de executar o servidor pela primeira vez, assegure que as seguintes variáveis de ambiente existem ou que seus valores no arquivo de configurações estão atualizados:

* DIFERENTONAS_ADMIN_EMAIL: email usado como administrador do sistema
* DIFERENTONAS_SECRET_FACEBOOK: secret key do Facebook para habilitar "Login com Facebook"
* DIFERENTONAS_SECRET_GOOGLE: secret key do Google para habilitar "Login com Google"

Iniciar o diferentonas-server sem configurar o "DIFERENTONAS_ADMIN_EMAIL" implicará num sistema sem administrador. Será necessário alterar o usuário no banco de dados manualmente ou recriar as tabelas.

## Nós web

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

```
git clone http://github.com/nazareno/diferentonas-server
cd diferentonas-server
./activator clean compile stage
./target/universal/stage/bin/diferentonas-server -J-server -Dhttp.port=9000 -Dplay.evolutions.db.default.autoApply=false -Ddb.default.driver=org.postgresql.Driver -Ddb.default.url=${DATABASE_URL} -Ddb.default.username=diferentonas -Ddb.default.password=${DATABASE_PASSWORD} -Ddiferentonas.data="data/" -DDIFERENTONAS_ADMIN_EMAIL=${ADMIN_EMAIL} -DDIFERENTONAS_SECRET_FACEBOOK=${DIFERENTONAS_SECRET_FACEBOOK} -DDIFERENTONAS_SECRET_GOOGLE=${DIFERENTONAS_SECRET_GOOGLE}
```


## Nó do BD

```
sudo apt-get -y update
sudo apt-get -y upgrade
sudo update-locale LC_ALL=en_US.UTF-8 LANG=en_US.UTF-8
sudo apt-get install postgresql

# em /etc/postgresql/9.1/main/postgresql.conf
listen_addresses = '*'

sudo -u postgres psql template1
> ALTER USER postgres with encrypted password 'your_password';
> CREATE USER diferentonas with encrypted password 'your_password';
> CREATE DATABASE diferentonas;
> GRANT ALL privileges ON diferentonas TO diferentonas;

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
