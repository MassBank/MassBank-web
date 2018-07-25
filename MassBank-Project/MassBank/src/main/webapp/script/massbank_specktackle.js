var MSchart;
var MSData;
// define global variables if they don not already exist in the scope
try{ id; }
catch(e) {
    if(e.name == "ReferenceError") {
        var id;
    }
}
try{ site; }
catch(e) {
    if(e.name == "ReferenceError") {
        var site;
    }
}
try{ dsn; }
catch(e) {
    if(e.name == "ReferenceError") {
        var dsn;
    }
}

// Read a page's GET URL variables and return them as an associative array
// (see http://stackoverflow.com/questions/12727081/function-to-retrieve-url-variables-using-javascript-and-jquery)
function getUrlVars() {
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

// initialize the spectrum chart
function initializeMSSpecTackle() {
    if (MSchart != undefined) return;
    MSchart = st.chart.ms().labels(true).margins([10,60,30,90]);
    MSchart.render("#spectrum_canvas");
    var ylims = [0,1100];
    MSData = st.data.set().x("peaks.mz").y("peaks.intensity").title("spectrumId").ylimits(ylims);
    MSchart.load(MSData);
}

// display the spectrum
function loadSpectrum(spectrum) {
	initializeMSSpecTackle();
    MSData.add(spectrum);
}

// construct the spectrum data structure
function loadData() {
	// array for mol2svg XHR promises
	var deferreds = [];
	// check if there is a spectrum canvas on the page (only one is allowed)
	if (d3.selectAll('#spectrum_canvas')[0].length === 1) {
		var spectrum = {};
		var line = "";
		var mzStart = null;
		var mzStop = null;
		spectrum["spectrumId"] = "";
		spectrum["peaks"] = [];
		// construct spectrum data peak list if it is given as an attribute of the #spectrum_canvas div
		if (d3.selectAll('#spectrum_canvas').attr('peaks')) {
			spectrum["spectrumId"] = "Query Spectrum";
			data = d3.selectAll('#spectrum_canvas').attr('peaks');
	        dataLines = data.split("@");
	        for (index = 0; index < dataLines.length; ++index) {
	            if (dataLines[index]) {
	                line = dataLines[index].split(",");
	                spectrum["peaks"][index] = {"mz":parseFloat(line[0]),"intensity":parseFloat(line[1])};
	                if (index == 0) {
	                    mzStart = parseFloat(line[0]);
	                    mzStop = parseFloat(line[1]);
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
		} 
		// if no peak list is specified try to get peak list for a component specified by it's id
		else {
			// not supported
		}
		$.when.apply($,deferreds).done(function () {
			loadSpectrum(spectrum);
		});
	}
}

$(document).ready(function () {
	loadData();
});