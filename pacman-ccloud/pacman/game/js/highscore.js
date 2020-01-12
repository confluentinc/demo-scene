var highestScore = 0;

function getHighestScore() {

    var request = new XMLHttpRequest();
    request.onreadystatechange = function() {
        if (this.readyState == 4) {
			if (this.status == 200) {
                var result = JSON.parse(this.responseText);
                highestScore = result.highestScore;
            }
            postMessage(highestScore);
		}
	};
	request.open('POST', '${highest_score_api}', true);
	request.setRequestHeader('Accept', 'application/vnd.ksql.v1+json');
	request.setRequestHeader('Content-Type', 'application/vnd.ksql.v1+json');
    request.send(JSON.stringify({}));
    
    setTimeout("getHighestScore()", 5000);

}

getHighestScore();
