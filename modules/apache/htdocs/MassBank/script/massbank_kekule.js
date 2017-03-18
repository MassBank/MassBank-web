$(document).ready(function (){
	// set new object in viewer
	var cmlData = '<cml xmlns="http://www.xml-cml.org/schema"><molecule id="m1"><atomArray><atom id="a2" elementType="C" x2="7.493264658965051" y2="35.58088907877604"/><atom id="a3" elementType="O" x2="8.186084981992602" y2="35.18088907877604"/><atom id="a1" elementType="C" x2="6.800444335937501" y2="35.18088907877604"/></atomArray><bondArray><bond id="b2" order="S" atomRefs2="a2 a3"/><bond id="b1" order="S" atomRefs2="a2 a1"/></bondArray></molecule></cml>';
	var myMolecule = Kekule.IO.loadFormatData(cmlData, 'cml');
	// chemViewer.setChemObj(myMolecule);

	// var chemViewer = new Kekule.ChemWidget.Viewer(document);
	var chemViewer = new Kekule.ChemWidget.Viewer(document.getElementById('molecul_viewer'));
	chemViewer.setDimension('200px', '200px');
	//chemViewer.appendToElem(document.getElementById('molecul_viewer')).setChemObj(myMolecule);
	chemViewer.setChemObj(myMolecule);
	// chemViewer.setPredefinedSetting('basic');
	chemViewer
  		.setEnableToolbar(true)
  		.setEnableDirectInteraction(true)
  		.setEnableEdit(false)
  		.setToolButtons(['zoomIn', 'zoomOut']);
  	chemViewer.setToolbarEvokeModes([
		  Kekule.Widget.EvokeMode.ALWAYS
	]);
	chemViewer.setToolbarRevokeModes([
  		Kekule.Widget.EvokeMode.ALWAYS
	]);
});