# MassBank Record Format 2.5
MassBank Consortium (March 05, 2020)

#### Updated
- **March 2020**: Add new tag for the inlet type.
- **October 2019**: Add UVPD dissociation method and some undocumented or new tags.
- **September 2018**: Add a new PROJECT tag, some undocumented tags used in RMassBank (COMMENT: CONFIDENCE, COMMENT: INTERNAL_ID, AC$MASS_SPECTROMETRY: FRAGMENTATION_MODE, AC$MASS_SPECTROMETRY: RESOLUTION, MS$DATA_PROCESSING: REANALYZE, MS$DATA_PROCESSING: RECALIBRATION) and cross references to HUPO-PSI
- **July 2017**: CH$CDK\_DEPICT added to render partially defined structures with CDK depict. AC$CHROMATOGRAPHY: NAPS\_RTI added to provide relative retention time information.
- **June 2017**: CH$LINK: COMPTOX added to link the CompTox Chemistry Dashboard
- **March 2016**: The default Creative Commons license of MassBank record is defined as CC BY. Two new tags are added, CH$LINK: INCHIKEY and PK$SPLASH. InChI key in CH$LINK: INCHIKEY is a hashed version of InChI code and used as an optional, common link based on chemical structures.  SPLASH in PK$SPLASH (Section 2.6.1) is a mandatory, hashed identifier of mass spectra.


## 1. Overview
Each MassBank Record has one-to-one relation to a specific mass spectrum. It is assumed that the sample of measurement of each mass spectrum is a single chemical substance. MassBank Record Information is classified into single line information and multiple line information, mandatory and optional, unique and iterative.

### 1.1 Syntax Rules
Single line information is either one of the followings:
* `tag : space value (; space value)`
* `tag : space subtag space value (; space value)`

Multiple line information:
* First line: `tag: space`
* Following lines: `space space value`

Last line of a MassBank Record is `//`.

### 1.2 Order of Information
MassBank Record Information in a MassBank Record is arranged in a fixed order (see Section 2).

### 1.3 Others
`[MS : space value ]` is the HUPO-PSI ID in [OLS](https://www.ebi.ac.uk/ols/index).


## Table 1.  MassBank Record Format (Summary)
<table>
  <tr>
    <th>Tag</th>
    <th>Mandatory/<br>Optional</th>
    <th>Unique/<br>Iterative</th>
    <th>Single line/<br>Multiple line</th>
    <th>Description</th>
    <th>Subsection<br>in manual</th>
  </tr>
  <tr>
    <td colspan="6"><b>Record Specific Information</b></td>
  </tr>
  <tr>
    <td>ACCESSION</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>Record identifier</td>
    <td><a href="#2.1.1">2.1.1</a></td>
  </tr>
  <tr>
    <td>RECORD_TITLE</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>Short title of the record</td>
    <td><a href="#2.1.2">2.1.2</a></td>
  </tr>
  <tr>
    <td>DATE</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>Date of creation or last modification of record</td>
    <td><a href="#2.1.3">2.1.3</a></td>
  </tr>
  <tr>
    <td>AUTHORS</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>Name and affiliation of authors</td>
    <td><a href="#2.1.4">2.1.4</a></td>
  </tr>
  <tr>
    <td>LICENSE</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>Creative Commons License or its compatible terms</td>
    <td><a href="#2.1.5">2.1.5</a></td>
  </tr>
  <tr>
    <td>COPYRIGHT</td>
    <td>O</td>
    <td>U</td>
    <td>S</td>
    <td>Copyright</td>
    <td><a href="#2.1.6">2.1.6</a></td>
  </tr>
  <tr>
    <td>PUBLICATION</td>
    <td>O</td>
    <td>U</td>
    <td>S</td>
    <td>Bibliographic information of reference</td>
    <td><a href="#2.1.7">2.1.7</a></td>
  </tr>
    <tr>
    <td>PROJECT</td>
    <td>O</td>
    <td>U</td>
    <td>S</td>
    <td>Information on a related project)</td>
    <td><a href="#2.1.8">2.1.8</a></td>
  </tr>
  <tr>
    <td>COMMENT</td>
    <td>O</td>
    <td>I</td>
    <td>S</td>
    <td>Comments</td>
    <td><a href="#2.1.9">2.1.9</a></td>
  </tr>
  <tr>
    <td colspan="6"><b>Information of Chemical Compound Analyzed</b></td>
  </tr>
  <tr>
    <td>CH$NAME</td>
    <td>M</td>
    <td>I</td>
    <td>S</td>
    <td>Chemical name</td>
    <td><a href="#2.2.1">2.2.1</a></td>
  </tr>
  <tr>
    <td>CH$COMPOUND_CLASS</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>Chemical category</td>
    <td><a href="#2.2.2">2.2.2</a></td>
  </tr>
  <tr>
    <td>CH$FORMULA</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>Chemical formula</td>
    <td><a href="#2.2.3">2.2.3</a></td>
  </tr>
  <tr>
    <td>CH$EXACT_MASS</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>Exact mass</td>
    <td><a href="#2.2.4">2.2.4</a></td>
  </tr>
  <tr>
    <td>CH$SMILES</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>SMILES code</td>
    <td><a href="#2.2.5">2.2.5</a></td>
  </tr>
  <tr>
    <td>CH$IUPAC</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>InChI code</td>
    <td><a href="#2.2.6">2.2.6</a></td>
  </tr>
  <tr>
    <td>CH$LINK subtag identifier</td>
    <td>O</td>
    <td>I</td>
    <td>S</td>
    <td>External database name with identifier</td>
    <td><a href="#2.2.8">2.2.8</a></td>
  </tr>
  <tr>
    <td colspan="6"><b>Information of Biological Sample</b></td>
  </tr>
  <tr>
    <td>SP$SCIENTIFIC_NAME</td>
    <td>O</td>
    <td>U</td>
    <td>S</td>
    <td>Scientific name of biological species</td>
    <td><a href="#2.3.1">2.3.1</a></td>
  </tr>
  <tr>
    <td>SP$LINEAGE</td>
    <td>O</td>
    <td>U</td>
    <td>S</td>
    <td>Lineage of species</td>
    <td><a href="#2.3.2">2.3.2</a></td>
  </tr>
  <tr>
    <td>SP$LINK subtag identifier</td>
    <td>O</td>
    <td>I</td>
    <td>S</td>
    <td>External database name with identifier</td>
    <td><a href="#2.3.3">2.3.3</a></td>
  </tr>
  <tr>
    <td>SP$SAMPLE</td>
    <td>O</td>
    <td>I</td>
    <td>S</td>
    <td>Information of sample preparation</td>
    <td><a href="#2.3.4">2.3.4</a></td>
  </tr>
  <tr>
    <td colspan="6"><b>Analytical Methods and Conditions</b></td>
  </tr>
  <tr>
    <td>AC$INSTRUMENT</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>Commercial name and manufacturer of instrument</td>
    <td><a href="#2.4.1">2.4.1</a></td>
  </tr>
  <tr>
    <td>AC$INSTRUMENT_TYPE</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>Type of instrument</td>
    <td><a href="#2.4.2">2.4.2</a></td>
  </tr>
  <tr>
    <td>AC$MASS_SPECTROMETRY: MS_TYPE</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>MSn type of data</td>
    <td><a href="#2.4.3">2.4.3</td>
  </tr>
  <tr>
    <td>AC$MASS_SPECTROMETRY: ION_MODE</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>Positive or negative mode of ion detection</td>
    <td><a href="#2.4.4">2.4.4</a></td>
  </tr>
  <tr>
    <td>AC$MASS_SPECTROMETRY: subtag</td>
    <td>O</td>
    <td>U</td>
    <td>S</td>
    <td>Analytical conditions of mass spectrometry</td>
    <td><a href="#2.4.5">2.4.5</a></td>
  </tr>
  <tr>
    <td>AC$CHROMATOGRAPHY: subtag</td>
    <td>O</td>
    <td>U/I</td>
    <td>S</td>
    <td>Analytical conditions of chromatographic separation</td>
    <td><a href="#2.4.6">2.4.6</a></td>
  </tr>
    <tr>
    <td>AC$GENERAL: subtag</td>
    <td>O</td>
    <td>U/I</td>
    <td>S</td>
    <td>General analytical conditions and information</td>
    <td><a href="#2.4.7">2.4.7</a></td>
  </tr>
  <tr>
    <td colspan="6"><b>Description of mass spectral data</b></td>
  </tr>
  <tr>
    <td>MS$FOCUSED_ION: subtag</td>
    <td>O</td>
    <td>U</td>
    <td>S</td>
    <td>Precursor ion and m/z</td>
    <td><a href="#2.5.1">2.5.1</a></td>
  </tr>
  <tr>
    <td>MS$DATA_PROCESSING: subtag</td>
    <td>O</td>
    <td>U</td>
    <td>S</td>
    <td>DATA processing method</td>
    <td><a href="#2.5.2">2.5.2</a></td>
  </tr>
  <tr>
    <td colspan="6"><b>Peak Information</b></td>
  </tr>
  <tr>
    <td>PK$SPLASH</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>Hashed identifier of mass spectra</td>
    <td><a href="#2.6.1">2.6.1</a></td>
  </tr>
  <tr>
    <td>PK$ANNOTATION</td>
    <td>O</td>
    <td>U</td>
    <td>M</td>
    <td>Chemical annotation of peaks by molecular formula</td>
    <td><a href="#2.6.2">2.6.2</a></td>
  </tr>
  <tr>
    <td>PK$NUM_PEAK</td>
    <td>M</td>
    <td>U</td>
    <td>S</td>
    <td>Total number of peaks</td>
    <td><a href="#2.6.3">2.6.3</a></td>
  </tr>
  <tr>
    <td>PK$PEAK</td>
    <td>M</td>
    <td>U</td>
    <td>M</td>
    <td>Peak(m/z, intensity and relative intensity</td>
    <td><a href="#2.6.4">2.6.4</a></td>
  </tr>
  <tr>
    <td colspan="6"><b>Supplementary Definitions</b></td>
  </tr>
  <tr>
    <td colspan="5">Description of isotope-labeled chemical compound</td>
    <td><a href="#2.7.1">2.7.1</a></td>
  </tr>
</table>

* General note. Decimal point should be a period, `.`, but not a comma, `,`.  For example, `m/z 425.7`.  No thousand separator is inserted.

## 2. MassBank Record Information
* Table 1 summarizes the current MassBank Record Information.
* MassBank Record Information consists of 6 groups (Table 2).
## Table 2.  Groups of MassBank Record Information.
| Information groups | Sections |
|--------------------|----------|
| Record Specific Information | 2.1 |
| Chemical Information (Tag starts with CH$) | 2.2 |
| Sample Information (Tag starts with SP$) | 2.3 |
| Analytical Chemistry Information (Tag starts with AC$) | 2.4 |
| Mass Spectral Data Information (Tag starts with MS$) | 2.5 |
| Mass Spectral Peak Data (Tag starts with PK$) | 2.6 |

* Information should be arranged by the order shown in Table 2.

### 2.1 Record Specific Information
#### <a name="2.1.1"></a>2.1.1 ACCESSION
Identifier of the MassBank Record. Mandatory

Example:
```
ACCESSION: ZMS00006
```

8-character fix-length string. Prefix two or three alphabetical capital characters specify the site, database or contributor, where the record was submitted or who has contributed. Prefixes currently used are listed in the “Prefix of ID” column of the MassBank "List of contributors, prefixes and projects" (https://github.com/MassBank/MassBank-data/blob/master/List_of_Contributors_Prefixes_and_Projects.md). Rest of the field are decimal letters which are the identifier of the record at each site.

A deprecated record is marked with the tag `DEPRECATED: ` followed by date and reason on the second line. The content of a deprecated record may not be valid.

Example:
```
ACCESSION: XY000010
DEPRECATED: 2019-05-03 considered noisy
```


#### <a name="2.1.2"></a>2.1.2 RECORD\_TITLE
Brief Description of MassBank Record. Mandatory

Example:
```
RECORD_TITLE: (-)-Nicotine; ESI-QQ; MS2; CE 40 V; [M+H]+
```

It consists of the values of `CH$NAME`; `AC$INSTRUMENT_TYPE`; `AC$MASS_SPECTROMETRY: MS_TYPE`.

#### <a name="2.1.3"></a>2.1.3 DATE
Date of the Creation or the Last Modification of MassBank Record. Mandatory

Example:
```
DATE: 2011.02.21 (Created 2007.07.07)
```

#### <a name="2.1.4"></a>2.1.4 AUTHORS
Authors and Affiliations of MassBank Record. Mandatory

Example:
```
AUTHORS: Akimoto N, Grad Sch Pharm Sci, Kyoto Univ and Maoka T, Res Inst Prod Dev.
```

If appropriate, it is suggested (but not mandatory) to add [MARC relator terms](https://locmirror.coffeecode.net/marc/relators/relaterm.html) after names to clarify the contributions of different authors. In particular, the following tags are suggested:
 * `[dtc]`: Data contributor, i.e. the person(s) who contribute the data, e.g. who acquired the raw data in-house, or who deposited data files that serve as the basis for the record in a public repository. This person or entity is not necessarily involved in the creation of the record.
 * `[com]`: Compiler, i.e. the person who created the record (but did not necessarily acquire the data or has any other scientific involvement).


Example:
```
AUTHORS: Earll M, EMBL-EBI [dtc]; Beisken S, EMBL-EBI [dtc]; Stravs MA, Eawag [com]
```

#### <a name="2.1.5"></a>2.1.5 LICENSE
Creative Commons License of Re-use of MassBank Record. Mandatory

Example:
```
LICENSE: CC BY
```

Contributors to MassBank are encouraged to show the license `CC BY`. This license mean that others are free to "share" (copy and redistribute the MassBank record in any medium or format) and to "adapt" (remix, transform, and build upon the MassBank record) for any purpose, even commercially. The contributors cannot revoke these freedoms as long as the others follow the license terms.

#### <a name="2.1.6"></a>2.1.6 COPYRIGHT
Copyright of MassBank Record. Optional

Example:
```
COPYRIGHT: Keio University
```

#### <a name="2.1.7"></a>2.1.7 PUBLICATION
Reference of the Mass Spectral Data. Optional

Example 1: 
```
PUBLICATION: Iida T, Tamura T, et al, J Lipid Res. 29, 165-71 (1988). [PMID: 3367086]
```

Example 2:
```
PUBLICATION: Schymanski EL, Jeon J, et al., Environ. Sci. Technol. 48, 2097-2098 (2014). [DOI: 10.1021/es5002105]
```

Citation with PubMed ID or DOI is recommended.

#### <a name="2.1.8"></a>2.1.8 PROJECT
A project tag of a project related to the record. Optional
Project tags currently used are listed in the “Project Tag” column of the MassBank [List of contributors, prefixes and projects](https://github.com/MassBank/MassBank-data/blob/master/List_of_Contributors_Prefixes_and_Projects.md).

Example:
```
PROJECT: NATOXAQ Natural Toxins and Drinking Water Quality - From Source to Tap
PROJECT: SOLUTIONS for present and future emerging pollutants in land and water resources management
```

#### <a name="2.1.9"></a>2.1.9 COMMENT
Comments. Optional and Iterative
 
In MassBank, COMMENT fields are often used to show the relations of the present record with other MassBank records and with data files. In these cases, the terms in brackets [ and ] are reserved for the comments specific to the following five examples.
Example 1:
```
COMMENT: This record is a MS3 spectrum. Link to the MS2 spectrum is added in the following comment field.
COMMENT: [MS2] KO008089
```
Example 2:
```
COMMENT: This record was generated by merging the following three MassBank records.
COMMENT: [Merging] KO006229 Tiglate; ESI-QTOF; MS2; CE:10 V [M-H]-.
COMMENT: [Merging] KO006230 Tiglate; ESI-QTOF; MS2; CE:20 V [M-H]-.
COMMENT: [Merging] KO006231 Tiglate; ESI-QTOF; MS2; CE:30 V [M-H]-.
```
Example 3:
```
COMMENT: This record was merged into a MassBank record, KOX00012, with other records.
COMMENT: [Merged] KOX00012
```
Example 4:
```
COMMENT: Analytical conditions of LC-MS were described in separate files.
COMMENT: [Mass spectrometry] ms1.txt
COMMENT: [Chromatography] lc1.txt.
```
Example 5:
```
COMMENT: Profile spectrum of this record is given as a JPEG file.
COMMENT: [Profile] CA000185.jpg
```

#### <a name="2.1.10"></a>2.1.10 COMMENT: subtag Description
Comment subtags. Optional and Iterative

##### 2.1.10 Subtag: CONFIDENCE
Description of a confidence level (e.g. Reference Standard or Standard Compound) and/or the confidence according to [Schymanski et al. 2014](https://dx.doi.org/10.1021/es5002105)

Example:
```
COMMENT: CONFIDENCE Reference Standard (Level 1)
```

##### 2.1.10 Subtag: INTERNAL_ID
Internal ID tag of the laboratory (e.g. compound number).

Example:
```
COMMENT: INTERNAL_ID 21
```

### 2.2 Information of Chemical Compound Analyzed

#### <a name="2.2.1"></a>2.2.1 CH$NAME
Name of the Chemical Compound Analyzed. Mandatory and Iterative

Example: 
```
CH$NAME: D-Tartaric acid
CH$NAME: (2S,3S)-Tartaric acid
```

No prosthetic molecule of adducts (HCl, H2SO3, H2O, etc), conjugate ions (Chloride, etc) , and protecting groups (TMS, etc.) is included. Synonyms could be added. If chemical compound is a stereoisomer, stereochemistry should be indicated.

#### <a name="2.2.2"></a>2.2.2 CH$COMPOUND\_CLASS
Category of Chemical Compound. Mandatory

Example:
```
CH$COMPOUND_CLASS: Natural Product; Carotenoid; Terpenoid; Lipid
```

Either Natural Product or Non-Natural Product should be precedes the other class names .

#### <a name="2.2.3"></a>2.2.3 CH$FORMULA
Molecular Formula of Chemical Compound. Mandatory

Example 1: 
```
CH$FORMULA: C9H10ClNO3
```

Example 2:
```
CH$FORMULA: [C5H14NO]+
```

It follows the Hill's System. No prosthetic molecule is included (see <a href="#2.2.1">2.2.1</a> `CH$NAME`). If possible the neutral forn should be given. Charged molecules are given in square brackets with charge behind. Molecular formulae of derivatives by chemical modification with TMS, etc. should be given in <a href="#2.5.1">2.5.1</a> `MS$FOCUSED_ION: DERIVATIVE_FORM`.

#### <a name="2.2.4"></a>2.2.4 CH$EXACT\_MASS
Monoisotopic Mass of Chemical Compound. Mandatory

Example:
```
CH$EXACT_MASS: 430.38108
```

A value with 5 digits after the decimal point is recommended.

#### <a name="2.2.5"></a>2.2.5 CH$SMILES
SMILES String. Mandatory

Example:
```
CH$SMILES: NCC(O)=O
```

Isomeric SMILES but not a canonical one.

#### <a name="2.2.6"></a>2.2.6 CH$IUPAC
IUPAC International Chemical Identifier (InChI Code). Mandatory

Example:
```
CH$IUPAC: InChI=1S/C2H5NO2/c3-1-2(4)5/h1,3H2,(H,4,5)
```

Not IUPAC name.

#### <a name="2.2.7"></a>2.2.7 CH$CDK\_DEPICT
Displays partially defined structures with CDK depict in record view.  In test phase, advanced users only. Optional and Iterative

Example 1:
```
CH$CDK_DEPICT_SMILES CCOCCOCCO |Sg:n:3,4,5:2:ht| PEG-2
```

Example 2:
```
CH$CDK_DEPICT_GENERIC_SMILES c1ccc(cc1)/C=C/C(=O)O[R]
```

Example 2:
```
CH$CDK_DEPICT_STRUCTURE_SMILES c1ccc(cc1)/C=C/C(=O)O
```

#### <a name="2.2.8"></a>2.2.8 CH$LINK: subtag identifier
Identifier and Link of Chemical Compound to External Databases. Optional and Iterative

Example:
```
CH$LINK: CAS 56-40-6
CH$LINK: COMPTOX DTXSID50274017
CH$LINK: INCHIKEY UFFBMTHBGFGIHF-UHFFFAOYSA-N
CH$LINK: KEGG C00037
CH$LINK: PUBCHEM SID: 11916 CID:182232
```
Currently MassBank records have links to the following external databases:
```
CAS
CAYMAN
CHEBI
CHEMBL
CHEMPDB
CHEMSPIDER
COMPTOX
HMDB
INCHIKEY
KAPPAVIEW
KEGG
KNAPSACK
LIPIDBANK
LIPIDMAPS
NIKKAJI
PUBCHEM
ZINC
```

CH$LINK fields should be arranged by the alphabetical order of database names.

### 2.3 Information of Biological Sample
#### <a name="2.3.1"></a>2.3.1 SP$SCIENTIFIC\_NAME
Scientific Name of biological species, from which the sample was prepared. Optional

Example:
```
SP$SCIENTIFIC_NAME: Mus musculus
```

#### <a name="2.3.2"></a>2.3.2 SP$LINEAGE
Evolutionary lineage of the species, from which the sample was prepared. Optional

Example:
```
SP$LINEAGE: cellular organisms; Eukaryota; Fungi/Metazoa group; Metazoa; Eumetazoa; Bilateria; Coelomata; Deuterostomia; Chordata; Craniata; Vertebrata; Gnathostomata; Teleostomi; Euteleostomi; Sarcopterygii; Tetrapoda; Amniota; Mammalia; Theria; Eutheria; Euarchontoglires; Glires; Rodentia; Sciurognathi; Muroidea; Muridae; Murinae; Mus
```

#### <a name="2.3.3"></a>2.3.3 SP$LINK subtag identifier
Identifier of Biological Species in External Databases. Optional and iterative

Example:
```
SP$LINK: NCBI-TAXONOMY 10090
```

`SP$LINK` fields should be arranged by the alphabetical order of database names.

#### <a name="2.3.4"></a>2.3.4 SP$SAMPLE
Tissue or Cell, from which Sample was Prepared. Optional and iterative

Example: 
```
SP$SAMPLE: Liver extracts
```

### 2.4 Analytical Method and Conditions
#### <a name="2.4.1"></a>2.4.1 AC$INSTRUMENT
Commercial Name and Model of Chromatographic Separation Instrument, if any were coupled, and Mass Spectrometer and Manufacturer. Mandatory

Example:
```
AC$INSTRUMENT: LC-10ADVPmicro HPLC, Shimadzu; LTQ Orbitrap, Thermo Electron.
```

Cross-reference to HUPO-PSI: Instrument model [MS:1000031]
All the instruments are given together in a single line. This record is not iterative.

#### <a name="2.4.2"></a>2.4.2 AC$INSTRUMENT\_TYPE
General Type of Instrument. Mandatory

Example:
```
AC$INSTRUMENT_TYPE: LC-ESI-QTOF
```

Format is `(Separation tool type-)Ionization method-Ion analyzer type(Ion analyzer type)`.

Separation tool types are:
```
CE
GC
LC
```

Ionization methods are:
```
APCI
APPI
EI
ESI
FAB
MALDI
FD
CI
FI
SIMS
```

Ion analyzer types are:
```
B
E
FT
IT
Q
TOF
```

In tandem mass analyzers, no `-` is inserted between ion analyzers.
`FT` includes `FTICR` and other type analyzers using `FT`, such as Orbitrap(R). `IT` comprises quadrupole ion trap analyzers such as 3D ion trap and linear ion trap. 

Other examples of `AC$INSTRUMENT_TYPE` data are:
```
ESI-QQ
ESI-QTOF
GC-EI-EB
LC-ESI-ITFT
```
Cross-reference to HUPO-PSI:
Ionization methods [MS:1000008]: APCI[MS:1000070], APPI[MS:1000382], EI[MS:1000389], ESI[MS:1000073], FAB[MS:1000074], MALDI[MS:1000075], FD[MS:1000257], CI[MS:1000071], FI[MS:1000258], SI[MS:1000402].

Ion analyzer types [MS:1000443]: B[MS:1000080], E[MS:1000254], IT[MS:1000264], Q[MS:1000081], TOF[MS:1000084]

#### <a name="2.4.3"></a>2.4.3 AC$MASS\_SPECTROMETRY: MS\_TYPE
Data Type. Mandatory

Example:
```
AC$MASS_SPECTROMETRY: MS_TYPE MS2
```

Other examples of `AC$MASS_SPECTROMETRY` data are as follows:
```
MS1
MS2
MS3
MS4
```

Brief definition of terms used in `MS_TYPE`:
* `MS2` is 1st generation product ion spectrum(of `MS`)
* `MS3` is 2nd generation product ion spectrum(of `MS`)
* `MS2` is the precursor ion spectrum of `MS3`*

Reference: [IUPAC Recommendations 2006](http://old.iupac.org/reports/provisional/abstract06/murray_prs.pdf)

#### <a name="2.4.4"></a>2.4.4 AC$MASS\_SPECTROMETRY: ION\_MODE
Polarity of Ion Detection. Mandatory

Example:
```
AC$MASS_SPECTROMETRY: ION_MODE POSITIVE
```

Either of POSITIVE or NEGATIVE is allowed. Cross-reference to HUPO-PSI: POSITIVE [MS:1000030] or NEGATIVE [MS:1000129]; Ion mode [MS:1000465]

#### <a name="2.4.5"></a>2.4.5 AC$MASS\_SPECTROMETRY: subtag Description
Other Optional Experimental Methods and Conditions of Mass Spectrometry.

Description is a list of numerical values with/without unit or a sentence.
`AC$MASS_SPECTROMETRY` fields should be arranged by the alphabetical order of subtag names.


##### 2.4.5 Subtag: CAPILLARY\_VOLTAGE
Voltage Applied to Capillary Electrophoresis or Voltage Applied to the Interface of LC-MS in `kV`.

Example:
```
AC$MASS_SPECTROMETRY: CAPILLARY_VOLTAGE 4 kV
```

##### 2.4.5 Subtag: COLLISION\_ENERGY
Collision Energy for Dissociation.

Example 1:
```
AC$MASS_SPECTROMETRY: COLLISION_ENERGY 20 kV
```

Example 2:
```
AC$MASS_SPECTROMETRY: COLLISION_ENERGY Ramp 10-50 kV
```

Example 3:
```
AC$MASS_SPECTROMETRY: COLLISION_ENERGY 10% (nominal)
```

##### 2.4.5 Subtag: COLLISION\_GAS
Name of Collision Gas.

Example:
```
AC$MASS_SPECTROMETRY: COLLISION_GAS N2
```

Cross-reference to HUPO-PSI: Collision gas [MS:1000419]

##### 2.4.5 Subtag: DATE
Date of Analysis.

##### 2.4.5 Subtag: DESOLVATION\_GAS\_FLOW
Flow Rate of Desolvation Gas.

Example:
```
AC$MASS_SPECTROMETRY: DESOLVATION_GAS_FLOW 600.0 L/h
```

##### 2.4.5 Subtag: DESOLVATION\_TEMPERATURE
Temperature of Desolvation Gas.

Example:
```
AC$MASS_SPECTROMETRY: DESOLVATION_TEMPERATURE 400 C
```

##### 2.4.5 Subtag: FRAGMENTATION\_MODE
Fragmentation method used for dissociation or fragmentation.

Example:
```
AC$MASS_SPECTROMETRY: FRAGMENTATION_MODE CID
```

Data of type `AC$FRAGMENTATION_MODE` are:

```
BIRD
CID
ECD
EDD
ETD
HCD
IRMPD
MPD
NETD
SID
UVPD
```

Cross-reference to HUPO-PSI: dissociation method [MS:1000044]

##### 2.4.5 Subtag: IONIZATION
The method by which gas phase ions are generated from the sample.

Example:
```
AC$MASS_SPECTROMETRY: IONIZATION ESI
```

Ionization methods are:

```
APCI
APPI
EI
ESI
FAB
MALDI
FD
CI
FI
SIMS
```

Cross-reference to HUPO-PSI: ionization type [MS:1000008]

##### 2.4.5 Subtag: IONIZATION\_ENERGY
Energy of Ionization (aka 	FRAGMENT_VOLTAGE, IONIZATION_POTENTIAL).

Example:

```
AC$MASS_SPECTROMETRY: IONIZATION_ENERGY 70 eV
```

##### 2.4.5 Subtag: LASER
Desorption and Ionization Conditions in MALDI.

Example:

```
AC$MASS_SPECTROMETRY: LASER 337 nm nitrogen laser, 20 Hz, 10 nsec
```

##### 2.4.5 Subtag: MATRIX
Matrix Used in MALDI and FAB.

Example:

```
AC$MASS_SPECTROMETRY: MATRIX 1-2 uL m-NBA
```

##### 2.4.5. Subtag : MASS\_ACCURACY
Relative Mass Accuracy.

Example:
```
AC$MASS_SPECTROMETRY: MASS_ACCURACY 50 ppm over a range of about m/z 100-1000
```

##### 2.4.5 Subtag: MASS\_RANGE\_MZ`
Mass Range of the Scan (aka Scanning Range or Scan Range) in m/z.

Example:
```
AC$MASS_SPECTROMETRY: MASS_RANGE_MZ 100-1000
```

##### 2.4.5 Subtag: REAGENT\_GAS
Name of Reagent Gas.

Example:
```
AC$MASS_SPECTROMETRY: REAGENT_GAS ammonia
```

##### 2.4.5 Subtag: RESOLUTION
Resolution (aka Mass Resolution or Resolving Power) is the smallest mass difference between two equal magnitude peaks so that the valley between them is a specified fraction of the peak height.

Example:
```
AC$MASS_SPECTROMETRY: RESOLUTION 15000
```
Cross-reference to HUPO-PSI: mass resolution [MS:1000011]

##### 2.4.5 Subtag: SCANNING\_SETTING
Setting of the Scan Event without Range which should be given in <a href="#2.4.5">2.4.5</a> `AC$MASS_SPECTROMETRY: MASS_RANGE_M/Z`.

Example 1:
```
AC$MASS_SPECTROMETRY: SCANNING_SETTING 1 sec/scan
```

Example 2:
```
AC$MASS_SPECTROMETRY: SCANNING_SETTING 1 amu/sec
```

Example 3:
```
AC$MASS_SPECTROMETRY: SCANNING_SETTING 1 cycle/sec
```

##### 2.4.5 Subtag: SOURCE\_TEMPERATURE
Temperature of the Ion Source in GC-MS and LC-MS (aka Ion Source Temperature), for Example of the EI Source or the Heated ESI Source.

Example:

```
AC$MASS_SPECTROMETRY: SOURCE_TEMPERATURE 280 C
```

##### Undocumented Subtags
`ACTIVATION_PARAMETER`
`ACTIVATION_TIME`
`ATOM_GUN_CURRENT`
`AUTOMATIC_GAIN_CONTROL`
`BOMBARDMENT`
`CAPILLARY_TEMPERATURE`
`CAPILLARY_VOLTAGE`
`CDL_SIDE_OCTOPOLES_BIAS_VOLTAGE`
`CDL_TEMPERATURE`
`DATAFORMAT`
`DRY_GAS_FLOW`
`DRY_GAS_TEMP`
`GAS_PRESSURE`
`HELIUM_FLOW`
`INTERFACE_VOLTAGE`
`ION_GUIDE_PEAK_VOLTAGE`
`ION_GUIDE_VOLTAGE`
`ION_SPRAY_VOLTAGE`
`IT_SIDE_OCTOPOLES_BIAS_VOLTAGE`
`LENS_VOLTAGE`
`NEBULIZER`
`NEBULIZING_GAS` 
`NEEDLE_VOLTAGE`
`OCTPOLE_VOLTAGE`
`ORIFICE_TEMP`
`ORIFICE_TEMPERATURE`
`ORIFICE_VOLTAGE`
`PROBE_TIP`
`RING_VOLTAGE`
`SAMPLE_DRIPPING`
`SKIMMER_VOLTAGE`
`SPRAY_VOLTAGE`
`TUBE_LENS_VOLTAGE`

#### <a name="2.4.6"></a>2.4.6 AC$CHROMATOGRAPHY: subtag Description
Experimental Methods and Conditions of Chromatographic Separation. Optional

AC$CHROMATOGRAPHY fields should be arranged by the alphabetical order of subtag names.

##### 2.4.6 Subtag: CARRIER\_GAS
Carrier Gas Used for GC-MS.

Example 1:

```
AC$CHROMATOGRAPHY: CARRIER_GAS Helium
```

Example 2:
```
AC$CHROMATOGRAPHY: CARRIER_GAS Nitrogen
```

Example 3:
```
AC$CHROMATOGRAPHY: CARRIER_GAS Hydrogen
```

##### 2.4.6 Subtag: COLUMN\_NAME
Commercial Name of Chromatography Column and Manufacture.

Example of LC Column Name:
```
AC$CHROMATOGRAPHY: COLUMN_NAME Acquity UPLC BEH C18 2.1 by 50 mm (Waters, Milford, MA, USA)
```

Example of GC Column Name: 
```
AC$CHROMATOGRAPHY: COLUMN_NAME Fused silica capillary id=50 um L=100 cm (HMT, Tsuruoka, Japan)
```

##### 2.4.6 Subtag: COLUMN\_TEMPERATURE
Static Column Temperature in GC-MS and LC-MS.

Example:
```
AC$CHROMATOGRAPHY: COLUMN_TEMPERATURE 40 C
```

##### 2.4.6 Subtag: COLUMN\_TEMPERATURE\_GRADIENT
Dynamic Column Temperature Gradient (aka Oven Temperature) in GC-MS and LC-MS.

Example:
```
AC$CHROMATOGRAPHY: COLUMN_TEMPERATURE_GRADIENT 55 C at 0 min, 55 C at 3 min, 180 C at 11.33 min, 280 C at 26.72 min, 280 C at 31.72 min, 300 C at 33.72 min, 300 C at 39 min
```

##### 2.4.6 Subtag: FLOW\_GRADIENT
Gradient of Mobile Phases in LC-MS.

Example:
```
AC$CHROMATOGRAPHY: FLOW_GRADIENT 0/100 at 0 min, 15/85 at 5 min, 21/79 at 20 min, 90/10 at 24 min, 95/5 at 26 min, 0/100, 30 min
```

##### 2.4.6 Subtag: FLOW\_RATE
Flow Rate of Migration Phase in GC-MS and LC-MS.

Example:

```
AC$CHROMATOGRAPHY: FLOW_RATE 0.25 mL/min
```

##### 2.4.6 Subtag: INJECTION\_TEMPERATURE
Temperature of the Injection Port in GC-MS.

Example:

```
AC$CHROMATOGRAPHY: INJECTION_TEMPERATURE 250 C
```

##### 2.4.6 Subtag: INJECTION\_TEMPERATURE_GRADIENT
Temperature of the Injection Port in GC-MS in Case of Cold Injection, Thermodesorption or other Trapping Systems.

Example:

```
AC$CHROMATOGRAPHY: INJECTION_TEMPERATURE_GRADIENT 10 C at 0 sec, 250 C at 3 sec with 80 C/sec
```

##### 2.4.6 Subtag: INLET\_TYPE
Type of the Injection or of the Injection Port

Example:
```
AC$CHROMATOGRAPHY: INLET_TYPE flow injection analysis
```

Data of type `AC$CHROMATOGRAPHY: INLET_TYPE` are:

```
atmospheric pressure inlet
capillary flow technology
cold injection inlet
cold on column inlet
direct inlet
direct insertion probe
direct liquid introduction
electrospray inlet
flow injection analysis
infusion
nanospray inlet
split inlet
splitless inlet
```

Cross-reference to HUPO-PSI: inlet type [MS:1000007]


##### 2.4.6 Subtag: KOVATS\_RTI
C8-C30 n-Alkanes Based Retention Time Index for GC-MS.

Example:
```
AC$CHROMATOGRAPHY: KOVATS_RTI 2000
```

Reference: E. Kovats, Adv. Chromatogr. 1 (1965) 229
Reference: E. Kovats, Helv. Chim. Acta 41 (1958) 1915
Reference: [Rostad et al. 1986](https://doi.org/10.1002/jhrc.1240090603)

##### 2.4.6 Subtag: LEE\_RTI
Polycyclic Aromatic Hydrocarbons Based Retention Time Index for GC-MS.

Example:
```
AC$CHROMATOGRAPHY: LEE_RTI 200
```

Reference: [Rostad et al. 1986](https://doi.org/10.1002/jhrc.1240090603)


##### 2.4.6 Subtag: NAPS\_RTI
N-alkylpyrinium-3-sulfonate Based Retention Time Index for LC-MS.

Example:
```
AC$CHROMATOGRAPHY: NAPS_RTI 100
```

Reference: [Quilliam et al. 2015](https://nrc-publications.canada.ca/eng/view/fulltext/?id=b4db3589-ae0b-497e-af03-264785d7922f)

##### 2.4.6 Subtag: UOA\_RTI
Experimental Retention Time Index (Range 1-1000) for LC-MS based on the QSRR Approach of University of Athens, Trace Analysis and Mass Spectrometry Group.

Example:
```
AC$CHROMATOGRAPHY: UOA_RTI 50
```

Reference: [Aalizadeh et al. 2019](https://doi.org/10.1016/j.jhazmat.2018.09.047)

##### 2.4.6 Subtag: UOA\_PREDICTED\_RTI
Predicted Retention Time Index (Range 1-1000) for LC-MS based on the QSRR Approach of University of Athens, Trace Analysis and Mass Spectrometry Group.

Example:
```
AC$CHROMATOGRAPHY: UOA_PREDICTED_RTI 50
```

Reference: [Aalizadeh et al. 2019](https://doi.org/10.1016/j.jhazmat.2018.09.047)


##### 2.4.6 Subtag: RETENTION\_TIME
Experimental Retention Time based on Chromatography.

Example:
```
AC$CHROMATOGRAPHY: RETENTION_TIME 40.3 min
```

Cross-reference to HUPO-PSI: Retention time [MS:1000016]


##### 2.4.6 Subtag: UOA\_PREDICTED\_RETENTION\_TIME
Predicted Retention Time for LC-MS based on the QSRR Approach of University of Athens, Trace Analysis and Mass Spectrometry Group.

Example:
```
AC$CHROMATOGRAPHY: TRAMS_PREDICTED_RETENTION_TIME 40.3 min
```

Reference: [Aalizadeh et al. 2019](https://doi.org/10.1016/j.jhazmat.2018.09.047)

##### 2.4.6 Subtag: SOLVENT
Chemical Composition of Buffer Solution.  Iterative

Example: 

```
AC$CHROMATOGRAPHY: SOLVENT A acetonitrile-methanol-water (19:19:2) with 0.1% acetic acid
AC$CHROMATOGRAPHY: SOLVENT B 2-propanol with 0.1% acetic acid and 0.1% ammonium hydroxide (28%)
```

##### 2.4.6 Subtag: TRANSFERLINE\_TEMPERATURE
Temperature of the Transferline between GC and MS instrument.

Example:
```
AC$CHROMATOGRAPHY: TRANSFERLINE_TEMPERATURE 200 C
```

#### <a name="2.4.7"></a>2.4.7 AC$GENERAL: subtag Description
Experimental Methods and Conditions which are not included in other sections. Optional

AC$GENERAL fields should be arranged by the alphabetical order of subtag names.

##### 2.4.7 Subtag: CONCENTRATION
Concentration of Analytical Standard Used for the Generation of MassBank Records. The mandatory concentration unit is ug/L.

Example:
```
AC$GENERAL: CONCENTRATION 1 ug/L
```


##### Undocumented Subtags
`ANALYTICAL_TIME`
`COLUMN_PRESSURE`
`INTERNAL_STANDARD`
`INTERNAL_STANDARD_MT`
`MIGRATION_TIME`
`PRECONDITIONING`
`RUNNING_BUFFER`
`RUNNING_VOLTAGE`
`SAMPLE_INJECTION`
`SAMPLING_CONE`
`SHEATH_LIQUID`
`TIME_PROGRAM`
`WASHING_BUFFER`


### 2.5 Description of Mass Spectral Data
#### <a name="2.5.1"></a>2.5.1 MS$FOCUSED\_ION: subtag Description
Information of Precursor or Molecular Ion. Optional

##### 2.5.1 Subtag: BASE\_PEAK
m/z of Base Peak.

Example:
```
MS$FOCUSED_ION: BASE_PEAK 73
```

Cross-reference to HUPO-PSI: precursor m/z [MS:1000504]

##### 2.5.1 Subtag: DERIVATIVE\_FORM
Molecular Formula of Derivative for GC-MS.

Example 1:

```
MS$FOCUSED_ION: DERIVATIVE_FORM C19H42O5Si4
```

Example 2:
```
MS$FOCUSED_ION: DERIVATIVE_FORM C{9+3*n}H{16+8*n}NO5Si{n}
```

##### 2.5.1 Subtag: DERIVATIVE\_MASS
Exact Mass of Derivative for GC-MS.

Example: 

```
MS$FOCUSED_ION: DERIVATIVE_MASS 462.21093
```

##### 2.5.1 Subtag: DERIVATIVE\_TYPE
Type of Derivative for GC-MS.

Example: 

```
MS$FOCUSED_ION: DERIVATIVE_TYPE 4 TMS
```

##### 2.5.1 Subtag: ION\_TYPE
Type of Focused Ion.

Example: 

```
MS$FOCUSED_ION: ION_TYPE [M+H]+
```

Types currently used in MassBank are:
```
[M]+
[M]+*
[M+H]+
[2M+H]+
[M+Na]+
[M-H+Na]+
[2M+Na]+
[M+2Na-H]+
[(M+NH3)+H]+
[M+H-H2O]+
[M+H-C6H10O4]+
[M+H-C6H10O5]+
[M]-
[M-H]-
[M-2H]-
[M-2H+H2O]-
[M-H+OH]-
[2M-H]-
[M+HCOO-]-
[(M+CH3COOH)-H]-
[2M-H-CO2]-
[2M-H-C6H10O5]-
```

##### 2.5.1 Subtag: PRECURSOR\_INT
Intensity of Focused Ion.

Example: 

```
MS$FOCUSED_ION: PRECURSOR_INT 10000
```

##### 2.5.1 Subtag: PRECURSOR\_MZ
m/z of Precursor Ion in MSn spectrum.

Example:
```
MS$FOCUSED_ION: PRECURSOR_MZ 289.07123
```

Calculated exact mass is preferred to the measured accurate mass of the precursor ion.

##### 2.5.1 Subtag: PRECURSOR\_TYPE
Type of Precursor Ion in MSn spectrum.

Example for MS2:
```
MS$FOCUSED_ION: PRECURSOR_TYPE [M-H]-
```
Example for MS3:
```
MS$FOCUSED_ION: PRECURSOR_TYPE [M+CH3COO]-/[M-CH3]-
```

The syntax is `[<n>M<+-><molecular formula>]<charge>`

Example: 
```
[M]+
[M+Na]+
[2M-H]-
[2M-H-CO2]-
```

See see <a href="#2.5.1">2.5.1</a> `mS$DATA_ION_TYPE ` for a full list.

Cross-reference to HUPO-PSI: isolation window attribute [MS: 1000792]

##### Undocumented Subtags
`FULL_SCAN_FRAGMENT_ION_PEAK`


#### <a name="2.5.2"></a>2.5.2 MS$DATA\_PROCESSING: subtag
Data Processing Method of Peak Detection. Optional

`MS$DATA_PROCESSING` fields should be arranged by the alphabetical order of subtag names. Cross-reference to HUPO-PSI: Data processing [MS:1000543]


##### 2.5.2 Subtag: COMMENT
Addtional Comments on Data Processing.

Example:
```
MS$DATA_PROCESSING: COMMENT Relative m/z normalised by peak(m/z=68.9396). set 999 for peak(m/z=568.9187,rel=1046).
```

##### 2.5.2 Subtag: DEPROFILE
Method for the Centroiding of Profile Data.

Example:
```
MS$DATA_PROCESSING: DEPROFILE Proteowizard 3.0.19022
```

##### 2.5.2 Subtag: FIND\_PEAK
Peak Detection.

Example:
```
MS$DATA_PROCESSING: FIND_PEAK Convexity search; threshold = 9.1
```

##### 2.5.2 Subtag: REANALYZE
Data processing to include reanalyzed peaks (e.g. in RMassBank).

Example:
```
MS$DATA_PROCESSING: REANALYZE Peaks with additional N2/O included
```

##### 2.5.2 Subtag: RECALIBRATE
Data processing to recalibrate mass accuracy (e.g. in RMassBank). 

Example:
```
MS$DATA_PROCESSING: RECALIBRATE loess on assigned fragments and MS1
```

##### 2.5.2 Subtag: WHOLE
Whole Process in Single Method / Software.

Example:
```
MS$DATA_PROCESSING: WHOLE Analyst 1.4.2
```

### 2.6 Information of Mass Spectral Peaks
#### <a name="2.6.1"></a>2.6.1 PK$SPLASH
Hashed Identifier of Mass Spectra. Mandatory and Single Line Information

Example:
```
PK$SPLASH: splash10-z200000000-87bb3c76b8e5f33dd07f
```

#### <a name="2.6.2"></a>2.6.2 PK$ANNOTATION
Chemical Annotation of Peaks with Molecular Formula. Optional and Multiple Line Information

Line 1 defines the record format of the annotation blocks. Contributors freely define the record format by using appropriate terms.
Line 2 or later：sequence of multiple line annotation blocks.
The first line of each annotation block should be indented by `space space`. The second or later line in each annotation block should be indented by `space space space space`.

Example 1: 

```
PK$ANNOTATION: m/z annotation exact_mass error(ppm) formula 
  794.76 [PC(18:0,20:4)-CH3]- 794.56998 239 C45H81NO8P
```

Example 2: 

```
PK$ANNOTATION: m/z {annotation formula exact_mass error(ppm)}
  494.34 [lyso PC(alkyl-18:0,-)]- C25H53NO6P 494.36105 -42
```
Example 3:
```
PK$ANNOTATION: m/z formula annotation exact_mass error(ppm) 
  167.08947 C9H12O2N [M+1]+(13C) 167.08961 0.81
  168.08681 C9H12O2N [M+1]+(13C, 15N) 168.08664 1.04
```
Example 4:
RMassBank provides the option for automated tentative annotation of the fragment peaks including in-source adduct fragments (N2/O)
```
PK$ANNOTATION: m/z tentative_formula formula_count mass error(ppm)
  94.0656 C6H8N+ 1 94.0651 4.89
  105.0702 C8H9+ 1 105.0699 3.36
  106.0652 C7H8N+ 1 106.0651 0.52
  107.0733 C7H9N+ 1 107.073 2.92
  122.0968 C8H12N+ 1 122.0964 3
  133.0766 C8H9N2+ 1 133.076 4.34
```
The automated annotation by RMassBank is currently tentative.

See Section 2.7.2 about more details of Example 3. 

#### <a name="2.6.3"></a>2.6.3 PK$NUM\_PEAK
Total Number of Peaks in PK$PEAK (2.6.4). Mandatory

Example:
```
PK$NUM_PEAK: 86
```

#### <a name="2.6.4"></a>2.6.4 PK$PEAK
Peak Data.  Mandatory and Multiple line Information

Example:
```
PK$PEAK: m/z int. rel.int.
  326.65 5.3 5
  328.28 7.6 7
```
Line 1:  fixed string which denotes the format of Line 2 or later.
`PK$PEAK: m/z int. rel.int.`

Line 2 or later: `space` `space` `MZ` `space` `INT` `space` `REL`
- MZ: m/z of the peak.
- INT: intensity of the peak.
- REL: an integer from 1 to 999 which denotes relative intensity of the peak.

Peaks are arranged in the ascending order of m/z.

### 2.7 Supplementary Definitions
#### 2.7.1 Description of Isotope-Labeled Compounds
*This section will be updated in near future as molfiles are deprecated*

This section defines the chemical information of isotope-labeled chemical compounds.
CH$NAME is Chemical Name followed by ”–[(Labeled Positions-)Isotopic Atom Name with the Number of Isotopic Atoms]”.

Examples:
```
CH$NAME: Glycine-[2-13C, 15N]
CH$NAME: L-Aspartic acid-[2-15N][3,3-d2]
CH$NAME: Benzene-[d6]
```

MOLFILE depends on whether the labeled position is specified. If the labeled position is specified, molfile defines the isotopic atom name and the labeled position. Otherwise molfile should be the same to that of the non-labeled chemical compound.
CH$FORMULA should be the same to that of the non-labeled chemical compound.
CH$EXACT_MASS is the monoisotopic mass, but not the sum of the mass of the isotopes. Thus CH$EXACT_MASS should be equal to that of the non-labeled chemical compound.
CH$SMILES is the same to that of the non-labeled chemical compound.
CH$IUPAC, which is InChI code, should define the isotope name and the labeled positions if these two are specified. If not, InChI code is the same to that of the non-labeled chemical compound.
MS$FOCUSED_ION: PRECURSOR_M/Z should be the value that was actually used in the mass spectrometry. 
MS$FOCUSED_ION: PRECURSOR_TYPE should be the same to that of non-labeled chemical compound.

Example:
```
MS$FOCUSED_ION: PRECURSOR_TYPE [M+H]+
```

Record Editor correctly generates CH$FORMULA, CH$EXACT_MASS, CH$SMILES, and CH$IUPAC from the molfile of the isotope-labeled chemical compound.

#### 2.7.2 PK$ANNOTATION of Natural Abundant Isotopic Peaks

This section describes the annotation of natural abundant isotopic peaks. Optional and Multiple Line Information.

Example:
```
PK$ANNOTATION: m/z formula annotation exact_mass error(ppm) 
  167.08947 C9H12O2N [M+1]+(13C) 167.08961 0.81
  168.08681 C9H12O2N [M+1]+(13C, 15N) 168.08664 1.04
```

Line 1 defines the record format of Line 2 or later lines. 
The first line of each annotation block should be indented by space space.
