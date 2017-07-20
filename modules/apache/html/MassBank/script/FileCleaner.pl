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
# File cleaner
#
# ver 1.0.3  2011.06.13
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
#
#Script overview
#
# Delete the specified extension from the specified directory.
# Restrict the extensions that can be specified as files to be deleted to some extent.
# The file to be deleted is a file whose fixed time has passed since the last update date.
# Delete all files that are 7 days old by default.
#
# how to use:
#    FileCleaner.pl [絶対パス] [拡張子] [日数]
#
# 補足：
#    [絶対パス]：削除対象ファイルを含む絶対パス（必須）
#    [拡張子]：削除対象ファイルの拡張子、大括弧で囲み"/"区切りで複数指定可能（必須）
#    [日数]：削除対象ファイルの保持日数（任意）
#
# 使用例：
#
# Supplement:
# [Absolute path]: absolute path including the file to be deleted (required)
# [Extension]: Extension of file to be deleted, enclosed in square brackets "/" Multiple designation possible (required)
# [Days]: number of days to keep files to be deleted (optional)
#
# Example of use:
#
#    FileCleaner.pl /usr/local/tomcat/logs/ [log] 30
#    FileCleaner.pl /tmp/ [log/tmp/txt]
#    FileCleaner.pl "C:/MassBank/tomcat/logs/" [log/txt]
#
################################################################################

# 指定可能な拡張子
my @extensionList = ("log", "tmp", "txt", "out", "xls", "tgz", "zip", "gif", "jpg", "png", "lck", "mol", "html");

my $targetPath = "";				# 削除対象ファイルを含む絶対パス | Absolute path including the file to be deleted
my @targetExtList = ();				# 削除対象ファイルの拡張子リスト | Extension list of files to be deleted
my $keepTime = 60 * 60 * 24 * 7;	# 削除対象ファイルの保持時間（デフォルト7日） | Retention time of deleted file (default 7 days)


# パラメータ数チェック
# Number of parameters check
if ( @ARGV < 2 ) {
	die "Usage... $0 path extension [days] \n";
}
else {
	# 絶対パス取得及び整形
	# Absolute Path Acquisition and Shaping
	$targetPath = $ARGV[0];
	$targetPath =~ s|"||g;
	$targetPath =~ s|\\|/|g;
	if ( $targetPath !~ m|^.*/$|o ) { $targetPath .= "/"; }
	# 絶対パスチェック
	# Absolute path check
	if ( $targetPath !~ m|^/[^/].*$|o && $targetPath !~ m|^\w:/.*$|o ) {
		die "Please specify the absolute path.\n";
	}
	if ( ! -e $targetPath ) {
		die "Please specify the existing path.\n";
	}
	# 拡張子チェック
	# Extension check
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
# Days check
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
# File erase
my $nowTime = time;
opendir( TARGET_PATH, $targetPath );
%targetExtHash;
for ( @targetExtList ) { $targetExtHash{$_} = $_; }
while ( my $targetFile = readdir(TARGET_PATH) ) {
	next if $targetFile =~ m|^\.{1,2}$|o;					# "." や ".." はスキップ | "." Or ".." skipped
	(my $targetFileExt = $targetFile) =~ s|^.*\.([^\.]*)$|$1|o; 
	next if !exists($targetExtHash{$targetFileExt});	# 指定された拡張子以外はスキップ | Skip other than specified extensions
	
	my @filestat = stat "$targetPath$targetFile";
	my $updateTime = $filestat[9];						# 最終更新時間取得 | Get last update time
	if ( ($nowTime - $updateTime) > $keepTime ) {
		unlink "$targetPath$targetFile";
	}
}
closedir( TARGET_PATH );

exit(0);
