FROM node:20-alpine AS build
WORKDIR /app

# Cache node_modules layer
COPY package*.json ./
RUN npm ci --legacy-peer-deps

COPY . .
RUN npx ng build --configuration production --output-path /dist

# Production Nginx stage
FROM nginx:alpine
COPY --from=build /dist/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
