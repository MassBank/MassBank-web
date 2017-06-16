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
# Molfile情報取得
#
# ver 3.0.4  2009.08.20
#
#-------------------------------------------------------------------------------
use CGI;
use DBI;

print "Content-Type: text/plain\n\n";

#リクエストパラメータ補足
# query : 検索値
# qtype : 検索条件種別
#         i … ID検索
#         n … 化合物名検索
#         s … 部分一致検索
# obype : 返却値種別
#         i … ID返却 
#         n … 化合物名返却
#         m … Molfile内容返却
#         g … GifファイルID及びファイル有無返却
#         q … Gifファイル名及びMolfile名返却（存在しない場合はN/A）
# dsn   : DB名

$query = new CGI;
$Query = $query->param('query');
$Qtype = $query->param('qtype');
$Otype = $query->param('otype');
$DbName = $query->param('dsn');
open(F, "DB_HOST_NAME");
while ( <F> ) {
	chomp;
	$Host .= $_;
}
$DB = "DBI:mysql:$DbName:$Host";
$User = 'bird';
$PassWord = 'bird2006';

$MolDir = "../DB/molfile/$DbName";
$GifDir = "../DB/gif/$DbName";

$Query =~ s/"/""/g;
if ( $Qtype eq 'i' ) { $cond = 'FILE="'.$Query.'"'; }
elsif ( $Qtype eq 'n' ) { $cond = 'NAME="'.$Query.'"'; }
elsif (  $Qtype eq 's' ) { $cond = 'UPPER(NAME) LIKE UPPER("%'.$Query.'%")'; }
else {  &errorexit; }

if ( $Otype =~ /i/ ) { $OutI = 1; } else { $OutI = 0; }
if ( $Otype =~ /n/ ) { $OutN = 1; } else { $OutN = 0; }
if ( $Otype =~ /m/ ) { $OutM = 1; } else { $OutM = 0; }
if ( $Otype =~ /g/ ) { $OutG = 1; } else { $OutG = 0; }
if ( $Otype =~ /q/ ) { $OutQ = 1; } else { $OutQ = 0; }

$dbh = DBI->connect($DB, $User, $PassWord) || &errorexit;

@ans = &MySql("SELECT * FROM MOLFILE WHERE $cond");

if ( $OutQ == 1 ) {
	$gifFileName = "N/A";
	$molFileName = "N/A";
	foreach $x ( @ans ) {
		($id, $name) = @$x;
		
		if ( $gifFileName eq "N/A" ) {
			if ( -f "$GifDir/$id.gif" ) {
				$gifFileName = "$id.gif";
			}
		}
		if ( $molFileName eq "N/A" ) {	
			if ( -f "$MolDir/$id.mol" ) {
				$molFileName = "$id.mol";
			}
		}
		if ( $gifFileName ne "N/A" && $molFileName ne "N/A" ) {
			last;
		}
	}
	print "$gifFileName\t$molFileName";
}
else {
	print scalar(@ans), "\n";
	foreach $x ( @ans ) {
		($id, $name) = @$x;
		print "$id\n" if ( $OutI == 1 );
		print "$name\n" if ( $OutN == 1 );
		if ( $OutG == 1 ) {
			if ( -f "$GifDir/$id.gif" ) {
				 print "$id\ttrue\n"; 
			} else {
				 print "$id\tfalse\n"; 
			}
		}
		next if ( $OutM == 0 );
		@mol = ();
		open(F, "$MolDir/$id.mol");
		while ( <F> ) { push(@mol, $_); }
		close(F);
		print scalar(@mol), "\n";
		foreach $x ( @mol ) { print $x; }
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
