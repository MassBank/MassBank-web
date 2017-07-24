/*******************************************************************************
 *
 * Copyright (C) 2008 JST-BIRD MassBank
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 ******************************************************************************/
#include <string>
using namespace std;

#define DB_USER		"bird"
#define DB_PASSWD	"bird2006"

#define PARAM_WEIGHT_LINEAR		true
#define PARAM_WEIGHT_SQUARE		false
#define PARAM_NORM_LOG			true
#define PARAM_NORM_SQRT			false

struct QUERY_PARAM {
	int		start;
	int		num;
	int		floor;
	int		celing;
	int		threshold;
	int		cutoff;
	float	tolerance;
	string	colType;
	bool	weight;
	bool	norm;
	string	tolUnit;
	string	val;
	string	instType;
	string	ion;
	int precursor;
	string	mstype;
};

struct HIT_PEAK {
	string	qMz;
	double	qVal;
	string	hitMz;
	double	hitVal;
};

struct RES_SCORE {
	string	id;
	double	score;
};


bool dbConnect();
long dbExecuteSql( const char * );
void dbClose();
void dbError( const char * );
bool getReqParam();
void setQueryParam();
void setQueryPeak();
bool searchPeak();
void setScore();
void outResult();
static vector<string> split( string, string );
static int urlDecode( char *s, int len );
bool existHeapTable( string );
int strncmpi(const char *, const char *, unsigned);
string replace_all(const string &, const string &, const string &);
