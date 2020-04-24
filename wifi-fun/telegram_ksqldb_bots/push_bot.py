# @rmoff 24 April 2020
#
import requests,json
#
# Change these two values for your own auth token and chat id
BOT_URL = 'https://api.telegram.org/botxxxxx:YYYYYYYY/'
CHAT_ID=-431366401
#
#
def sendMessage(message):
    message_url = BOT_URL + 'sendMessage'

    message_data = {
        "chat_id": CHAT_ID,
        "text": message,
    }            

    print('👈 Sending:\n\t%s' % (message_data))

    requests.post(message_url, json=message_data)

def runQueryWithLookup():
    print('--> runQueryWithLookup <--')

    ksqlDB_url = "http://localhost:8088/query"


    query   = """
            SELECT TIMESTAMPTOSTRING(P.ROWTIME,'yyyy-MM-dd HH:mm:ss',
                                                'Europe/London') AS TS, 
                P.WLAN_SA[1]                                      AS MAC, 
                P.WLAN_SSID[1]                                    AS SSID,
                S.REASON                                          AS REASON  
            FROM PCAP_PROBE P
                    INNER JOIN SSID_ALERT_LIST S
                    ON P.WLAN_SSID[1] = S.ROWKEY
             EMIT CHANGES;
            """          
    payload = {"ksql": query, 
               "streamsProperties": {
                   "ksql.streams.auto.offset.reset": "latest"}
                }
    headers= {
        "Content-Type": "application/vnd.ksql.v1+json",
        "Accept": "application/vnd.ksql.v1+json",
        "Accept-Encoding": "chunked"
    }                

    print('⚡️ Sending query to ksqlDB:\n\t%s' % (payload))
    r = requests.request("POST", ksqlDB_url, headers=headers, data=json.dumps(payload), stream=True)

    if r.encoding is None:
        r.encoding = 'utf-8'

    for line in r.iter_lines(decode_unicode=True):
        if line:
            print('⬅️ Got chunk from ksqlDB: %s' % (line))
            # This is a nasty hack, but the data that comes back is a JSON array in 
            # bleeding chunks, e.g. one line at a time of this: 
            #   [{"header":{"queryId":"none","schema":"`TS` STRING, `KSQL_COL_1` STRING, `WLAN_SSID` ARRAY<STRING>"}},
            #   {"row":{"columns":["2020-03-09 15:46:40","48:d3:43:43:cd:d1",["VM9654567"]]}},
            #   {"row":{"columns":["2020-03-09 15:48:49","c8:d1:2a:96:cc:64",["VMP9476994"]]}},
            #   {"finalMessage":"Limit Reached"}]
            # So each line received is not in itself valid JSON. Hence, a dirty hack to strip the trailing
            # comma from a row and then treat it as JSON on its own.
            # If the API changes, this code will 💔
            if "row" in line and "columns" in line:
                # strip the final character from the line
                j=line[:-1]
                # load the JSON object
                result=json.loads(j)
                # Extract each column
                probe_ts=result['row']['columns'][0]
                probe_mac=result['row']['columns'][1]
                probe_ssid=result['row']['columns'][2]
                probe_reason=result['row']['columns'][3]

                sendMessage('📣 At %s MAC address %s probed for SSID `%s`\n\nThis SSID is being tracked because: %s' % (probe_ts,probe_mac,probe_ssid,probe_reason))

# Yes I know that this could & should be refactored into a single routine with the one above. Feel free to submit a PR :-)
def runQuery():
    print('--> runQuery <--')
    ksqlDB_url = "http://localhost:8088/query"
    query   = """
        SELECT TIMESTAMPTOSTRING(ROWTIME,'yyyy-MM-dd HH:mm:ss','Europe/London') AS TS, 
               WLAN_SA[1], 
               WLAN_SSID[1] 
          FROM PCAP_PROBE 
         WHERE WLAN_SSID[1] = 'RNM0'
          EMIT CHANGES LIMIT 1;
          """   
    payload = {"ksql": query, 
               "streamsProperties": {
                   "ksql.streams.auto.offset.reset": "latest"}
                }
    headers= {
        "Content-Type": "application/vnd.ksql.v1+json",
        "Accept": "application/vnd.ksql.v1+json",
        "Accept-Encoding": "chunked"
    }                

    print('⚡️ Sending query to ksqlDB:\n\t%s' % (payload))
    r = requests.request("POST", ksqlDB_url, headers=headers, data=json.dumps(payload), stream=True)

    if r.encoding is None:
        r.encoding = 'utf-8'

    for line in r.iter_lines(decode_unicode=True):
        if line:
            print('⬅️ Got chunk from ksqlDB: %s' % (line))
            # This is a nasty hack, but the data that comes back is a JSON array in 
            # bleeding chunks, e.g. one line at a time of this: 
            #   [{"header":{"queryId":"none","schema":"`TS` STRING, `KSQL_COL_1` STRING, `WLAN_SSID` ARRAY<STRING>"}},
            #   {"row":{"columns":["2020-03-09 15:46:40","48:d3:43:43:cd:d1",["VM9654567"]]}},
            #   {"row":{"columns":["2020-03-09 15:48:49","c8:d1:2a:96:cc:64",["VMP9476994"]]}},
            #   {"finalMessage":"Limit Reached"}]
            # So each line received is not in itself valid JSON. Hence, a dirty hack to strip the trailing
            # comma from a row and then treat it as JSON on its own.
            # If the API changes, this code will 💔
            if "row" in line and "columns" in line:
                # strip the final character from the line
                j=line[:-1]
                # load the JSON object
                result=json.loads(j)
                # Extract each column
                probe_ts=result['row']['columns'][0]
                probe_mac=result['row']['columns'][1]
                probe_ssid=result['row']['columns'][2]

                sendMessage('📣 At %s MAC address %s probed for SSID `%s`' % (probe_ts,probe_mac,probe_ssid))

runQueryWithLookup()
