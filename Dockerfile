# ==========================================
# TRAXUP Central - Build
# ==========================================
FROM node:20-bookworm-slim AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

RUN npm run build


# ==========================================
# TRAXUP Central - Produção
# ==========================================
FROM nginx:alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build /app/dist/erp-central-produto/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
