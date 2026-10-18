FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --legacy-peer-deps
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
ENV BACKEND_URL=http://127.0.0.1:8080 \
    AI_SERVICE_URL=http://127.0.0.1:8091 \
    NGINX_ENVSUBST_FILTER=^(BACKEND_URL|AI_SERVICE_URL)$
COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/intellibank/browser /usr/share/nginx/html
EXPOSE 80
