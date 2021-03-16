importScripts('env-vars.js');
importScripts('shared.js');

var scoreboard;

function getScoreboard() {
	loadSummaryStats(function(highestScore, usersSet) {
		
		getScoreboardJson(function(sc) {
			scoreboard = sc?sc:scoreboard;
			postMessage(scoreboard);	
			
		}, usersSet);
	});
	
	setTimeout("getScoreboard()", 1000);
}

getScoreboard();
