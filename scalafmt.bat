@echo off
java -jar %~dp0scalafmt %*

REM upgrade with coursier bootstrap -f com.geirsson:scalafmt-cli_2.11:<version> --main org.scalafmt.cli.Cli -o scalafmt
