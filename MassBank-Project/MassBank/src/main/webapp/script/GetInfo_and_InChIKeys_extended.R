## Script by Erik Müller, Tobias Schulze, Emma Schymanski
## Edited 7.7.14 E. Schymanski (adding InChI Key functionality)
## Edited 31.1.16 T. Schulze (adding parsing of more fields)
## note new dependence on obabel.exe
## LICENSE: GPL 3.0
## Copyright (c) 2014-2017

# get InChI Keys using Open Babel
# need to define the directory containing Open Babel, specifically "obabel.exe"
# This does not work if the path contains a space; the exe can be pasted anywhere
create.inchikey <- function(SMILES, babel_dir) {
  cmd <- paste(babel_dir, "/obabel -:'", SMILES, "' -oinchikey", sep="")
  res <- system(cmd, intern=TRUE, ignore.stderr=TRUE)
  return(res)
}
# batch conversion would be faster, but stops at first error without warning.

##Directory is the name of the directory
##csvname is the designated name that the csv-file will get
##babel_dir is the directory path (no spaces!) containing obabel.exe
getInfoFixKey <- function(Directory,csvname, babel_dir){
		Files <- list.files(Directory, pattern="*.txt", full.names=TRUE, recursive=TRUE)
        #recursive=TRUE should get all sub-dirs
        #need to add pattern to skip the mols and tsvs
		wantedmat <- matrix(0,length(Files),(25))
        colnames(wantedmat) <- c("ACCESSION","LICENSE","AUTHORS","RESEARCH_GROUP","NAME","FORMULA","EXACT_MASS","IUPAC","INCHIKEY","SMILES","CSID","INSTRUMENT","INSTRUMENT_TYPE","MS_TYPE","IONIZATION","ION_MODE","FRAGMENTATION_MODE","COLL_E","RESOLUTION","BASE_PEAK","PRECURSOR_MZ","PRECURSOR_TYPE","SPLASH","EULINK","JPLINK")
		for(i in 1:length(Files)){
            # if((i %% 1000) == 0){
                # print(i)
            # }
            fileConnection <- file(normalizePath(Files[i]))
			record <- readLines(fileConnection)
			close(fileConnection)
      
            ## Check if fields contain NAs
			CSIDTRUE <- grep('CH$LINK: CHEMSPIDER ',record, value = TRUE, fixed = TRUE)
			CSIDFALSE <- "N/A"
			INCHIKEYTRUE <- grep('CH$LINK: INCHIKEY ',record, value = TRUE, fixed = TRUE)
			INCHIKEYFALSE <- "N/A"
			INCHIKEY <- ifelse(length(INCHIKEYTRUE)==1, substring(grep('CH$LINK: INCHIKEY ',record, value = TRUE, fixed = TRUE),19), INCHIKEYFALSE)
			SMILES <- substring(grep('CH$SMILES: ',record, value = TRUE, fixed = TRUE),12)
			SMILES_NA <- grep("N/A",SMILES, value = TRUE, fixed = TRUE)
			INCHIKEY_NA <- grep("N/A",INCHIKEY, value = TRUE, fixed = TRUE)
			INSTRUMENT_TYPE_TRUE <- grep('AC$INSTRUMENT_TYPE: ',record, value = TRUE, fixed = TRUE)
			INSTRUMENT_TYPE_FALSE <- "N/A"
			INSTRUMENT_TRUE <- grep('AC$INSTRUMENT: ',record, value = TRUE, fixed = TRUE)
			INSTRUMENT_FALSE <- "N/A"
			RESOLUTION_TRUE <- grep('AC$MASS_SPECTROMETRY: RESOLUTION ',record, value = TRUE, fixed = TRUE)
			RESOLUTION_FALSE <- "N/A"
			MS_TYPE_TRUE <- grep('AC$MASS_SPECTROMETRY: MS_TYPE ',record, value = TRUE, fixed = TRUE)
			MS_TYPE_FALSE <- "N/A"
			PRECURSOR_TYPE_TRUE <- grep('MS$FOCUSED_ION: PRECURSOR_TYPE ',record, value = TRUE, fixed = TRUE)
			PRECURSOR_TYPE_FALSE <- "N/A"
			PRECURSOR_MZ_TRUE <- grep('MS$FOCUSED_ION: PRECURSOR_M/Z ',record, value = TRUE, fixed = TRUE)
			PRECURSOR_MZ_FALSE <- "N/A"
			BASE_PEAK_TRUE <- grep('MS$FOCUSED_ION: BASE_PEAK ',record, value = TRUE, fixed = TRUE)
			BASE_PEAK_FALSE <- "N/A"
			IONIZATION_TRUE <- grep('AC$MASS_SPECTROMETRY: IONIZATION ',record, value = TRUE, fixed = TRUE)
			IONIZATION_FALSE <- "N/A"
			FRAGMENTATION_MODE_TRUE <- grep('AC$MASS_SPECTROMETRY: FRAGMENTATION_MODE ',record, value = TRUE, fixed = TRUE)
			FRAGMENTATION_MODE_FALSE <- "N/A"
			COLL_E_TRUE <- grep('AC$MASS_SPECTROMETRY: COLLISION_ENERGY ',record, value = TRUE, fixed = TRUE)
			COLL_E_FALSE <- "N/A"
            SPLASH_TRUE <- grep('PK$SPLASH',record, value = TRUE, fixed = TRUE)
            SPLASH_FALSE <- "N/A"
      #fill in missing InChI Key where possible with Open Babel conversion from SMILES
      # Can only attempt this if SMILES exists; takes a while so don't recalculate unless necessary
      if((length(INCHIKEY_NA)==1)&&(length(SMILES_NA)!=1)) {
        new_inchikey <- create.inchikey(SMILES, babel_dir)
        if(length(new_inchikey)>=1) {
          INCHIKEY <- new_inchikey
        }
      }

## Parse the fields from the records	  
## The information block
wantedmat[i,'ACCESSION'] <- substring(grep('ACCESSION:',record, value = TRUE, fixed = TRUE),12)
wantedmat[i,'LICENSE'] <- substring(grep('LICENSE:',record, value = TRUE, fixed = TRUE),10)
wantedmat[i,'AUTHORS'] <- substring(grep('AUTHORS:',record, value = TRUE, fixed = TRUE),10)
wantedmat[i,'RESEARCH_GROUP'] <- basename(dirname(Files[i]))
chnames <- list()
chnames <- as.list(substring(grep('CH$NAME:',record, value = TRUE, fixed = TRUE),10))
wantedmat[i,'NAME'] <- chnames[[1]]
wantedmat[i,'FORMULA'] <- substring(grep('CH$FORMULA:',record, value = TRUE, fixed = TRUE),13)
wantedmat[i,'EXACT_MASS'] <- substring(grep('CH$EXACT_MASS',record, value = TRUE, fixed = TRUE),16)
wantedmat[i,'SMILES'] <- SMILES
wantedmat[i,'IUPAC'] <- substring(grep('CH$IUPAC:',record, value = TRUE, fixed = TRUE),11)

## The next lines check if field is NA or not (for optional fields)
#ifelse(is.na(INCHIKEYTRUE) == TRUE, wantedmat[i,'INCHIKEY'] <- INCHIKEYFALSE, wantedmat[i,'INCHIKEY'] <- substring(grep('CH$LINK: INCHIKEY',record, value = TRUE, fixed = TRUE),19))
#ifelse(is.na(CSIDTRUE) == TRUE, wantedmat[i,'CSID'] <- CSIDFALSE, wantedmat[i,'CSID'] <- substring(grep('CH$LINK: CHEMSPIDER',record, value = TRUE, fixed = TRUE),21))
#INCHIKEY <- ifelse(length(INCHIKEYTRUE)==1, wantedmat[i,'INCHIKEY'] <- substring(grep('CH$LINK: INCHIKEY',record, value = TRUE, fixed = TRUE),19), wantedmat[i,'INCHIKEY'] <- INCHIKEYFALSE)
wantedmat[i,'INCHIKEY'] <- INCHIKEY
ifelse(length(CSIDTRUE)==1, wantedmat[i,'CSID'] <- substring(grep('CH$LINK: CHEMSPIDER',record, value = TRUE, fixed = TRUE),21), wantedmat[i,'CSID'] <- CSIDFALSE)

## The instrument block
ifelse(length(INSTRUMENT_TRUE)==1, wantedmat[i,'INSTRUMENT'] <- substring(grep('AC$INSTRUMENT:',record, value = TRUE, fixed = TRUE),15), wantedmat[i,'INSTRUMENT'] <- INSTRUMENT_FALSE)
ifelse(length(INSTRUMENT_TYPE_TRUE)==1, wantedmat[i,'INSTRUMENT_TYPE'] <- substring(grep('AC$INSTRUMENT_TYPE:',record, value = TRUE, fixed = TRUE),20), wantedmat[i,'INSTRUMENT_TYPE'] <- INSTRUMENT_TYPE_FALSE)

## The spectroscopy block
ifelse(length(MS_TYPE_TRUE)==1, wantedmat[i,'MS_TYPE'] <- substring(grep('AC$MASS_SPECTROMETRY: MS_TYPE ',record, value = TRUE, fixed = TRUE),30), wantedmat[i,'MS_TYPE'] <- MS_TYPE_FALSE)
ifelse(length(IONIZATION_TRUE)==1, wantedmat[i,'IONIZATION'] <- substring(grep('AC$MASS_SPECTROMETRY: IONIZATION ',record, value = TRUE, fixed = TRUE),33), wantedmat[i,'IONIZATION'] <- IONIZATION_FALSE)
wantedmat[i,'ION_MODE'] <- substring(grep('AC$MASS_SPECTROMETRY: ION_MODE',record, value = TRUE, fixed = TRUE),31)
ifelse(length(FRAGMENTATION_MODE_TRUE)==1, wantedmat[i,'FRAGMENTATION_MODE'] <- substring(grep('AC$MASS_SPECTROMETRY: FRAGMENTATION_MODE ',record, value = TRUE, fixed = TRUE),33), wantedmat[i,'FRAGMENTATION_MODE'] <- FRAGMENTATION_MODE_FALSE)
ifelse(length(COLL_E_TRUE)==1, wantedmat[i,'COLL_E'] <- substring(grep('AC$MASS_SPECTROMETRY: COLLISION_ENERGY ',record, value = TRUE, fixed = TRUE),39), wantedmat[i,'COLL_E'] <- COLL_E_FALSE)
ifelse(length(RESOLUTION_TRUE)==1, wantedmat[i,'RESOLUTION'] <- substring(grep('AC$MASS_SPECTROMETRY: RESOLUTION ',record, value = TRUE, fixed = TRUE),33), wantedmat[i,'RESOLUTION'] <- RESOLUTION_FALSE)

## The MS block
ifelse(length(BASE_PEAK_TRUE)==1, wantedmat[i,'BASE_PEAK'] <- substring(grep('MS$FOCUSED_ION: BASE_PEAK ',record, value = TRUE, fixed = TRUE),26), wantedmat[i,'BASE_PEAK'] <- BASE_PEAK_FALSE)
ifelse(length(PRECURSOR_MZ_TRUE)==1, wantedmat[i,'PRECURSOR_MZ'] <- substring(grep('MS$FOCUSED_ION: PRECURSOR_M/Z ',record, value = TRUE, fixed = TRUE),30), wantedmat[i,'PRECURSOR_MZ'] <- PRECURSOR_MZ_FALSE)
ifelse(length(PRECURSOR_TYPE_TRUE)==1, wantedmat[i,'PRECURSOR_TYPE'] <- substring(grep('MS$FOCUSED_ION: PRECURSOR_TYPE ',record, value = TRUE, fixed = TRUE),31), wantedmat[i,'PRECURSOR_TYPE'] <- PRECURSOR_TYPE_FALSE)
ifelse(length(SPLASH_TRUE)==1, wantedmat[i,'SPLASH'] <- substring(grep('PK$SPLASH:',record, value = TRUE, fixed = TRUE),12), wantedmat[i,'SPLASH'] <- SPLASH_FALSE)

## The deep links
wantedmat[i,'EULINK'] <- paste("http://massbank.eu/MassBank/FwdRecord.jsp?id=", substring(grep('ACCESSION:',record, value = TRUE, fixed = TRUE),12), sep="")
}

## Write the csv file
write.csv(wantedmat,csvname)
return("Successfully wrote the csv")
}
