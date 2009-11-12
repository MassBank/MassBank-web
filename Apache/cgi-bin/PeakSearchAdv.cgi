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
# ver 1.0.0  2009.05.27
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

my $where = "";
#------------------------------------------------
# Product Ion
#------------------------------------------------
my $outer_in = "";
if ( $type eq "product" ) {
	#--------------------------------------------
	# AND
	#--------------------------------------------
	if ( $mode eq 'and' ) {
		$sql = "select * from ";
		my $num = @formula_list - 1;
		for my $i ( 0 .. $num ) {
			if ( $formula_list[$i] ne '' ) {
				$sql .= "(select ID from PRODUCT_ION where FORMULA='$formula_list[$i]' group by ID) as t$i";
			}
			if ( $i > 0 ) {
				$where .= "t" . ($i - 1) . ".ID=t" . $i . ".ID";
				if ( $i < $num ) {
					$where .= " and ";
				}
			}
			if ( $i < $num ) {
				$sql .= ", ";
			}
		}
		if ( $where ne '' ) {
			$sql .= " where $where";
		}
#		print STDERR "$sql\n";
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
		$outer_in = "select ID FROM PRODUCT_ION where FORMULA in($in)";
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
		my $like = "";
		foreach my $formula ( @formula_list ){
			if ( $formula ne '' ) {
				$like .= "%>$formula-";
			}
		}
		$like .= "%";
		$where = "PATH like '$like'";
		$outer_in = "select distinct ID from NEUTRAL_LOSS_PATH where $where";
	}
	#--------------------------------------------
	# AND
	#--------------------------------------------
	elsif ( $mode eq 'and' ) {
		my @sql_formula_list = ();
		@formula_list = sort(grep {!$count{$_}++} @formula_list);		#d•¡íœ
		for my $i ( 0 .. $#formula_list ) {
			push(@sql_formula_list, "'" . $formula_list[$i] . "'");
		}
		$concat_formula = join(",", @formula_list );
		$in = join(",", @sql_formula_list );
		$sql = "select ID,NEUTRAL_LOSS from PRE_PRO where NEUTRAL_LOSS in($in) "
			 . "group by ID,NEUTRAL_LOSS order by ID,NEUTRAL_LOSS";
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
	$sql = "select NAME, S.ID, ION, FORMULA, EXACT_MASS from SPECTRUM S, RECORD R "
		 . "where S.ID in($outer_in) and S.ID=R.ID";
#	if ( $db_name eq 'Keio' ) {
#		$sql .= " and S.ID=K.ID";
#	}
#	print STDERR "$sql\n";
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
