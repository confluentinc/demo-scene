importScripts('shared.js');

var scoreboard;

function getScoreboard() {
	getScoreboardJson(function(sc) {
		scoreboard = sc?sc:scoreboard;
		postMessage(scoreboard);	
		
	});
	setTimeout("getScoreboard()", 1000);
}

getScoreboard();
