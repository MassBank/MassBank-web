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
# Quick Search 検索処理
#
# ver 3.0.2  2009.07.07
#
#-------------------------------------------------------------------------------
use DBI;
use CGI;

$query = new CGI;
$db_name = $query->param('dsn');
if ( $db_name eq '' ) {
	$db_name = "MassBank";
}
open(F, "DB_HOST_NAME");
while ( <F> ) {
	chomp;
	$host_name .= $_;
}

$DB = "DBI:mysql:$db_name:$host_name";
$User = 'bird';
$PassWord = 'bird2006';
$dbh  = DBI->connect($DB, $User, $PassWord) || die "connect error \n";

print "Content-Type: text/plain\n\n";

$query = new CGI;
@params = $query->param();
foreach $key ( @params ) {
	$val = $query->param($key);
	if ( $key eq 'id' ) {
		push(@id, $val);
	}
	elsif ( $key eq 'display' ) {
		$disp = $val;
	}
	elsif ( $key eq 'inst' ) {
		@inst = $query->param($key);
	}
	elsif( $key ne 'check' ) {
		$Arg{$key} = $val;
	}
}

$where_ion = "";
if ( $Arg{'ion'} eq '1' ) {
	$where_ion = " and ion > 0";
}
elsif ( $Arg{'ion'} eq '-1' ) {
	$where_ion = " and ion < 0";
}

$isAll = 0;
foreach $inst (@inst) {
	if ( $inst eq 'all' ) {
		$isAll = 1;
		last;
	}
}
if ( !$isAll ) {
	for ( $i = 0; $i < @inst; $i ++ ) {
		$where_inst .= " INSTRUMENT_TYPE='@inst[$i]'";
		if ($i != @inst -1) {
			$where_inst .= " or";
		}
	}
	$sql = "select INSTRUMENT_NO from INSTRUMENT where"
		 . "$where_inst";
	@ans = &MySql($sql);
	$cnt = @ans;
	if ( $cnt == 0 ) {
		$dbh->disconnect;
		exit(0);
	}
	foreach $item ( @ans ) {
		$inst_no = $$item[0];
		$in .= "$inst_no,";
	}
	chop $in;
	
	$where1 .= " R.INSTRUMENT_NO in($in)";
}

if ( $Arg{'compound'} ne '' ) {
	$compound = $Arg{'compound'};
	$compound =~ s/'/''/g;
	$where2 = " N.NAME like '\%$compound\%'";
}


$tol = $Arg{'tol'} + 0;
if ( $Arg{'mz'} ne '' ) {
	$mz = $Arg{'mz'} + 0;
	$mz1 = $mz - $tol;
	$mz2 = $mz + $tol;
	if ( $where2 ne '' ) {
		$where2 .= " $Arg{'op1'}";
	}
	$where2 .= " R.EXACT_MASS between $mz1 and $mz2";
}

if ( $Arg{'formula'} ne '' ) {
	$formula = $Arg{'formula'};
	$formula =~ s/\*/%/g;
	if ( $where2 ne '' ) {
		$where2 .= " $Arg{'op2'}";
	}
	$where2 .= " R.FORMULA like '$formula'"; 
}

$sql = "select distinct R.ID, R.FORMULA, R.EXACT_MASS from RECORD R "
	 . "LEFT JOIN CH_NAME N ON R.ID = N.ID";
if ( $where1 ne '' ) {
	$sql .= " where ($where1)";
}
if ( $where2 ne '' ) {
	if ( $where1 eq '' ) {
		$sql .= " where ";
	}
	else {
		$sql .= " and ";
	}
	$sql .= "($where2)";
}

@ans = &MySql($sql);
$cnt = @ans;
if ( $cnt == 0 ) {
	$dbh->disconnect;
	exit(0);
}
foreach $item ( @ans ) {
	$id = $$item[0];
	$formula = $$item[1];
	$emass= $$item[2];
	$sql = "select NAME, ID, ION from SPECTRUM where ID = '$id'"
		 . "$where_ion";
	@rec = &MySql($sql);
	foreach $rec ( @rec ) {
		print join("\t", @$rec), "\t$formula\t$emass\n";
	}
}
$dbh->disconnect;
exit(0);

sub MySql() { local($sql) = @_;
	local($sth, $n, $i, @ans, @ret);
	@ret = ();
	$sth = $dbh->prepare($sql);
	unless( $sth->execute ) {
		return undef;
	}
	$n = $sth->rows;
	for ( $i = 0; $i < $n; $i ++ ) { @ans = $sth->fetchrow_array; push(@ret, [@ans]); }
	$sth->finish;
	return @ret;
}

1;
