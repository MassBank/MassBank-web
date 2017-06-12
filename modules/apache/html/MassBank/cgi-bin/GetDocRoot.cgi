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
# Apacheドキュメントルート取得
#
# ver 1.0.0  2010.10.19
#
#-------------------------------------------------------------------------------

print "Content-Type: text/plain;charset=UTF-8\n\n";

my $docRoot = "$ENV{DOCUMENT_ROOT}";
my $slashIndex = rindex($docRoot, "/");
my $backSlashIndex = rindex($docRoot, "\\");
my $lastIndex = length($docRoot)-1;

if ( $slashIndex != -1 ) {
	if ( $slashIndex != $lastIndex ) {
		$docRoot = "$docRoot/";
	}	
}
elsif ( $backSlashIndex != -1 ) {
	if ( $backSlashIndex != $lastIndex ) {
		$docRoot = "$docRoot\\";
	}	
}

print "$docRoot\n";

exit(0);
