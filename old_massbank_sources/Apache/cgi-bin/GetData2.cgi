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
# 複数スペクトル情報の取得
#
# ver 3.0.1  2008.12.05
#
#-------------------------------------------------------------------------------
use CGI;
use DBI;

$query = new CGI;
$ids = $query->param('id');
$db_name = $query->param('dsn');
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

@id_list = split( ',', $ids );
$diff = $query->param('diff');
$mzs = $query->param('mz');
if ( $mzs ne '' ) {
	$tols = $query->param('tol');
	$ints = $query->param('int');
	@mz_list = split( ',', $mzs );
	@tol_list = split( ',', $tols );
	@int_list = split( ',', $ints );
}

foreach $id ( @id_list ) {
	@ans = &MySql("select MZ,RELATIVE from PEAK where ID='$id' order by MZ");
	$num = @ans;
	if ( $num == 0 ) {
		print "0\t0\n";
		next;
	}
	foreach $rec ( @ans ) {
		print join("\t", @$rec), "\t\t"; 
	}
	
	@ans = &MySql("select PRECURSOR_MZ from SPECTRUM where ID = '$id'");
	$precursor = $ans[0][0];
	if ( $precursor ne '' ) {
		print "precursor=$precursor\t";
	}
	
	if ( $mzs ne '' ) {
		print "hit=";
		if ( $diff eq 'no' ) {
			$where = ' and (';
			for ( $i = 0; $i <= $#mz_list; $i++ ) {
				$mz1 = $mz_list[$i] - $tol_list[$i] - 0.00001;
				$mz2 = $mz_list[$i] + $tol_list[$i] + 0.00001;
				$where .= "(MZ between $mz1 and $mz2 and RELATIVE > $int_list[$i])";
				if ( $i < $#mz_list ) {
					$where .= " or ";
				}
			}
			$where .= ')';
			$sql = "select MZ from PEAK where ID='$id'" . $where;
			@ans = &MySql( $sql );
			foreach $rec ( @ans ) {
				print "@$rec[0]\t";
			}
		}
		else {
			for ( $i = 0; $i <= $#mz_list; $i++ ) {
				$mz = $mz_list[$i];
				$mz1 = $mz - $tol_list[$i] - 0.00001;
				$mz2 = $mz + $tol_list[$i] + 0.00001;
				$sql = "select t2.MZ, t1.MZ from PEAK as t1 left join PEAK as t2"
					  ." on t1.ID = t2.ID where t1.ID = '$id'";
				$where = " and t1.MZ between t2.MZ + $mz1 and t2.MZ + $mz2 "
					   . "and t1.RELATIVE > $int_list[$i] and t2.RELATIVE > $int_list[$i]";
				@ans = &MySql( $sql . $where );
				$rec_cnt = @ans;
				if ( $rec_cnt == 0 ) {
					print "$mz,,\t";
				}
				else {
					foreach $rec ( @ans ) {
						print "$mz,";
						print join( ",", @$rec ), "\t"; 
					}
				}
			}
		}
	}
	print "\n";
}
$dbh->disconnect;

exit(0);

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
