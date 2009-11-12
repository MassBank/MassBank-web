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
# [Admin Tool] TREE.sqlファイル生成
#
# ver 3.0.1  2008.12.05
#
#-------------------------------------------------------------------------------
use CGI;
use Cwd;

my $query = new CGI;
my $src_dir = $query->param('src_dir');
my $out_dir = $query->param('out_dir');
my $db_name = $query->param('db');
my $name = $query->param('name');

my $fileName = $_[0];
my $cdir = getcwd();
my $subdir     = $cdir . "/GenTree/";
my $prog1      = $subdir . "GetAnotInfo.pl";
my $prog2      = $subdir . "GenTreeSQL.pl";
my $fname_conf = $subdir . "MassBank_tree.conf";
my $fname_info = $out_dir. $db_name . "_TREE.info";
my $fname_sql  = $out_dir. $db_name . "_TREE.sql";
my $cmd1 = "perl \"$prog1\" \"$fname_conf\" \"$src_dir\" > \"$fname_info\"";
my $cmd2 = "perl \"$prog2\" \"MassBank / $name\" \"$fname_info\" > \"$fname_sql\"";
`$cmd1`;
`$cmd2`;
print "Content-Type: text/plain\n\n";
print "OK\n";
exit(0);
