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
# [Admin Tool] 分析機器情報の追加・削除処理
#
# ver 1.0.2  2008.12.05
#
#-------------------------------------------------------------------------------
use CGI;
use DBI;

$query = new CGI;
$act = $query->param('act');
$inst_no = $query->param('inst_no');
$inst_type = $query->param('inst_type');
$inst_name = $query->param('inst_name');
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
if ( $act eq 'add') { 
	$sql = "insert INSTRUMENT(INSTRUMENT_NO, INSTRUMENT_TYPE, INSTRUMENT_NAME) "
		 . "values($inst_no, '$inst_type', '$inst_name')";
}
elsif ( $act eq 'del') { 
	$sql = "delete from INSTRUMENT where INSTRUMENT_NO=$inst_no";
}
else {
	exit(0);
}

$SQLDB = "DBI:mysql:$db_name:$host_name";
$User = 'bird';
$PassWord = 'bird2006';
$dbh  = DBI->connect($SQLDB, $User, $PassWord) || exit(0);
$sth = $dbh->prepare($sql);
unless( $sth->execute ) {
	exit(0);
}
$sth->finish;
$dbh->disconnect;
print "ok";
exit(0);
1;
