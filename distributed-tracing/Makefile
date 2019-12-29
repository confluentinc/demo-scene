#!/bin/bash

export INTERCEPTORSPATH:=tracing-interceptors
export INTERCEPTORS_VERSION:=master-SNAPSHOT

all: clean prep build downloadInterceptors

clean:
	rm -fr $(INTERCEPTORSPATH)/*.jar
	
prep:
	if [[ ! -d $(INTERCEPTORSPATH) ]]; then mkdir $(INTERCEPTORSPATH); fi

build:
	./mvnw -f app/pom.xml clean install
	
downloadInterceptors:
	cd $(INTERCEPTORSPATH)
	./mvnw dependency:get -DremoteRepositories=https://jitpack.io \
    -DgroupId=com.github.riferrei \
    -DartifactId=kafka-tracing-support \
    -Dversion=$(INTERCEPTORS_VERSION) \
    -Dtransitive=false \
    -Ddest=./$(INTERCEPTORSPATH) \
     