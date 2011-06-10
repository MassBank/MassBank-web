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
# レコード情報取得
#
# ver 1.0.1  2011.06.10
#
#-------------------------------------------------------------------------------
use DBI;
use CGI;

my $query = new CGI;
my $db_name = $query->param('dsn');
if ( $db_name eq '' ) {
	$db_name = "MassBank";
}
my $ids = $query->param('ids');
my @id_list = split(',', $ids);

# $mode：ver
#   [ID]\t[RECORD FORMAT VERSION]
# $mode：peak
#   [ACCESSION INFORMATION] & [PEAK INFORMATION]
# $mode：none
#   [ALL RECORD INFORMATION]
my $mode = $query->param('mode');
if ( $mode eq 'ver' ) {
	if ( $#id_list < 0 ) {
		if ( -d "../DB/annotation/$db_name/" ) {
			my @recfiles = glob "../DB/annotation/$db_name/*.txt";
			foreach my $rec_file ( @recfiles ) {
				(my $acc = $rec_file) =~ s/.*\/(........)\.txt$/$1/;
				push(@id_list, $acc);
			}
		}
	}
}

print "Content-Type: text/plain\n\n";
foreach my $id ( @id_list ){
	my $path = "../DB/annotation/$db_name/$id.txt";
	my $isVer = 0;
	if ( -f $path ) {
		open(F, $path);
		my $isPeakLine = false;
		while ( <F> ) {
			my $line = $_;
			if ( $mode eq 'peak' ) {
				if ( index($line, 'PK$PEAK') >= 0 ) {
					$isPeakLine = true;
				}
				elsif ( index($line, 'ACCESSION') == -1 ) {
					if ( $isPeakLine eq false ) {
						next;
					}
				}
			}
			elsif ( $mode eq 'ver' ) {
				if ( index($line, 'LICENSE') != -1 ) {
					print "$id\t2\n";
					$isVer = 1;
					last;
				}
				next;
			}
			print $line;
		}
		close(F);
	}
	if ( $mode eq 'ver' && $isVer == 0 ) {
		print "$id\t1\n";
	}
}
exit(0);
