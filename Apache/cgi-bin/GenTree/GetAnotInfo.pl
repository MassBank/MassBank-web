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
# ver 1.0.10  2009.06.19
#
#-------------------------------------------------------------------------------
$conf = shift(@ARGV);

require $conf || die "configuration file error\n";

# ver 1.0.1 add Start ----------------------------------------------------
@SrcDir = @ARGV;
# ver 1.0.1 add End   ----------------------------------------------------

for $tag ( 0 .. $#Tag ) {
	@tag = @{$Tag[$tag]};
	@info = ();
	foreach $dir ( @SrcDir ) {
		opendir(dIR, $dir);
		while ( $file = readdir(dIR) ) {
# ver 1.0.1 mod Start ----------------------------------------------------
#			next if ( $file =~ /^\./ );
			next if ( $file =~ /^\./ || $file !~ /\.txt$/ );
# ver 1.0.1 mod End   ----------------------------------------------------
			local(@data) = ();
# ver 1.0.7 add Start ----------------------------------------------------
				$ion_type = '';
# ver 1.0.7 add End   ----------------------------------------------------
			open(fILE, "$dir/$file");
			while ( <fILE> ) {
				s/\r?\n?//g;
				($key, $val) = /^([^:]*): (.*)$/;
				for $i ( 0 .. $#tag ) {
					if ( $key eq $tag[$i] && $data[$i] eq '' ) {
						$eval = sprintf('$data[$i] = %s($i, $val)', $GetProc[$tag]);
						eval($eval);
					}
				}
				$acc = $val if ( $key eq 'ACCESSION' );
# ver 1.0.8 add Start ----------------------------------------------------
				$title = $val if ( $key eq 'RECORD_TITLE' );
# ver 1.0.8 add End   ----------------------------------------------------
# ver 1.0.8 mod Start ----------------------------------------------------
#				$ion_type = $val if ( $key eq 'AC$INSTRUMENT_TYPE' );
				$inst_type = $val if ( $key eq 'AC$INSTRUMENT_TYPE' );
# ver 1.0.8 mod End   ----------------------------------------------------
			}
			close(fILE);

			for $j ( 0 .. $#data ) {
				if ( @data[$j] eq '' ) {
					splice(@data, $j, 1);
				}
			}
# ver 1.0.8 add Start ----------------------------------------------------
			my($info, $ion) = ($title =~ /^[^;]*; [^;]*; [^;]*; ([^;]*); (.*)$/);
			if ( $info eq 'MERGED' ) {
				$data[4] = $ion;
				$data[5] = "MS/MS $info";
			}
# ver 1.0.8 add End   ----------------------------------------------------
# ver 1.0.8 mod Start ----------------------------------------------------
#			if ( $ion_type ne '' ) {
#				$data[0] = $ion_type;
#			}
			if ( $inst_type ne '' ) {
				$data[0] = $inst_type;
			}
# ver 1.0.8 mod End   ----------------------------------------------------

			push(@data, $acc);
			push(@info, [ @data ]);
		}
		closedir(dIR);
	}
	foreach $info ( sort mySort @info ) {
		local(@out) = @$info;
		foreach $i ( 0 .. $#out ) {
			$eval = sprintf('$out[$i] = %s($i, $out[$i])', $OutProc[$tag]);
			eval($eval);
		}
		print join("\t", @out), "\n";
	}
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
