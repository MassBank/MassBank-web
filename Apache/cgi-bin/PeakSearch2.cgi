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
# Peak Search 検索処理
#
# ver 3.0.2  2010.11.16
#
#-------------------------------------------------------------------------------
use DBI;
use CGI;

print "Content-Type: text/plain\n\n";

$query = new CGI;
@params = $query->param();
foreach $key ( @params ) {
	$val = $query->param($key);
	if ( $key eq 'id' ) {
		push(@id, $val);
	}
	elsif ( $key eq 'type' ) {
		$type = $val;
	}
	elsif ( $key eq 'dsn' ) {
		$db_name = $val;
	}
	elsif ( $key eq 'ion' ) {
		$where_ion = "";
		if ( $val eq '1' ) {
			$where_ion = " and S.ION > 0";
		}
		elsif ( $val eq '-1' ) {
			$where_ion = " and S.ION < 0";
		}
	}
	elsif ( $key eq 'inst' ) {
		@inst = $query->param($key);
	}
	elsif ( $key ne 'check' ) {
		$Arg{$key} = $val;
	}
}
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
$dbh = DBI->connect($DB, $User, $PassWord) || die "connect error \n";

$heap_tbl_name = 'PEAK_HEAP';
$sql = "show tables like '$heap_tbl_name'";
@ans = &MySql($sql);
$tbl_name = "PEAK";
$ans_tbl_name = $ans[0][0];
if ( lc($ans_tbl_name) eq lc($heap_tbl_name) ) {
	$sql = "select count(*) from $heap_tbl_name";
	@ans = &MySql($sql);
	$rec_cnt = $ans[0][0];
	if ( $rec_cnt > 0 ) {
		$tbl_name = $heap_tbl_name;
	}
}

if ($#inst < 0) {
	$dbh->disconnect;
	exit(0);
}
$isAll = 1;
foreach $inst (@inst) {
	if ( $inst ne 'all' ) {
		$isAll = 0;
	}
	else {
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
	$where_inst = " and R.INSTRUMENT_NO in($in)";
}

%res = ();
for ( $i = 0; $i < $Arg{'num'}; $i ++ ) {
	$mz = $Arg{"mz$i"} + 0;
	last if ( $mz eq 0 );
	$op = $Arg{"op$i"};
	$tol = $Arg{"tol$i"} + 0;
	$tol = - $tol if ( $tol < 0 );
	$min = $mz - $tol - 0.00001;
	$max = $mz + $tol + 0.00001;
	$val = $Arg{"int$i"} + 0;
	if ( $type eq 'diff' ) {
		$sql = "select t1.ID from $tbl_name as t1 left join $tbl_name as t2 on t1.ID = t2.ID "
			 . "where (t1.MZ between t2.MZ + $min and t2.MZ + $max) and t1.RELATIVE > $val and t2.RELATIVE > $val";
	}
	else {
		$sql = "select ID from PEAK where (MZ between $min and $max) and RELATIVE > $val";
	}
	@ans = &MySql($sql);
	if ( $i != 0 ) {
		if ( $op eq 'and' ) { %new = (); }
		for $item ( @ans ) {
			$id = $$item[0];
			if ( $op eq 'and' ) {
				$new{$id} = '' if ( defined($res{$id}) );
			}
			elsif ( $op eq 'or' ) {
				$new{$id} = '';
			}
		}
	}
	else {
		%new = ();
		for $item ( @ans ) {
			$id = $$item[0];
			$new{$id} = '';
		}
	}
	%res = %new;
}
@id = sort keys(%res);
$cnt = @id;
if ( $cnt == 0 ) {
	$dbh->disconnect;
	exit(0);
}
%idlist = ();
%ionlist = ();
foreach $id ( @id ) {
	$sql = "select S.NAME, S.ID, S.ION, R.FORMULA, R.EXACT_MASS from SPECTRUM S, RECORD R "
		 . "where S.ID = '$id' and S.ID = R.ID"
		 . "$where_ion"
		 . "$where_inst";
	@rec = &MySql($sql);
	foreach $rec ( @rec ) {
		print join("\t", @$rec), "\n";
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
