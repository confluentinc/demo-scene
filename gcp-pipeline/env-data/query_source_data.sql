SET 'auto.offset.reset' = 'earliest';

select items->stationreference, \
       items->earegionname, \
       items->eaareaname, \
       items->town, \
       items->RiverName, \
       items->label, \
       items->lat, items->long, \
       items->measures->latestreading->datetime,\
       items->measures->parameterName, \
       items->measures->latestreading->value, \
       items->measures->unitname \
from   flood_monitoring_L2481 limit 2;

select items->stationreference, \
       items->earegionname, \
       items->eaareaname, \
       items->town, \
       items->RiverName, \
       items->label, \
       items->lat, items->long, \
       items->measures->latestreading->datetime,\
       items->measures->parameterName, \
       items->measures->latestreading->value, \
       items->measures->unitname \
from   flood_monitoring_L2404 limit 2;

select items->stationreference, \
       items->earegionname, \
       items->eaareaname, \
       items->town, \
       items->RiverName, \
       items->label, \
       items->lat, items->long, \
       items->measures->latestreading->datetime,\
       items->measures->parameterName, \
       items->measures->latestreading->value, \
       items->measures->unitname \
from   flood_monitoring_059793 limit 2;
