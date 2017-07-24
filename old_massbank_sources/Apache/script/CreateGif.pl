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
# 化学構造式用GIFファイル生成
#
# ver 1.0.1  2010.10.08
#
#-------------------------------------------------------------------------------
use File::Spec;
use File::Basename;

#-------------------------------------------------------------------------------
# MolfileをベースにGIFファイルを生成する（gif、gif_small、gif_large）
# GIFファイル生成処理を実行するDB名をスペース区切りで指定もしくは、DB名を指定せずに実行
# DB名を指定しない場合は全てのDBが対象となる
#    perl CreateGif.pl <dbname> <dbname> <dbname>...
#       or
#    perl CreateGif.pl
#-------------------------------------------------------------------------------

my $massbank_dir = File::Spec->rel2abs( dirname($0) . "/../" );

# 処理対象のDB一覧を取得
my @db_names = ();
if (scalar(@ARGV) > 0) {
	foreach my $arg_db_name (@ARGV) {
		push (@db_names, $arg_db_name);
	}
}
else {
	opendir(DIR, "$massbank_dir/DB/molfile/");
	while (defined($dir = readdir(DIR))) {
		if($dir ne "." && $dir ne "..") {
			push (@db_names, $dir);
		}
	}
	closedir(DIR);
}

# 対象DBごとに処理を実行
umask(0);
foreach my $db_name (@db_names) {
	
	# 存在するMolfile一覧を取得
	if ( ! -d "$massbank_dir/DB/molfile/$db_name/" ) { next; }
	my @molfiles = glob "$massbank_dir/DB/molfile/$db_name/*.mol";
	
	# GIF出力ディレクトリが存在しなければ作成しGIFを出力
	# 既に同一ファイル名のGIFが存在する場合は出力しない
	@gif_dir_list = ('gif_small', 'gif', 'gif_large');
	foreach my $gif_dir (@gif_dir_list) {
		
		if ( ! -d "$massbank_dir/DB/$gif_dir" ) {
			mkdir("$massbank_dir/DB/$gif_dir", 0777) or die($!);
		}
		if ( ! -d "$massbank_dir/DB/$gif_dir/$db_name" ) {
			mkdir("$massbank_dir/DB/$gif_dir/$db_name", 0777) or die($!);
		}
		
		my $gif_size = 80;
		$gif_size = 180 if $gif_dir eq 'gif';
		$gif_size = 400 if $gif_dir eq 'gif_large';
		
		foreach my $mol_file ( @molfiles ) {
			my $gif_file = $mol_file;
			$gif_file =~ s/molfile/$gif_dir/;
			$gif_file =~ s/\.mol/\.gif/;
			if ( ! -f $gif_file ) {
				`java -jar $massbank_dir/applet/MolView.jar size=$gif_size out_dir=$massbank_dir/DB/$gif_dir/$db_name/ molfile=$mol_file`;
			}
			chmod(0777, $gif_file);
		}
	}
}
exit (0);
