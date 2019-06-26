FROM node:dubnium-stretch
RUN mkdir -p /home/node/app/node_modules && \
    chown -R node:node /home/node/app && \
    apt-get update && \
    apt-get install -y kafkacat
WORKDIR /home/node/app
COPY package*.json ./
USER node
RUN npm install
COPY --chown=node:node . .
EXPOSE 3000
CMD [ "bash", "start.sh" ] 
