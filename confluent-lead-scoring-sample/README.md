At Confluent, we continuously strive to showcase the power of our data streaming platform through real-world applications, exemplified by [Customer Zero](https://www.confluent.io/blog/supercharge-lead-scoring-with-apache-flink-and-google-cloud-vertex-ai/), Confluent's initiative to adopt and use Confluent internally for production use cases.
In this repo, we present the latest use case of Customer Zero that harnesses the capabilities of generative AI, data streaming, and real-time predictions to enhance lead scoring for sales, helping our team prioritize high-value prospects and address complex challenges within our organization. 

By integrating these advanced technologies, we not only enhance our operational efficiency but also pave the way for innovative solutions that can dynamically respond to customer needs.

After the webinar, you might still be curious of the details of the whole pipeline. I'll breakdown the components bit by bit here. 

## Repo overview
This project is designed to build, package, and deploy a machine learning model for lead scoring. 
The core components include a Dockerfile for containerization, a `pyproject.toml` file for managing dependencies, 
and a set of Python files located under the `lead_scoring_transform` directory.

## Project Structure
Dockerfile: This file is used to create a Docker image that contains all the necessary dependencies to run the application. 
The image can be built using the `docker build` command below and pushed to a Docker Hub using the `docker push` command.

pyproject.toml: This file manages the project’s Python dependencies, including the required libraries for machine learning, 
data processing, model evaluation, and data streaming in Confluent Cloud.

lead_scoring_transform/: This folder contains the Python scripts that implement the logic for transforming raw data into a lead scoring model. 
The code in this directory includes the necessary steps for preprocessing data and scoring leads.

## Workflow
1. Building the Docker Image:
Using the Dockerfile, the project is packaged into a Docker image. This image includes all dependencies defined in `pyproject.toml` and the Python files from the `lead_scoring_transform` folder.
```
docker build -t <your_image_name> .
```

2. Pushing the Docker Image:
After building the Docker image, it is pushed to a Docker registry (in this project we used Google Container Registry) for easy access during deployment.
```
docker push <your_image_name>
```

3. Import the Model to Vertex AI:
You can deploy your model using Google Cloud's Vertex AI platform, ensuring that your lead scoring model is operational and ready for inference.
   Please refer to [this article](https://medium.com/@angusll/deploying-an-deep-learning-model-using-kserve-and-google-cloud-vertex-ai-ae2850caf27) for step by step details
4. Deploy model to Vertex AI Endpoint
After the model is imported, you can deploy it to an endpoint for online serving. Similarly, please refer to the article above for step by step details.
5. Once you've deployed the model to an endpoint, you can send it a POST request to verify that it's working as expected.
```
curl \
-X POST \
-H "Authorization: Bearer $(gcloud auth print-access-token)" \
-H "Content-Type: application/json" \
"https://us-central1-aiplatform.googleapis.com/v1/projects/${PROJECT_ID}/locations/us-central1/endpoints/${ENDPOINT_ID}:predict" \
-d "@${INPUT_DATA_FILE}"
```
## KServe
In this project, KServe is used to build the model server for the following reasons:

1. Flexibility. If the default Serving Runtime doesn't meet your requirements, you can create your own model server using the KServe ModelServer API to deploy it as a Custom Serving Runtime on KServe. It offers great flexibility that caters to the various goals we hope to achieve with the model server.

2. Simplicity. KServe offers machine learning-specific template methods like preprocess, predict, and postprocess, allowing you to seamlessly deploy your model once you inherit them correctly.

All you need to do is:

1. Subclass `kserve.Model`.

2. Override the `load`, `preprocess`, and `predict` methods.

Below is a code sample of how to implement your custom model based on the two steps above. This is the only Python file that you need in this project.

```
from kserve import Model, ModelServer
from typing import Dict

class YourModel(Model):
    def __init__(self, name: str):
        super().__init__(name)
        self.name = name
        self.load()

    def load(self):
        # implement your load method

    def preprocess(self):
        # implement your preprocess method

    def predict(self, request: Dict) -> Dict:
        # implement your predict method

if __name__ == "__main__":
    model = YourModel("custom-model")
    kserve.ModelServer().start([model])
```

## Additional Resources
- [Companion blog post](https://www.confluent.io/blog/supercharge-lead-scoring-with-apache-flink-and-google-cloud-vertex-ai/) to this demo
- [How to deploy a custom Python serving runtime in KServe](https://kserve.github.io/website/latest/modelserving/v1beta1/custom/custom_model/)
- [KServe Model Source Code](https://github.com/kserve/kserve/blob/master/python/kserve/kserve/model.py#L183)
- How to [import models to Vertex AI](https://cloud.google.com/vertex-ai/docs/model-registry/import-model) and [deploy a model to an endpoint](https://cloud.google.com/vertex-ai/docs/general/deployment)