# Building Agentic Systems with Apache Kafka and Apache Flink

This repository demonstrates how to create a simple agentic workflow using **Apache Kafka, Apache Flink, OpenAI models, Pinecone, and MongoDB**.

You can watch the recording of the talk **"Building Agentic Systems with Apache Kafka and Apache Flink"** by **Olena Kutsenko** at *Current Bengaluru 2025* for further insights.

## Use Case: Agentic System for Pet Owners

This system is designed as a **one-stop solution** for pet owners, enabling them to:

- Retrieve reliable pet care information
- Book veterinary appointments
- Schedule grooming sessions
- Submit support tickets for pet-related concerns

## Technology Stack

We use the following technologies:

- **Apache Kafka** – For event-driven architecture
- **Apache Flink (SQL)** – To interact with models and external data sources
- **OpenAI models** – For embedding and reasoning
- **Pinecone** – For storing vector data
- **MongoDB** – For managing customer data

In this example, we leverage **Confluent services**, using a **Confluent compute pool** with **30 Confluent Flink Units**.



## Setting Up Connections Using Confluent CLI

Once external sources like **Pinecone** and **OpenAI** are configured, use the **Confluent CLI** to establish secure connections. Refer to [`1-create-connections-with-confluent-cli.md`](1-create-connections-with-confluent-cli.md) for examples.

For detailed documentation, see:  
[Confluent CLI - Creating Flink Connections](https://docs.confluent.io/confluent-cli/current/command-reference/flink/connection/confluent_flink_connection_create.html)



## Accessing External Customer Data in MongoDB

In this demo, **MongoDB** is used to store customer data. We stream data from **MongoDB** into a **Kafka topic** using a **Kafka connector**. You can use a similar approach for other data storage solutions.

For a more detailed MongoDB setup in an **RAG scenario**, check the example in this repository:  
[Agentic RAG Example](https://github.com/confluentinc/demo-scene/tree/master/agentic-rag)

Make sure to define the **MongoDB record schema** (see `mongodb-schema.json`).  
Example records can be found in `customer-data-example.json`.



## Populating Pinecone with Vector Data

Example values used in this demo can be found in `documentation-sample.json`.



## Creating Tables and Models

The SQL queries for setting up tables and models used in this demo are available in [`2-create-tables-and-models.sql`](2-create-tables-and-models.sql).



## Setting Up the Pipeline

Follow the step-by-step pipeline setup in [`3-setup-pipeline-flow.sql`](3-setup-pipeline-flow.sql).



## Cleanup

To clean up resources after running the demo, refer to [`4-clean-up.sql`](4-clean-up.sql).  
If needed, you can drop some or all tables using the provided scripts.

## Additional resources

- [Flink AI: Hands-On FEDERATED_SEARCH()—Search a Vector Database with Confluent Cloud for Apache Flink®](https://www.confluent.io/blog/flink-ai-rag-with-federated-search/)
- [Flink AI: Real-Time ML and GenAI Enrichment of Streaming Data with Flink SQL on Confluent Cloud](https://www.confluent.io/blog/flinkai-realtime-ml-and-genai-confluent-cloud/)
- [Using Apache Flink® for Model Inference: A Guide for Real-Time AI Applications](https://www.confluent.io/blog/using-flink-for-model-inference-a-guide-for-realtime-ai-applications/)
- [What are AI agents?](https://www.ibm.com/think/topics/ai-agents)
- [Building effective agents](https://www.anthropic.com/research/building-effective-agents)
- [Understanding the planning of LLM agents: A survey](https://arxiv.org/pdf/2402.02716)
- [MCP](https://www.anthropic.com/news/model-context-protocol)
- [MCP Confluent GitHub repo](https://github.com/confluentinc/mcp-confluent)


