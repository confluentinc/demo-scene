FROM ubuntu:latest

RUN apt-get update && apt-get install -y git wget curl python
RUN wget https://pkg.osquery.io/deb/osquery_4.0.2_1.linux.amd64.deb
RUN dpkg -i osquery_4.0.2_1.linux.amd64.deb
RUN apt-get install -f
RUN curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
RUN python get-pip.py
ADD requirements.txt /
RUN pip install -r requirements.txt
RUN useradd -ms /bin/bash jay
RUN useradd -ms /bin/bash neha
RUN useradd -ms /bin/bash jun
