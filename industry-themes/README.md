Modest interpretations on various industry based use cases can be found here. Most include the data, producers and ksqlDB code necessary for demonstrating how stream processing can be applied to business problems.

## Requirements

In some cases, a Docker compose file is provided for easy deployment but the producers can easily be modify to run on any Confluent environment. There are also a few that use Confluent Cloud but again these can be modified for other deployments. Also keep in mind that these have been developed over a period of time where ksqlDB sytnax has changed and some older use cases may not work with newer ksqlDB server instances without some minor changes.

## Contents

- [Tracking field assets for utilities companies](field_management)
- [Client services for Foreign Exchange market trading](fx_client_services)
- [Looking up claim statics by patient with FHIR events](healthcare_claims_microservices)
- [Medical device alerts from noisy sensors](medical_devices)
- Building next best offer engines with session information ([Banking](next_best_offer_banking) or [Insurance](next_best_offer_insurance))
- [Pizza order status tracking with nested JSON](pizza_orders)
- [Acitivty based discounting for ecom](real_time_discounting)
- [Simple spend based segmentation for grocery](real_time_segmentation)  
- [Truck GPS and sensor correlation for alerts](truck_sensors)
