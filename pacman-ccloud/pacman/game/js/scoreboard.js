function loadScoreboardPage(){
	getScoreboardJson();
}

function getScoreboardJson() {

	var contentType = "application/json";
	var url = SCOREBOARD_API;
	
	if (CLOUD_PROVIDER == "GCP" || CLOUD_PROVIDER == "AZR") {
		// NOT IMPLEMENTED
	}

	const request = new XMLHttpRequest();
	request.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {
			var result = JSON.parse(this.responseText);
			console.log(result);
		   //document.getElementById("demo").innerHTML = xhttp.responseText;
		}
	};

	
	request.open("GET", url, true);
	request.setRequestHeader("Content-Type", contentType);
	request.send();

	return 

}

function json2table(json, classes, headers) {
	var cols = Object.keys(json[0]);

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
		row.row.columns.forEach((column) => {
			bodyRows += '<td>' + column + '</td>';
		})

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