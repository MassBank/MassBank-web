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
# レコードリスト別のサマリー情報取得
#
# ver 3.0.1  2008.12.05
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
$host_name = "";
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
	$Arg{$key} = $val;
}

# IndexType -> Site
if ( $Arg{'idxtype'} eq 'site') {
	$sqlparam = "";
	$sql = getSql( 1, $sqlparam );
}

# IndexType -> Instrument Type
elsif ( $Arg{'idxtype'} eq 'inst' ) {
	$sql = "select INSTRUMENT_NO from INSTRUMENT where INSTRUMENT_TYPE='$Arg{'srchkey'}'";
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
	$sqlparam = "where R.INSTRUMENT_NO in($in)";
	$sql = getSql( 0, $sqlparam );
}

# IndexType -> Ionization Mode
elsif ( $Arg{'idxtype'} eq 'ion' ) {
	if ( $Arg{'srchkey'} eq 'Positive' ) {
		$sqlparam = "where S.ION > 0";
	}
	elsif ( $Arg{'srchkey'} eq 'Negative' ) {
		$sqlparam = "where S.ION < 0";
	}
	$sql = getSql( 0, $sqlparam );
}

# IndexType -> Compound Name
elsif ( $Arg{'idxtype'} eq 'cmpd' ) {
	# Condition -> 0-9
	if ( $Arg{'srchkey'} eq '1-9' ) {
		$sqlparam = "where NAME regexp '^[0-9]' ";
		$sql = getSql( 1, $sqlparam );
	}
	# Condition -> other
	elsif ( $Arg{'srchkey'} eq 'Others' ) {
		$sqlparam = "where NAME regexp '^[^a-z0-9]' ";
		$sql = getSql( 1, $sqlparam );
	}
	# Condition -> a-z
	else {
		$sqlparam = "where NAME like '$Arg{'srchkey'}\%' ";
		$sql = getSql( 1, $sqlparam );
	}
}

@ans = &MySql( $sql );
$cnt = @ans;
if ( $cnt == 0 ) {
	$dbh->disconnect;
	exit(0);
}
foreach $item ( @ans ) {
	print join("\t", @$item), "\n";
}
$dbh->disconnect;
exit(0);


sub getSql() {
	local(@tmp) = @_;
	$sqltype = @tmp[0];
	$sqlparam = @tmp[1];
	@sqlstr = ("select Tmp.name, Tmp.id, Tmp.ion, Tmp.formula, Tmp.emass from "
				 . "( select distinct S.ID as id, R.FORMULA as formula, R.EXACT_MASS as emass, S.NAME as name, S.ION as ion "
				 . "from SPECTRUM S "
				 . "LEFT JOIN RECORD R ON S.ID = R.ID "
				 . "LEFT JOIN CH_NAME N ON R.ID = N.ID "
				 . "$sqlparam) as Tmp",
			   "select S.NAME, S.ID, S.ION, R.FORMULA, R.EXACT_MASS "
				 . "from SPECTRUM S left join RECORD R on S.ID = R.ID "
			     . "$sqlparam");
	return @sqlstr[$sqltype];
}


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
