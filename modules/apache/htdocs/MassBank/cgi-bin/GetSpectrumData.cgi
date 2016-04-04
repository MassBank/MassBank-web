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
# スペクトルのグループ情報取得
#
# ver 3.0.4  2011.07.12
#
#-------------------------------------------------------------------------------
use CGI;
use DBI;

my $query = new CGI;
my $db_name = $query->param('dsn');
my $id = $query->param('id');
my $ion = $query->param('ion');
my $relation = $query->param('relation');
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

# get child spectrum info
if ( $relation ne 'true' ) {
	&getChildInfo($id);
}
# get child spectrum info & get relation parent spectrum info
else {
	my @parent_info = &MySql("SELECT s.NAME, r.INSTRUMENT_NO FROM SPECTRUM s LEFT JOIN RECORD r ON s.ID=r.ID WHERE s.ID='$id'");
	if ( $parent_info[0][0] ne '' ) {
		my $title = $parent_info[0][0];
		$title =~ s|^([^;]*; [^;]*;) .*|$1|;
		$title =~ s|'|\\'|;
		my $ionStr = "";
		$ionStr = " and s.ION > 0" if $ion > 0;
		$ionStr = " and s.ION < 0" if $ion < 0;
		my $inst = $parent_info[0][1];
		my @child_ids = &MySql("SELECT s.ID FROM SPECTRUM s LEFT JOIN RECORD r ON s.ID=r.ID WHERE INSTR(s.NAME, '$title')=1$ionStr and r.INSTRUMENT_NO='$inst' ORDER BY 1");
		for( $i=0; $i<=$#child_ids; $i++ ) {
			$child_id = $child_ids[$i][0];
			if ( $child_id ne '' ) {
				&getChildInfo($child_id);
			}
		}
	}
	else {
		&getChildInfo($id);
	}
}
$dbh->disconnect;
exit(0);

sub getChildInfo() {
	local($key_id) = @_;
	my(@ans, $num, $rec, $name, $precursor);
	
	@ans = &MySql("select MZ, RELATIVE from PEAK where ID='$key_id' order by MZ");
	$num = @ans;
	if ( $num == 0 ) {
		print "0\t0\t\t";
	}
	else {
		foreach $rec ( @ans ) {
			print join("\t", @$rec), "\t\t"; 
		}
	}
	
	# follow record info
	print "::";

	@ans = &MySql("select NAME, PRECURSOR_MZ from SPECTRUM where ID = '$key_id'");
	$name = $ans[0][0];
	if ( $name ne '' ) {
		print "\tname=$name\t";
	}
	$precursor = $ans[0][1];
	if ( $precursor ne '' ) {
		print "\tprecursor=$precursor\t";
	}
	print "\tid=$key_id\t\n";
}

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
