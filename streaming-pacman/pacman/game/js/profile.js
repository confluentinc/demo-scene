function loadProfilePage() {
	console.log("loadProfilePage()");
	
	loadProfileStats();

}

function profilejson2table(json, classes, headers) {
	console.log("profilejson2table()");

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