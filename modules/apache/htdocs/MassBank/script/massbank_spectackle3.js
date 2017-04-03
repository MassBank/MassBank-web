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

    MSData.add(spectrum);

    var urlVars = getUrlVars();

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
    var urlVars = getUrlVars();

    // array for mol2svg XHR promises
    var deferreds = [];
    // hide the tooltip-mol sub-div until
    // all promises are fulfilled
    d3.selectAll('.molecule_viewer')
        .style('display', 'none');
    // resolve all SDfile URLs one by one 
    d3.selectAll('.molecule_viewer').each(function () {
        var molid = d3.select(this).attr('molecule');
        // var moldivid = '#molecule_viewer';
        var moldivid = '#' + d3.select(this).attr('id');
        d3.selectAll(moldivid)
            .append('div')
            .attr('id', 'tooltips-mol-'+molid+'.mol')
            .style('float', 'left')
            .style('height', '100%')
            .style('width', '50%');
        // draw to the tooltip-mol sub-div and assign a title
        // d3.selectAll(moldivid).html(
        //     '<em>XX000001.mol</em><br/>'
        // );
        // var jqxhr = MSchart.mol2svg.draw('../cgi-bin/GetMolfile2.cgi?&type=getmol&names=gaba&dsn=MassBank', moldivid);
        // var jqxhr = st.util.mol2svg(100,100).draw('../cgi-bin/GetMolfile2.cgi?&type=getmol&names=gaba&dsn=MassBank', moldivid);
        var jqxhr = st.util.mol2svg(100,100).draw('../DB/molfile/MassBank/'+molid+'.mol', moldivid);
        deferreds.push(jqxhr);
        // wait until all XHR promises are finished
        $.when.apply($, deferreds).done(function () {
        // hide the spinner
        // spinner.css('display', 'none');
        // make the tooltip-mol sub-div visible
        d3.selectAll(moldivid)
            .style('display', 'inline');
        })
        .fail(function () {
            // hide the spinner
            // spinner.css('display', 'none');
        });
    });

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

