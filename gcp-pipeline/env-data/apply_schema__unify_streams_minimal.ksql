CREATE STREAM ENVIRONMENT_DATA WITH \
        (VALUE_FORMAT='AVRO') AS \
SELECT  items->stationreference AS stationreference, \
        items->earegionname AS earegionname, \
        items->label AS label, \
        items->lat AS lat, items->long AS long, \
        items->measures->latestreading->datetime AS reading_ts, \
        items->measures->parameterName AS parameterName, \
        items->measures->latestreading->value AS reading_value, \
        items->measures->unitname AS unitname \
 FROM   flood_monitoring_L2404 ;

INSERT INTO ENVIRONMENT_DATA \
SELECT  items->stationreference AS stationreference, \
        items->earegionname AS earegionname, \
        items->label AS label, \
        items->lat AS lat, items->long AS long, \
        items->measures->latestreading->datetime AS reading_ts, \
        items->measures->parameterName AS parameterName, \
        items->measures->latestreading->value AS reading_value, \
        items->measures->unitname AS unitname \
 FROM   flood_monitoring_L2481 ;

INSERT INTO ENVIRONMENT_DATA \
SELECT  items->stationreference AS stationreference, \
        items->earegionname AS earegionname, \
        items->label AS label, \
        items->lat AS lat, items->long AS long, \
        items->measures->latestreading->datetime AS reading_ts, \
        items->measures->parameterName AS parameterName, \
        items->measures->latestreading->value AS reading_value, \
        items->measures->unitname AS unitname \
 FROM   flood_monitoring_059793 ;

INSERT INTO ENVIRONMENT_DATA \
SELECT  items->stationreference AS stationreference, \
        items->earegionname AS earegionname, \
        items->label AS label, \
        items->lat AS lat, items->long AS long, \
        items->measures[0]->latestreading->datetime AS reading_ts, \
        items->measures[0]->parameterName AS parameterName, \
        items->measures[0]->latestreading->value AS reading_value, \
        items->measures[0]->unitname AS unitname \
 FROM   flood_monitoring_3680 ;

INSERT INTO ENVIRONMENT_DATA \
SELECT  items->stationreference AS stationreference, \
        items->earegionname AS earegionname, \
        items->label AS label, \
        items->lat AS lat, items->long AS long, \
        items->measures[1]->latestreading->datetime AS reading_ts, \
        items->measures[1]->parameterName AS parameterName, \
        items->measures[1]->latestreading->value AS reading_value, \
        items->measures[1]->unitname AS unitname \
 FROM   flood_monitoring_3680 ;
