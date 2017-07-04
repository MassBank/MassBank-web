package massbank;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;

public class StructureToSvgStringGenerator {
	
	public static String fromInChI(String inchi){
		
		String svg	= null;
		try {
			// get atom container
			InChIGeneratorFactory inchiFactory = InChIGeneratorFactory.getInstance();
			InChIToStructure inchi2structure = inchiFactory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance());
			IAtomContainer mol	= inchi2structure.getAtomContainer();
			
			// get the SVG XML string
			svg = new DepictionGenerator().depict(mol).toSvgStr();
		} catch (CDKException e) {
			e.printStackTrace();
		}
		
		return svg;
	}
	public static String fromSMILES(String smiles){
		
		String svg	= null;
		try {
			// get atom container
	    	SmilesParser smipar = new SmilesParser(DefaultChemObjectBuilder.getInstance());
	    	IAtomContainer mol = smipar.parseSmiles(smiles);
			
			// get the SVG XML string
			svg = new DepictionGenerator().depict(mol).toSvgStr();
		} catch (CDKException e) {
			e.printStackTrace();
		}
		
		return svg;
	}
	public static String resizeSvg(String svg, int width, int heigth, String styleParameters){
		return svg.replaceAll(
				"width='[0-9]*(\\.[0-9]*)?mm' height='[0-9]*(\\.[0-9]*)?mm'", 
				"width=\"" + width + "\" height=\"" + heigth + "\"" + 
				((styleParameters != null) ? " style=\"" + styleParameters + "\"" : "")
		);
	}
}
