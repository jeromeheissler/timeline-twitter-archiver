var compteur = 0;
var temps = 60; 
function loadTweet()	{
	$.post("/loadTweet", function(data)	{
		for (var i = 0; i < data.tweets.length; i++) {
			var object = data.tweets[i];
			$("#timeline").prepend(
				"<div id=\"tweet-"+object.id_str+"\" title=\""+object.date+"\" class=\"row tweet\">"+
					"<div class=\"span3\">"+object.author+"</div>"+
					"<div class=\"span9\">"+object.message+"</div>"+
				"</div>");
		}
		var progress = 	"<div class=\"progress\">"+
						"	<div class=\"bar\" id=\"bar\" style=\"width: 0%;\"><p id=\"cpt\">60s</p></div>"+
						"</div>";
		$("#action").html(progress);
		setTimeout(function(){loadTweet()}, temps*1000);
		compteur = 0;
		timer();
	})
} 

function timer()	{
	if(compteur < temps)	{
		$("#cpt").html(temps-compteur+"s");
		$("#bar").css("width", (compteur++/temps)*100+"%");
		setTimeout(function(){timer()}, 1000);
	}
}