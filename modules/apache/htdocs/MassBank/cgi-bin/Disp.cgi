#! /usr/bin/perl
#-------------------------------------------------------------------------------
#
# Copyright (C) 2008 JST-BIRD MassBank
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#
#===============================================================================
#
# レコードページ表示
#
# ver 3.0.30  2012.11.22
#
#-------------------------------------------------------------------------------
%FMT = (
'LICENSE:',                       'http://creativecommons.org/licenses/',
'PUBLICATION:',                   'http://www.ncbi.nlm.nih.gov/pubmed/%s?dopt=Citation',
'COMMENT: \[MSn\]',               'Dispatcher.jsp?type=disp&id=%s&site=%s',  # version 2
'COMMENT: \[Merging\]',           'Dispatcher.jsp?type=disp&id=%s&site=%s',  # version 2
'COMMENT: \[Merged\]',            'Dispatcher.jsp?type=disp&id=%s&site=%s',  # version 2
'COMMENT: \[Mass spectrometry\]', '',                                        # version 2
'COMMENT: \[Chromatography\]',    '',                                        # version 2
'COMMENT: \[Profile\]',           '../DB/profile/%s/%s',                     # version 2
'COMMENT: \[Mixture\]',           'Dispatcher.jsp?type=disp&id=%s&site=%s',  # version 2
'CH\$LINK: CAS',                  'http://webbook.nist.gov/cgi/cbook.cgi?ID=%s',
'CH\$LINK: CAYMAN',               'http://www.caymanchem.com/app/template/Product.vm/catalog/%s',
'CH\$LINK: CHEBI',                'http://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:%s',
'CH\$LINK: CHEMPDB',              'http://www.ebi.ac.uk/msd-srv/chempdb/cgi-bin/cgi.pl?FUNCTION=getByCode&amp;CODE=%s',
'CH\$LINK: CHEMSPIDER',           'http://www.chemspider.com/%s',
'CH\$LINK: FLAVONOIDVIEWER',      'http://www.metabolome.jp/software/FlavonoidViewer/',
'CH\$LINK: HMDB',                 'http://www.hmdb.ca/metabolites/%s',
'CH\$LINK: KAPPAVIEW',            'http://kpv.kazusa.or.jp/kpv4/compoundInformation/view.action?id=%s',
'CH\$LINK: KEGG',                 'http://www.genome.jp/dbget-bin/www_bget?%s:%s',
'CH\$LINK: KNAPSACK',             'http://kanaya.naist.jp/knapsack_jsp/info.jsp?sname=C_ID&word=%s',
'CH\$LINK: LIPIDBANK',            'http://lipidbank.jp/cgi-bin/detail.cgi?id=%s',
'CH\$LINK: LIPIDMAPS',            'http://www.lipidmaps.org/data/get_lm_lipids_dbgif.php?LM_ID=%s',
'CH\$LINK: NIKKAJI',              'http://nikkajiweb.jst.go.jp/nikkaji_web/pages/top.jsp?SN=%s&CONTENT=syosai',
'CH\$LINK: PUBCHEM',              'http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?',
'CH\$LINK: OligosaccharideDataBase', 'http://www.fukuyama-u.ac.jp/life/bio/biochem/%s.html%s',
'CH\$LINK: OligosaccharideDataBase2D', 'http://www.fukuyama-u.ac.jp/life/bio/biochem/%s.html',
'SP\$LINK: NCBI-TAXONOMY', 'http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=%s',
'MS\$RELATED_MS: PREVIOUS_SPECTRUM', 'Dispatcher.jsp?type=disp&id=%s'
);

use CGI;
use DBI;

print "Content-Type: text/html; charset=utf-8\n\n";

$query = new CGI;
@params = $query->param();
foreach my $key ( @params ) {
	$val = $query->param($key);
	$Arg{$key} = $val;
	if ( $key eq 'type' ) {
		$type = $val;
	}
	elsif ( $key eq 'dsn' ) {
		$db_name = $val;
	}
}

# get my server url
my $url = $query->url;
my $myServer = substr($url, 0, index($url, "/cgi-bin"));
open(F, "../massbank.conf");
while ( <F> ) {
	if ( index($_, "FrontServer") != -1 ) {
		$myServer = $_;
		$myServer =~ s/.*"(.*)".*/$1/g;
		$myServer =~ s/\r?\n?//g;
		if ( $myServer !~ m|/$|o ) {
			$myServer .= "/";
		}
		last;
	} 
}
close(F);

$id = $Arg{'id'};
$src = $Arg{'src'};
$mz_num = $Arg{'num'};
if ( $src eq '' ) {
	$src = 0;
}
$qmz = $Arg{'qmz'};
$cutoff = $Arg{'CUTOFF'};
$nloss = $Arg{'nloss'};
$product = $Arg{'product'};
$mode = $Arg{'mode'};

$DB = "../DB/annotation/$db_name";
$file_path = "$DB/$id.txt";
unless ( -f $file_path ) {
	print "Content-Type: text/html; charset=utf-8\n\n";
	exit(0);
}
open(F, $file_path);

my $acc = "";
my $name = "";
my $version = 1;
while ( <F> ) {
	s/\r?\n?//g;
	push(@Line, $_);
	if ( /^ACCESSION: / ) { s/^ACCESSION: //; $acc = $_; }
	elsif ( /^RECORD_TITLE: / ) { s/^RECORD_TITLE: //; $name = $_; }
	elsif ( /^LICENSE: / ) { $version = 2; }
}

if ( $db_name eq '' ) {
	$db_name = "MassBank";
}
open(F, "DB_HOST_NAME");
while ( <F> ) {
	s/\r?\n?//g;
	$host_name .= $_;
}

$SQLDB = "DBI:mysql:$db_name:$host_name";
$User = 'bird';
$PassWord = 'bird2006';
$dbh  = DBI->connect($SQLDB, $User, $PassWord) || &myexit;
@ans = &MySql("select PRECURSOR_MZ from SPECTRUM where ID = '$acc'");
$precursor = $ans[0][0];
$short_name = substr( $name, 0, index($name, ';') );
$str_merge = "";
if ( index($name, "MERGED") != -1 ) {
	$str_merge = "<span style=\"background-color:Blue;color:white;font-size:12pt;font-family:Arial;\">&nbsp;MERGED&nbsp;SPECTRUM&nbsp;</span>";
}

print << "HTML";
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="en">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<meta name="author" content="MassBank" />
		<meta name="coverage" content="worldwide" />
		<meta name="Targeted Geographic Area" content="worldwide" />
		<meta name="rating" content="general" />
		<meta name="copyright" content="Copyright (c) 2006 MassBank Project" />
		<meta name="description" content="MassBank Record of $acc">
		<meta name="keywords" content="$short_name">
		<meta name="revisit_after" content="30 days">
		<meta http-equiv="Content-Style-Type" content="text/css">
		<meta http-equiv="Content-Script-Type" content="text/javascript">
		<link rel="stylesheet" type="text/css" href="../css/Common.css">
		<script type="text/javascript" src="../script/Common.js"></script>
		<title>$short_name Mass Spectrum</title>
	</head>
	<body style="font-family:Times;">
		<table border="0" cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<td>
					<h1>MassBank Record: $acc $str_merge</h1>
				</td>
			</tr>
		</table>
		<iframe src="../menu.html" width="860" height="30px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
		<hr size="1">
		<br>
		<font size="+1" style=\"background-color:LightCyan\">&nbsp;$name&nbsp;</font>
		<hr size="1">
HTML

$height = 200;
$param = "";
if ( $mz_num ne '' ) {
	#------------------------
	# Peak Difference Search
	#------------------------
	if ( $type eq 'dispdiff' ) {
		$max_cnt = 0;
		$hit = 0;
		$param = "\t\t\t<param name=\"type\" value=\"diff\">\n";
		for ( $i = 0; $i < $mz_num; $i++ ) {
			$mz = $Arg{"mz$i"} + 0;
			last if ( $mz eq 0 );
			$tol = $Arg{"tol$i"} + 0;
			$tol = - $tol if ( $tol < 0 );
			$min = $mz - $tol - 0.00001;
			$max = $mz + $tol + 0.00001;
			$val = $Arg{"int$i"} + 0;
			$sql = "select t2.MZ, t1.MZ from PEAK as t1 left join PEAK as t2 on t1.ID = t2.ID "
				 . "where t1.ID = '$id' and (t1.MZ between t2.MZ + $min and t2.MZ + $max) "
				 . "and t1.RELATIVE > $val and t2.RELATIVE > $val";
			@ans = &MySql($sql);
			$rec_num = @ans;
			next if ( $rec_num == 0 );
			$hit++;
			$out_mz = "";
			$cnt = 0;
			$mz1_prev = 0;
			$mz2_prev = 0;
			for $item ( @ans ) {
				$mz1 = $$item[0];
				$mz2 = $$item[1];
				if ( $mz1 >= $mz1_prev + 1 && $mz2 >= $mz2_prev + 1 ) {
					$cnt++;
				}
				$out_mz .= "$mz1,$mz2@";
				$mz1_prev = $mz1;
				$mz2_prev = $mz2;
			}
			$max_cnt = $cnt if ( $cnt > $max_cnt );
			$pnum = $i+1;
			$param .= "\t\t\t<param name=\"diff$pnum\" value=\"$mz\">\n";
			$param .= "\t\t\t<param name=\"mz$pnum\" value=\"$out_mz\">\n";
		}
		$param .= "\t\t\t<param name=\"margin\" value=\"$max_cnt\">\n";
		$height += 12 * $max_cnt;
		$mz_num = $hit;
	}
	#------------------------
	# Peak Search
	#------------------------
	else {
		$param = "\t\t\t<param name=\"type\" value=\"peak\">\n";
		$where = " and (";
		for ( $i = 0; $i < $mz_num; $i++ ) {
			$mz = $Arg{"mz$i"} + 0;
			last if ( $mz eq 0 );
			$tol = $Arg{"tol$i"} + 0;
			$tol = - $tol if ( $tol < 0 );
			$min = $mz - $tol - 0.00001;
			$max = $mz + $tol + 0.00001;
			$val = $Arg{"int$i"} + 0;
			$where .= "(MZ between $min and $max and RELATIVE > $val)";
			if ( $i < $mz_num - 1 ) {
				$where .= " or ";
			}
		}
		$where .= ")";
		$sql = "select MZ from PEAK where ID='$id'" . $where;
		@ans = &MySql($sql);
		$rec_num = @ans;
		$out_mz = "";
		for $item ( @ans ) {
			$out_mz .= "$$item[0]@";
		}
		chop $out_mz;
		$param .= "\t\t\t<param name=\"mz1\" value=\"$out_mz\">\n";
		$mz_num = "1";
	}
}

#------------------------
# Quick Search by Peak
#------------------------
if ( $qmz ne '' ) {
	@mzs = split(',', $qmz);
	my $cnt = @mzs;
	for ( $i = 0; $i < $cnt; $i++ ) {
	 	$mz = $mzs[$i];
		$min = $mz - 0.3 - 0.00001;
		$max = $mz + 0.3 + 0.00001;
		$range .= "(MZ between $min and $max)";
		$in .= "ROUND($mz,3)";
		if ( $i < $cnt - 1 ) {
			$range .= " or ";
			$in .= ",";
		}
	}
	$sql = "select MZ from PEAK where ID='$id' and RELATIVE > $cutoff and ROUND(MZ,3) in($in)";
	@ans = &MySql($sql);
	for $item ( @ans ) {
		$out_mz .= "$$item[0]@";
	}

	$sql = "select distinct(MZ) from PEAK where ID='$id' and RELATIVE > $cutoff and ($range)";
	@ans = &MySql($sql);
	for $item ( @ans ) {
		$out_mz2 .= "$$item[0]@";
	}

	$param .= "\t\t\t<param name=\"type\" value=\"qpeak\">\n";
	$param .= "\t\t\t<param name=\"mz1\" value=\"$out_mz2\">\n";
	if ( $out_mz ne '' ) {
		$param .= "\t\t\t<param name=\"mz2\" value=\"$out_mz\">\n";
		$mz_num = "2";
	}
 	else {
		$mz_num = "1";
	}
}
#------------------------
# Product Ion
#------------------------
elsif ( $product ne '' ) {
	my @formula_list = split(",", $product);
	@formula_list = grep {!$count{$_}++} @formula_list;		# 重複削除
	$mz_num = scalar(@formula_list);
	$param  = "\t\t\t\t\t\t<param name=\"type\" value=\"product\">\n";
	my $cnt = 1;
	for my $formula ( @formula_list ) {
		my $mz = &MassCalc($formula);
		my $val ="$mz,$formula";
		$param .= "\t\t\t\t\t\t<param name=\"ion$cnt\" value=\"$val\">\n";
		$cnt++;
	}
}
#------------------------
# Neutral Loss
#------------------------
elsif ( $nloss ne '' ) {
	my @formula_list = split(",", $nloss);
	#--------------------
	# SEQUENCE
	#--------------------
	if ( $mode eq 'seq' ) {
		my $like = "";
		foreach my $formula ( @formula_list ){
			if ( $formula ne '' ) {
				$like .= "%>$formula-";
			}
		}
		$like .= "%";
		$where = "PATH like '$like'";
		$sql = "select PATH, PP_NO from NEUTRAL_LOSS_PATH where ID='$id' and $where";
#		print "SQL:$sql<br>";
		my @ans = &MySql($sql);
		my @path_items = ();
		my @ppno_items = ();
		my @hit_ppno = ();
		for my $item ( @ans ) {
			my $path = $$item[0];
			my $ppno = $$item[1];
			$path = substr($path, 1, length($path)-2);
			@path_items = split("->", $path);
			@ppno_items = split(",", $ppno);
			for my $l1 ( 0 .. $#path_items ) {
				for my $l2 ( 0 .. $#formula_list ) {
					if ( $path_items[$l1] eq $formula_list[$l2] ) {
						push(@hit_ppno, @ppno_items[$l1]);
						last;
					}
				}
			}
		}
		@hit_ppno = grep {!$count{$_}++} @hit_ppno;		# 重複削除
		my $ppno = join(",", @hit_ppno);
		$where = "and NO in($ppno)";
	}
	#--------------------
	# AND
	#--------------------
	elsif ( $mode eq 'and' ) {
		my @sql_formula_list = ();
		@formula_list = sort(grep {!$count{$_}++} @formula_list);		# 重複削除
		for my $i ( 0 .. $#formula_list ) {
			push(@sql_formula_list, "'" . $formula_list[$i] . "'");
		}
		$in = join(",", @sql_formula_list );
		$where = "and NEUTRAL_LOSS in($in)";
	}
	$sql = "select PRECURSOR, PRODUCT, NEUTRAL_LOSS from PRE_PRO where ID='$id' "
		 . $where . " order by NEUTRAL_LOSS";
#	print "SQL:$sql<br>";
	my @ans = &MySql($sql);

	# アプレットのパラメータをセットする
	$mz_num = scalar(@ans);
	$param  = "\t\t\t\t\t\t<param name=\"type\" value=\"nloss\">\n";
	my $cnt = 1;
	for my $item ( @ans ) {
		my $pre_mz = &MassCalc($$item[0]);	# プレカーサイオンを数値に変換
		my $pro_mz = &MassCalc($$item[1]);	# プロダクトイオンを数値に変換
		my $nloss = $$item[2];				# ニュートラルロス分子式
		my $val ="$pro_mz,$pre_mz,$nloss";
		$param .= "\t\t\t\t\t\t<param name=\"nloss$cnt\" value=\"$val\">\n";
		$cnt++;
	}
	$height += 12 * $cnt;
}


print << "HTML";
		<table>
			<tr>
				<td valign="top">
					<font style="font-size:10pt;" color="dimgray">Mass Spectrum</font>
HTML

if ( $version == 1 ) {
	$profile = "../DB/profile/$db_name/$acc.jpg";
	if ( -e $profile ) {
		print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"$profile\" target=\"_blank\"><font style=\"font-size:10pt;\">Profile</font></a>";
	}
}

print << "HTML";
					<br>
					<applet code="Display.class" archive="../applet/Display2.jar" width="750" height="$height">
						<param name="id" value="$id">
						<param name="site" value="$src">
						<param name="num" value="$mz_num">
HTML

if ( $param ne '' ) {
	print $param;
}
if ( $precursor ne '' ) {
print << "HTML";
						<param name="precursor" value="$precursor">
HTML
}

print << "HTML";
					</applet>
				</td>
				<td valign="top">
					<font style="font-size:10pt;" color="dimgray">Chemical Structure</font><br>
HTML

# Chemical Structure
@compound_name = split(';', $name);
$cond = $compound_name[0];
$cond =~ s/"/""/g;
$sql = "SELECT FILE FROM MOLFILE WHERE NAME=\"$cond\"";
@ans = &MySql($sql);
$gifId = $ans[0][0];
$gifFile = "../DB/gif/$db_name/$gifId.gif";
$gifUrl = "$myServer"."DB/gif/$db_name/$gifId.gif";
$gifLargeFile = "../DB/gif_large/$db_name/$gifId.gif";
$gifLargeUrl = "$myServer"."DB/gif_large/$db_name/$gifId.gif";
if ( -f $gifFile ) {
	if ( ! -f $gifLargeFile ) {
		$gifLargeUrl = "../image/not_available_l.gif";
	}
	print "\t\t\t\t\t<img src=\"$gifUrl\" alt=\"\" border=\"1\" width=\"180\" height=\"180\" onClick=\"expandMolView('$gifLargeUrl')\" style=\"margin:10px; cursor:pointer\"><br>\n";
}
else {
print << "HTML";
					<applet code="MolView.class" archive="../applet/MolView.jar" width="200" height="200">
						<param name="site" value="$src">
						<param name="compound_name" value="$compound_name[0]">
					</applet>
HTML
}

print << "HTML";
				</td>
			</tr>
		</table>
HTML


print "<hr size=\"1\">\n";
print "<pre style=\"font-family:Courier New;font-size:10pt\">\n";

my @boundary = ( 'CH\$', 'AC\$', 'PK\$' );
my $num = @boundary;
my $step = 0;
my $isRelated = false;
my $isSpBoundary = false;
my $isMsBoundary = false;
foreach my $l ( @Line ) {
	if ( $step < $num && $l =~ /^$boundary[$step]/ ) {
		print "<hr size=\"1\" color=\"silver\" width=\"98%\" align=\"left\">";
		$step++;
	}
	if ( $isSpBoundary eq false && $l =~ /^SP\$/ ) {
		print "<hr size=\"1\" color=\"silver\" width=\"98%\" align=\"left\">";
		$isSpBoundary = true;
	}
	if ( $isMsBoundary eq false && $l =~ /^MS\$/ ) {
		print "<hr size=\"1\" color=\"silver\" width=\"98%\" align=\"left\">";
		$isMsBoundary = true;
	}
	if ( $l =~ /^RELATED_RECORD/ ) {
		print "<hr size=\"1\" color=\"silver\" width=\"98%\" align=\"left\">";
		print "$l\n";
		$isRelated = true;
		next;
	}
	if ( $isRelated eq true ) {
		if ( substr($l,1,1) ne "*" ) {
			my $tmpVal = $l;
			$tmpVal =~ s/^\s*(.*?)\s*$/$1/;	# Trim
			$pos = index($tmpVal,' ');
			if ( $pos >= 0 ) {
				$id = substr($tmpVal, 0, $pos );
				$name = substr($tmpVal, $pos + 1 );
				$url = "./Dispatcher.jsp?type=disp&id=$id&site=$src";
				print "&nbsp;&nbsp;<a href=\"$url\" target=\"_blank\">$id</a>&nbsp;$name\n";
				next;
			}
		}
	}
	
	my $item_name = '';
	my $val = '';
	my @vals = ();
	my $array_key = '';
	foreach my $key ( keys %FMT ) {
		if (  $l =~ /^COMMENT: \[MS[0-9]*\] / && $key eq 'COMMENT: \[MSn\]') {
			($val = $l) =~ s/^COMMENT: \[MS[0-9]*\] //;
			@vals = split('\s', $val);
			$array_key = $key;
			($item_name = $l) =~ s/^(COMMENT: \[MS[0-9]*\]) .*/$1/;
			last;
		}
		elsif ( $l =~ /^$key/ ) {
			($val = $l) =~ s/^$key //;
			@vals = split('\s', $val);
			$array_key = $key;
			($item_name = $key) =~ s/\\//g;
			last;
		}
	}
	if ( $val eq '' ) {
		print "$l\n";
	}
	else {
		print "$item_name";
		if ( $array_key eq 'LICENSE:' ) {
			($cc) = ($val =~ m/(CC [^ ]*)/o);
			if ( $cc ne '' ) {
				$url = $FMT{$array_key};
				if ( index($ENV{'HTTP_ACCEPT_LANGUAGE'}, 'ja') != -1 ) {
					$url =~ s/\.org/\.jp/o;
				}
				$val =~ s/$cc//o;
				$val =~ s/^\s*(.*?)\s*$/$1/;
				$val = "<a href=\"$url\" target=\"_blank\">$cc</a>".($val ne "" ? " $val" : "");
			}
			else {
				($otherLink) = ($val =~ m/(http:\/\/[^ ]*)/);
				if ( $otherLink ne '' ) {
				$val =~ s/$otherLink//o;
				$val =~ s/^\s*(.*?)\s*$/$1/;
				$val = "<a href=\"$otherLink\" target=\"_blank\">$otherLink</a>".($val ne "" ? " $val" : "");
				}
			}
			print " $val";
		}
		elsif ( $array_key eq 'PUBLICATION:' ) {
			($pmid) = ($val =~ m/\[PMID:\s(.*)\]/o);
			if ( $pmid ne '' ) {
				$url = sprintf( $FMT{$array_key}, $pmid );
				$link = "<a href=\"$url\" target=\"_blank\">$pmid</a>";
				$val =~ s/\[PMID:.*\]/\[PMID: $link\]/o;
			}
			print " $val";
		}
		elsif ( index($array_key, 'COMMENT:') != -1 ) {
			$link_id = $vals[0];
			if ( $array_key ne 'COMMENT: \[Profile\]' ) {
				$url = sprintf( $FMT{$array_key}, $link_id, $src );
			}
			else {
				$url = sprintf( $FMT{$array_key}, $db_name, $link_id );
			}
			if ( $url ne "" ) {
				$link = "<a href=\"$url\" target=\"_blank\">$link_id</a>";
				$val =~ s/$link_id/$link/;
			}
			print " $val";
		}
		elsif ( index($array_key, 'CH\$LINK: OligosaccharideDataBase') == -1 ) {
			foreach $val ( @vals ) {
				$comment = "";
				if ( $array_key eq 'CH\$LINK: PUBCHEM' ) {
					@id = split( ':', $val );
					$id_num = @id;
					if ( $id_num == 1 ) {
						$number = $id[0];
						$pname = '';
						print " ";
					}
					else {
						print " $id[0]:";
						$number = $id[1];
						$pname = lc($id[0]);
					}
					if ( $pname eq '' ) {
						$url = "$FMT{$array_key}sid=$number";
					}
					else {
						$url = "$FMT{$array_key}$pname=$number";
					}
				}
				else {
					$pos = index( $val, '(' );
					if ( $pos >= 0 ) {
						$comment = substr( $val, $pos, length($val)-$pos );
						$val = substr( $val, 0, $pos );
					}
					if ( $array_key eq 'CH\$LINK: KEGG' ) {
						if ( substr($val, 0, 1) eq 'G' ) {
							$pre = "gl";
						}
						elsif ( substr($val, 0, 1) eq 'D' ) {
							$pre = "dr";
						}
						else {
							$pre = "cpd";
						}
						$url = sprintf( $FMT{$array_key}, $pre, $val );
					}
					else {
						$url = sprintf( $FMT{$array_key}, $val );
					}
					if ( $array_key eq 'MS\$RELATED_MS: PREVIOUS_SPECTRUM' ) {
						$url .= "&site=$src";
					}
					$number = $val;
					print " ";
				}
				print "<a href=\"$url\" target=\"_blank\">$number</a>";
				if ( $comment ne '' ) {
					print $comment;
				}
			}
		}
		else {
			my $vals_length = @vals;
			print " ";
			if ( $array_key eq 'CH\$LINK: OligosaccharideDataBase' ) {
				if ( $vals_length > 0 ) {
					$htmlId = $vals[0];
				}
				if ( $vals_length > 1 ) {
					$link = $vals[1];
					$url = sprintf( $FMT{$array_key}, $htmlId, "#anchor$link" );
				}
				print "$htmlId <a href=\"$url\" target=\"_blank\">$link</a>";
			}
			elsif ( $array_key eq 'CH\$LINK: OligosaccharideDataBase2D' ) {
				if ( $vals_length >= 1 ) {
					$htmlId = $vals[0];
					shift(@vals);
					$url = sprintf( $FMT{$array_key}, $htmlId );
				}
				print "<a href=\"$url\" target=\"_blank\">$htmlId</a> @vals";
			}
		}
		print "\n";
	}
}

$dbh->disconnect;
&myexit;

sub myexit() {
print << "HTML";
</pre>
		<hr size=1>
		<iframe src="../copyrightline.html" width="800" height="20px" frameborder="0" marginwidth="0" scrolling="no"></iframe>
	</body>
</html>
HTML
exit(0);
}

sub MySql() { local($sql) = @_;
	local($sth, $n, $i, @ans, @ret);
	@ret = ();
	$sth = $dbh->prepare($sql);
	$sth->execute || &myexit;
	$n = $sth->rows;
	for ( $i = 0; $i < $n; $i ++ ) { @ans = $sth->fetchrow_array; push(@ret, [@ans]); }
	$sth->finish;
	return @ret;
}

sub MassCalc() {
	%mass_list = ("H"  => 1.007825032,  "Be" => 9.0121821,    "B"  => 11.0093055,  "C"  => 12.00000000,
				  "N"  => 14.003074005, "O"  => 15.994914622, "F"  => 18.99840320, "Na" => 22.98976967,
				  "Al" => 26.98153844,  "Si" => 27.976926533, "P"  => 30.97376151, "S"  => 31.97207069,
				  "Cl" => 34.96885271,  "V"  => 50.9439637,   "Cr" =>  51.9405119, "Fe" => 55.9349421,
				  "Ni" => 57.9353479,   "Co" => 58.9332001,   "Cu" => 62.9296011,  "Zn" => 63.9291466,
				  "Ge" => 73.9211782,   "Br" => 78.9183376,   "Mo" => 97.9054078,  "Pd" => 105.903483,
				  "I"  => 126.904468,   "Sn" => 119.9021966,  "Pt" => 194.964774,  "Hg" => 201.970626 );

	my $formula = shift;
	my @atom_list = &GetAtomList($formula);
	my $mass = 0;
	foreach my $atom_num (@atom_list) {
		my @item = split(':', $atom_num);
		my $atom = $item[0];
		my $num  = $item[1];
		$mass += $mass_list{$atom} * $num;
	}
	$mass = int(($mass * 1000) + 0.5) / 1000;
	return $mass;
}

sub GetAtomList() {
	my $formula = shift;
	my @atom_list = ();
	my $start_pos = 0;
	my $end_pos = length($formula);

	for ( my $pos = $start_pos; $pos <= $end_pos; $pos++ ) {
		my $chr = "";
		if ( $pos < $end_pos ) {
			$chr = substr( $formula, $pos, 1 );
		}
		if ( $pos == $end_pos || ($pos > 0 && $chr =~ /[\D]/ && $chr eq uc($chr)) ) {
			# 元素記号 + 個数を切り出す
			my $item = substr( $formula, $start_pos, $pos - $start_pos );

			# 元素記号と個数を分解
			my $isFound = false;
			$pos1 = length($item);
			for ( my $i = 1; $i < length($item); $i++ ) {
				$chr = substr( $item, $i, 1 );
				if ( $chr =~ /[\d]/ ) {
					$pos1 = $i;
					$isFound = true;
					last;
				}
			}

			my $atom = substr($item, 0, $pos1);
			my $num = 1;
			if ( $isFound eq true ) {
				$num = substr($item, $pos1);
			}

			# 元素が同じ場合
			if ( $atom_list{$atom} ne '' ) {
				$num = $num + $atom_list{$atom};
			}

			# 値格納
			push(@atom_list, "$atom:$num");
			$start_pos = $pos;
		}
	}
	return @atom_list;
}

1;
