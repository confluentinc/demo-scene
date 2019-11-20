# OSQuery Python Extension and Confluent Platform

This is an example of how to use [OSQuery's Extensions](https://osquery.readthedocs.io/en/stable/deployment/extensions/) to send OSQuery results to Confluent Platform.

## OSQuery

To run `osqueryd` with the provided python extension, run the following command.

```bash
OSQURY_CONFIG=cp.json osqueryd --extension /confluent_kafka.ext --logger_plugin=confluent_logger --config_plugin=osquery_confluent_config
```

OSQuery has 3 command line tools:

* osqueryd - the daemon that runs queries in the background
* osqueryi - a cli to execute queries and display them in std out
* osqueryctl - that controls the osqueryd process

The configration below is passed to osqueryd. Create a file called `config.json` and past the config below.

```json
{
    "config": {
        "bootstrap.servers": "broker:29092"
    },
    "source": {
        "schedule": {
            "open_sockets": {
                "query": "select * from process_open_sockets where path <> '' or remote_address <> '';",
                "interval": 10,
                "version": "1.4.5",
                "description": "Retrieves all the open sockets per process in the target system.",
                "value": "Identify malware via connections to known bad IP addresses as well as odd local or remote port bindings",
                "key": "['hostIdentifier']"
            },
            "open_files": {
                "query": "select * from process_open_files where path not in ('/dev/null', '/dev/urandom', '/dev/random') and path not like '/usr/share/fonts/%' and path not like '%Chrome%' and path not like '%.cache%';",
                "interval": 5,
                "version": "1.4.5",
                "description": "Retrieves all the open files per process in the target system.",
                "value": "Identify processes accessing sensitive files they shouldn't",
                "key": "['hostIdentifier']"
            },
            "logged_in_users": {
                "query": "select * from logged_in_users",
                "interval": 10,
                "version": "1.4.5",
                "description": "Retrieves the list of all the currently logged in users in the target system.",
                "value": "Useful for intrusion detection and incident response. Verify assumptions of what accounts should be accessing what systems and identify machines accessed during a compromise.",
                "key": "['hostIdentifier']"
            },
            "shell_history": {
                "query": "select * from shell_history;",
                "interval": 10,
                "version": "1.4.5",
                "description": "Retrieves the command history, per user, by parsing the shell history files.",
                "value": "Identify actions taken. Useful for compromised hosts.",
                "key": "['hostIdentifier']"
            },
            "listening_ports": {
                "query": "select * FROM listening_ports;",
                "interval": 10,
                "version": "1.4.5",
                "description": "Retrieves all the listening ports in the target system.",
                "value": "Categorized ports by well known ranges and importance.",
                "key": "['hostIdentifier']"
            },
            "arp_cache": {
                "query": "select * from arp_cache;",
                "interval": 10,
                "version": "1.4.5",
                "description": "Retrieves the ARP cache values in the target system.",
                "value": "Determine if connecting to unusual local devices.",
                "key": "['hostIdentifier']"
            },
            "processes": {
                "query": "select * from processes;",
                "interval": 5,
                "description": "Retrieves list of running processes.",
                "value": "Determine commonly used software",
                "key": "['hostIdentifier']"
            },
            "syslog": {
                "query": "select * from syslog;",
                "interval": 10,
                "description": "Retrieves the syslog data.",
                "value": "capture privileged actions or lateral movement (sshd) and more.",
                "key": "['hostIdentifier']"
            },
            "users": {
                "query": "select * from users;",
                "interval": 10,
                "description": "Retrieves the users.",
                "value": "used to join with other tables.",
                "key": "['hostIdentifier']"
            }
        }
    },
    "topic": "osquery-default-topic"
}
```

* `options` - holds the kafka configuration parameters. TLS is also supported. See the Kafka producer documentation in OSQuery for instructions.

### Executing osqueryd

```bash
osqueryd --config_path=/project/osquery/config.json --logger_plugin=kafka_producer
```

* `--config_path` is the path to the configuration above.
* `--logger_plugin` instructs osquery to use the Kafka Producer when serializing the logs.

## Docker compose commands

```bash
# builds the containers
make build

# starts the cluster
make cluster

# wait 30 sec for cluster to start
```

Proceed to localhost:9021 to view the Confluent Control Center. Naviate to the topics page and view the topics auto created by OSQuery. Specifically, you can view the incoming messages for the Processes topic which shows a stream if processes running on a host.
