var CLOUD_PROVIDER = "${cloud_provider}"
var EVENT_HANDLER_API = "${event_handler_api}"
var KSQLDB_QUERY_API = "${ksqldb_query_api}"
var SCOREBOARD_API = "${scoreboard_api}"
var highestScore = 0;

function getHighestScore() {

	var ksqlQuery = {};
	ksqlQuery.ksql =
		"SELECT HIGHEST_SCORE FROM HIGHEST_SCORE " +
		"WHERE ROWKEY = 'HIGHEST_SCORE';";

	var request = new XMLHttpRequest();
    request.onreadystatechange = function() {
        if (this.readyState == 4) {
			if (this.status == 200) {
				var result = JSON.parse(this.responseText);
				if (result[1] != undefined || result[1] != null) {
					var row = result[1].row;
					highestScore = row.columns[0];
				}
            }
            postMessage(highestScore);
		}
	};
	request.open('POST', KSQLDB_QUERY_API, true);
	request.setRequestHeader('Accept', 'application/vnd.ksql.v1+json');
	request.setRequestHeader('Content-Type', 'application/vnd.ksql.v1+json');
	request.send(JSON.stringify(ksqlQuery));
    
    setTimeout("getHighestScore()", 5000);

}

getHighestScore();
