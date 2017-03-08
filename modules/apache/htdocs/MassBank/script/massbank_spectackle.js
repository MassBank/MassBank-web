// MS spectackle variables
var MSchart;
var MSData;

function initializeMSSpecTackle() {

    if (MSchart != undefined) return;

    MSchart = st.chart.ms().xlabel("Mass-to-Charge").ylabel("Intensity").legend(true).labels(true);

    MSchart.render("#spectrum_canvas");

    MSData = st.data.set().x("peaks.mz").y("peaks.intensity").title("spectrumId");
    MSchart.load(MSData);

}

function loadSpectrum(spectrum) {

	initializeMSSpecTackle();
	
    MSData.add(spectrum);

}

// Read a page's GET URL variables and return them as an associative array.
function getUrlVars()
{
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

$(document).ready(function (){
		
	// load the spectrum
	var queryString = window.location.href.slice(window.location.href.indexOf('?') + 1);
	$.get("../cgi-bin/GetData.cgi" + queryString).
		done(function (data) {
			var spectrum = {};
			var urlVars = getUrlVars();
			var line = "";
			spectrum["spectrumId"] = urlVars["id"];
			spectrum["peaks"] = [];
			dataLines = data.split("\n");
			for (index = 0; index < dataLines.length(); ++index) {
				line = dataLines[index].split("\t");
				spectrum["peaks"][index] = {"mz":line[0],"intensity":line[2]};
			}
			spectrum["mzStart"] = 0;
			spectrum["mzStop"] = 200;
			loadSpectrum(spectrum);
		});
});

