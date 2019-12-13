var MSchart;
var MSData;

// initialize the spectrum chart
function initializeMSSpecTackle() {
    MSchart = st.chart.ms()
    	.xlabel("m/z")
    	.ylabel("Abundance")
        .margins([10,20,65,115]); // t, r, b, l
    MSchart.render("#spectrum_canvas");
    MSData = st.data.set()
    	.x("peaks.mz")
    	.y("peaks.intensity")
    	.ylimits([0,1000]);
    	//.title("spectrumId")
    MSchart.load(MSData);
}

$(document).ready(function () {
	initializeMSSpecTackle();
	MSData.add(data);
});
