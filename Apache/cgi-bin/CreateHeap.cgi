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
# ヒープテーブル作成
#
# ver 3.0.4  2011.03.02
#
#-------------------------------------------------------------------------------
use CGI;
use DBI;
use File::Spec;
use File::Basename;

#-------------------------------------------------------------------------------
# 指定したDBのヒープテーブルを作成する（カンマ区切りでDBの複数指定が可能）
#   例：CreateHeap.cgi?dsn=[db name],[db name],[db name]…
#
# dsnパラメータを未指定の場合は、massbank.confのDBを対象にヒープテーブルを作成する
# massbank.conf内でコメントアウトされたDBは対象外とする
#-------------------------------------------------------------------------------

print "Content-Type: text/plain\n\n";

my $query = new CGI;
my $massbank_dir = File::Spec->rel2abs( dirname($0) . "/../" );

# 処理対象のDB一覧を取得
my @db_names = split(/,/, $query->param('dsn'));
if ( $#db_names < 0 ) {
	open(MASSBANK_CONF, "$massbank_dir/massbank.conf");
	my $serverUrl = "";
	my $isCommentOut = 0;
	my $isInternal = 0;
	while ( my $line = <MASSBANK_CONF> ) {
		$line =~ s/\r?\n?//g;										# 改行コード変換
		
		# 自サーバURL取得
		if ($line =~ m|<FrontServer URL="(.*)"/>|) {
			$serverUrl = $1;
		}
		
		# コメントアウト終了検出
		if ($isCommentOut == 1) {
			if ($line !~ m|-->| || $line =~ m|<!--| ) {
				next;
			}
			$isCommentOut = 0;
		}
		
		# コメントアウト開始検出
		if ($line =~ m|<!--| ) {
			$isCommentOut = 1;
			if ($line =~ m|-->| ) {
				$isCommentOut = 0;
			}
			next;
		}
		
		# サーバ内部DB判定
		if ($line =~ m|<MiddleServer URL=".*"/>|) {
			$isInternal = 1;
			next;
		}
		if ($line =~ m|<URL>(.*)</URL>|) {
			if ($serverUrl eq $1) {
				$isInternal = 1;
			}
			else {
				$isInternal = 0;
			}
			next;
		}
		
		# サーバ内部DBかつ、コメントアウトされていないDB名を取得
		if ($isInternal == 1 && $line =~ m|<DB>(.*)</DB>| ) {
			push (@db_names, $1);
			$isInternal = 0;
		}
	}
	close(MASSBANK_CONF);
}

# DBホスト名取得
my $host_name = "";
open(F, "$massbank_dir/cgi-bin/DB_HOST_NAME");
while ( <F> ) {
	chomp;
	$host_name .= $_;
}

# ヒープテーブル作成SQL実行
my $tbl_name = 'PEAK_HEAP';
foreach my $db_name (@db_names) {
	print "\n[$db_name]\n";
	
	$DB = "DBI:mysql:$db_name:$host_name";
	$User = 'bird';
	$PassWord = 'bird2006';
	$dbh  = DBI->connect($DB, $User, $PassWord) || next;#die "connect error \n";
	
	$sql = "SHOW TABLES LIKE '$tbl_name'";
	@ret = mysql_query($sql);
	if ( uc($ret[0][0]) eq "$tbl_name" ) {
		$ret_name = $ret[0][0];
		$sql = "SELECT COUNT(*) FROM $ret_name";
		@ret = mysql_query($sql);
		$heapCnt = $ret[0][0];
		$sql = "SELECT COUNT(*) FROM PEAK";
		@ret = mysql_query($sql);
		$peakCnt = $ret[0][0];
		print "heapCnt=$heapCnt, peakCnt=$peakCnt";
		if ( $heapCnt == $peakCnt ) {
			$dbh->disconnect;
			print " --OK\n";
			next;
		}
	}
	
	$sql = "DROP TABLE IF EXISTS $tbl_name";
	mysql_execute($sql);
	$sql = "CREATE TABLE $tbl_name(INDEX(ID),INDEX(MZ),INDEX(RELATIVE)) "
	     . "TYPE=HEAP SELECT ID,MZ,RELATIVE FROM PEAK";
	mysql_execute($sql);
	print " CREATE TABLE $tbl_name --OK\n";
	
	$dbh->disconnect;
}
exit(0);

sub mysql_query() {
	local($sql) = @_;
	local($sth, $n, $i, @ans, @ret);
	@ret = ();
	$sth = $dbh->prepare($sql);
	$sth->execute;
	$n = $sth->rows;
	for ( $i = 0; $i < $n; $i ++ ) {
		@ans = $sth->fetchrow_array;
		push(@ret, [@ans]);
	}
	$sth->finish;
	return @ret;
}

sub mysql_execute() {
	local($sql) = @_;
	$sth = $dbh->prepare($sql);
	$sth->execute;
	$sth->finish;
	return;
}
