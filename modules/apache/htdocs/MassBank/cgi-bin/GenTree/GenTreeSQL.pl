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
# [Admin Tool] TREE.sqlファイル生成 - SQL出力処理
#
# ver 3.0.2  2009.06.19
#
#-------------------------------------------------------------------------------

$Table = 'TREE';
$Root = shift(@ARGV);
$File = shift(@ARGV);

%Total = ();
@Name = ();
open(IN, $File);
while ( <IN> ) {
	s/\r?\n?//g;
	@path = split("\t", $_);
	pop(@path);
	unshift(@path, $Root);
	foreach $i ( 0 .. $#path ) {
		$node = join("\t", @path[0 .. $i]);
		$parent = join("\t", @path[0 .. ($i - 1)]);
		if ( $Name[$i] ne $node ) {
			$Total{$parent} ++;
			$Name[$i] = $node;
		}
	}
}
close(F);

%ID = ('', 0);
%Count = ();
@Name = ();
$ID = 0;
open(IN, $File);
print "TRUNCATE TABLE $Table;\n";
print "START TRANSACTION;\n";
while ( <IN> ) {
	s/\r?\n?//g;
	@path = split("\t", $_);
	$acc = pop(@path);
	$leaf = pop(@path);
	unshift(@path, $Root);
	foreach $i ( 0 .. $#path ) {
		$node = join("\t", @path[0 .. $i]);
		$parent = join("\t", @path[0 .. ($i - 1)]);
		if ( $Name[$i] ne $node ) {
			$Count{$parent} ++;
			$pos = $Count{$parent};
			$ID ++;
			$ID{$node} = $ID;
			&Print($ID, $ID{$parent}, $pos, $Total{$node}, $path[$i], 'NULL');
			$Name[$i] = $node;
		}
	}
	$parent = join("\t", @path);
	$ID ++;
	$Count{$parent} ++;
	$pos = $Count{$parent};
	&Print($ID, $ID{$parent}, $pos, -1, $leaf, $acc);
}
print "COMMIT;\n";
close(F);

sub Print() { local($id, $parent, $pos, $num, $name, $acc) = @_;
	$acc = "'$acc'" if ( $acc ne 'NULL' );
	$name =~ s/'/''/g;
	print "INSERT $Table VALUES ($id, $parent, $pos, $num, '$name', $acc);\n";
}
