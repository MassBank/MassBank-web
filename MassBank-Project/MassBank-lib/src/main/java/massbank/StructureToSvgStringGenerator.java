package massbank;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

public class StructureToSvgStringGenerator {
	
	public static IAtomContainer structureFromInChI(String inchi){
		IAtomContainer mol	= null;
		try {
			// get atom container
			InChIGeneratorFactory inchiFactory = InChIGeneratorFactory.getInstance();
			InChIToStructure inchi2structure = inchiFactory.getInChIToStructure(inchi, SilentChemObjectBuilder.getInstance());
			mol	= inchi2structure.getAtomContainer();
		} catch (CDKException e) {
			System.out.println("Warning: " + e.getLocalizedMessage());
		}
		
		return mol;
	}
	
	public static IAtomContainer structureFromSMILES(String smiles){
		IAtomContainer mol	= null;
		try {
			// get atom container
	    	SmilesParser smipar = new SmilesParser(SilentChemObjectBuilder.getInstance());
	    	mol = smipar.parseSmiles(smiles);
		} catch (CDKException e) {
			System.out.println("Warning: " + e.getLocalizedMessage());
		}
		
		return mol;
	}
	
	public static String drawToSvg(IAtomContainer mol){
		String svg	= null;
		try {
			// get the SVG XML string
			svg = new DepictionGenerator().withAtomColors().depict(mol).toSvgStr();
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return svg;
	}
	
	public static String resizeSvg(String svg, int width, int heigth){
		return svg.replaceAll(
				"width='[0-9]*(\\.[0-9]*)?mm' height='[0-9]*(\\.[0-9]*)?mm'", 
				"width=\"" + width + "\" height=\"" + heigth + "\""
		);
	}
	
	public static String setSvgStyle(String svg, String styleParameters){
		return svg.replaceAll(
				"viewBox", 
				"style=\"" + styleParameters + "\" viewBox"
		);
	}
	
//	public static ClickablePreviewImageData createClickablePreviewImage(
//			String databaseName, String accession, String tmpFileFolder, String tmpUrlFolder,
//			int sizeSmall, int sizeMedium, int sizeBig
//	){
//		// fetch accession data
////		DatabaseManager dbManager	= new DatabaseManager(databaseName);
////		AccessionData accData	= dbManager.getAccessionData(accession);
////		dbManager.closeConnection();
//		
//		AccessionData accData	= AccessionData.getAccessionDataFromDatabase(accession);
//		if(accData == null)
//			return null;
//		
//		return createClickablePreviewImage(
//				accData, tmpFileFolder, tmpUrlFolder,
//				sizeSmall, sizeMedium, sizeBig
//		);
//	}
//	
//	public static ClickablePreviewImageData createClickablePreviewImage(
//			AccessionData accData, 
//			String tmpFileFolder, String tmpUrlFolder,
//			int sizeSmall, int sizeMedium, int sizeBig
//	){
//		// fetch structure
//		
//		String accession	= accData.get("ACCESSION").get(0)[2];
//		String inchi		= accData.get("CH$IUPAC").get(0)[2];
//		String smiles		= accData.get("CH$SMILES").get(0)[2];
//		
//		return StructureToSvgStringGenerator.createClickablePreviewImage(
//				accession, inchi, smiles, 
//				tmpFileFolder, tmpUrlFolder, 
//				sizeSmall, sizeMedium, sizeBig
//		);
//	}
	
	public static ClickablePreviewImageData createClickablePreviewImage(
			String accession, String inchi, String smiles,
			String tmpFileFolder, String tmpUrlFolder,
			int sizeSmall, int sizeMedium, int sizeBig
	){
		
		if(inchi != null && (inchi.equals("NA") || inchi.equals("N/A")))
			inchi	= null;
		if(smiles != null && (smiles.equals("NA") || smiles.equals("N/A")))
			smiles	= null;
		
		// generate SVG string from structure (inchi / smiles)
		boolean inchiThere	= inchi  != null;
		boolean smilesThere	= smiles != null;
		
		IAtomContainer mol	= null;
		if(mol == null && smilesThere)	mol	= StructureToSvgStringGenerator.structureFromSMILES(smiles);
		if(mol == null && inchiThere)	mol	= StructureToSvgStringGenerator.structureFromInChI(inchi);
		if(mol == null)
			return null;
		
		String svg = StructureToSvgStringGenerator.drawToSvg(mol);
		if(svg == null)
			return null;
		
		// path to temp file as local file and as url
		final SimpleDateFormat sdf	= new SimpleDateFormat("yyMMdd_HHmmss_SSS");
		String accession2			= accession.replaceAll("[^0-9a-zA-Z]", "_");
		
		String fileNameSmall		= sdf.format(new Date()) + "_" + accession2 + "_small.svg";
		String fileNameMedium		= sdf.format(new Date()) + "_" + accession2 + "_medium.svg";
		String fileNameBig			= sdf.format(new Date()) + "_" + accession2 + "_big.svg";
		
		String tmpFileSmall			= (new File(tmpFileFolder + fileNameSmall	)).getPath();
		String tmpFileMedium		= (new File(tmpFileFolder + fileNameMedium	)).getPath();
		String tmpFileBig			= (new File(tmpFileFolder + fileNameBig		)).getPath();
		
		if (!tmpUrlFolder.endsWith("/")) tmpUrlFolder= tmpUrlFolder + "/";
		String tmpUrlSmall			= tmpUrlFolder + fileNameSmall;
		String tmpUrlMedium			= tmpUrlFolder + fileNameMedium;
		String tmpUrlBig			= tmpUrlFolder + fileNameBig;
		
		// adapt size of svg image
		String svgSmall		= StructureToSvgStringGenerator.resizeSvg(svg, sizeSmall,	sizeSmall);
		String svgMedium	= StructureToSvgStringGenerator.resizeSvg(svg, sizeMedium,	sizeMedium);
		String svgBig		= StructureToSvgStringGenerator.resizeSvg(svg, sizeBig,		sizeBig);
		
		return new ClickablePreviewImageData(
				tmpFileSmall,
				tmpFileMedium,
				tmpFileBig, 
				tmpUrlSmall, 
				tmpUrlMedium, 
				tmpUrlBig,
				svgSmall, 
				svgMedium,
				svgBig            
		);
	}
	
	public static class ClickablePreviewImageData{
		public final String tmpFileSmall;
		public final String tmpFileMedium;
		public final String tmpFileBig;
		public final String tmpUrlSmall;
		public final String tmpUrlMedium;
		public final String tmpUrlBig;
		
		public final String svgSmall;
		public final String svgMedium;
		public final String svgBig;
		
		public ClickablePreviewImageData(
				String tmpFileSmall,
				String tmpFileMedium,
				String tmpFileBig,
				String tmpUrlSmall,
				String tmpUrlMedium,
				String tmpUrlBig,
				String svgSmall,
				String svgMedium,
				String svgBig
		) {
			this.tmpFileSmall	= tmpFileSmall;
			this.tmpFileMedium	= tmpFileMedium;
			this.tmpFileBig		= tmpFileBig;   
			this.tmpUrlSmall	= tmpUrlSmall;
			this.tmpUrlMedium	= tmpUrlMedium;
			this.tmpUrlBig		= tmpUrlBig;
			this.svgSmall		= svgSmall;         
			this.svgMedium		= svgMedium;        
			this.svgBig			= svgBig;           
		}
		
		public String getMediumClickableImage(){
			// write big image as temp file
			FileUtil.writeToFile(this.svgBig,		this.tmpFileBig);
			
			// add expandMolView on click for small image
			String svgMedium	= this.svgMedium.replaceAll(
					"</g>\\n</svg>", 
					"<rect class=\"btn\" x=\"0\" y=\"0\" width=\"200\" height=\"200\" onclick=\"expandMolView('" + this.tmpUrlBig + "')\" fill-opacity=\"0.0\" stroke-width=\"0\" /> </g>\\\\n</svg>"
			);
			// cursor for small image
			svgMedium	= StructureToSvgStringGenerator.setSvgStyle(svgMedium, "cursor:pointer");
			
			return svgMedium;
		}
		public String getMediumClickablePreviewLink(String previewName, String linkName){
			// write big image and medium image as temp file
			FileUtil.writeToFile(this.svgMedium,	this.tmpFileMedium);
			FileUtil.writeToFile(this.svgBig,		this.tmpFileBig);
			
			String linkHtml	= 
					"<a " +
					"href=\"" + this.tmpUrlMedium + "\" " +
					"class=\"preview_structure\" " +
					"title=\"" + previewName.toString() + "\" " +
					"onclick=\"expandMolView('" + this.tmpUrlBig + "');return false;\" " +
					"target=\"_blank\"" +
					">" + linkName + "</a>" +
					"\n";
			
			return linkHtml;
		}
	}
}
