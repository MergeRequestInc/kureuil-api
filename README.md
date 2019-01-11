# kureuil-api
Backend API for Kureil project

## How to build :

Install java8 jdk and sbt ( at : https://www.scala-sbt.org/ )

Run `sbt compile` to compile the project

## How to run : 

Install postgresql-9.6.11 or postgresql-10.6 ( from https://www.enterprisedb.com/downloads/postgres-postgresql-downloads ) or your distribution packages manager

Open psql command line (as postgres user in linux and type `psql`)

Create an user named kureuil with password kureuil and a database named kureuil by default ( or other, just configure it into your /etc/kureuil-api/application.conf or kureuil-api-run/src/resources/application.conf )
`CREATE USER kureuil; ALTER ROLE kureuil WITH CREATEDB; CREATE DATABASE kureuil OWNER kureuil; ALTER USER kureuil WITH ENCRYPTED PASSWORD 'kureuil';`

You have to add pgcrypto extension to the database :
`psql kureuil`

`CREATE EXTENSION pgcrypto;`

Now run the api: `sbt kureuil-api-run/run`
