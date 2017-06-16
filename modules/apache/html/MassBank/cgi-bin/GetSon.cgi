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
# Browse Page ツリー情報取得
#
# ver 3.0.1  2008.12.05
#
#-------------------------------------------------------------------------------
print "Content-Type: text/plain\n\n";

use CGI;

$query = new CGI;
$id = $query->param('id');
$db_name = $query->param('dsn');
if ( $db_name eq '' ) {
	$db_name = "MassBank";
}
open(F, "DB_HOST_NAME");
while ( <F> ) {
	chomp;
	$host_name .= $_;
}
use DBI;

$SQLDB = "DBI:mysql:$db_name:$host_name";
$User = 'bird';
$PassWord = 'bird2006';
$dbh  = DBI->connect($SQLDB, $User, $PassWord) || exit(0);
@ans = &MySql("select NO, INFO, SON, ID from TREE where PARENT = $id");
$dbh->disconnect;

foreach $rec ( @ans ) {
	$acc = @$rec[3];
	if ( $acc eq '' ) {
		$name = "";
	}
	else {
		@ans1 = &MySql("select NAME from SPECTRUM where ID = '$acc'");
		$name = $ans1[0][0];
	}
	print join("\t", @$rec);
	print "\t$name\n";
}

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
