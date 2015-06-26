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
# Molfileテーブル登録処理
#
# ver 1.0.2  2008.12.05
#
#-------------------------------------------------------------------------------
use DBI;
use File::Basename;
use File::Spec;
scalar(@ARGV) >= 2 || die "perl InsertData.pl <dir> <prefix> <dbname>\n";

$Dir = shift(@ARGV);
$List = "list.tsv";
$Prefix = shift(@ARGV);
$Path = dirname($0);
$DbTop = File::Spec->rel2abs( dirname($0) . "/../../" );
open(F, "$DbTop/cgi-bin/DB_HOST_NAME");
while ( <F> ) {
	chomp;
	$Host .= $_;
}
$DbName = shift(@ARGV);
if ( $DbName eq '' ) {
	$DbName = "MassBank";
}
my $os = $^O;
print "OS    : $os\n";
print "Dir   : $Dir\n";
print "DbTop : $DbTop\n";
print "Host  : $Host\n";
print "DbName: $DbName\n";

$DbDir = "$DbTop/DB/molfile/$DbName";

length($Prefix) == 2 || die "prefix must be 2 chars : $Prefix\n";
(-d $DbDir) || die "dbdir error : $DbDir\n";

$DB = "DBI:mysql:$DbName:$Host";
$User = 'bird';
$PassWord = 'bird2006';
$dbh = DBI->connect($DB, $User, $PassWord) || die "connect error\n";

open(F, "$Dir/$List") || die "not found $Dir/$List\n";

$n = 0;
@ans = &MySql('select ifnull(max(substring(FILE,3)),0) + 1 from MOLFILE');
$nid = $ans[0][0];
while ( <F> ) {
	$n ++;
	chop;
	($name0, $mol) = split("\t", $_);
	$name = $name0;
	$name =~ s/"/""/g;
	@ans = &MySql('select FILE from MOLFILE where NAME="'.$name.'"');
	if ( scalar(@ans) > 0 ) {
		print "$n : name '$name' exists already; NOT insert the data.\n";
		next;
	}
again:
	$id = sprintf('%s%06d', $Prefix, $nid);
	@ans = &MySql('select NAME from MOLFILE where FILE="'.$id.'"');
	if ( scalar(@ans) > 0 ) {
		print "$n : FILE '$id' exists already; try the next FILE.\n";
		$nid ++;
		goto again;
	}
	$dbh->do('insert into MOLFILE values("'.$id.'","'.$name.'")');
	my $from = "\"$Dir/$Molfile/$mol\"";
	my $to = "\"$DbDir/$id.mol\"";
	if ( $os eq 'MSWin32' ) {
		$from =~ s/\//\\/g;
		$to =~ s/\//\\/g;
		system("copy $from $to");
	} else {
		system("cp $from $to");
	}
	print "$n : '$name0' $mol ==> $id.mol\n";
	$nid ++;
}
close(F);
$dbh->disconnect;

exit(0);

sub MySql() { local($sql) = @_;
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

1;
