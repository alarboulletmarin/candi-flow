# --- Stage 1: Build ---
# Utilise une image de base contenant le JDK 21 (même version que celle utilisée pour le développement) basé sur Alpine (léger)
# Choisis l'image Temurin qui est un build OpenJDK populaire et bien supporté
FROM eclipse-temurin:21-jdk-alpine AS builder

# Définit le répertoire de travail dans le conteneur
WORKDIR /app

# Copie les fichiers de configuration Gradle et le wrapper d'abord
# pour profiter du cache de couches Docker si ces fichiers ne changent pas
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Rend le wrapper Gradle exécutable
RUN chmod +x ./gradlew

# Télécharge les dépendances (met en cache cette couche)
# L'option --no-daemon est recommandée dans les environnements CI/Docker
# On peut ignorer les erreurs ici si le but est juste de peupler le cache
# RUN ./gradlew dependencies --no-daemon || true
# Alternative plus robuste: tenter de build pour télécharger, supprimer les locks en cas d'échec, réessayer.
# Pour un Dockerfile simple, on copie souvent juste les sources ensuite.

# Copie le code source de l'application
COPY src ./src

# Construit l'application et crée le fichier JAR exécutable
# Assure-toi que ta tâche Gradle s'appelle bien 'bootJar' (standard pour Spring Boot)
RUN ./gradlew bootJar --no-daemon

# --- Stage 2: Runtime ---
# Utilise une image de base contenant uniquement le JRE 21 basé sur Alpine (plus légère que le JDK)
FROM eclipse-temurin:21-jre-alpine

# Définit le répertoire de travail
WORKDIR /app

# Copie UNIQUEMENT le fichier JAR construit depuis le stage 'builder'
# Le chemin exact peut varier selon ta configuration Gradle/Maven
# Pour Gradle bootJar, c'est souvent build/libs/NOM_ARTIFACT-VERSION.jar
# L'astérisque (*) suppose qu'il n'y a qu'un seul JAR dans build/libs
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose le port sur lequel l'application Spring Boot écoute (8080 par défaut)
EXPOSE 8080

# Commande pour lancer l'application lorsque le conteneur démarre
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Note pour Maven:
# - Copier pom.xml au lieu de build.gradle/settings.gradle
# - Utiliser ./mvnw package (ou verify) au lieu de ./gradlew bootJar
# - Le JAR se trouvera probablement dans /app/target/NOM_ARTIFACT-VERSION.jar
