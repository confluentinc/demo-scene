------ ##### SETTING UP TABLES ##### -------

---------------------------------- INPUT TOPIC -------------------------------------------------------------------------------------------------------------

-------- Input table for customer messages from the chat

CREATE TABLE customer_message
(
    conversation_id string NOT NULL,
    customer_id     string NOT NULL,
    cusomer_message string NOT NULL,
    `timestamp`     BIGINT NOT NULL
);

---------------------------------- EMBEDDINGS MODEL and  TOPICS --------------------------------------------------------------------------------------------------

-------- Add embedding model

CREATE
MODEL openai_embeddings
INPUT (input STRING)
OUTPUT (embedding ARRAY<FLOAT>)
WITH(
  'task' = 'embedding',
  'provider'= 'openai',
  'openai.input_format'='OPENAI-EMBED',
  'openai.model_version'='text-embedding-3-small',
  'openai.connection' = 'openai-connection-vector-embeddings' );

CREATE TABLE vector_store_docs
(
    text      STRING,
    embedding ARRAY<FLOAT>
) WITH (
      'connector' = 'pinecone',
      'pinecone.connection' = 'pinecone-pet-care-connection',
      'pinecone.embedding_column' = 'embedding'
      );

CREATE TABLE customer_message_and_embedding
(
    conversation_id string NOT NULL,
    customer_id     string NOT NULL,
    cusomer_message string,
    `timestamp`     BIGINT NOT NULL,
    embedding       array<float>
);

CREATE FUNCTION CLEAN_PINCONE_VECTOR_RESULT AS 'com.example.my.CleanPineConeVectorResult'
  USING JAR 'confluent-artifact://cfa-0x3jq6';

CREATE TABLE customer_message_and_resources
(
    conversation_id        string NOT NULL,
    customer_id            string NOT NULL,
    cusomer_message        string,
    `timestamp`            BIGINT NOT NULL,
    relevant_documentation string NOT NULL
);


---------------------------------- CUSTOMER INFO & MONGODB --------------------------------------------------------------------------------------------------

-------- Get info from MongoDB
SELECT *
from `mongo.pet-care.customer_info`

SELECT customer_id, pet_name
from `mongo.pet-care.customer_info`
DROP TABLE customer_message_and_customer_info
CREATE TABLE customer_message_and_customer_info
(
    conversation_id        string NOT NULL,
    customer_id            string NOT NULL,
    cusomer_message        string,
    `timestamp`            BIGINT NOT NULL,
    relevant_documentation string NOT NULL,
    pet_name               string,
    customer_email         string,
    pet_birthdate          string,
    pet_gender             string,
    allergies              string,
    pet_type               string
);

---------------------------------- LLM ------------------------------

CREATE
MODEL helpful_chatbot
INPUT(text STRING)
OUTPUT(chatbot_response STRING)
COMMENT 'chatbot based on openai gpt 3.5 turbo'
WITH (
  'provider' = 'openai',
  'task' = 'text_generation',
  'openai.connection' = 'openai-connection-completions',
  'openai.model_version' = 'gpt-3.5-turbo',
  'openai.system_prompt' =
  'You are a helpful agent that has deep knowledge of pet care. Answer the question from the customer based on the information about customer pet and provided piece of documentation. Give your extended advice on the issue. Talk about the pet using its name. Be friendly and respectful');

CREATE TABLE chatbot_output
(
    conversation_id  STRING NOT NULL,
    customer_id      STRING NOT NULL,
    cusomer_message  String NOT NULL,
    chatbot_response String
);

------ ##### Adding triage step ##### ------

CREATE
MODEL initial_triage
INPUT(text STRING)
OUTPUT(triage_result STRING)
COMMENT 'chatbot based on openai gpt 3.5 turbo'
WITH (
  'provider' = 'openai',
  'task' = 'text_generation',
  'openai.connection' = 'openai-connection-completions',
  'openai.model_version' = 'gpt-3.5-turbo',
  'openai.system_prompt' =
  'You are an agent that triages customer requests. Customers can do the following actions:
- GET_INFO,
- SCHEDULE_VET,
- BOOK_GROOMING,
- RAISE_ISSUE.

- GET_INFO is for any questions related to pet management and health that does not require vet involvement;
- SCHEDULE_VET - when there is a need based on your analysis;
- BOOK_GROOMING - when customer asks for a grooming appointment;
- RAISE_ISSUE - for issues related to services provided by the company.

YOUR TASK: based on content of customer message return ONE action type that fits best: GET_INFO, SCHEDULE_VET, BOOK_GROOMING or RAISE_ISSUE. DO NOT GIVE ANY OTHER INFORMATION, just type of the action.');

CREATE TABLE triage_output
(
    conversation_id        string NOT NULL,
    customer_id            string NOT NULL,
    cusomer_message        string NOT NULL,
    `timestamp`            BIGINT NOT NULL,
    relevant_documentation string NOT NULL,
    pet_name               string,
    customer_email         string,
    pet_birthdate          string,
    pet_gender             string,
    allergies              string,
    pet_type               string,
    triage_result          string
);


INSERT INTO triage_output
SELECT conversation_id,
       customer_id,
       cusomer_message,
       `timestamp`,
       relevant_documentation,
       pet_name,
       customer_email,
       pet_birthdate,
       pet_gender,
       allergies,
       pet_type,
       triage_result
FROM `customer_message_and_customer_info`,
     LATERAL TABLE(ML_PREDICT('initial_triage',
  CONCAT(
      'This is customer information: they has a pet who is ', pet_type,
      '. It is ', pet_gender,
      '. It was born on ', pet_birthdate,
      '. It has these allergies: ', allergies,
      '. This is customer request: ',
      cusomer_message,
      '. This is relevant documentation: ',
      relevant_documentation,
      'YOUR TASK: return one of the following actions that best correspond to customer request: GET_INFO, SCHEDULE_VET, BOOK_GROOMING or RAISE_ISSUE'
  )));

--------- Tables per task

CREATE TABLE recommendation_requests
(
    conversation_id        string NOT NULL,
    customer_id            string NOT NULL,
    cusomer_message        string NOT NULL,
    `timestamp`            BIGINT NOT NULL,
    relevant_documentation string NOT NULL,
    pet_name               string,
    customer_email         string,
    pet_birthdate          string,
    pet_gender             string,
    allergies              string,
    pet_type               string,
);

CREATE TABLE schedule_vet_requests
(
    conversation_id,
    customer_id,
    cusomer_message,
    `timestamp`,
    relevant_documentation,
    pet_name,
    customer_email,
    pet_birthdate,
    pet_gender,
    allergies,
    pet_type
);

CREATE TABLE book_grooming_requests
(
    conversation_id,
    customer_id,
    cusomer_message,
    `timestamp`,
    relevant_documentation,
    pet_name,
    customer_email,
    pet_birthdate,
    pet_gender,
    allergies,
    pet_type
);

CREATE TABLE raise_issue_requests
(
    conversation_id,
    customer_id,
    cusomer_message,
    `timestamp`,
    relevant_documentation,
    pet_name,
    customer_email,
    pet_birthdate,
    pet_gender,
    allergies,
    pet_type
);

--------- Recommendations

CREATE MODEL recommendation
INPUT(text STRING)
OUTPUT(recommendation STRING)
COMMENT 'chatbot based on openai gpt 3.5 turbo'
WITH (
  'provider' = 'openai',
  'task' = 'text_generation',
  'openai.connection' = 'openai-connection-completions',
  'openai.model_version' = 'gpt-3.5-turbo',
  'openai.system_prompt' =
  'You are a helpful agent that has deep knowledge of pet care. Answer the question or concern from the customer based on the information about customer pet and provided piece of documentation. Give your extended advice on the issue. Talk about the pet using its name. Be friendly and respectful');


CREATE TABLE recommendations_output(
                                       conversation_id STRING  NOT NULL,
                                       customer_id STRING  NOT NULL,
                                       cusomer_message String  NOT NULL,
                                       recommendation String
);

INSERT INTO `recommendations_output`
SELECT
    conversation_id,
    customer_id,
    cusomer_message,
    recommendation
FROM `customer_message_and_customer_info`,
     LATERAL TABLE(ML_PREDICT('recommendation',
  CONCAT(
      'This is customer information. He has a pet who is ', pet_type,
      '. It is ', pet_gender,
      '. It was born on ', pet_birthdate,
      '. It has these allergies: ', allergies,
      '. This is customer question: ',
      cusomer_message,
      '. This is relevant documentation: ',
      relevant_documentation
  )));


--------- Schedule

CREATE MODEL schedule
INPUT(text STRING)
OUTPUT(recommendation STRING)
COMMENT 'chatbot based on openai gpt 3.5 turbo'
WITH (
  'provider' = 'openai',
  'task' = 'text_generation',
  'openai.connection' = 'openai-connection-completions',
  'openai.model_version' = 'gpt-3.5-turbo',
  'openai.system_prompt' =
  'You are a helpful agent that has deep knowledge of pet care. Answer the question or concern from the customer based on the information about customer pet and provided piece of documentation. Give your extended advice on the issue. Talk about the pet using its name. Be friendly and respectful');
