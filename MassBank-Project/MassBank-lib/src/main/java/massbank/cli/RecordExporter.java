package massbank.cli;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import massbank.Record;
import massbank.export.RecordToJson;
import massbank.export.RecordToJsonLD;
import massbank.export.RecordToNIST_MSP;
import massbank.export.RecordToRIKEN_MSP;

public class RecordExporter {
	private static final Logger logger = LogManager.getLogger(RecordExporter.class);
	
/*
.ms2 text format: peptides
-----------------
https://skyline.ms/wiki/home/software/BiblioSpec/page.view?name=BiblioSpec%20input%20and%20output%20file%20formats
This format is recongnized by proteowizard's msconvert and can be converted into other formats such as .mzXML.
In an .ms2 file there are four types of lines. 
	Lines beginning with 'H' are header lines and contain information about how the data was collected as well as comments. They appear at the beginning of the file. 
	Lines beginning with 'S' are followed by the scan number and the precursor m/z. 
	Lines beginning with 'Z' give the charge state followed by the mass of the ion at that charge state. 
	Lines beginning with 'D' contain information relevant to the preceeding charge state. BlibToMs2's output will include D-lines with the sequence and modified sequence. 
The file is arranged with these S, Z and D lines for one spectrum followed by a peak list: 
	a pair of values giving each peaks m/z and intensity. Here is an example file 

H      CreationDate    Mon Apr 12 15:12:14 2010
H       Extractor       BlibToMs2
H       Library /home/me/research/search/demo.blib
S       1       1       636.34
Z       2       1253.36
D       seq     FKNGFQTGSASK
D       modified seq    FKNGFQTGSASK
187.40  12.5
193.10  19.5
242.30  14.2
244.30  9.0
S       2       2       745.3
Z       2       1471.7
D       seq     NFLETVELQVGLK
D       modified seq    NFLETVELQVGLK
1224.60 7.9
1228.70 468.9
1230.40 658.5
1231.50 144.2

BlibBuild .ssl file:
--------------------
https://skyline.ms/wiki/home/software/BiblioSpec/page.view?name=BiblioSpec%20input%20and%20output%20file%20formats

NIST *.msp file:
----------------
https://chemdata.nist.gov/mass-spc/ftp/mass-spc/PepLib.pdf
(section 'Spectrum Fields and Format')

Name: KDLGEEHFK/2
MW: 1103.561
Comment: Spec=Consensus Pep=N-Semitryp_irreg/miss_good Fullname=F.KDLGEEHFK.G/2 Mods=0 Parent=551.781 Inst=it Mz_diff=0.544 Mz_exact=551.7805 Mz_av=552.114 Protein="sp|P02769|ALBU_BOVIN Serum albumin precursor (Allergen Bos d 6) (BSA) - Bos taurus (Bovine)." Pseq=131/1 Organism="Protein" Se=4^X12:ex=0.00037/0.0003992,td=25.85/1379,sd=0/0,hs=38.5/1.433,bs=0.00027,b2=0.00028,bd=133^O10:ex=0.0002435/0.0009314,td=74.85/3.186e+004,pr=3.235e-007/8.612e-007,bs=2.73e-005,b2=5.56e-005,bd=1.56^I1:ex=0.0339/0,dc=0.939/0,do=6.14/0,bs=0.0339,bd=0.939^C1:ex=0.032/0,td=0/0,sd=0/0,hs=555/0,bs=0.032 Sample=7/bsa_cam,2,6/bsa_cam_different_voltages,1,3/bsa_none,0,1/nist_yl_31011_sigma_t9253_bsa_cam,4,6/nist_yl_31011_sigma_t9253_bsa_time_cam,4,6/nist_yl_31611_sigma_t9253_bsa_cam,0,3/nist_yl_sgma_t9253_bsa_none,1,2 Nreps=12/27 Missing=0.1916/0.0688 Parent_med=552.3075/0.22 Max2med_orig=100.0/0.0 Dotfull=0.743/0.044 Dot_cons=0.809/0.048 Unassign_all=0.173 Unassigned=0.105 Dotbest=0.83 Flags=12,9,1 Naa=9 DUScorr=1.5/0.71/2.9 Dottheory=0.84 Pfin=4.6e+008 Probcorr=6.7 Tfratio=6e+003 Pfract=0 Unassigned_corrected=0.011
Num peaks: 124
201.2	149	"? 11/10 0.7"
209.1	238	"b2-35/-0.02 11/11 0.7"
226.3	779	"b2-18/0.18 12/12 1.7"
227.3	484	"b2-17/0.18 12/12 0.9"
228.4	62	"b2-17i/1.28 7/10 0.2"

*/


public static void main(String[] arguments) {
	// load version and print
	final Properties properties = new Properties();
	try {
		properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("project.properties"));
	} catch (IOException e) {
		e.printStackTrace();
		System.exit(1);
	}
	System.out.println("Exporter version: " + properties.getProperty("version"));

	// parse command line
	Options options = new Options();
	options.addRequiredOption("o", "outfile", true, "name of output file");
	options.addRequiredOption("f", "format", true, "output format; possible values: RIKEN_MSP, NIST_MSP; json; jsonld");
	CommandLine cmd = null;
	try {
		cmd = new DefaultParser().parse(options, arguments);
	} catch (ParseException e) {
		// oops, something went wrong
		System.err.println("Parsing command line failed. Reason: " + e.getMessage());
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("RecordExporter [OPTIONS] <FILE|DIR> [<FILE|DIR> ...]", options);
		System.exit(1);
	}
	if (cmd.getArgList().size() == 0) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("RecordExporter [OPTIONS] <FILE|DIR> [<FILE|DIR> ...]", options);
		System.exit(1);
	}
	String format = cmd.getOptionValue("f");
	if (format != null) {
		if (!Arrays.asList("RIKEN_MSP", "NIST_MSP", "json", "jsonld").contains(format)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("RecordExporter [OPTIONS] <FILE|DIR> [<FILE|DIR> ...]", options);
			System.exit(1);
		}
	}

	// loop over all arguments
	// find all files in arguments and all *.txt files in directories and
	// subdirectories
	// specified in arguments
	List<Record> records = cmd.getArgList().parallelStream().map(argument -> {
		// find all files in arguments and all *.txt files in directories and
		// subdirectories
		// specified in arguments
		File argumentFile = new File(argument);
		List<File> filesToProcess = new ArrayList<File>();
		if (argumentFile.isFile() && FilenameUtils.getExtension(argument).equals("txt")) {
			filesToProcess.add(argumentFile);
		} else if (argumentFile.isDirectory()) {
			if (!argumentFile.getName().startsWith("."))
				filesToProcess.addAll(FileUtils.listFiles(argumentFile, new String[] { "txt" }, true));
		} else {
			logger.warn("Argument " + argument + " could not be processed.");
		}

		// read all files and process to Record
		List<Record> argumentRecords = filesToProcess.parallelStream().map(filename -> {
			Record record = null;
			try {
				String recordString = FileUtils.readFileToString(filename, StandardCharsets.UTF_8);
				Set<String> config = new HashSet<String>();
				config.add("legacy");
				record = Validator.validate(recordString, config);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return record;
		}).collect(Collectors.toList());
		return argumentRecords;
	})
	// concat all results
	.flatMap(Collection::stream).filter(Objects::nonNull)
	// output as List
	.collect(Collectors.toList());
	// System.out.println(recordfiles.toString());

	File outfile = new File(cmd.getOptionValue("o"));

	switch (format) {
	case "RIKEN_MSP":
		RecordToRIKEN_MSP.recordsToRIKEN_MSP(outfile, records);
		break;
	case "NIST_MSP":
		RecordToNIST_MSP.recordsToNIST_MSP(outfile, records);
		break;
	case "json":
		RecordToJson.recordsToJson(outfile, records);
		break;
	case "jsonld":
		RecordToJsonLD.recordsToJson(outfile, records);
		break;
	default:
		logger.error("This code should not run.");
		System.exit(1);
	}

}
}
