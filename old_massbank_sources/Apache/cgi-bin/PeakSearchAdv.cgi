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
# Peak Search Advanced
#
# ver 1.0.3 2011.08.24
#
#-------------------------------------------------------------------------------
use DBI;
use CGI;

print "Content-Type: text/plain\n\n";

my $query = new CGI;
my @params = $query->param();
my @formula_list = ();
my $type = "";
my $mode = "";
my $db_name = "";
my $where_inst = "";
my $where_ms = "";
my $where_ion = "";
my @inst = ();
my @ms = ();
foreach $key ( @params ) {
	my $val = trim( $query->param($key) );
	my $pos = index($key, 'formula');
	if ( $pos >= 0 && $val ne '' ) {
		my $num = substr($key, 7, 1);
		@formula_list[$num - 1] = $val;
	}
	elsif ( $key eq 'stype' ) {
		$type = $val;
	}
	elsif ( $key eq 'mode' ) {
		$mode = $val;
	}
	elsif ( $key eq 'dsn' ) {
		$db_name = $val;
	}
	elsif ( $key eq 'ion_adv' ) {
		$where_ion = "";
		if ( $val eq '1' ) {
			$where_ion = " AND s.ION > 0";
		}
		elsif ( $val eq '-1' ) {
			$where_ion = " AND s.ION < 0";
		}
	}
	elsif ( $key eq 'inst_adv' ) {
		@inst = $query->param($key);
	}
	elsif ( $key eq 'ms_adv' ) {
		@ms = $query->param($key);
	}
}

if ( $db_name eq '' ) {
	$db_name = "MassBank";
}
open(F, "DB_HOST_NAME");
while ( <F> ) {
	chomp;
	$host_name .= $_;
}
close(F);
$DB = "DBI:mysql:$db_name:$host_name";
$User = 'bird';
$PassWord = 'bird2006';
$dbh = DBI->connect($DB, $User, $PassWord) || die "connect error \n";

# Instrument Type condition
my $isInstAll = 0;
if ( $#inst >= 0 ) {
	foreach my $inst (@inst) {
		if ( $inst eq 'all' ) {
			$isInstAll = 1;
			last;
		}
	}
}
else {
	$isInstAll = 1;
}
if ( !$isInstAll ) {
	for ( $i = 0; $i < @inst; $i ++ ) {
		$where_inst .= " INSTRUMENT_TYPE='@inst[$i]'";
		if ($i != @inst -1) {
			$where_inst .= " or";
		}
	}
	$sql = "SELECT INSTRUMENT_NO FROM INSTRUMENT WHERE"
		 . "$where_inst";
	@ans = &MySql($sql);
	$cnt = @ans;
	if ( $cnt == 0 ) {
		$dbh->disconnect;
		exit(0);
	}
	my $in = "";
	foreach $item ( @ans ) {
		$inst_no = $$item[0];
		$in .= "$inst_no,";
	}
	chop $in;
	$where_inst = " AND r.INSTRUMENT_NO IN($in)";
}

# MS Type condition
my $isMsAll = 0;
if ( $#ms >= 0 ) {
	foreach $ms (@ms) {
		if ( $ms eq 'all' ) {
			$isMsAll = 1;
			last;
		}
	}
}
else {
	$isMsAll = 1;
}
if ( !$isMsAll ) {
	$sql = "SHOW FIELDS FROM RECORD LIKE 'MS_TYPE'";
	@ans = &MySql($sql);
	$cnt = @ans;
	if ( $cnt == 0 ) {
		$dbh->disconnect;
		exit(0);
	}
	my $in = "";
	for ( my $i=0; $i<@ms; $i++ ) {
		$in .= "'@ms[$i]',";
	}
	chop $in;
	$where_ms .= " AND r.MS_TYPE IN($in)";
}

my $where = "";
#------------------------------------------------
# Product Ion
#------------------------------------------------
my $outer_in = "";
if ( $type eq "product" ) {
	$sql = "SHOW TABLES LIKE 'PRODUCT_ION'";
	@ans = &MySql($sql);
	$cnt = @ans;
	if ( $cnt == 0 ) {
		$dbh->disconnect;
		exit(0);
	}
	#--------------------------------------------
	# AND
	#--------------------------------------------
	if ( $mode eq 'and' ) {
		$sql = "SELECT * FROM ";
		my $num = @formula_list - 1;
		for my $i ( 0 .. $num ) {
			if ( $formula_list[$i] ne '' ) {
				$sql .= "(SELECT ID FROM PRODUCT_ION WHERE FORMULA='$formula_list[$i]') AS t$i";
			}
			if ( $i > 0 ) {
				$where .= "t" . ($i - 1) . ".ID=t" . $i . ".ID";
				if ( $i < $num ) {
					$where .= " AND ";
				}
			}
			if ( $i < $num ) {
				$sql .= ", ";
			}
		}
		if ( $where ne '' ) {
			$sql .= " WHERE $where";
		}
		@rec = &MySql($sql);
		$num = @rec - 1;
		for my $i ( 0 .. $num ) {
			$outer_in .= "'$rec[$i][0]'";
			if ( $i < $num ) {
				$outer_in .= ", ";
			}
		}
	}
	#--------------------------------------------
	# OR
	#--------------------------------------------
	elsif ( $mode eq 'or' ) {
		my $in = "";
		for my $i ( 0 .. $#formula_list ) {
			if ( $formula_list[$i] ne '' ) {
				$in .= "'$formula_list[$i]'";
			}
			if ( $i < $#formula_list ) {
				$in .= ", ";
			}
		}
		$outer_in = "SELECT ID FROM PRODUCT_ION WHERE FORMULA IN($in)";
	}
}
#------------------------------------------------
# Neutral Loss
#------------------------------------------------
elsif ( $type eq "neutral" ) {
	#--------------------------------------------
	# SEQUENCE
	#--------------------------------------------
	if ( $mode eq 'seq' ) {
		$sql = "SHOW TABLES LIKE 'NEUTRAL_LOSS_PATH'";
		@ans = &MySql($sql);
		$cnt = @ans;
		if ( $cnt == 0 ) {
			$dbh->disconnect;
			exit(0);
		}
		my $like = "";
		foreach my $formula ( @formula_list ){
			if ( $formula ne '' ) {
				$like .= "%>$formula-";
			}
		}
		$like .= "%";
		$where = "PATH LIKE '$like'";
		$outer_in = "SELECT DISTINCT ID FROM NEUTRAL_LOSS_PATH WHERE $where";
	}
	#--------------------------------------------
	# AND
	#--------------------------------------------
	elsif ( $mode eq 'and' ) {
		$sql = "SHOW TABLES LIKE 'PRE_PRO'";
		@ans = &MySql($sql);
		$cnt = @ans;
		if ( $cnt == 0 ) {
			$dbh->disconnect;
			exit(0);
		}
		my @sql_formula_list = ();
		@formula_list = sort(grep {!$count{$_}++} @formula_list);		#重複削除
		for my $i ( 0 .. $#formula_list ) {
			push(@sql_formula_list, "'" . $formula_list[$i] . "'");
		}
		$concat_formula = join(",", @formula_list );
		$in = join(",", @sql_formula_list );
		$sql = "SELECT ID, NEUTRAL_LOSS FROM PRE_PRO WHERE NEUTRAL_LOSS IN($in) "
			 . "GROUP BY ID, NEUTRAL_LOSS ORDER BY ID, NEUTRAL_LOSS";
		my %concat_nloss = ();
		my @item = &MySql($sql);
		if ( scalar(@item) > 0 ) {
			foreach $item ( @item ) {
				my $id = $$item[0];
				my $nloss = $$item[1];
				$concat_nloss{$id} = $concat_nloss{$id} . $nloss . ",";
			}
			@id_list = ();
			while ( ($id, $nloss) = each(%concat_nloss) ) {
				chop $nloss;
				if ( $nloss eq $concat_formula ) {
					push(@id_list, "'" . $id . "'");
				}
			}
			$outer_in = join(",", @id_list);
		}
	}
}

if ( $outer_in ne "" ) {
	$sql = "SELECT s.NAME, s.ID, s.ION, r.FORMULA, r.EXACT_MASS FROM SPECTRUM s, RECORD r "
		 . "WHERE s.ID IN($outer_in) AND s.ID = r.ID"
		 . "$where_ion"
		 . "$where_inst"
		 . "$where_ms";
	@rec = &MySql($sql);
	foreach $rec ( @rec ) {
		print join("\t", @$rec), "\n";
	}
}
$dbh->disconnect;
exit(0);

sub MySql() { local($sql) = @_;
	local($sth, $n, $i, @ans, @ret);
	@ret = ();
	$sth = $dbh->prepare($sql);
	unless( $sth->execute ) {
		return undef;
	}
	$n = $sth->rows;
	for ( $i = 0; $i < $n; $i ++ ) { @ans = $sth->fetchrow_array; push(@ret, [@ans]); }
	$sth->finish;
	return @ret;
}

sub trim {
	my $val = shift;
	$val =~ s/^ *(.*?) *$/$1/;
	return $val;
}

1;
