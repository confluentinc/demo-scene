import asyncio
import json
import random
import string
import pandas as pd
import streamlit as st
from confluent_kafka import Consumer, TopicPartition
from setupsocket import on_select
import altair as alt


config_dict = {
    "bootstrap.servers": st.secrets["BOOTSTRAP_URL"],
    "sasl.mechanisms": "PLAIN",
    "security.protocol": "SASL_SSL",
    "auto.offset.reset": "earliest",
    "session.timeout.ms": "45000",
    "sasl.username": st.secrets["SASL_USERNAME"],
    "sasl.password": st.secrets["SASL_PASSWORD"],
    "group.id": "consumer_of_stocks",
}

consumer = Consumer(config_dict)

st.title("Stock Price Averages")
st.write(
    "View tumbling averages for SPY stock. The chart may not show up if trading is closed for the day or otherwise not happening."
)

option = st.selectbox(
    "Start viewing stock for:",
    (["SPY"]),
    index=None,
)


async def main():
    if isinstance(option, str):
        # ordering the coroutines
        await asyncio.gather(on_select(option), display_quotes(placeholder))


async def display_quotes(component):
    component.empty()
    price_history = []
    window_history = []
    topic_name = option

    # starting from a specific partition here, it may be different depending on the topic so try a few out or just start from the beginning with the auto.offset.reset config
    partition = TopicPartition(f"tumble_interval_{topic_name}", 0, 7)
    consumer.assign([partition])
    consumer.seek(partition)

    while True:
        try:

            msg = consumer.poll(0.1)

            await asyncio.sleep(0.5)

            print("Received message: {}".format(msg))
            if msg is None:
                continue

            elif msg.error():
                print("Consumer error: {}".format(msg.error()))

            with component:
                # remove byte mess caused by json_registry in Flink processing vs json schema in consumer
                data_string_with_bytes_mess = "{}".format(msg.value())
                data_string_without_bytes_mess = data_string_with_bytes_mess.replace(
                    data_string_with_bytes_mess[0:22], ""
                )
                data_string_without_bytes_mess = data_string_without_bytes_mess[:-1]
                quote_dict = json.loads(data_string_without_bytes_mess)

                last_price = quote_dict["price"]

                window_end = quote_dict["window_end"]

                window_end_string = window_end[:0] + window_end[10:]

                price_history.append(last_price)
                window_history.append(window_end_string)

                # create data frame for altair to use
                data = pd.DataFrame(
                    {
                        "price_in_USD": price_history,
                        "window_end": window_history,
                    },
                )

                domain_end = max(price_history)
                domain_start = min(price_history)

                chart = (
                    alt.Chart(data)
                    .mark_line()
                    .encode(
                        x="window_end",
                        y=alt.Y(
                            "price_in_USD",
                            scale=alt.Scale(domain=[domain_start, domain_end]),
                        ),
                    )
                    .transform_window(
                        rank="rank()",
                        sort=[alt.SortField("window_end", order="descending")],
                    )
                    .transform_filter((alt.datum.rank < 20))
                )

                st.altair_chart(chart, theme=None, use_container_width=True)

        except KeyboardInterrupt:
            print("Canceled by user.")
            consumer.close()

        # We create the placeholder once


placeholder = st.empty()


st.subheader(
    "What's going on behind the scenes of this chart?",
    divider="rainbow",
)
st.image(
    "./graph.png",
    caption="chart graphing relationship of different nodes in the data pipeline",
)
st.markdown(
    "First, data is piped from the [Alpaca API](https://docs.alpaca.markets/docs/getting-started) websocket into a Kafka topic located in Confluent Cloud. Next, the data is processed in [Confluent Cloud’s](https://confluent.cloud/) Flink SQL workspace with a query like this."
)
st.code(
    """INSERT INTO tumble_interval
SELECT symbol, DATE_FORMAT(window_start,'yyyy-MM-dd hh:mm:ss.SSS'), DATE_FORMAT(window_end,'yyyy-MM-dd hh:mm:ss.SSS'), AVG(price)
FROM TABLE(
        TUMBLE(TABLE SPY, DESCRIPTOR($rowtime), INTERVAL '5' SECONDS))
GROUP BY
    symbol,
    window_start,
    window_end;
""",
    language="python",
)
st.markdown(
    "Then, the data is consumed from a Kafka topic backing the FlinkSQL table in Confluent Cloud, and visualized using Streamlit."
)
st.markdown(
    "For more background on this project and to run it for yourself, visit the [GitHub repository](https://github.com/Cerchie/alpaca-kafka-flink-streamlit/tree/main)."
)
st.markdown(
    "Note: the Kafka consumer for this project reads from the earlist offset on Mar 13"
)

asyncio.run(main())
