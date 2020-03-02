FROM fedora:latest

LABEL maintainer="Ricardo Ferreira <riferrei@riferrei.net>"

# Install pre-reqs
RUN dnf install wget -y
RUN dnf install gcc -y

# Install librdkafka
RUN rpm --import https://packages.confluent.io/rpm/5.4/archive.key
COPY confluent.repo /etc/yum.repos.d
RUN dnf install librdkafka-devel -y

# Install Go v1.14
RUN wget https://dl.google.com/go/go1.14.linux-amd64.tar.gz && tar -xvf go1.14.linux-amd64.tar.gz && rm go1.14.linux-amd64.tar.gz
RUN mv go /usr/local
ENV GOROOT=/usr/local/go
ENV PATH="${GOROOT}/bin:${PATH}"

# Build the application
WORKDIR /redis-sink
COPY go.mod .
COPY redis-sink.go .
RUN go build -o redis-sink .

CMD ["./redis-sink"]