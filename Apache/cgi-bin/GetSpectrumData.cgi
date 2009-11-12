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
# スペクトルのグループ情報取得
#
# ver 3.0.2  2008.12.05
#
#-------------------------------------------------------------------------------
use CGI;
use DBI;

$query = new CGI;
$db_name = $query->param('dsn');
$id = $query->param('id');
$relation = $query->param('relation');
if ( $db_name eq '' ) {
	$db_name = "MassBank";
}
open(F, "DB_HOST_NAME");
while ( <F> ) {
	chomp;
	$host_name .= $_;
}

print "Content-Type: text/plain\n\n";

$SQLDB = "DBI:mysql:$db_name:$host_name";

$User = 'bird';
$PassWord = 'bird2006';
$dbh  = DBI->connect($SQLDB, $User, $PassWord) || exit(0);

# get child spectrum info
if ( $relation ne 'true' ) {
	&getChildInfo($id);
}
# get child spectrum info & get relation parent spectrum info
else {
	@parent_ids = &MySql("select PARENT_ID from SPECTRUM where ID='$id'");
	if ( $parent_ids[0][0] ne '' ) {
		$parent_id = $parent_ids[0][0];
		@child_ids = &MySql("select ID from SPECTRUM where PARENT_ID='$parent_id'");
		for( $i=0; $i<=$#child_ids; $i++ ) {
			$child_id = $child_ids[$i][0];
			if ( $child_id ne '' ) {
				&getChildInfo($child_id);
			}
		}
	}
	else {
		&getChildInfo($id);
	}
}
$dbh->disconnect;
exit(0);

sub getParentInfo() {
	local($key_id) = @_;
	my(@ans, $num, $rec, $name, $precursor);
	@ans = &MySql("select pp.MZ, pp.RELATIVE from PARENT_SPECTRUM ps left join PARENT_PEAK pp on ps.SPECTRUM_NO = pp.SPECTRUM_NO where ps.ID = '$key_id' order by pp.MZ");
	$num = @ans;
	if ( $num == 0 ) {
		print "0\t0\t\t";
	}
	else {
		foreach $rec ( @ans ) {
			print join("\t", @$rec), "\t\t";
		}
	}
	
	# follow record info
	print "::";
	
	@ans = &MySql("select NAME, PRECURSOR_MZ from PARENT_SPECTRUM where ID = '$key_id'");
	$name = $ans[0][0];
	if ( $name ne '' ) {
		print "\tname=$name\t";
	}
	$precursor = $ans[0][1];
	if ( $precursor ne '' ) {
		print "\tprecursor=$precursor\t";
	}
	print "\tid=$key_id\t\n";
}

sub getChildInfo() {
	local($key_id) = @_;
	my(@ans, $num, $rec, $name, $precursor);
	
	@ans = &MySql("select MZ, RELATIVE from PEAK where ID='$key_id' order by MZ");
	$num = @ans;
	if ( $num == 0 ) {
		print "0\t0\t\t";
	}
	else {
		foreach $rec ( @ans ) {
			print join("\t", @$rec), "\t\t"; 
		}
	}
	
	# follow record info
	print "::";

	@ans = &MySql("select NAME, PRECURSOR_MZ from SPECTRUM where ID = '$key_id'");
	$name = $ans[0][0];
	if ( $name ne '' ) {
		print "\tname=$name\t";
	}
	$precursor = $ans[0][1];
	if ( $precursor ne '' ) {
		print "\tprecursor=$precursor\t";
	}
	print "\tid=$key_id\t\n";
}

sub MySql() { local($sql) = @_;
	local($sth, $n, $i, @ans, @ret);
	@ret = ();
	$sth = $dbh->prepare($sql);
	$sth->execute || exit(0);
	$n = $sth->rows;
	for ( $i = 0; $i < $n; $i ++ ) { @ans = $sth->fetchrow_array; push(@ret, [@ans]); }
	$sth->finish;
	return @ret;
}
1;
