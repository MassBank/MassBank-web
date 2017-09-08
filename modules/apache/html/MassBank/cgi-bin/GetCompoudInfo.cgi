#! /usr/bin/perl
#-------------------------------------------------------------------------------
#
# Copyright (C) 2010 JST-BIRD MassBank
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
# 化合物情報取得
#
# ver 1.0.0  2010.10.29
#
#-------------------------------------------------------------------------------
use CGI;
use DBI;

print "Content-Type: text/plain\n\n";

my $query = new CGI;
my $name = $query->param('name');
my $DbName = $query->param('dsn');
my $id = $query->param('id');
open(F, "DB_HOST_NAME");
while ( <F> ) {
	chomp;
	$Host .= $_;
}
my $DB = "DBI:mysql:$DbName:$Host";
my $User = 'bird';
my $PassWord = 'bird2006';
my $GifDir = "../DB/gif/$DbName";
my $GifSmallDir = "../DB/gif_small/$DbName";
my $GifLargeDir = "../DB/gif_large/$DbName";

my $dbh = DBI->connect($DB, $User, $PassWord) || &errorexit;


if ( $id ne '' ) {
	@ans = &MySql("select FORMULA, EXACT_MASS from RECORD where id = '$id'");
	my $formula = $ans[0][0];
	my $mass = $ans[0][1];
	print "---FORMULA:$formula\n";
	print "---EXACT_MASS:$mass\n";
}

$name =~ s/\'/\'\'/g;
@ans = &MySql("select FILE, NAME from MOLFILE where NAME='$name'");
print STDERR "select FILE, NAME from MOLFILE where NAME='$name'";
foreach $x ( @ans ) {
	($fname, $name) = @$x;
	my $allGif = 1;
	print "---NAME:$name\n";
	if ( -f "$GifDir/$fname.gif" ) {
		print "---GIF:$fname.gif\n";
	}
	if ( -f "$GifSmallDir/$fname.gif" ) {
		print "---GIF_SMALL:$fname.gif\n";
	}
}
$dbh->disconnect;
exit(0);

sub errorexit() {
	print "-1\n";
	exit(0);
}

sub MySql() { local($sql) = @_;
	local($sth, $n, $i, @ans, @ret);
	@ret = ();
	$sth = $dbh->prepare($sql) || &errorexit;
	$sth->execute || &errorexit;
	$n = $sth->rows;
	for ( $i = 0; $i < $n; $i ++ ) {
	@ans = $sth->fetchrow_array;
	push(@ret, [@ans]);
	}
	$sth->finish || &errorexit;
	return @ret;
}

1;
