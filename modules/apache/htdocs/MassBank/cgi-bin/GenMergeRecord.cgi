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
# [Admin Tool] 統合スペクトル生成
#             (レコードファイル, MERGE.sql, UPADTE.sql)
#
# ver 1.0.2  2010.09.16
#
#-------------------------------------------------------------------------------
use CGI;
use DBI;
use POSIX 'strftime';
use File::Path;

print "Content-Type: text/plain\n\n";

$query = new CGI;
my $out_dir = $query->param('out_dir');
my $db_name = $query->param('db');
my $ids = $query->param('id');
my @target_ids = split(',' , $ids);
open(F, "DB_HOST_NAME");
while ( <F> ) {
	chomp;
	$host_name .= $_;
}
$SQLDB = "DBI:mysql:$db_name:$host_name";
$User = 'bird';
$PassWord = 'bird2006';
$dbh = DBI->connect($SQLDB, $User, $PassWord) || &myexit;

#------------------------------------------------------
# 統合スペクトルの最終IDを取得
#------------------------------------------------------
my $sql = "select max(ID) from SPECTRUM";
my @ans = &MySql($sql);
my $id = $ans[0][0];
my $sql = "select ifnull(max(ID),0) from SPECTRUM where NAME like '%MERGED%'";
my @ans = &MySql($sql);
my $max_id = $ans[0][0];
my $prefix = substr($id, 0, 2);
local $last_no = substr($max_id, 3) + 1;

$out_dir = "$out_dir/$db_name";
mkpath($out_dir);
open(FS, ">$out_dir/$db_name" . "_MERGED.sql");
open(FU, ">$out_dir/$db_name" . "_UPADTE.sql");
print FS "START TRANSACTION;\n";
print FU "START TRANSACTION;\n";

#------------------------------------------------------
# 化合物名リストを取得
#------------------------------------------------------
my $sql = "select substring_index(NAME,';',2) as XNAME from SPECTRUM group by XNAME order by ID";
my @ans0 = &MySql($sql);

my ($id, $get_record_name, $get_ion, $get_ion_type, $get_xname);
local ($record_name, $ion, $ion_type, $formula, $mass, $inst_no, $smiles, $iupac, $xname);
local (@group_id, @group_title, $merge_id);
local $date = strftime "%Y.%m.%d", localtime;

for my $rs0 ( @ans0 ) {
	my $name = $$rs0[0];
	$name =~ s/\'/\\\'/g;

	#------------------------------------------------------
	# 同一化合物名のものをグループ化し、
	# グループ内でレコードタイトル順にソートする
	#------------------------------------------------------
	$sql = "select S.ID, NAME, ION, substring(NAME,instr(NAME,'[M')) as ION_TYPE,"
				. " FORMULA, EXACT_MASS, INSTRUMENT_NO, SMILES, IUPAC,"
				. " concat(substring_index(NAME,';',2), substring(NAME,instr(NAME,'[M'))) as XNAME"
				. " from SPECTRUM S, RECORD R where substring_index(NAME,';',2) = '$name'"
				. " and S.ID = R.ID order by XNAME desc";
	my @ans = &MySql($sql);

	for my $rs ( @ans ) {
		$id = $$rs[0];
		my $isFound = false;

		#------------------------------------------------------
		# 処理対象外IDの場合は、以降の処理は行わない
		#------------------------------------------------------
		for my $target_id (@target_ids) {
			if ( $id eq $target_id ) {
				$isFound = true;
				break;
			}
		}
		if ( $isFound eq false ) {
			next;
		}

		$get_record_name = $$rs[1];
		$get_ion         = $$rs[2];
		$get_ion_type    = $$rs[3];
		$get_xname       = $$rs[9];
		#------------------------------------------------------
		# レコード名から化合物名を取り出す
		#------------------------------------------------------
 		my @short_name = split(";", $get_record_name);
		my $compound_name = $short_name[0];

		#------------------------------------------------------
		# 同一化合物名でも別レコードになるかをION TYPEにて判別
		#------------------------------------------------------
		my $isChange = false;
		if ( $xname eq '' ) {
			$isChange = false;
		}
		elsif ( $get_xname ne $xname ) {
			$isChange = true;
		}
		else {
			if ( $get_ion_type eq '' ) {
				if ( $get_ion ne $ion ) {
					$isChange = true;
				}
			}
			else {
				if ( $get_ion_type ne $ion_type ) {
					$isChange = true;
				}
			}
		}

		if ( $isChange eq true ) {
			&makeRecordFile();
			@group_id = ();
			@group_title = ();
		}
		$record_name = $$rs[1];
		$ion         = $$rs[2];
		$ion_type    = $$rs[3];
		$formula     = $$rs[4];
		$mass        = $$rs[5];
		$inst_no     = $$rs[6];
		$smiles      = $$rs[7];
		$iupac       = $$rs[8];
		$xname       = $$rs[9];

		push(@group_id, $id);
		push(@group_title, $record_name);
	}
}
&makeRecordFile();

print FS "COMMIT;\n";
print FU "COMMIT;\n";
close(FS);
close(FU);
print "Content-Type: text/plain\n\n";
print "OK\n";
exit(0);


sub makeRecordFile() {
	my ($sql, $rs, @ans);
	my $id = $group_id[0];
	$merge_id = $prefix . substr("X00000", 0, 6 - length($last_no)) . $last_no++;

	my @short_name = split(";", $record_name);
	my $inst = trim($short_name[1]);
	my $del_arry_num = 0;
	if ( $inst eq "MS/MS"
		|| $inst eq "LC-MS/MS"
		|| $inst eq "LC-Q/MS" ) {
		# QqQ-MS, QqTOF, LC-QqQ, LC-Q
		$del_arry_num = 3;
	}
	else {
		# LC-TOF, GC-TOF, CE-TOF
		$del_arry_num = -1;
	}

	#------------------------------------------------------
	# レコードファイル生成
	#------------------------------------------------------
	open(F, ">$out_dir/" . $merge_id . ".txt");
	#-- ACCESSION -----------------------------------------
	print F "ACCESSION: $merge_id\n";

	#-- RECORD_TITLE --------------------------------------
	my $compound_name = $short_name[0];
	my $record_name = "$compound_name;";
	for my $i ( 1 .. $#short_name ) {
		if ( $i == $del_arry_num ) {
			$record_name .= " MERGED;";
		}
		else {
			$record_name .= "$short_name[$i];";
		}
	}
	chop $record_name;
	print F "RECORD_TITLE: $record_name\n";

	#-- 日付 ----------------------------------------------
	print F "DATE: $date\n";

	#-- 化合物情報 ----------------------------------------
	$sql = "select NAME from CH_NAME where ID = '$id' order by NAME";
	@ans = &MySql($sql);
	my @ch_name = ();
	for $rs ( @ans ) {
		my $name = $$rs[0];
		print F "CH\$NAME: $name\n";
		$name =~ s/'/''/g;
		push(@ch_name, "('$merge_id','$name')");
	}
	print F "CH\$FORMULA: $formula\n";
	print F "CH\$EXACT_MASS: $mass\n";
	print F "CH\$SMILES: $smiles\n";
	print F "CH\$IUPAC: $iupac\n";

	#-- CH_LINK --------------------------------------------
	my @item_name = (
		"CAS", "CHEBI", "CHEMPDB", "KEGG", "NIKKAJI", "PUBCHEM"
	);
	my $col_name = join(",", @item_name);
	$sql = "select $col_name from CH_LINK where ID = '$id'";
	@ans = &MySql($sql);
	my @ch_link = ();
	for my $i ( 0 .. $#{$ans[0]} ) {
		my $link = $ans[0][$i];
		push(@ch_link, "'$link'");
		if ( $link ne '' ) {
			print F "CH\$LINK: $item_name[$i] $link\n";
		}
	}

	#-- 統合元スペクトルの情報 ----------------------------
	my @related = ();
	for $i ( 0 .. $#group_id ) {
		my $str = "$group_id[$i] $group_title[$i]";
		push(@related, $str);
	}
	print F "RELATED_RECORD:\n";
	foreach my $str ( sort(@related) ) {
		print F "  $str\n";
	}
	print F "//\n";
	close(F);

	#------------------------------------------------------
	# SPECTRUMテーブルUPDATE SQL
	#------------------------------------------------------
	foreach my $child_id ( sort(@group_id) ) {
		print FU "update SPECTRUM set PARENT_ID='$merge_id' where ID ='$child_id';\n";
	}

	#------------------------------------------------------
	# SPECTRUMテーブルINSERT SQL
	#------------------------------------------------------
	$record_name =~ s/'/''/g;
	$sql = "insert into SPECTRUM(ID,NAME,ION) "
		 . "values('$merge_id','$record_name',$ion);";
	print FS "$sql\n";

	#------------------------------------------------------
	# PEAKテーブルINSERT SQL
	#------------------------------------------------------
	my $in = "";
	for my $gid (@group_id) {
		$in .= "'$gid',";
	}
	chop $in;
	$sql = "select MZ, max(INTENSITY) from PEAK where ID in($in) group by MZ order by MZ";
	@ans = &MySql($sql);
	my $max_inte = 0;
	for $rs ( @ans ) {
		my $inte = $$rs[1];
		if ( $inte > $max_inte ) {
			$max_inte = $inte;
		}
	}
	my $val = "";
	for $rs ( @ans ) {
		my $mz   = $$rs[0];
		my $inte = $$rs[1];
		my $rel = int(($inte / $max_inte * 999) + 0.5);
		if ( $rel == 0 ) {
			$rel = 1;
		}
		$val .= "('$merge_id',$mz,$inte,$rel),";
	}
	chop $val;
	$sql = "insert into PEAK values$val;";
	print FS "$sql\n";

	#------------------------------------------------------
	# RECORDテーブルINSERT SQL
	#------------------------------------------------------
	$sql = "insert into RECORD(ID,DATE,FORMULA,EXACT_MASS,INSTRUMENT_NO,SMILES,IUPAC) "
		 . "values('$merge_id','$date','$formula',$mass,$inst_no,'$smules','$iupac');";
	print FS "$sql\n";

	#------------------------------------------------------
	# CH_NAMEテーブルINSERT SQL
	#------------------------------------------------------
	my $col_val = join(",", @ch_name);
	$sql = "insert into CH_NAME values$col_val;";
	print FS "$sql\n";

	#------------------------------------------------------
	# CH_LINKテーブルINSERT SQL
	#------------------------------------------------------
	$col_val = join(",", @ch_link);
	$sql = "insert into CH_LINK(ID,$col_name) values('$merge_id',$col_val);";
	print FS "$sql\n";
}

sub MySql() {
	my $sql = $_[0];
	my @ret = ();
	my $sth = $dbh->prepare($sql);
	$sth->execute || exit(0);
	$n = $sth->rows;
	for ( $i = 0; $i < $n; $i ++ ) { my @ans = $sth->fetchrow_array; push(@ret, [@ans]); }
	$sth->finish;
	return @ret;
}

sub trim {
	my $val = shift;
	$val =~ s/^ *(.*?) *$/$1/;
	return $val;
}
