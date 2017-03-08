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

