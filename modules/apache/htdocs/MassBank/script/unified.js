var MSchart;
var MSData;

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

function initializeMSSpecTackle() {
    if (MSchart != undefined) return;
    MSchart = st.chart.ms().labels(true).margins([10,60,30,90]);
    MSchart.render("#spectrum_canvas");
    MSData = st.data.set().x("peaks.mz").y("peaks.intensity").title("spectrumId");
    MSchart.load(MSData);
}

function loadSpectrum(spectrum) {
	initializeMSSpecTackle();
    MSData.add(spectrum);
}

function loadMolFile() {
	var urlVars = getUrlVars();
    // array for mol2svg XHR promises
    var deferreds = [];
    // hide the tooltip-mol sub-div until
    // all promises are fulfilled
    // d3.selectAll('.molecule_viewer').style('display', 'none');
    // resolve all SDfile URLs one by one 
    d3.selectAll('.molecule_viewer').each(function () {
    	molDivId = "#" + d3.select(this).attr('id');
    	d3.selectAll(molDivId)
            .append('div')
            .attr('id', 'tooltips-mol-'+molDivId+'.mol')
            .style('float', 'left')
            .style('height', '100%')
            .style('width', '50%');
            var idSplit = d3.select(this).attr('id').split("_");
            if (idSplit[2] !== undefined && idSplit[3] !== undefined) {
            	var molId = idSplit[2];
            	var dsn = idSplit[3];
            	if (!isNaN(dsn)) {
            		var jqxhrList = $.get('../massbank.conf').done(function (data){
			            var list = data.evaluate('//MassBank/MyServer/DB',data,null,XPathResult.ORDERED_NODE_SNAPSHOT_TYPE,null);
			            var dsn = list.snapshotItem(dsn).textContent;
			            jqxhrMolFile = $.get('../cgi-bin/GetMolfileById.cgi?id='+ molId +'&dsn=' + dsn,"text").done(function (data) {
				            var jqxhr = st.util.mol2svg(100,100).draw('../DB/molfile/'+dsn+'/'+data+'.mol', molDivId);
				            deferreds.push(jqxhr);    
				        });
			    	});
            	} else {
	            	var jqxhrMolFile = $.get('../cgi-bin/GetMolfileById.cgi?id='+ molId +'&dsn=' + dsn).done(function (data) {
	                	var jqxhr = st.util.mol2svg(100,100).draw('../DB/molfile/'+dsn+'/'+data+'.mol', molDivId);
	                	deferreds.push(jqxhr);    
	            	});		
            	}
            } else {
            	var molId = urlVars["id"];
            	var dsn = urlVars["dsn"];
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
        // hide the spinner
        // spinner.css('display', 'none');
        // make the tooltip-mol sub-div visible
        d3.selectAll(molDivId)
            .style('display', 'inline');
        })
        .fail(function () {
            // hide the spinner
            // spinner.css('display', 'none');
        });
    });
}

function loadData() {
	var deferreds = [];
	if (d3.selectAll('#spectrum_canvas')[0].length === 1) {
		var spectrum = {};
		var line = "";
		var mzStart = null;
		var mzStop = null;
		spectrum["spectrumId"] = "";
		spectrum["peaks"] = [];
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
		} else {
			var urlVars = getUrlVars();
			var id = urlVars["id"];
			var dsn = urlVars["dsn"];
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