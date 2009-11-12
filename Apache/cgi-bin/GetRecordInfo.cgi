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
# ver 1.0.0  2009.07.10
#
#-------------------------------------------------------------------------------
use DBI;
use CGI;

my $query = new CGI;
my $ids = $query->param('ids');
my $db_name = $query->param('dsn');
if ( $db_name eq '' ) {
	$db_name = "MassBank";
}
my $mode = $query->param('mode');
print "Content-Type: text/plain\n\n";
my @id_list = split(',', $ids);
foreach my $id ( @id_list ){
	my $path = "../DB/annotation/$db_name/$id.txt";
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
			print $line;
		}
		close(F);
	}
}
exit(0);
