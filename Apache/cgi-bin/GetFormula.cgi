#! /usr/bin/perl
#-------------------------------------------------------------------------------
#
# Copyright (C) 2009 JST-BIRD MassBank
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
# アノテートされた分子式の取得
#
# ver 1.0.1  2012.11.22
#
#-------------------------------------------------------------------------------
use DBI;
use CGI;

my $query = new CGI;
my $ids = $query->param('id');
my $db_name = $query->param('dsn');
if ( $db_name eq '' ) {
	$db_name = "MassBank";
}

print "Content-Type: text/plain\n\n";

my $SQLDB = "DBI:mysql:$db_name:$host_name";
my $User = 'bird';
my $PassWord = 'bird2006';
my $dbh  = DBI->connect($SQLDB, $User, $PassWord) || exit(0);

@id_list = split( ',', $ids );
foreach $id ( @id_list ) {
	$in .= "'$id',";
}
chop $in;

my $sql = "SHOW TABLES LIKE 'PRODUCT_ION'";
my @ans = &MySql( $sql );
$cnt = @ans;
if ( 0 < $cnt ) {
	$sql = "select ID, FORMULA from PRODUCT_ION where ID in($in)";
	@ans = &MySql( $sql );
	foreach my $rec ( @ans ) {
		$id = @$rec[0];
		$formula = @$rec[1];
		$mass = MassCalc($formula);
		print "$id\t$mass\t$formula\t\n";
	}
}

$dbh->disconnect;
exit(0);


sub MySql() {
	my $sql = $_[0];
	my @ans = ();
	my @ret = ();
	my $sth = $dbh->prepare($sql);
	$sth->execute || exit(0);
	my $n = $sth->rows;
	for ( my $i = 0; $i < $n; $i ++ ) {
		@ans = $sth->fetchrow_array;
		push(@ret, [@ans]);
	}
	$sth->finish;
	return @ret;
}

sub MassCalc() {
	%mass_list = ("H"  => 1.007825032,  "Be" => 9.0121821,    "B"  => 11.0093055,  "C"  => 12.00000000,
				  "N"  => 14.003074005, "O"  => 15.994914622, "F"  => 18.99840320, "Na" => 22.98976967,
				  "Al" => 26.98153844,  "Si" => 27.976926533, "P"  => 30.97376151, "S"  => 31.97207069,
				  "Cl" => 34.96885271,  "V"  => 50.9439637,   "Cr" =>  51.9405119, "Fe" => 55.9349421,
				  "Ni" => 57.9353479,   "Co" => 58.9332001,   "Cu" => 62.9296011,  "Zn" => 63.9291466,
				  "Ge" => 73.9211782,   "Br" => 78.9183376,   "Mo" => 97.9054078,  "Pd" => 105.903483,
				  "I"  => 126.904468,   "Sn" => 119.9021966,  "Pt" => 194.964774,  "Hg" => 201.970626 );

	my $formula = shift;
	my @atom_list = &GetAtomList($formula);
	my $mass = 0;
	foreach my $atom_num (@atom_list) {
		my @item = split(':', $atom_num);
		my $atom = $item[0];
		my $num  = $item[1];
		$mass += $mass_list{$atom} * $num;
	}
	$mass = int(($mass * 1000) + 0.5) / 1000;
	return $mass;
}

sub GetAtomList() {
	my $formula = shift;
	my @atom_list = ();
	my $start_pos = 0;
	my $end_pos = length($formula);

	for ( my $pos = $start_pos; $pos <= $end_pos; $pos++ ) {
		my $chr = "";
		if ( $pos < $end_pos ) {
			$chr = substr( $formula, $pos, 1 );
		}
		if ( $pos == $end_pos || ($pos > 0 && $chr =~ /[\D]/ && $chr eq uc($chr)) ) {
			my $item = substr( $formula, $start_pos, $pos - $start_pos );
			$pos1 = length($item);
			$isFound = false;
			for ( my $i = 1; $i < length($item); $i++ ) {
				$chr = substr( $item, $i, 1 );
				if ( $chr =~ /[\d]/ ) {
					$pos1 = $i;
					$isFound = true;
					last;
				}
			}

			my $atom = substr($item, 0, $pos1);
			my $num = 1;
			if ( $isFound eq true ) {
				$num = substr($item, $pos1);
			}
			if ( $atom_list{$atom} ne '' ) {
				$num = $num + $atom_list{$atom};
			}
			push(@atom_list, "$atom:$num");
			$start_pos = $pos;
		}
	}
	return @atom_list;
}
