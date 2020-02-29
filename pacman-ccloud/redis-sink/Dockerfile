FROM ubuntu:latest

LABEL maintainer="Ricardo Ferreira <riferrei@riferrei.net>"

# Install pre-reqs
RUN apt-get update
RUN apt-get install -y software-properties-common apt-utils
RUN apt-get install -y wget

# Confluent Golang
RUN wget -qO - https://packages.confluent.io/deb/5.4/archive.key | apt-key add -
RUN add-apt-repository "deb [arch=amd64] https://packages.confluent.io/deb/5.4 stable main"
RUN apt-get install -y pkg-config
RUN apt-get install -y libsasl2-dev
RUN apt-get install -y librdkafka-dev

# Get Golang 1.13
RUN wget https://dl.google.com/go/go1.13.linux-amd64.tar.gz
RUN tar -xvf go1.13.linux-amd64.tar.gz
RUN rm go1.13.linux-amd64.tar.gz
RUN mv go /usr/local
ENV GOROOT=/usr/local/go
ENV PATH="${GOROOT}/bin:${PATH}"

# Build the app
WORKDIR /redis-sink
COPY go.mod .
COPY redis-sink.go .
RUN go build -o redis-sink .

CMD ["./redis-sink"]