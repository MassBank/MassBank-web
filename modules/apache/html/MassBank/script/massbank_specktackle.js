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

// load data from molfile and display the structure
function loadMolFile() {
	var urlVars = getUrlVars();
    // array for mol2svg XHR promises
    var deferreds = [];
    // hide the sub-div until all promises are fulfilled
    d3.selectAll('.molecule§viewer').style('display', 'none');
    // resolve all file URLs one by one 
    d3.selectAll('.molecule§viewer').each(function () {
    	var molDivId = "#" + d3.select(this).attr('id');
    	d3.selectAll(molDivId)
            .append('div')
            .attr('id', 'tooltips-mol-'+molDivId+'.mol')
            .style('float', 'left')
            .style('height', '100%')
            .style('width', '50%');
            var idSplit = d3.select(this).attr('id').split("§");
            // the id of the mol div takes the form molecule§viewer[§id][§site|dsn]
            // example id with site molecule§viewer§XX000001§0
            // example id with dsn molecule§viewer§XX000001§MassBank
            // check if the id contains an id and site or dsn
            if (idSplit[2] !== undefined && idSplit[3] !== undefined) {
            	var molId = idSplit[2];
            	var dsn = idSplit[3];
            	// check if the id is a site, i.e. a number
            	if (!isNaN(dsn)) {
            		var jqxhrList = $.get('../massbank.conf').done(function (data){
			            var list = data.evaluate('//DB',data,null,XPathResult.ORDERED_NODE_SNAPSHOT_TYPE,null);
			            dsn = list.snapshotItem(dsn).textContent;
			            jqxhrMolFile = $.get('../cgi-bin/GetMolfileById.cgi?id='+ molId +'&dsn=' + dsn,"text").done(function (data) {
				            var jqxhr = st.util.mol2svg(100,100).draw('../DB/molfile/'+dsn+'/'+data+'.mol', molDivId);
				            deferreds.push(jqxhr);    
				        });
			    	});
            	} 
            	// if it is not a number take the value as the string for the dsn parameter
            	else {
	            	var jqxhrMolFile = $.get('../cgi-bin/GetMolfileById.cgi?id='+ molId +'&dsn=' + dsn).done(function (data) {
	                	var jqxhr = st.util.mol2svg(100,100).draw('../DB/molfile/'+dsn+'/'+data+'.mol', molDivId);
	                	deferreds.push(jqxhr);    
	            	});		
            	}
            } 
            // if the id does not contain an id and site or dsn try to retrieve both parameters from the page's URL
            else {
            	var molId = id;
            	var molIdURL = urlVars["id"];
            	var dsnURL = urlVars["dsn"];
    			if (molIdURL !== undefined) {
					molId = molIdURL;
				}
				if (dsnURL !== undefined) {
					dsn = dsnURL;
				}
            	if (molId !== "" && dsn !== "") {
            		var jqxhr = $.get("../cgi-bin/GetMolfileById.cgi?dsn="+dsn+"&id="+molId,"text");
				    jqxhr.done(function (data) {
				        var jqxhr = st.util.mol2svg(200,200).draw('../DB/molfile/'+dsn+'/'+data+'.mol', molDivId);
				        deferreds.push(jqxhr);
				    });
            	}
            }
        // wait until all XHR promises are finished
        $.when.apply($, deferreds).done(function () {
        // make the tooltip-mol sub-div visible
        d3.selectAll(molDivId)
            .style('display', 'inline');
        });
    });
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
			// try to get id from the url
			var urlVars = getUrlVars();
			var idURL = urlVars["id"];
			var dsnURL = urlVars["dsn"];
			if (idURL !== undefined) {
				id = idURL;
			}
			if (dsnURL !== undefined) {
				dsn = dsnURL;
			}
			if (id !== "" && dsn !== "") {
				var jqxhr = $.get("../cgi-bin/GetData.cgi?id="+id+"&dsn="+dsn,"text");
				jqxhr.done(function (data) {
					spectrum["spectrumId"] = urlVars["id"];
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
				});
				deferreds.push(jqxhr);
			}
		}
		$.when.apply($,deferreds).done(function () {
			loadSpectrum(spectrum);
		});
	}
}

$(document).ready(function () {
	loadData();
	loadMolFile();
});