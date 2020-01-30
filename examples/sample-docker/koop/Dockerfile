FROM node:10.18.1-alpine AS base

# tools
RUN apk --update add git && \
    rm -rf /var/lib/apt/lists/* && \
    rm /var/cache/apk/*

# install Koop
RUN git clone https://github.com/koopjs/koop-provider-marklogic.git /app
WORKDIR /app
COPY docker.json /app/config
RUN npm install
ENV NODE_ENV=docker

EXPOSE 80

CMD node server.js
