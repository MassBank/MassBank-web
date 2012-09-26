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
# [Admin Tool] TREE.sqlファイル生成 - 中間ファイル作成処理
#
# ver 1.0.14  2012.09.25
#
#-------------------------------------------------------------------------------

my $src_dir = $ARGV[0];

@info = ();
opendir(dIR, $src_dir);
while ( $file = readdir(dIR) ) {
	next if ( $file =~ /^\./ || $file !~ /\.txt$/ );
	local(@data) = ();
	my $version = 1;
	my $acc = '';
	my $title = '';
	my $instrument = '';
	my $mw = '';
	my $formula = '';
	my $name = '';
	my $ion_ptype = '';
	my $ion_itype = '';
	my $ion_mode = '';
	my $mstype = '';
	my $ce = '';
	my $rt = '';
	my $mt = '';
	my $sc = '';
	my $pmz = '';
	open(fILE, "$src_dir/$file");
	while ( <fILE> ) {
		s/\r?\n?//g;
		
		# get value of MassBank record
		if ( /^ACCESSION: (.*)$/ )                                 { $acc = $1; }
		if ( /^RECORD_TITLE: (.*)$/ )                              { $title = $1; }
		if ( /^LICENSE:/ )                                         { $version = 2; }
		if ( /^CH\$EXACT_MASS: (.*)$/ )                            { $mw = $1; }
		if ( /^CH\$FORMULA: (.*)$/ )                               { $formula = $1; }
		if ( /^AC\$INSTRUMENT_TYPE: (.*)$/ )                       { $instrument = $1; }
		if ( $version != 1 ) {
			if ( /^AC\$MASS_SPECTROMETRY: MS_TYPE (.*)$/ )          { $mstype = $1; }
			if ( /^AC\$MASS_SPECTROMETRY: ION_MODE (.*)$/ )         { $ion_mode = $1; }
			if ( /^AC\$MASS_SPECTROMETRY: COLLISION_ENERGY (.*)$/ ) { $ce = $1; }
			if ( /^AC\$CHROMATOGRAPHY: MIGRATION_TIME (.*)$/ )      { $mt = $1; }
			if ( /^AC\$CHROMATOGRAPHY: RETENTION_TIME (.*)$/ )      { $rt = $1; }
			if ( /^AC\$CHROMATOGRAPHY: SAMPLING_CONE (.*)$/ )       { $sc = $1; }
		}
		else {
			if ( /^AC\$ANALYTICAL_CONDITION: MS_TYPE (.*)$/ )          { $mstype = $1; }
			if ( /^AC\$ANALYTICAL_CONDITION: MODE (.*)$/ )             { $ion_mode = $1; }
			if ( /^AC\$ANALYTICAL_CONDITION: COLLISION_ENERGY (.*)$/ ) { $ce = $1; }
			if ( /^AC\$ANALYTICAL_CONDITION: MIGRATION_TIME (.*)$/ )   { $mt = $1; }
			if ( /^AC\$ANALYTICAL_CONDITION: RETENTION_TIME (.*)$/ )   { $rt = $1; }
			if ( /^AC\$ANALYTICAL_CONDITION: SAMPLING_CONE (.*)$/ )    { $sc = $1; }
		}
		if ( /^MS\$FOCUSED_ION: PRECURSOR_TYPE (.*)$/ )            { $ion_ptype = $1; }
		if ( /^MS\$FOCUSED_ION: PRECURSOR_M\/Z (.*)$/ )            { $pmz = $1; }
		if ( /^MS\$FOCUSED_ION: ION_TYPE (.*)$/ )                  { $ion_itype = $1; }
	}
	close(fILE);
	
	# edit value
	($name) = ($title =~ /^([^;]*);/);
	$mw = int($mw + 0.5) if( $mw > 0 );
	
	# set tree value
	# $data[0] : INSTRUMENT_TYPE
	# $data[1] : EXACT_MASS
	# $data[2] : FORMULA
	# $data[2] : RECORD_TITLE[0]
	# $data[4] : ION_TYPE or PRECURSOR_TYPE or ION_MODE
	# $data[5] : (PRECURSOR_M/Z or COLLISION_ENERGY or RETENTION_TIME or MIGRATION_TIME or SAMPLING_CONE) or (ACCESSION)
	$data[0] = $instrument;
	$data[1] = "MW ".$mw;
	$data[2] = $formula;
	$data[3] = $name;
	$data[4] = $ion_itype;
	$data[4] = $ion_ptype if ($data[4] eq '');
	$data[4] = $ion_mode if ($data[4] eq '');
	$data[5] = $mstype if ($mstype ne '');
	$data[5] .= "  /  $pmz" if ($pmz ne '');
	$data[5] .= "  /  CE:$ce" if ($ce ne '');
	$data[5] .= "  /  RT:$rt" if ($ce eq '' && $rt ne '');
	$data[5] .= "  /  MT:$mt" if ($ce eq '' && $rt eq '' && $mt ne '');
	$data[5] .= "  /  SC:$sc" if ($ce eq '' && $rt eq '' && $mt eq '' && $sc ne '');
	$data[5] = $acc if ($data[5] eq '');
	for $j ( 0 .. $#data ) {
		if ( $data[$j] eq '' ) { $data[$j] = '---'; }
	}
	
	push(@data, $acc);
	push(@info, [ @data ]);
}
closedir(dIR);
foreach $info ( sort mySort @info ) {
	local(@out) = @$info;
	print join("\t", @out), "\n";
}
exit(0);

sub mySort() {
	local(@a) = @$a;
	local(@b) = @$b;
	local($x, $y, $i);
	foreach $i ( 0 .. $#a ) {
		$x = $a[$i];
		$y = $b[$i];
		if ( $x =~ /^\d+$/ ) {
			return ($x <=> $y) if ( $x <=> $y );
		} else {
			return ($x cmp $y) if ( $x cmp $y );
		}
	}
	return 0;
}

1;
