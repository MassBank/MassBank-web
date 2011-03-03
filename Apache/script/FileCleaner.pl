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
# ファイルクリーナー
#
# ver 1.0.2  2011.03.03
#
#-------------------------------------------------------------------------------

################################################################################
# スクリプト概要
#
# 指定のディレクトリから、指定の拡張子のファイルを削除する。
# 削除するファイルとして指定できる拡張子はある程度制限する。
# 削除対象のファイルは最終更新日から一定の時間が経過したファイルとする。
# デフォルトでは7日経過したファイルを全て削除する。
#
# 使用方法：
#    FileCleaner.pl [絶対パス] [拡張子] [日数]
#
# 補足：
#    [絶対パス]：削除対象ファイルを含む絶対パス（必須）
#    [拡張子]：削除対象ファイルの拡張子、大括弧で囲み"/"区切りで複数指定可能（必須）
#    [日数]：削除対象ファイルの保持日数（任意）
#
# 使用例：
#    FileCleaner.pl /usr/local/tomcat/logs/ [log] 30
#    FileCleaner.pl /tmp/ [log/tmp/txt]
#    FileCleaner.pl "C:/MassBank/tomcat/logs/" [log/txt]
#
################################################################################

# 指定可能な拡張子
my @extensionList = ("log", "tmp", "txt", "out", "xls", "tgz", "zip", "gif", "jpg", "png");

my $targetPath = "";				# 削除対象ファイルを含む絶対パス
my @targetExtList = ();				# 削除対象ファイルの拡張子リスト
my $keepTime = 60 * 60 * 24 * 7;	# 削除対象ファイルの保持時間（デフォルト7日）


# パラメータ数チェック
if ( @ARGV < 2 ) {
	die "Usage... $0 path extension [days] \n";
}
else {
	# 絶対パス取得及び整形
	$targetPath = $ARGV[0];
	$targetPath =~ s|"||g;
	$targetPath =~ s|\\|/|g;
	if ( $targetPath !~ m|^.*/$|o ) { $targetPath .= "/"; }
	# 絶対パスチェック
	if ( $targetPath !~ m|^/[^/].*$|o && $targetPath !~ m|^\w:/.*$|o ) {
		die "Please specify the absolute path.\n";
	}
	if ( ! -e $targetPath ) {
		die "Please specify the existing path.\n";
	}
	# 拡張子チェック
	$ARGV[1] =~ s|[\[\]]||g;
	my %extesionHash;
	for ( @extensionList ) { $extesionHash{$_} = $_; }
	my $extError = 0;
	@targetExtList = split( /\//, $ARGV[1] );
	foreach my $extension ( @targetExtList ) {
		if ( !exists($extesionHash{$extension}) ) {
			$extError = 1;
		}		
	}
	if ( $extError ) {
		die "Please specify the permitted extension... @extensionList\n";
	}
}
# 日数チェック
if ( defined($ARGV[2]) ) {
	if ( $ARGV[2] !~ m|\d+| ) {
		die "Please specify the numerical value of one or more.\n";
	}
	elsif ( $ARGV[2] < 1 ) {
		die "Please specify 1 days or more.\n";
	}
	else {
		$keepTime = 60 * 60 * 24 * $ARGV[2];
	}	
}

# ファイル消去
my $nowTime = time;
opendir( TARGET_PATH, $targetPath );
%targetExtHash;
for ( @targetExtList ) { $targetExtHash{$_} = $_; }
while ( my $targetFile = readdir(TARGET_PATH) ) {
	next if $targetFile =~ m|^\.{1,2}$|o;					# "." や ".." はスキップ
	(my $targetFileExt = $targetFile) =~ s|^.*\.([^\.]*)$|$1|o; 
	next if !exists($targetExtHash{$targetFileExt});	# 指定された拡張子以外はスキップ
	
	my @filestat = stat "$targetPath$targetFile";
	my $updateTime = $filestat[9];						# 最終更新時間取得
	if ( ($nowTime - $updateTime) > $keepTime ) {
		unlink "$targetPath$targetFile";
	}
}
closedir( TARGET_PATH );

exit(0);
