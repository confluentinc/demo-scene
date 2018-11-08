#!/bin/bash

export PLUGINPATH:="connect-plugins"
export INTERCEPTORSPATH:="monitoring-interceptors"
export INTERCEPTORS_VERSION="5.0.1"

all: prep interceptors

prep:
	if [[ ! -d $(PLUGINPATH) ]]; then mkdir $(PLUGINPATH); fi
	if [[ ! -d $(INTERCEPTORSPATH) ]]; then mkdir $(INTERCEPTORSPATH); fi

interceptors:
	./mvnw dependency:get -DremoteRepositories=http://packages.confluent.io/maven \
    -DgroupId=io.confluent \
    -DartifactId=monitoring-interceptors \
    -Dversion=$(INTERCEPTORS_VERSION) \
    -Dtransitive=false \
    -Ddest=$(INTERCEPTORSPATH)/

clean:
	rm -fr $(INTERCEPTORSPATH)/*.jar
