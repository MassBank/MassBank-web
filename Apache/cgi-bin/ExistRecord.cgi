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
# レコード存在チェック
#
# ver 3.0.1  2008.12.05
#
#-------------------------------------------------------------------------------
use DBI;
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

print "Content-Type: text/plain\n\n";

$DB = "DBI:mysql:$db_name:$host_name";
$User = 'bird';
$PassWord = 'bird2006';
$dbh  = DBI->connect($DB, $User, $PassWord) || die "connect error \n";
$sql = "select ID from SPECTRUM where ID='$id'";
$sth = $dbh->prepare($sql);
unless( $sth->execute ) {
	exit(0);
}
$rows = $sth->rows;
if ( $rows >= 1 ) {
	print $id;
}
$sth->finish;
$dbh->disconnect;
exit(0);
