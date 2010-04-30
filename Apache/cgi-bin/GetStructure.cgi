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
# 構造情報一括取得
#
# ver 1.0.0  2010.04.28
#
#-------------------------------------------------------------------------------
use CGI;
use DBI;

print "Content-Type: text/plain\n\n";

my $query = new CGI;
my $gifType = $query->param('gtype');
my $names = $query->param('names');
my $DbName = $query->param('dsn');
open(F, "DB_HOST_NAME");
while ( <F> ) {
	chomp;
	$Host .= $_;
}
my $DB = "DBI:mysql:$DbName:$Host";
my $User = 'bird';
my $PassWord = 'bird2006';
my $MolDir = "../DB/molfile/$DbName";
my $GifDir = "../DB/gif/$DbName";
if ( $gifType eq 'gif_small' || $gifType eq 'gif_big' ) {
	$GifDir = "../DB/$gifType/$DbName";
}
my $dbh = DBI->connect($DB, $User, $PassWord) || &errorexit;
my @name_list = split( '@', $names );
my $in = "NAME in(";
foreach my $name ( @name_list ) {
	$name =~ s/\'/\'\'/g;
	$in .= "'$name',";
}
chop $in;
$in .= ")";
@ans = &MySql("select FILE, NAME from MOLFILE where $in");
foreach $x ( @ans ) {
	($fname, $name) = @$x;
	print "---NAME:$name\n";
	if ( -f "$GifDir/$fname.gif" ) {
		print "---GIF:$fname.gif\n";
	}
	else {
		open(F, "$MolDir/$fname.mol");
		@mol = ();
		while ( <F> ) {
			push(@mol, $_);
		}
		close(F);
		foreach $x ( @mol ) {
			print "$x";
		}
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
