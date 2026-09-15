FROM node:20-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install --no-audit --no-fund
COPY . .
RUN npm run build
ARG RELEASE_SHA=local
RUN printf '{"commit":"%s"}\n' "$RELEASE_SHA" > dist/tplug-erp/browser/build-info.json
FROM nginx:1.28-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/tplug-erp/browser /usr/share/nginx/html
EXPOSE 80
