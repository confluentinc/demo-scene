# Code heavily inspired (lifted pretty much verbatim) from https://djangostars.com/blog/how-to-create-and-deploy-a-telegram-bot/
# Changes made by @rmoff to add call out to ksqlDB
#
# To run this: 
#
# 1. Sign up for ngrok and run it: 
#   ./ngrok http 8080
# 2. Note the provided external URL from ngrok and set it as webhook for your telegram bot
#   curl -L http://api.telegram.org/botXXXXXYYYYYYYY/setWebHook?url=https://xxxyyy12345.ngrok.io
# 3. Run the bot
#   python bot.py
#
# Don't forget to also update the bot code for the hardcoded elements 
# with telegram bot auth token, ksqlDB connection details, etc. 

import requests  
from bottle import Bottle, response, request as bottle_request
import requests, json,datetime
 

class BotHandlerMixin:  
    BOT_URL = None

    def get_chat_id(self, data):
        """
        Method to extract chat id from telegram request.
        """
        chat_id = data['message']['chat']['id']

        return chat_id

    def get_message(self, data):
        """
        Method to extract message id from telegram request.
        """
        message_text = data['message']['text']

        return message_text

    def send_message(self, prepared_data):
        """
        Prepared data should be json which includes at least `chat_id` and `text`
        """       
        message_url = self.BOT_URL + 'sendMessage'
        requests.post(message_url, json=prepared_data)


class TelegramBot(BotHandlerMixin, Bottle):  
    BOT_URL = 'https://api.telegram.org/botXXXXXXXXYYYYYY/'

    def __init__(self, *args, **kwargs):
        super(TelegramBot, self).__init__()
        self.route('/', callback=self.post_handler, method="POST")

    def lookup_last_probe_enriched(self,device):
        ksqldb_url = "http://localhost:8088/query"
        headers = {'Content-Type':'application/vnd.ksql.v1+json; charset=utf-8'}
        query={'ksql':'SELECT PROBE_COUNT, FIRST_PROBE, LAST_PROBE, UNIQUE_SSIDS_PROBED, SSIDS_PROBED FROM PCAP_STATS_ENRICHED_01 WHERE ROWKEY = \''+device+'\';'}

        r = requests.post(ksqldb_url, data=json.dumps(query), headers=headers)

        if r.status_code==200:
            result=r.json()
            if len(result)==2:

                probe_count=result[1]['row']['columns'][0]
                probe_first=datetime.datetime.fromtimestamp(float(result[1]['row']['columns'][1])/1000).strftime("%Y-%m-%d %H:%M:%S")
                probe_last= datetime.datetime.fromtimestamp(float(result[1]['row']['columns'][2])/1000).strftime("%Y-%m-%d %H:%M:%S")
                unique_ssids=result[1]['row']['columns'][3]
                probed_ssids=result[1]['row']['columns'][4]

                return('📡 Wi-Fi probe stats for %s\n\tEarliest probe : %s\n\tLatest probe   : %s\n\tProbe count    : %d\n\tUnique SSIDs   : %s' % (device, probe_first, probe_last, probe_count, probed_ssids))
            else:
                return('🛎 No result found for device %s' % (device))
        else:
            return('❌ Query failed (%s %s)\n%s' % (r.status_code, r.reason, r.text))

    def lookup_last_probe(self,device):
        ksqldb_url = "http://localhost:8088/query"
        headers = {'Content-Type':'application/vnd.ksql.v1+json; charset=utf-8'}
        query={'ksql':'SELECT PROBE_COUNT, FIRST_PROBE, LAST_PROBE, UNIQUE_SSIDS_PROBED, SSIDS_PROBED FROM PCAP_STATS_01 WHERE ROWKEY = \''+device+'\';'}

        r = requests.post(ksqldb_url, data=json.dumps(query), headers=headers)

        if r.status_code==200:
            result=r.json()
            if len(result)==2:
                probe_count=result[1]['row']['columns'][0]
                probe_first=result[1]['row']['columns'][1]
                probe_last=result[1]['row']['columns'][2]
                unique_ssids=result[1]['row']['columns'][3]
                probed_ssids=result[1]['row']['columns'][4]

                return('📡 Wi-Fi probe stats for %s\n\tEarliest probe : %s\n\tLatest probe   : %s\n\tProbe count    : %d\n\tUnique SSIDs   : %d (%s)' % (device, probe_first, probe_last, probe_count, unique_ssids, probed_ssids))
            else:
                return('🛎 No result found for device %s' % (device))
        else:
            return('❌ Query failed (%s %s)\n%s' % (r.status_code, r.reason, r.text))

    def prepare_data_for_answer(self, data):
        message = self.get_message(data)
        print('👉 Received message sent to us:\n\t%s' % (message))
        answer = self.lookup_last_probe_enriched(message)
        print('👈 Returning message back to sender:\n\t%s' % (answer))
        chat_id = self.get_chat_id(data)
        json_data = {
            "chat_id": chat_id,
            "text": answer,
        }

        return json_data

    def post_handler(self):
        data = bottle_request.json
        answer_data = self.prepare_data_for_answer(data)
        self.send_message(answer_data)

        return response


if __name__ == '__main__':  
    app = TelegramBot()
    app.run(host='localhost', port=8080)