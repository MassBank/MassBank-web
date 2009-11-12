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
# ヒープテーブル作成
#
# ver 3.0.1  2008.12.05
#
#-------------------------------------------------------------------------------
use CGI;
use DBI;

print "Content-Type: text/plain\n\n";

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

$tbl_name = 'PEAK_HEAP';
$sql = "show tables like 'PEAK_HEAP'";
@ret = mysql_query($sql);
print $ret;
if ( lc($ret[0][0]) eq 'PEAK_HEAP' ) {
	$ret_name = $ret[0][0];
	$sql = "select count(*) from $ret_name";
	@ret = mysql_query($sql);
	$cnt = $ret[0][0];
	$sql = "select count(*) from PEAK";
	@ret = mysql_query($sql);
	$cnt1 = $ret[0][0];
	print "DB=$db_name cnt=$cnt, $cnt1";
	if ( $cnt == $cnt1 ) {
		$dbh->disconnect;
		print " -- OK\n";
		exit(0);
	}
}

$sql = "DROP TABLE IF EXISTS $tbl_name";
mysql_execute($sql);
$sql = "CREATE TABLE $tbl_name(INDEX(ID),INDEX(MZ),INDEX(RELATIVE)) "
     . "TYPE=HEAP SELECT ID,MZ,RELATIVE FROM PEAK";
mysql_execute($sql);
print "\nCREATE TABLE $tbl_name OK\n";

$dbh->disconnect;

sub mysql_query() {
	local($sql) = @_;
	local($sth, $n, $i, @ans, @ret);
	@ret = ();
	$sth = $dbh->prepare($sql);
	$sth->execute;
	$n = $sth->rows;
	for ( $i = 0; $i < $n; $i ++ ) {
		@ans = $sth->fetchrow_array;
		push(@ret, [@ans]);
	}
	$sth->finish;
	return @ret;
}

sub mysql_execute() {
	local($sql) = @_;
	$sth = $dbh->prepare($sql);
	$sth->execute;
	$sth->finish;
	return;
}
