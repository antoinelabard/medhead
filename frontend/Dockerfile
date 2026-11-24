# syntax=docker/dockerfile:1
# Contexte de build : racine du dépôt (le client est généré depuis contracts/openapi).

FROM openapitools/openapi-generator-cli:v7.27.0 AS api-client
COPY contracts/openapi/ /contracts/openapi/
COPY frontend/openapi-generator.config.json /config/openapi-generator.config.json
RUN bash /usr/local/bin/docker-entrypoint.sh generate \
      -i /contracts/openapi/medhead-api.yaml \
      -g typescript-angular \
      -c /config/openapi-generator.config.json \
      -o /generated

FROM node:24.22.3-alpine AS build
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --ignore-scripts --no-audit --no-fund
COPY frontend/ ./
COPY --from=api-client /generated/ src/app/api/generated/
RUN npx ng build --configuration production

FROM nginxinc/nginx-unprivileged:1.30.6-alpine
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /frontend/dist/frontend/browser/ /usr/share/nginx/html/
USER 101
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --retries=6 \
    CMD wget -q --spider http://127.0.0.1:8080/ || exit 1
