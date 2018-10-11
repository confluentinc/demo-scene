-- York rainfall
DROP STREAM flood_monitoring_059793;
CREATE STREAM flood_monitoring_059793 \
    (meta STRUCT<publisher VARCHAR, \
                 comment VARCHAR>, \
     items STRUCT<eaRegionName VARCHAR, \
                  label VARCHAR, \
                  stationReference VARCHAR, \
                  lat DOUBLE, \
                  long DOUBLE, \
                  measures STRUCT<label VARCHAR, \
                        latestReading STRUCT<\
                            dateTime VARCHAR, \
                            value DOUBLE>,\
                        parameterName VARCHAR, \
                        unitName VARCHAR>> \
    ) WITH (KAFKA_TOPIC='flood-monitoring-059793-x04',VALUE_FORMAT='JSON');

DROP STREAM flood_monitoring_L2404;
CREATE STREAM flood_monitoring_L2404 \
    (meta STRUCT<publisher VARCHAR, \
                 comment VARCHAR>, \
     items STRUCT<eaRegionName VARCHAR, \
                  label VARCHAR, \
                  stationReference VARCHAR, \
                  lat DOUBLE, \
                  long DOUBLE, \
                  measures STRUCT<label VARCHAR, \
                        latestReading STRUCT<\
                            dateTime VARCHAR, \
                            value DOUBLE>,\
                        parameterName VARCHAR, \
                        unitName VARCHAR>> \
    ) WITH (KAFKA_TOPIC='flood-monitoring-L2404-x04',VALUE_FORMAT='JSON');

DROP STREAM flood_monitoring_L2481;
CREATE STREAM flood_monitoring_L2481 \
    (meta STRUCT<publisher VARCHAR, \
                 comment VARCHAR>, \
     items STRUCT<eaRegionName VARCHAR, \
                  label VARCHAR, \
                  stationReference VARCHAR, \
                  lat DOUBLE, \
                  long DOUBLE, \
                  measures STRUCT<label VARCHAR, \
                        latestReading STRUCT<\
                            dateTime VARCHAR, \
                            value DOUBLE>,\
                        parameterName VARCHAR, \
                        unitName VARCHAR>> \
    ) WITH (KAFKA_TOPIC='flood-monitoring-L2481-x04',VALUE_FORMAT='JSON');

DROP STREAM flood_monitoring_3680;
CREATE STREAM flood_monitoring_3680 \
    (meta STRUCT<publisher VARCHAR, \
                 comment VARCHAR>, \
     items STRUCT<eaRegionName VARCHAR, \
                  label VARCHAR, \
                  stationReference VARCHAR, \
                  lat DOUBLE, \
                  long DOUBLE, \
                  measures ARRAY<STRUCT<label VARCHAR, \
                        latestReading STRUCT<\
                            dateTime VARCHAR, \
                            value DOUBLE>,\
                        parameterName VARCHAR, \
                        unitName VARCHAR>>> \
    ) WITH (KAFKA_TOPIC='flood-monitoring-3680',VALUE_FORMAT='JSON');
