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
# バッチ検索処理
#
# ver 1.0.1  2008.12.05
#
#-------------------------------------------------------------------------------

$from = 'massbank@iab.keio.ac.jp';
$to = <>;
chop $to;
$time = <>;
chop $time;

while ( <> ) {
	chop;
	last if ( /^$/ );
}

$tmpfile = "/tmp/batchsender.$$.".time().'.txt';

open(TMP, "> $tmpfile" );
while ( <> ) {
	chop;
	$query = $_;
	$all = <>; chop($all);
	$cnt = <>; chop($cnt);
	print TMP "$query\t$all\t$cnt\n";
	while ( <> ) {
		chop;
		last if ( /^$/ );
		($name, $acc, $ion, $form, $hitscore) = split("\t", $_);
		($hit, $score) = ($hitscore =~ /^([0-9]*)(\.[0-9]*)$/);
		if ( $ion > 0 ) { $ion = "[P]"; }
		elsif ( $ion < 0 ) { $ion = "[N]"; }
		else { $ion = "[-]"; }
		@out = ($acc, $name, $form, $ion, sprintf('%.4f', $score), $hit);
		print TMP join("\t", @out), "\n";
	}
	print TMP "\n";
}
close(TMP);

open(MAIL, "| /usr/sbin/sendmail -f $from -t $to");
print MAIL << "HEAD";
From: $from
To: $to
Subject: MassBank Batch Search Results
MIME-Version: 1.0
Content-Type: multipart/mixed; boundary="NextPart"

HEAD

print MAIL << 'PHEAD';
--NextPart
Content-Type: text/plain
Content-Disposition: inline

PHEAD

print MAIL << 'DATA';
Dear Users,

Thank you for using MassBank Batch Service.

DATA
print MAIL "The results for your request dated '$time' are attached to this e-mail.\n";
print MAIL << 'DATA';
--
MassBank.jp - High Resolution Mass Spectral Database
  URL: http://www.massbank.jp
  E-mail: massbank@iab.keio.ac.jp

DATA

print MAIL << 'PHEAD';
--NextPart
Content-Type: text/plain; name="MassBankResult.txt"
Content-Disposition: attachment; filename="MassBankResults.txt"

PHEAD

print MAIL << 'DATA';
***** MassBank Batch Search Results *****

DATA
print MAIL "Request Date: $time\n";
print MAIL "\n";

open(TMP, $tmpfile);
$n = 0;
while ( <TMP> ) {
	chop;
	$n ++;
	($query, $all, $cnt) = split("\t", $_);
	print MAIL "### Query $n ###\n";
	print MAIL "# Name: $query\n";
	print MAIL "# Hit: $all\n\n";
	print MAIL "Top $cnt List\n";
	print MAIL "Accession\tTitle\tFormula\tIon\tScore\tHit\n\n";
	while ( <TMP> ) {
		chop;
		last if ( /^$/ );
		print MAIL "$_\n";
	}
	print MAIL "\n";
}
close(TMP);
print MAIL << 'DATA';
##### END #####

*********************************************************
*  MassBank.jp - High Resolution Mass Spectral Database *
*    URL: http://www.massbank.jp/                       *
*********************************************************

DATA

print MAIL << 'PHEAD';
--NextPart
Content-Type: text/html; name="MassBankResults.htm"
Content-Disposition: attachment; filename="MassBankResults.htm"

PHEAD

print MAIL << 'DATA';
<html>
<head><title>MassBank Batch Search Results</title></head>
<body>
<h1><a href="http://www.massbank.jp/" target="_blank">MassBank</a> Batch Search Results</h1>
<hr>
DATA
print MAIL "<h2>Request Date : $time<h2>\n";
print MAIL "<hr>\n";
open(TMP, $tmpfile);
$n = 0;
while ( <TMP> ) {
	chop;
	$n ++;
	($query, $all, $cnt) = split("\t", $_);
	print MAIL "<h2>Query $n</h2><br>\n";
	print MAIL "Name: $query<br>\n";
	print MAIL "Hit: $all<br>\n";
	print MAIL '<table border="1">', "\n";
	print MAIL '<tr><td colspan="6">Top ', $cnt, " List</td></tr>\n";
	print MAIL "<tr><th>Accession</th><th>Title</th><th>Formula</th><th>Ion</th><th>Score</th><th>Hit</th></tr>\n";
	while ( <TMP> ) {
		chop;
		last if ( /^$/ );
		($acc, $title, $form, $ion, $score, $hit) = split("\t", $_);
		print MAIL '<tr>';
		print MAIL '<td><a href="http://www.massbank.jp/jsp/FwdRecord.jsp?id=',$acc,'" target="_blank">', $acc, '</td>';
		print MAIL "<td>$title</td>";
		print MAIL "<td>$form</td>";
		print MAIL "<td>$ion</td>";
		print MAIL "<td>$score</td>";
		print MAIL '<td align="right">', $hit, '</td>';
		print MAIL "<tr>\n";
	}
	print MAIL "</table>\n";
	print MAIL "<hr>\n";
}
print MAIL << 'DATA';
</body>
</html>

DATA

print MAIL << 'TAIL';
--NextPart--

TAIL

close(MAIL);

print << 'HTML';
Content-type: text/html

<HTML><HEAD></HEAD><BODY></BODY></HTML>
HTML

system("rm -f $tmpfile");

exit(0);

1;
