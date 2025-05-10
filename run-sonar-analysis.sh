#!/bin/bash

# Aller dans le répertoire du projet API
cd candiflow-api

# Nettoyer, compiler et exécuter les tests avec génération du rapport JaCoCo
./gradlew clean build jacocoTestReport

# Exécuter l'analyse SonarQube
# Remplacez YOUR_TOKEN par le token généré dans SonarQube
./gradlew sonarqube \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=sqa_f49efdb65a61ad1690a626c6464f9a54d5ccd828
