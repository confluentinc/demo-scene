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

	return '<table><thead><tr style="float:center">' +
		   name + '\'s avg stats per life' +
		   '</tr></thead><tbody>' +
		   '<tr><td>Score</td><td style="float:right;width:50%">'+ avgScore +'</td></tr>' +
		   '<tr><td>Super Bubbles</td><td style="float:right;width:50%">'+ avgSupBub +'</td></tr>' +
		   '<tr><td>Fruits</td><td style="float:right;width:50%">'+ avgFruits +'</td></tr>' +
		   '</tbody></table>';

}