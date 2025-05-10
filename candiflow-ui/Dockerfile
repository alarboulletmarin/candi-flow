# Utilise Node.js 18 comme image de base
FROM node:18-alpine

# Définit le répertoire de travail dans le conteneur
WORKDIR /app

# Copie les fichiers de configuration npm
COPY package.json package-lock.json ./

# Installe les dépendances
RUN npm ci

# Copie le reste des fichiers de l'application
COPY . .

# Expose les ports utilisés par Expo et React Native
EXPOSE 19000 19006 8081

# Commande pour démarrer l'application
CMD ["npm", "start", "--", "--web"]
