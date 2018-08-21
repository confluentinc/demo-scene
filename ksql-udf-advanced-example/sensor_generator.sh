#! /bin/bash

#Log levels
OFF=0
FATAL=100
ERROR=200
WARN=300
INFO=400
DEBUG=500

#Sensor value range
MINVAL=2
MAXVAL=8
DECVAL=2
EVENTID=99999

#Measurement array
ASIZE=210
ADELIM="#"
initVal=""
msgSize=0
msgArr=""
arrLen=0
msgNr=0

#MQTT Client command
PUBEXE="mosquitto_pub"
HOST="0.0.0.0" 
PORT=1883
TOPIC="car/engine/temperature"
QOS=2

#Set log level for the script
LOGLEVEL=$INFO

log()
{ # This function will log the input message to STDOUT if the log level for the message is lower or equal to the log level set for the script
	local logMessage=$1
	local msgLevel=$2

	if [ $LOGLEVEL -ge $msgLevel ]
	then
		echo -e $logMessage
	fi
}

initialVal()
{ # This function initialises a sensor value for the signal we want to generate
  
  local initBase=-1
  local initFract=0
  local decMod=$((10**$DECVAL))
  log "Modulus divisor value for decimal generation is: $decMod" $DEBUG

  until [ $initBase -ge $MINVAL ] && [ $initBase -lt $MAXVAL ]
  do
    initBase=$((RANDOM % $MAXVAL))
    log "Ïnitial generated sensor base value is: $initBase" $DEBUG
  done

  initFract=$(($RANDOM % $decMod))

  # Prepend 0's for fractions with a shorter length then the nr of defined decimails0
  while [ ${#initFract} -lt $DECVAL ]
  do
    initFract=0${initFract}
    log "Random generated number was smaller than 100, appending it with zeros. Current number value is $initFract" $DEBUG
  done

  initVal="${initBase}.${initFract}"
  log "Ïnitial generated sensor value is: $initVal" $DEBUG
  return 0
}

genInterval()
{ # This function will generate an increasing or decreasing interval of sensor reading values, based on its input parameters of startReading, intervalSize and intervalDirection

  local sensReading="`echo $1 | cut -f 1 -d '.'``echo $1 | cut -f 2 -d '.'`"
  log "Last sensor reading value: $sensReading" $DEBUG
  local intSize=$2
  log "Interval size: $intSize" $DEBUG
  local intDirection=$3
  local incrDiv=0
  local readIncr=-1
  local newReading
  local intArr=""
  local i=0

  while [ $i -lt $intSize ]
  do
    # Generate an reading value increment size (choice between 1/100th or 1/10th multple)
    incrDiv=$((10**$(($(($RANDOM % 2))+1))))
    log "Increment divisor: $incrDiv" $DEBUG
    
    # Generate reading value increment
    readIncr=$(($RANDOM % $incrDiv))
    
    if [ $intDirection -eq 0 ]
    then 
      readIncr=$(($readIncr * -1))
    fi    
    log "Reading increment is : $readIncr" $DEBUG
   
    #Calculate new reading
    sensReading=$(($sensReading + $readIncr))
    log "New generated reading number in interval is: $sensReading" $DEBUG
    
    newReading="`echo $sensReading | cut -c 1`.`echo $sensReading|cut -c 2-3`"
    log "New generated reading value in interval is: $newReading" $DEBUG

    # Swap the interval direction in case we've crossed a boundary value
    if [ $intDirection -eq 0 ] && [ $sensReading -le $(($MINVAL * 100)) ]
    then
      intDirection=1
    elif [ $intDirection -eq 1 ] && [ $sensReading -ge $(($MAXVAL * 100)) ]
    then
      intDirection=0
    fi

    intArr="${intArr}${newReading}${ADELIM} "
    log "Current sensor readings interval array: $intArr" $DEBUG
    i=$(($i+1))
  done
  msgArr=${msgArr}${intArr}
  msgSize=$(($msgSize + $intSize))
  return 0
}


randomTemp()
{ # This function will generate a random temperature value between 0 and 10 and two decimals precision

 local temp=""
 # Generate random number	
 local rNum=$RANDOM
 log "Random generated number : $rNum" $DEBUG

  # Append 0's for random numbers smaller than 100
  while [ ${#rNum} -lt 3 ]
  do 
    rNum=${rNum}0
    log "Random generated number was smaller than 100, appending it with zeros. Current number value is $rNum" $DEBUG
  done

  # Construct random temperature value from first 3 digits of random number
  temp="`echo $rNum|cut -c 1`.`echo $rNum|cut -c 2-3`"
  log "Random generated temperature value of $temp" $DEBUG

  return $temp
}

# We are not in the main program. Let's first initialize a first measurement value
initialVal

if [ $? -eq 0 ]
then 
  log "initialVal function returned initial sensor value : $initVal" $DEBUG

  # Now we will start the endless loop to generate sensorvalues in ranges
  while true;
  do
    if [ $msgSize -eq 0 ]
    then
      #initialise a new message array
      msgNr=$(($msgNr + 1))
      msgArr="${EVENTID},"
    fi

    # Now lets generate sensor value ranges that increase or decrease from the current sensor value
    rangeLength=$(($(($RANDOM % 10))+1)) # a value between 1 and 10
    if [ $ASIZE -lt $(($msgSize + $rangeLength)) ]
    then
	    rangeLength=$(($ASIZE - $msgSize))
    fi
    log "Length for the interval is: $rangeLength" $DEBUG

    rangeDir=$((RANDOM % 2)) # 0 is decreasing interval and 1 is an increasing interval
    log "Direction for the interval is: $rangeDir" $DEBUG
   
    genInterval $initVal $rangeLength $rangeDir

    if [ $? -eq 0 ]
    then
      arrLen=${#msgArr}
      log "Current measurements array for next message to sent is: $msgArr" $DEBUG
      if [ $msgSize -eq $ASIZE ]
      then 
	# We need to prepare the message for sending, by removing the last two characters (delimter)
	msgArr=`echo $msgArr | cut -c 1-$(($arrLen - 2))`
    # log "${PUBEXE} -h ${HOST} -p ${PORT} -t ${TOPIC} -q ${QOS} -m \"${msgArr}\"" $DEBUG
    $(${PUBEXE} -h ${HOST} -p ${PORT} -t ${TOPIC} -q ${QOS} -m "${msgArr}")

	if [ $? -eq 0 ] 
        then 
          log "New message published, nr: $msgNr" $INFO
	  msgSize=0

	  log "Current message measurement array has a length of : $arrLen" $DEBUG
       	  initVal=`echo $msgArr | cut -c $(($arrLen - 5))-$(($arrLen))`
   	  log "New start measurement value for next message is : $initVal" $DEBUG
        fi
      else
        # We need take the last measurement from the interval and set it as the initialisation value for the next interval
	log "Current message measurement array has a length of : $arrLen" $DEBUG
	initVal=`echo $msgArr | cut -c $(($arrLen - 5))-$(($arrLen - 2))`
	log "New start measurement value for next interval is : $initVal" $DEBUG
      fi
    fi
  done
fi
