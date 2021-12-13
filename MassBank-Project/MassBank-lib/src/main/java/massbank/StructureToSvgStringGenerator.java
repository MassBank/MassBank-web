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
	
	public static ClickablePreviewImageData createClickablePreviewImage(
			String accession, String inchi, String smiles,
			String tmpFileFolder, String tmpUrlFolder,
			int sizeSmall, int sizeBig
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
		String accession2			= accession.replaceAll("[^0-9a-zA-Z]", "_");
		String fileNameSmall		= accession2 + "_small.svg";
		String fileNameBig			= accession2 + "_big.svg";
		
		String tmpFileSmall			= (new File(tmpFileFolder + fileNameSmall	)).getPath();
		String tmpFileBig			= (new File(tmpFileFolder + fileNameBig		)).getPath();
		
		if (!tmpUrlFolder.endsWith("/")) tmpUrlFolder= tmpUrlFolder + "/";
		String tmpUrlSmall			= tmpUrlFolder + fileNameSmall;
		String tmpUrlBig			= tmpUrlFolder + fileNameBig;
		
		// adapt size of svg image
		String svgSmall		= StructureToSvgStringGenerator.resizeSvg(svg, sizeSmall,	sizeSmall);
		String svgBig		= StructureToSvgStringGenerator.resizeSvg(svg, sizeBig,		sizeBig);
		
		return new ClickablePreviewImageData(
				tmpFileSmall,
				tmpFileBig, 
				tmpUrlSmall, 
				tmpUrlBig,
				svgSmall, 
				svgBig            
		);
	}
	
	public static class ClickablePreviewImageData{
		public final String tmpFileSmall;
		public final String tmpFileBig;
		public final String tmpUrlSmall;
		public final String tmpUrlBig;
		public final String svgSmall;
		public final String svgBig;
		
		public ClickablePreviewImageData(
				String tmpFileSmall,
				String tmpFileBig,
				String tmpUrlSmall,
				String tmpUrlBig,
				String svgSmall,
				String svgBig
		) {
			this.tmpFileSmall	= tmpFileSmall;
			this.tmpFileBig		= tmpFileBig;   
			this.tmpUrlSmall	= tmpUrlSmall;
			this.tmpUrlBig		= tmpUrlBig;
			this.svgSmall		= svgSmall;         
			this.svgBig			= svgBig;           
		}
	}
}
