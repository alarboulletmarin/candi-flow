# Étape 1: Build de l'application
FROM node:18-alpine as build

# Définit le répertoire de travail
WORKDIR /app

# Copie les fichiers de configuration npm
COPY package.json package-lock.json ./

# Installe les dépendances avec une stratégie plus permissive
RUN npm install --legacy-peer-deps

# Copie le reste des fichiers de l'application
COPY . .

# Construction de l'application web statique
RUN npx expo export --platform web

# Étape 2: Servir l'application avec Nginx
FROM nginx:alpine

# Copie les fichiers de build depuis l'étape précédente
COPY --from=build /app/dist /usr/share/nginx/html

# Copie la configuration Nginx personnalisée
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose le port 80
EXPOSE 80

# Commande par défaut pour démarrer Nginx
CMD ["nginx", "-g", "daemon off;"]
