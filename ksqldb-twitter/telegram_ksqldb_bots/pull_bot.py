# Code heavily inspired (lifted pretty much verbatim) from https://djangostars.com/blog/how-to-create-and-deploy-a-telegram-bot/
# Changes made by @rmoff to add call out to ksqlDB
#
# To run this: 
#
# 1. Sign up for ngrok and run it: 
#   ./ngrok authtoken <ngrok token>
#   ./ngrok http 8080
# 2. Note the provided external URL from ngrok and set it as webhook for your telegram bot
#   curl -L http://api.telegram.org/bot<token>/setWebHook?url=http://<id>.ngrok.io
# 3. Run the bot
#   python pull_bot.py
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
    BOT_URL = 'https://api.telegram.org/bot<token>/'

    def __init__(self, *args, **kwargs):
        super(TelegramBot, self).__init__()
        self.route('/', callback=self.post_handler, method="POST")

    def lookup_user_stats(self,user):
        ksqldb_url = "http://localhost:8088/query"
        headers = {'Content-Type':'application/vnd.ksql.v1+json; charset=utf-8'}
        query={'ksql':'SELECT TWEET_COUNT, FIRST_TWEET_TS, LATEST_TWEET_TS, LATEST_TWEET FROM USER_STATS WHERE ROWKEY = \''+user+'\';'}

        r = requests.post(ksqldb_url, data=json.dumps(query), headers=headers)

        if r.status_code==200:
            result=r.json()
            if len(result)==2:
                tweet_count=result[1]['row']['columns'][0]
                first_tweet_ts=result[1]['row']['columns'][1]
                last_tweet_ts=result[1]['row']['columns'][2]
                last_tweet=result[1]['row']['columns'][3]

                return('📡 Twitter stats for %s\n\tTweet count : %s, first tweet was at %s, last tweet was at %s\n\tLatest tweet:\n%s' % (user, tweet_count, first_tweet_ts, last_tweet_ts, last_tweet))
            else:
                return('🛎 No result found for user %s' % (user))
        else:
            return('❌ Query failed (%s %s)\n%s' % (r.status_code, r.reason, r.text))

    def prepare_data_for_answer(self, data):
        message = self.get_message(data)
        print('👉 Received message sent to us:\n\t%s' % (message))
        answer = self.lookup_user_stats(message)
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
