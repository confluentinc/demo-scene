importScripts('shared.js');

var scoreboard;

function getScoreboard(){
	getScoreboardJson(function(sc){
		scoreboard = sc?sc:scoreboard;
		postMessage(scoreboard);	
		
	});
	setTimeout("getScoreboard()", 5000);
}

getScoreboard();
