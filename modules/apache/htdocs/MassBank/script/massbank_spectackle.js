// MS spectackle variables
var MSchart;
var MSData;

function initializeMSSpecTackle() {

    if (MSchart != undefined) return;

    MSchart = st.chart.ms().labels(true).margins([10,60,30,90]);

    MSchart.render("#spectrum_canvas");

    MSData = st.data.set().x("peaks.mz").y("peaks.intensity").title("spectrumId");

    MSchart.load(MSData);

}

function loadSpectrum(spectrum) {

	initializeMSSpecTackle();
	alert(JSON.stringify(spectrum));
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
	var jqxhr = $.get("../cgi-bin/GetData.cgi?" + queryString,"text");
	jqxhr.done(function (data) {
		var spectrum = {};
		var urlVars = getUrlVars();
		var line = "";
		var mzStart = null;
		var mzStop = null;
		spectrum["spectrumId"] = urlVars["id"];
		spectrum["peaks"] = [];
		dataLines = data.split("\n");
		for (index = 0; index < dataLines.length; ++index) {
			if (dataLines[index]) {
				line = dataLines[index].split("\t");
				spectrum["peaks"][index] = {"mz":parseFloat(line[0]),"intensity":parseFloat(line[2])};
				if (index == 0) {
					mzStart = parseFloat(line[0]);
					mzStop = parseFloat(line[2]);
				} else {
					if (mzStart > parseFloat(line[0])) {
						mzStart = parseFloat(line[0]);
					}
					if (mzStop < parseFloat(line[0])) {
						mzStop = parseFloat(line[0]);
					}
				}
			}
		}
		spectrum["mzStart"] = mzStart;
		spectrum["mzStop"] = mzStop;
		loadSpectrum(spectrum);

	});
	jqxhr.fail(function (jqXHR, textStatus, errorThrown) {
		alert("Error: " + errorThrown);
	});
});

