var highestScore = 0;

function getHighestScore() {

	var ksqlQuery = {};
	ksqlQuery.ksql =
		"SELECT HIGHEST_SCORE FROM HIGHEST_SCORE " +
		"WHERE ROWKEY = 'HIGHEST_SCORE_KEY';";

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
	request.open('POST', '${ksqldb_query_api}', true);
	request.setRequestHeader('Accept', 'application/vnd.ksql.v1+json');
	request.setRequestHeader('Content-Type', 'application/vnd.ksql.v1+json');
	request.send(JSON.stringify(ksqlQuery));
    
    setTimeout("getHighestScore()", 5000);

}

getHighestScore();
