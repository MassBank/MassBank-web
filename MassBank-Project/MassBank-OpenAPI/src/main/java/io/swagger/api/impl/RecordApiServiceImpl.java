package io.swagger.api.impl;

import io.swagger.api.*;



import java.util.Map;
import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2020-01-17T13:53:26.722Z[GMT]")public class RecordApiServiceImpl extends RecordApiService {
    @Override
    public Response recordIdGet(String id, SecurityContext securityContext) throws NotFoundException {
        String record ="ACCESSION: SM861901\n" + 
        		"RECORD_TITLE: Captopril; LC-ESI-QFT; MS2; CE: 35 NCE; R=35000; [M+H]+\n" + 
        		"DATE: 2016.12.12\n" + 
        		"AUTHORS: Krauss M, Schymanski EL, Weidauer C, Schupke H, UFZ and Eawag\n" + 
        		"LICENSE: CC BY\n" + 
        		"COPYRIGHT: Copyright (C) 2016 UFZ/Eawag\n" + 
        		"PUBLICATION: Schymanski, E. L.; Ruttkies, C.; Krauss, M.; Brouard, C.; Kind, T.; Dührkop, K.; Allen, F.; Vaniya, A.; Verdegem, D.; Böcker, S.; et al. Critical Assessment of Small Molecule Identification 2016: Automated Methods. Journal of Cheminformatics 2017, 9 (1). DOI:10.1186/s13321-017-0207-1\n" + 
        		"COMMENT: CONFIDENCE standard compound\n" + 
        		"COMMENT: INTERNAL_ID 8619\n" + 
        		"CH$NAME: Captopril\n" + 
        		"CH$NAME: (2S)-1-[(2S)-2-methyl-3-sulfanylpropanoyl]pyrrolidine-2-carboxylic acid\n" + 
        		"CH$COMPOUND_CLASS: N/A; Environmental Standard\n" + 
        		"CH$FORMULA: C9H15NO3S\n" + 
        		"CH$EXACT_MASS: 217.07726\n" + 
        		"CH$SMILES: C[C@H](CS)C(=O)N1CCC[C@H]1C(O)=O\n" + 
        		"CH$IUPAC: InChI=1S/C9H15NO3S/c1-6(5-14)8(11)10-4-2-3-7(10)9(12)13/h6-7,14H,2-5H2,1H3,(H,12,13)/t6-,7+/m1/s1\n" + 
        		"CH$LINK: CAS 62571-86-2\n" + 
        		"CH$LINK: CHEBI 3380\n" + 
        		"CH$LINK: KEGG D00251\n" + 
        		"CH$LINK: PUBCHEM CID:44093\n" + 
        		"CH$LINK: INCHIKEY FAKRSMQSSFJEIM-RQJHMYQMSA-N\n" + 
        		"CH$LINK: CHEMSPIDER 40130\n" + 
        		"CH$LINK: COMPTOX DTXSID1037197\n" + 
        		"AC$INSTRUMENT: Q Exactive Plus Orbitrap Thermo Scientific\n" + 
        		"AC$INSTRUMENT_TYPE: LC-ESI-QFT\n" + 
        		"AC$MASS_SPECTROMETRY: MS_TYPE MS2\n" + 
        		"AC$MASS_SPECTROMETRY: ION_MODE POSITIVE\n" + 
        		"AC$MASS_SPECTROMETRY: IONIZATION ESI\n" + 
        		"AC$MASS_SPECTROMETRY: FRAGMENTATION_MODE HCD\n" + 
        		"AC$MASS_SPECTROMETRY: COLLISION_ENERGY 35  (nominal)\n" + 
        		"AC$MASS_SPECTROMETRY: RESOLUTION 35000\n" + 
        		"AC$CHROMATOGRAPHY: COLUMN_NAME Kinetex C18 EVO 2.6 um, 2.1x50 mm, precolumn 2.1x5 mm, Phenomenex\n" + 
        		"AC$CHROMATOGRAPHY: FLOW_GRADIENT 95/5 at 0 min, 95/5 at 1 min, 0/100 at 13 min, 0/100 at 24 min\n" + 
        		"AC$CHROMATOGRAPHY: FLOW_RATE 300 uL/min\n" + 
        		"AC$CHROMATOGRAPHY: RETENTION_TIME 5.861 min\n" + 
        		"AC$CHROMATOGRAPHY: SOLVENT A water with 0.1% formic acid\n" + 
        		"AC$CHROMATOGRAPHY: SOLVENT B methanol with 0.1% formic acid\n" + 
        		"MS$FOCUSED_ION: BASE_PEAK 189.1019                                                                                                             \n" + 
        		"MS$FOCUSED_ION: PRECURSOR_M/Z 218.0845                                                                                                         \n" + 
        		"MS$FOCUSED_ION: PRECURSOR_TYPE [M+H]+                                                                                                          \n" + 
        		"MS$DATA_PROCESSING: RECALIBRATE loess on assigned fragments and MS1                                                                            \n" + 
        		"MS$DATA_PROCESSING: REANALYZE Peaks with additional N2/O included                                                                              \n" + 
        		"MS$DATA_PROCESSING: WHOLE RMassBank 2.3.1                                                                                                      \n" + 
        		"PK$SPLASH: splash10-01b9-9720000000-8218e12ef8f7e988c312                                                                                       \n" + 
        		"PK$ANNOTATION: m/z tentative_formula formula_count mass error(ppm)                                                                             \n" + 
        		"  70.0652 C4H8N+ 1 70.0651 0.62                                                                                                                \n" + 
        		"  75.0263 C3H7S+ 1 75.0263 0.14\n" + 
        		"  95.0855 C7H11+ 1 95.0855 -0.69\n" + 
        		"  97.0647 C6H9O+ 1 97.0648 -0.6\n" + 
        		"  103.0212 C4H7OS+ 1 103.0212 -0.38\n" + 
        		"  109.1011 C8H13+ 1 109.1012 -0.7\n" + 
        		"  114.0549 C5H8NO2+ 1 114.055 -0.67\n" + 
        		"  116.0706 C5H10NO2+ 1 116.0706 -0.32\n" + 
        		"  137.0957 C9H13O+ 1 137.0961 -2.92\n" + 
        		"  155.1065 C9H15O2+ 1 155.1067 -0.88\n" + 
        		"  172.079 C8H14NOS+ 1 172.0791 -0.57\n" + 
        		"  184.0969 C9H14NO3+ 1 184.0968 0.7\n" + 
        		"  200.0739 C9H14NO2S+ 1 200.074 -0.32\n" + 
        		"  218.0845 C9H16NO3S+ 1 218.0845 -0.27\n" + 
        		"PK$NUM_PEAK: 14\n" + 
        		"PK$PEAK: m/z int. rel.int.\n" + 
        		"  70.0652 16540395 934\n" + 
        		"  75.0263 9400146 531\n" + 
        		"  95.0855 53190.4 3\n" + 
        		"  97.0647 36510.9 2\n" + 
        		"  103.0212 201177.7 11\n" + 
        		"  109.1011 19142.7 1\n" + 
        		"  114.0549 45679.7 2\n" + 
        		"  116.0706 17676558 999\n" + 
        		"  137.0957 20042.7 1\n" + 
        		"  155.1065 45508.7 2\n" + 
        		"  172.079 4405647 248\n" + 
        		"  184.0969 72044.4 4\n" + 
        		"  200.0739 717682.4 40\n" + 
        		"  218.0845 6912122.5 390\n" + 
        		"//";
        //return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
        return Response.ok().entity(record).build();
    }
}
