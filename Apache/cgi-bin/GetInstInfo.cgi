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
# INSTRUMENT情報とMS情報の取得
#
# ver 1.0.5  2011.07.22
#
#-------------------------------------------------------------------------------
use CGI;
use DBI;

my $query = new CGI;
my $recVersion = $query->param('ver');					# MassBank Record version
if ( !defined($recVersion) ) { $recVersion = 1; }
my $isPeakAdv = $query->param('padv');					# PeakSearchAdvanced flag
if ( !defined($isPeakAdv) ) { $isPeakAdv = 0; }
my $db_name = $query->param('dsn');
if ( $db_name eq '' ) {
	$db_name = "MassBank";
}
open(F, "DB_HOST_NAME");
my $host_name = "";
while ( <F> ) {
	chomp;
	$host_name .= $_;
}

print "Content-Type: text/plain\n\n";

my $SQLDB = "DBI:mysql:$db_name:$host_name";
my $User = 'bird';
my $PassWord = 'bird2006';
my $dbh  = DBI->connect($SQLDB, $User, $PassWord) || exit(0);

# Exist check PRE_PRO table
my $isExistPrePro = 0;
if ( $isPeakAdv ) {
	$sql = "SHOW TABLES LIKE 'PRE_PRO'";
	@ans = &MySql($sql);
	$cnt = @ans;
	if ( $cnt != 0 ) {
		$isExistPrePro = 1;
	}
}

# Get instrument information
if ( $recVersion > 1 ) {
	print "INSTRUMENT_INFORMATION\n";
}
if ( !$isPeakAdv ) {
	my $sql = "SELECT INSTRUMENT_NO, INSTRUMENT_TYPE, INSTRUMENT_NAME FROM INSTRUMENT";
	my @ans = &MySql( $sql );
	foreach my $rec ( @ans ) {
		print join("\t", @$rec) ,"\n";
	}
}
else {
	if ( $isExistPrePro ) {
		my $sql = "SELECT i.INSTRUMENT_NO, i.INSTRUMENT_TYPE, i.INSTRUMENT_NAME FROM INSTRUMENT i, (SELECT DISTINCT r.INSTRUMENT_NO FROM PRE_PRO pp LEFT JOIN RECORD r ON pp.ID = r.ID) tmp WHERE i.INSTRUMENT_NO = tmp.INSTRUMENT_NO";
		my @ans = &MySql( $sql );
		foreach my $rec ( @ans ) {
			print join("\t", @$rec) ,"\n";
		}
	}
}

# Get ms information
if ( $recVersion > 1 ) {
	print "MS_INFORMATION\n";
	$sql = "SHOW FIELDS FROM RECORD LIKE 'MS_TYPE'";
	@ans = &MySql($sql);
	$cnt = @ans;
	if ( $cnt != 0 ) {
		if ( !$isPeakAdv ) {
			$sql = "SELECT DISTINCT MS_TYPE FROM RECORD";
			@ans = &MySql( $sql );
			foreach my $rec ( @ans ) {
				print join("\t", @$rec) ,"\n";
			}
		}
		else {
			if ( $isExistPrePro ) {
				$sql = "SELECT DISTINCT r.MS_TYPE FROM PRE_PRO pp LEFT JOIN RECORD r ON pp.ID = r.ID";
				@ans = &MySql( $sql );
				foreach my $rec ( @ans ) {
					print join("\t", @$rec) ,"\n";
				}
			}
		}
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
1;

