## Generation of a static statistics page for MassBank server
## Script by Erik Müller and Tobias Schulze
## Edited: 2016/05/24 by Tobias Schulze
## note new dependence on obabel.exe
## LICENSE: GPL 3.0
## Copyright (c) 2014-2017


#Path to the record database
path <- "/var/www/html/MassBank/DB/annotation/"

# Source the script
source("/var/www/html/MassBank/script/GetInfo_and_InChIKeys_extended.R")

# Run the script and generate a csv
getInfoFixKey(path, "/var/www/html/MassBank/recordlist.csv","/usr/bin")

# Read csv, get prefixes of the accessions, and split the dataframe
statCsv <- read.csv("/var/www/html/MassBank/recordlist.csv")
statCsv$PREFIX <- gsub(statCsv$ACCESSION,pattern = "[0-9]*",replacement = "")
splitStats <- split.data.frame(statCsv,statCsv$RESEARCH_GROUP)


# The beginning of the HTML file
HTMLheader <- '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"https://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    <html xmlns="https://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="Content-Script-Type" content="text/javascript" />
    <meta http-equiv="Content-Style-Type" content="text/css" />
    <meta name="author" content="MassBank" />
    <meta name="coverage" content="worldwide" />
    <meta name="Targeted Geographic Area" content="worldwide" />
    <meta name="classification" content="general,computers,internet,miscellaneous" />
    <meta name="rating" content="general" />
    <meta name="description" content="MassBank is the first public repository of mass spectral data for sharing them among scientific research community. MassBank data are useful for the chemical identification and structure elucidation of chemical comounds detected by mass spectrometry." />
    <meta name="keywords" content="Statistics, MassBank, resolution, mass, spectral, database" />
    <link rel="stylesheet" href="./css/stats.css" type="text/css" media="all" />
    <title>massbank.eu | Statistics</title>
    </head>
    
    <body id="stats">

    <table>
    <tr>
    <th>Research Groups<br />(Contact Name)</th>
    <th class="prefix_en">Prefix of ID</th>
    <th class="instms_en">Analysis Equipment<br />(Analysis Method)</th>
    <th class="spectra_en">Number of Spectra</th>
    <th class="compound_en">Number of Compounds</th>
    </tr>'

# The end of the HTML file
HTMLend <- '</table>
</body>
</html>'

tableLines <- HTMLheader

for(i in 1:length(splitStats)){

    # Get the prefix
    prefix <- splitStats[[i]]$PREFIX[1]
    
    # Assign every prefix the instrument types contained and the number of spectra recorded with
    # that type
    types <- unique(splitStats[[i]]$INSTRUMENT_TYPE)
    typeSpectraNum <- sapply(types,function(type) length(which(splitStats[[i]]$INSTRUMENT_TYPE == type)))
    
    rowspanRGROUP <- length(types)

    firstCol   <- paste0('<td rowspan="', rowspanRGROUP, '">', i, '. ', splitStats[[i]]$RESEARCH_GROUP[1], '</td>')
    secondCol  <- paste0('<td rowspan="', rowspanRGROUP, '">', prefix, '</td>')
    lastCol    <- paste0('<td rowspan="', rowspanRGROUP, '">', length(unique(splitStats[[i]]$SMILES)), '</td>')
    
    typesIndex <- 1
    
    # Construct every row
    for(rownum in 1:length(types)){
        tableLines <- c(tableLines,"<tr>")
        if(rownum == 1){
            tableLines <- c(tableLines, firstCol, secondCol)
        }
        
        tableLines <- c(tableLines,paste0('<td class="text_right">',types[rownum],'</td>'),paste0('<td class="text_right">',typeSpectraNum[rownum],'</td>'))
        
        if(rownum == 1){
            tableLines <- c(tableLines,lastCol)
        }
        tableLines <- c(tableLines,"</tr>")
    }
}

tableLines <- c(tableLines,HTMLend)
fileConn <- file("/var/www/html/MassBank/Statistics.html","w")
writeLines(tableLines,fileConn)
close(fileConn)
