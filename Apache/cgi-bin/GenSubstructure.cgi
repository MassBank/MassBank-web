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
# [Admin Tool] Structure Search 用テキストファイル生成及び登録
#
# ver 1.0.1 2009.09.14
#
#-------------------------------------------------------------------------------
use CGI;
use File::Spec;

print "Content-Type: text/plain\n\n";

my $query = new CGI;
my $db = $query->param('db');
my $abs = File::Spec->rel2abs($0);
my $base_dir = substr($abs, 0, index($abs, 'cgi-bin') - 1);
my $ss_dir = "$base_dir/StructureSearch";

#--------------------------------------------
# 登録情報初期化（ファイル削除）
#--------------------------------------------
`rm -f $ss_dir/*_filter.txt`;

my @molfile_all = ();
my $file_name = '';
my @db_list = split(/,/, $db);
foreach $db_name (@db_list) {
	my $molfile_dir = "$base_dir/DB/molfile/$db_name/";
	my @molfile_name = ();
	opendir(DH, $molfile_dir);
	while ( $file_name = readdir(DH) ) {
		# '.'や'..'も取得されるのでスキップする
		next if $file_name =~ /^\.{1,2}$/;
		# 拡張子.mol以外は対象外とする
		next if $file_name !~ /\.mol$/o;
		my $file_path = $molfile_dir . $file_name;
		$file_name =~ s/.mol//;
		push( @molfile_name, $file_name );
		push( @molfile_all, $file_path );
	}
	#--------------------------------------------
	# DB毎のfilter.txtを生成する
	#--------------------------------------------
	open(F, ">$ss_dir/" . $db_name . '_filter.txt');
	foreach $file_name (sort @molfile_name) {
		print F "$file_name\n";
	}
	close(F);
	closedir(DH);
}

#------------------------------------------------
# MolfileList.txtを生成する
#------------------------------------------------
open(F, ">$ss_dir/MolfileList.txt");
foreach $file_name (sort @molfile_all) {
	print F "$file_name\n";
}
close(F);

#------------------------------------------------
# 登録処理と後処理
#------------------------------------------------
if ( $^O ne 'MSWin32' ) {
	`$ss_dir/struct_server -c $ss_dir/MolfileList.txt`;
}
else {
	`$ss_dir\\struct_server -c $ss_dir\\MolfileList.txt`;
}
`rm -f $ss_dir/MolfileList.txt`;


print "OK\n";
exit(0);
