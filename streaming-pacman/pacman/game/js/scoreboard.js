function loadScoreboardPage() {
	loadSummaryStats(function(highestScore, usersSet) {
		
		getScoreboardJson(function(playersScores) {
			var headers = ["Rank","Name", "Score", "Level", "Losses"];
			document.getElementById('scoreboard').innerHTML = json2table(playersScores, 'table', headers);
			window.localStorage.setItem("playersScores", JSON.stringify(playersScores));	
			
		}, usersSet);
	});

}

function json2table(json, classes, headers) {

	var headerRow = '';
	var bodyRows = '';
	classes = classes || '';

	function capitalizeFirstLetter(string) {
	  return string.charAt(0).toUpperCase() + string.slice(1);
	}

	headers.map(function(col) {
	  headerRow += '<th>' + capitalizeFirstLetter(col) + '</th>';
	});

	json.forEach((row, index) => {
		bodyRows += '<tr>';
		bodyRows += '<td>#' + (index+1) + '</td>';
		bodyRows += '<td>' + row.user + '</td>';
		bodyRows += '<td>' + row.score + '</td>';
		bodyRows += '<td>' + row.level + '</td>';
		bodyRows += '<td>' + row.losses + '</td>';
		bodyRows += '</tr>';
	});

	return '<table class="' +
		   classes +
		   '"><thead><tr>' +
		   headerRow +
		   '</tr></thead><tbody>' +
		   bodyRows +
		   '</tbody></table>';

}