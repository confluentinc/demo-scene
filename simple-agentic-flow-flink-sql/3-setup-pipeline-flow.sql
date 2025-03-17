---------------------------------- ADD TO INPUT TOPIC ------------------------------------------

INSERT INTO customer_message values
    ('conversation_6',
     'customer_3',
     'My cat does not want to eat, what can I do?'
        , UNIX_TIMESTAMP());


---------------------------------- CHECK INPUT TOPIC ------------------------------------------
SELECT * FROM customer_message WHERE customer_id = 'customer_3'

---------------------------------- CALL TO EMBEDDING API ------------------------------------------
    INSERT INTO customer_message_and_embedding
SELECT * FROM customer_message, lateral table(ml_predict('openai_embeddings', cusomer_message));

---------------------------------- CHECK DATA WITH EMBEDDING ------------------------------------------
SELECT * FROM customer_message_and_embedding WHERE customer_id = 'customer_3'


---------------------------------- SEMANTIC SEARCH ------------------------------------------
    INSERT INTO vector_store_result
SELECT * FROM customer_message_and_embedding,
              LATERAL TABLE(FEDERATED_SEARCH('vector_store_docs', 3, embedding));

---------------------------------- CHECK RESULTS FROM SEMANTIC SEARCH ------------------------------------------
SELECT * FROM vector_store_result WHERE customer_id = 'customer_3'

---------------------------------- CLEAN SEMANTIC SEARCH RESULTS WITH UDF ------------------------------------------

    INSERT INTO customer_message_and_resources
SELECT conversation_id, customer_id, cusomer_message, `timestamp`,
       CLEAN_PINCONE_VECTOR_RESULT(search_results) AS relevant_documentation
FROM vector_store_result

---------------------------------- CHECK CLEANED DATA ------------------------------------------
SELECT * FROM customer_message_and_resources WHERE customer_id = 'customer_3'


---------------------------------- GET CUSTOMER AND ITS PET INFO FROM MONGODB ------------------------------------------

    INSERT INTO customer_message_and_customer_info
SELECT
    customer_message_and_resources.conversation_id,
    customer_message_and_resources.customer_id,
    customer_message_and_resources.cusomer_message,
    customer_message_and_resources.`timestamp`,
    customer_message_and_resources.relevant_documentation,
    customers.pet_name,
    customers.customer_email,
    customers.pet_birthdate,
    customers.pet_gender,
    customers.allergies,
    customers.pet_type
FROM customer_message_and_resources
         INNER JOIN `mongo.pet-care.customer_info` customers
                    ON customer_message_and_resources.customer_id=customers.customer_id;

---------------------------------- CHECK CUSTOMER AND ITS PET INFO ------------------------------------------

SELECT * FROM customer_message_and_customer_info WHERE customer_id = 'customer_3'


---------------------------------- SEND INFERENCE REQUEST TO LLM ------------------------------------------

    INSERT INTO `chatbot_output`
SELECT
    conversation_id,
    customer_id,
    cusomer_message,
    chatbot_response
FROM `customer_message_and_customer_info`,
     LATERAL TABLE(ML_PREDICT('helpful_chatbot',
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


---------------------------------- SEE QUESTION AND ANSWER ------------------------------------------

SELECT cusomer_message, chatbot_response FROM chatbot_output WHERE customer_id = 'customer_3'