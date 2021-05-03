function loadHighestScore(player, callback) {

	var highestScore ;
	
	ksqlQuery = `select  HIGHEST_SCORE from STATS_PER_USER WHERE USER='${player}';`;

	var request = new XMLHttpRequest();
    request.onreadystatechange = function() {
        if (this.readyState == 4) {
			if (this.status == 200) {
				var result = JSON.parse(this.responseText);
				if (result[1] != undefined || result[1] != null) {
					var row = result[1];
					highestScore = row[0];
				}
            }
            callback(highestScore);
		}
	};
	sendksqlDBQuery(request, ksqlQuery);

}

function loadSummaryStats(callback) {

	var highestScore;
	var usersSet;
	
	ksqlQuery = `SELECT HIGHEST_SCORE_VALUE, USERS_SET_VALUE FROM SUMMARY_STATS WHERE SUMMARY_KEY='SUMMARY_KEY';`;

	var request = new XMLHttpRequest();
    request.onreadystatechange = function() {
        if (this.readyState == 4) {
			if (this.status == 200) {
				var result = JSON.parse(this.responseText);
				if (result[1] != undefined || result[1] != null) {
					var row = result[1];
					highestScore = row[0];
					usersSet = row[1];
				}
            }
            callback(highestScore, usersSet);
		}
	};
	sendksqlDBQuery(request, ksqlQuery);

}


function sendksqlDBStmt(request, ksqlQuery){
	var query = {};
	query.ksql = ksqlQuery;
	query.endpoint = "ksql";
	request.open('POST', KSQLDB_QUERY_API, true);
	request.setRequestHeader('Accept', 'application/json');
	request.setRequestHeader('Content-Type', 'application/json');
	request.send(JSON.stringify(query));
}

function sendksqlDBQuery(request, ksqlQuery){
	var query = {};
	query.sql = ksqlQuery;
	query.endpoint = "query-stream";
	request.open('POST', KSQLDB_QUERY_API, true);
	request.setRequestHeader('Accept', 'application/json');
	request.setRequestHeader('Content-Type', 'application/json');
	request.send(JSON.stringify(query));
}

function getScoreboardJson(callback,userList) {

	var userListCsv = userList.map(user => `'${user}'`).join(',');

	ksqlQuery = `select USER, HIGHEST_SCORE, HIGHEST_LEVEL, TOTAL_LOSSES from STATS_PER_USER WHERE USER IN (${userListCsv});`;

	const request = new XMLHttpRequest();
	request.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {
			var result = JSON.parse(this.responseText);
			if (result[1] == undefined || result[1] == null) {
				logger('Empty Scoreboard')
				return;
			}

			//First element is the header
			result.shift();
			var playersScores = result.map((item) => ({ user: item[0], score:item[1],level:item[2],losses:item[3] }));

			playersScores = playersScores.sort(function(a, b) {
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
	
	sendksqlDBQuery(request, ksqlQuery);

}

