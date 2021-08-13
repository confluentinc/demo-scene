function loadProfilePage() {
	
	loadProfileStats(function(stats){
		document.getElementById('profile').innerHTML = profilejson(stats);
	},window.name);

}

function profilejson(stats) {
	console.log(stats);

	var name = stats[0]
	var avgScore = stats[1]
	var avgSupBub = stats[2]
	var avgFruits = stats[3]
	console.log(name);

	return '<table><thead><tr><div style="text-align:center;width:100%;color:yellow">' +
		   name + '\'s avg stats per life' +
		   '</div></tr></thead><tbody>' +
		   '<tr><td style="width:40%">Score</td><td style="text-align:right;width:60%">'+ parseFloat(avgScore).toFixed(1) +'</td></tr>' +
		   '<tr><td style="width:40%">Super Bubbles</td><td style="text-align:right;width:60%">'+ parseFloat(avgSupBub).toFixed(1) +'</td></tr>' +
		   '<tr><td style="width:40%">Fruits</td><td style="text-align:right;width:60%">'+ parseFloat(avgFruits).toFixed(1) +'</td></tr>' +
		   '</tbody></table>';

}