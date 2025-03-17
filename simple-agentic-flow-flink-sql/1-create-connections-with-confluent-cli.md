# Setting up connections to OpenAI and Pinecone

Documentation reference: https://docs.confluent.io/confluent-cli/current/command-reference/flink/connection/confluent_flink_connection_create.html


## Pinecone

```bash
confluent flink connection create pinecone-connection --environment your-confluent-environment-name \
--cloud AWS \
--region us-east-1 \
--type pinecone \
--endpoint https://change-with-your-pinecone-endpoint.pinecone.io/query \
--api-key change-with-your-pinecone-key
```

## OpenAI embedding

```bash
confluent flink connection create openai-connection-vector-embeddings \
--environment your-confluent-environment-name \
--cloud AWS \
--region us-east-1 \
--type openai \
--endpoint https://api.openai.com/v1/embeddings \
--api-key change-with-your-open-ai-key
```

## OpenAI completions

```bash
confluent flink connection create openai-connection-completions \
--cloud AWS \
--region us-east-1 \
--environment your-confluent-environment-name \
--type openai \
--endpoint https://api.openai.com/v1/chat/completions \
--api-keychange-with-your-open-ai-key
```
