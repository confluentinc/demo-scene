#!/bin/sh

# Make the folder readable to all so that the files can 
# be shared with other Docker images
chmod o+rx -R /opt/oracle/product/12.2.0.1/dbhome_1/inventory
chmod o+rx -R /opt/oracle/product/12.2.0.1/dbhome_1/bin
