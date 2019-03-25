# K.U.R.E.U.I.L
#
#
# version: '1'

FROM ubuntu:16.04

# updates / installations

RUN apt-get update -y && apt-get install -y software-properties-common \
    	    	      	 	 	    wget \
					    apt-transport-https
RUN add-apt-repository ppa:webupd8team/java
RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
RUN wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -
RUN sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt/ $(lsb_release -sc)-pgdg main" > /etc/apt/sources.list.d/PostgreSQL.list'
RUN apt-get update -y
RUN yes | apt-get install --yes oracle-java8-installer \
    	    	      	  	postgresql-10 \
			 	sbt

# get kureuil-api
COPY . /kureuil-api

#open port 4220
EXPOSE 4220

#install kureuil-api
RUN (cd /kureuil-api && sbt compile)
RUN (cd /kureuil-api && sh psqlConfigScript.sh)

# start kureuil-api
CMD (cd /kureuil-api/ && sbt kureuil-api-run/run)