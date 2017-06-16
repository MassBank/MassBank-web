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
# サーバ状態チェックプログラム
#
# ver 1.0.0 2009.01.26
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

$SQLDB = "DBI:mysql:$db_name:$host_name";
$User = 'bird';
$PassWord = 'bird2006';
unless ( $dbh = DBI->connect($SQLDB, $User, $PassWord) ) {
	print "NG";
	exit(0);
}
$sql = "select count(*) from SPECTRUM";
$sth = $dbh->prepare($sql);
unless( $sth->execute ) {
	print "NG";
	exit(0);
}
$sth->finish;
$dbh->disconnect;
print "OK";
exit(0);
