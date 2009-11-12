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
# 統計表示用のレコードカウント取得
#
# ver 3.0.1  2008.12.05
#
#-------------------------------------------------------------------------------
use DBI;
use CGI;

my $query = new CGI;
my $db_name = $query->param('dsn');
	if ( $db_name eq '' ) {
		$db_name = "MassBank";
	}
open(F, "DB_HOST_NAME");
my $host_name = "";
while ( <F> ) {
	chomp;
	$host_name .= $_;
}

my $DB = "DBI:mysql:$db_name:$host_name";
my $User = 'bird';
my $PassWord = 'bird2006';
my $dbh  = DBI->connect($DB, $User, $PassWord) || die "connect error \n";

print "Content-Type: text/plain\n\n";

my @params = (
		   'Positive', 'Negative',
		   'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
		   'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1-9', 'Others');

# Total ===================================================================
outCount( '', 'site', 1, '' );

# Instrument Type =========================================================
my $head = "";
my $val = "";
my $sqlparam = "";
my $sqltype = 0;
my @ans = MySql("select distinct INSTRUMENT_NO, INSTRUMENT_TYPE from INSTRUMENT order by INSTRUMENT_NO");
foreach my $item ( @ans ) {
	$head = "INSTRUMENT:";
	$val = "$$item[1]";
	$sqlparam = "where R.INSTRUMENT_NO = '$$item[0]' ";
	outCount( $head, $val, 0, $sqlparam );
}

# Compound Name ===========================================================
foreach my $val ( @params ) {
	if ( $val eq 'Positive' || $val eq 'Negative' ) {
		$head = "ION:";
		$sqltype = 0;
		if ( $val eq 'Positive' ) {
			$sqlparam = "where S.ION > 0";
		}
		elsif ( $val eq 'Negative' ) {
			$sqlparam = "where S.ION < 0";
		}
	}
	else {
		$head = "COMPOUND:";
		$sqltype = 1;
		if ( $val eq '1-9' ) {
			$sqlparam = "where NAME regexp '^[0-9]' ";
		}
		elsif ( $val eq 'Others' ) {
			$sqlparam = "where NAME regexp '^[^a-z0-9]' ";
		}
		else {
			$sqlparam = "where NAME like '$val\%' ";
		}
	}
	outCount( $head, $val, $sqltype, $sqlparam );
}
$dbh->disconnect;
exit(0);

sub outCount() {
	my $head = $_[0];
	my $val = $_[1];
	my $sqltype = $_[2];
	my $sqlparam = $_[3];
	my $sql = getSql( $sqltype, $sqlparam );
	my @ans = &MySql($sql);
	my $cnt = @ans;
	if ( $cnt == 0 ) {
		$dbh->disconnect;
		exit(0);
	}
	foreach my $item ( @ans ) {
		my $count = $$item[0];
		print "$head$val\t$count\n";
	}
}

sub getSql() {
	my $sqltype = $_[0];
	my $sqlparam = $_[1];
	my @sqlstr = ("select count(*) from "
				 . "( select distinct S.ID from SPECTRUM S "
				 . "LEFT JOIN RECORD R ON S.ID = R.ID "
				 . "LEFT JOIN CH_NAME N ON R.ID = N.ID "
				 . "$sqlparam) as Tmptbl",
				  "select count(*) from SPECTRUM "
			     . "$sqlparam");
	return $sqlstr[$sqltype];
}

sub MySql() {
	my $sql = $_[0];
	my @ans = ();
	my @ret = ();
	my $sth = $dbh->prepare($sql);
	unless( $sth->execute ) {
		return undef;
	}
	my $n = $sth->rows;
	for ( my $i = 0; $i < $n; $i++ ) { my @ans = $sth->fetchrow_array; push(@ret, [@ans]); }
	$sth->finish;
	return @ret;
}
1;
