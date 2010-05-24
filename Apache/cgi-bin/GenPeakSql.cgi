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
# [Admin Tool] PEAK.sqlファイル生成
#
# ver 3.0.6  2010.05.24
#
#-------------------------------------------------------------------------------
use CGI;

my $query = new CGI;
my $src_dir = $query->param('src_dir');
my $out_dir = $query->param('out_dir');
my $db_name = $query->param('db');
my $fname = $query->param('fname');
my @fname_list = split(',' , $fname);

my %PrecType = ('[M+H]+', 1, '[M+2H]++', 2, '[M-H]-', -1, '[M-2H]--', -2);
my %Mode = ('POSITIVE', 1, 'NEGATIVE', -1);

open(OUT, ">$out_dir/$db_name"."_PEAK.sql");
print OUT "START TRANSACTION;\n";
foreach my $name ( @fname_list ) {
	my $file_path = "$src_dir/$name";
	open(IN, "<$file_path");
	while ( <IN> ) {
		s/\r?\n?//g;
		if ( /^ACCESSION: / ) {
			s/^[^:]*: //;
			$Acc = $_;
			$ionP = 0;
			$ionM = 0;
			$precursor = "NULL";
		}
		elsif ( /^RECORD_TITLE: / ) {
			s/^[^:]*: //;
			$Title = $_;
			$Title =~ s/'/''/g;
		}
		elsif ( /^MS\$FOCUSED_ION: / ) {
			my($item, $val) = ($_ =~ /^[^:]*: ([^\s]*) (.*)/);
			if ( $item eq 'PRECURSOR_TYPE' ) {
				if ( $PrecType{$val} ) { $ionP = $PrecType{$val} };
			}
			elsif ( $item eq 'PRECURSOR_M/Z' ) {
				$pos = rindex( $val, "/" ); 
				if ( $pos >= 0 ) {
					$val = substr($val, $pos + 1);
				}
				$val =~ s/^\s+//;
				$val =~ s/\s+$//;
				# simple numeric check
				if ( length($val) > 0 && !($val =~ /[^-,^.,^0-9]/) ) {
					$precursor = $val;
				}
			}
		}
		elsif ( /^AC\$ANALYTICAL_CONDITION: / ) {
			my($item, $val) = ($_ =~ /^[^:]*: ([^\s]*) (.*)/);
			if ( $item eq 'MODE' ) {
				if ( $Mode{$val} ) { $ionM = $Mode{$val} };
			}
		}
		elsif ( /^PK\$PEAK: / ) {
			while ( <IN> ) {
				s/\r?\n?//g;
				last if ( $_ eq '//' );
				s/^ *//;
				(my $mz, my $val, my $rval) = split;
				print OUT "INSERT PEAK VALUES ('$Acc', $mz, $val, $rval);\n";
			}
			
			if ( $ionP == 0 ) { $ion = $ionM; } else { $ion = $ionP; }
			print OUT "INSERT SPECTRUM(ID, NAME, ION, PRECURSOR_MZ) "
							. "VALUES('$Acc', '$Title', $ion, $precursor);\n";
		}
	}
	close(IN);
}
print OUT "COMMIT;\n";
close(OUT);
print "Content-Type: text/plain\n\n";
print "OK\n";
exit(0);
