var CLOUD_PROVIDER = "${cloud_provider}"
var EVENT_HANDLER_API = "${event_handler_api}"
var KSQLDB_QUERY_API = "${ksqldb_query_api}"
var SCOREBOARD_API = "${scoreboard_api}"

function loadHighestScore(callback) {

	var highestScore ;
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
            callback(highestScore);
		}
	};
	request.open('POST', KSQLDB_QUERY_API, true);
	request.setRequestHeader('Accept', 'application/vnd.ksql.v1+json');
	request.setRequestHeader('Content-Type', 'application/vnd.ksql.v1+json');
	request.send(JSON.stringify(ksqlQuery));

}

function getScoreboardJson(callback) {

	var contentType = "application/json";
	var url = SCOREBOARD_API;
	var method = "POST";
	
	if (CLOUD_PROVIDER == "GCP" || CLOUD_PROVIDER == "AZR") {
		// NOT IMPLEMENTED
	}

	const request = new XMLHttpRequest();
	request.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {
			var result = JSON.parse(this.responseText);
			var playersScores = result.scoreboard.sort(function(a, b) {
				var res=0
				if (a.score > b.score) res = 1;
				if (b.score > a.score) res = -1;

				if (a.score == b.score){
					if (a.level > b.level) res = 1;
					if (b.level > a.level) res = -1;

					if (a.level == b.level){
						if (a.losses < b.losses) res = 1;
						if (b.losses > a.losses) res = -1;
					} 
				} 
			  
				return res * -1;
			  });;
			callback(playersScores);
	
		}
	};

	
	request.open(method, url, true);
	request.setRequestHeader("Content-Type", contentType);
	request.send(); 

}

function calcRankingFromScoreboard(scoreboard,username){
	var ranking;
	for (var i = 0, l = scoreboard.length; i < l; i++) {
		var player = scoreboard[i];
		if(player.user===username) { 
	
			ranking = i+1 ;
			break;
		}
	}
	return ranking;
}
