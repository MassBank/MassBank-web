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
# CGIのバージョン情報取得
#
# ver 1.0.3  2008.12.05
#
#-------------------------------------------------------------------------------
use Cwd;

print "Content-Type: text/plain\n\n";
my @files = ();
my @fileList1 = GetFileList(".");
foreach $name1 ( @fileList1 ) {
	# ディレクトリの場合
	if ( $name1 =~ /\.(cgi|pl|conf)$/ ) {
		push(@files, $name1);
	}
	elsif ( -d $name1 ) {
		my $dir = $name1;
		my @fileList2 = GetFileList($dir);
		foreach $name2 ( @fileList2 ) {
			if ( $name2 =~ /\.(cgi|pl|conf)$/ ) {
				push(@files, "$dir/$name2");
			}
		}
	}
}

foreach $fileName ( sort(@files) ) {
	my $version;
	if ( isExeBinary($fileName) ) {
		$version = GetVerBin($fileName);
	}
	else {
		$version = GetVerPerl($fileName);
	}
	print "$fileName\t$version\n";
}
exit(0);

# ファイル形式判定
sub isExeBinary() {
	my $fileName = $_[0];
	my @str = ();
	open(FH, "<$fileName");
	for ( my $i = 0; $i < 4; $i++ ) {
		read( FH, $str[$i], 1 );
	}
	if ( ord($str[0]) == 0x7f ) {
		my $ident = $str[1] . $str[2] . $str[3];
		if ( $ident eq 'ELF' ) {
			return 1;
		}
	}
	else {
		my $ident = $str[0] . $str[1];
		# EXE(Windowsバイナリ)
		if ( $ident eq 'MZ' ) {
			return 1;
		}
	}
	close(FH);
	return 0;
}

# バイナリモジュールからバージョンを取得
sub GetVerBin() {
	my $fileName = $_[0];
	my $cdir = getcwd();
	my $cmd = "\"$cdir/$fileName\" -v";
	my $version = `$cmd`;
	if ( $version eq '' ) {
		$version = '-';
	}
	return $version;
}

# Perlスクリプトからバージョンを取得
sub GetVerPerl() {
	my $fileName = $_[0];
	my $version = '-';
	my $isStart = false;
	open(FH, "<$fileName");
	while ( my $line = <FH> ) {
		chomp $line;
		if ( $isStart eq true ) {
			if ( $line =~ /^#--/ ) {
				last;
			}
			my $find_str = '# ver';
			my $pos = index( $line, $find_str );
			if ( $pos >= 0 ) {
				my $pos_start = $pos + length($find_str);
				$version = substr( $line, $pos_start, length($line)-$pos_start );
			}
		}
		else {
			if ( $line =~ /^#--/ ) {
				$isStart = true;
			}
		}
	}
	close(FH);
	return $version;
}
# ファイル一覧取得
sub GetFileList() {
	my $path = $_[0];
	my @files = ();
	opendir(DH, $path);
	while ( my $file = readdir(DH) ) {
		# '.'や'..'も取れるので、スキップする
		next if $file =~ /^\.{1,2}$/;
		push(@files, $file);
	}
	closedir(DH);
	return @files;
}
