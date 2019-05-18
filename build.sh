#!/usr/bin/env bash
shopt -s expand_aliases

sh clear.sh;

mkdir build;
mkdir jars;

(
  echo "Компиляция клиента...";
  javac18 -sourcepath src -d build src/ifmo/programming/lab6/client/Client.java -encoding UTF-8
) && (
  echo "Сборка клиентского jar...";
  jar cfm jars/ClientApp.jar manifests/CLIENT_MANIFEST.MF -C build .
) && (
  echo "Компиляция сервера...";
  javac18 -sourcepath src -d build src/ifmo/programming/lab6/server/Server.java -encoding UTF-8
) && (
  echo "Сборка серверного jar...";
  jar cfm jars/ServerApp.jar manifests/SERVER_MANIFEST.MF -C build .
) && (
  echo "Готово!";
)
