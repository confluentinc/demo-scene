import shap
import numpy
import pandas as pd
from cloudevents.abstract import CloudEvent
from kserve import Model, ModelServer
from google.cloud import storage
from tritonclient.grpc.service_pb2 import ModelInferRequest, ModelInferResponse
from google.cloud import bigquery
from collections import OrderedDict
from typing import Union, Dict
from confluent_kafka import Producer
from confluent_kafka.serialization import StringSerializer, SerializationContext, MessageField
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer
from lead_scoring_transform.configs import load_pickle_file


class LeadScoringTransformerServer(Model):

    def __init__(
            self,
            model_bucket: str,
            model_key: str,
            project_id: str,
            name: str,
    ):
        super().__init__(name)
        self.model_bucket = model_bucket
        self.model_key = model_key
        self.project_id = project_id

    def preprocess(self, request: Union[Dict, CloudEvent]) -> pd.DataFrame:
        sample = request['instances']

        df = pd.DataFrame.from_dict(sample)

        # TODO: Preprocess your data here and return df

        return df

    def predict(self, df: pd.DataFrame) -> Union[Dict, ModelInferResponse]:
        # Select the final set of features here
        final_features = []

        final_df = df[final_features]

        vertex_ai_response = []

        for row in final_df.itertuples(index=False, name=None):
            row_df = pd.DataFrame([row], columns=final_df.columns)

            # Load your model. In this project our team trained our model offline and upload it as a pickle load_pickle_file.
            # We recommend you check out Vertex AI’s documentation on using AutoML and custom training to train your models.
            selected_model = load_pickle_file("<your model name>")

            # TODO: Call the 'predict_proba' function of the model to generate outputs
            scores = numpy.round(selected_model.predict_proba(row_df)[:, 1] * 100)

            row_df['lead_score'] = scores
            row_dict = row_df.to_dict(orient='records')

            for item in row_dict:
                # After you've got the prediction results, you can do all sorts of things here, including sending them to a kafka topic
                avro_serializer = AvroSerializer(schema_registry_client, avro_schema, processed_data_to_dict)
                producer.produce(topic=topic_processed_data, value=avro_serializer(processed_data_instance, SerializationContext(topic, MessageField.VALUE)))
                producer.flush()

            # TODO: Gather all useful info and send them to output
            prediction_object = OrderedDict()
            prediction_object['leadorcontactid'] = lead_or_contact_id
            prediction_object['score'] = scores
            prediction_object['reasons'] = reason_lst
            prediction_object['model_group'] = model_group
            vertex_ai_response.append(prediction_object)

        return {"predictions": [dict(result) for result in vertex_ai_response]}

def main():
    model = LeadScoringTransformerServer(
        model_bucket="your-model-bucket",
        model_key="your-model-key",
        project_id="your-project-id",
        name="your-project-name",
    )
    # model.load()
    ModelServer(http_port=8080, workers=1).start(models=[model])