from typing import Union, Dict
from cloudevents.abstract import CloudEvent
from kserve import Model, ModelServer
from tritonclient.grpc.service_pb2 import ModelInferRequest


class LeadScoringServer(Model):
    def preprocess(
        self, request: Union[Dict, CloudEvent]
    ) -> Union[Dict, ModelInferRequest]:
        return request

def main():
    model = LeadScoringServer("lead-scoring-server")
    ModelServer(http_port=8080).start(models=[model])
