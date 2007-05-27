//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef BSPLINE_H__
#define BSPLINE_H__

#include <Util/Array.h>


template <class T> struct BezierControlPoints
{
	T v[4];
};


template <class T> struct SplineBasisMatrix
{
	T x[4];
};


template <class T> inline void cubicBSplineBasis(const T &a, const T &b, const T &c, const T &d, SplineBasisMatrix<T> &x)
{
	x.x[0] = (	-a			+ b * 3.0		- c * 3.0		+ d	)  *  ( 1.0 / 6.0 );
	x.x[1] = (	a * 3.0		- b * 6.0		+ c * 3.0			)  *  ( 1.0 / 6.0 );
	x.x[2] = (	-a * 3.0					+ c * 3.0			)  *  ( 1.0 / 6.0 );
	x.x[3] = (	a			+ b * 4.0		+ c				)  *  ( 1.0 / 6.0 );
}

template <class T> inline void cubicCatmulRomBasis(const T &a, const T &b, const T &c, const T &d, SplineBasisMatrix<T> &x)
{
	x.x[0] = (	-a			+ b * 3.0		- c * 3.0		+ d	) * 0.5;
	x.x[1] = (	a * 2.0		- b * 5.0		+ c * 4.0		- d	) * 0.5;
	x.x[2] = (	-a						+ c				) * 0.5;
	x.x[3] = (				b							);
}

template <class T> inline void cubicBezierBasis(const T &a, const T &b, const T &c, const T &d, SplineBasisMatrix<T> &x)
{
	x.x[0] = (	-a			+ b * 3.0		- c * 3.0		+ d	);
	x.x[1] = (	a * 3.0		- b * 6.0		+ c * 3.0			);
	x.x[2] = (	-a * 3.0		+ b * 3.0						);
	x.x[3] = (	a										);
}


template <class T> inline void differentiateBasis(SplineBasisMatrix<T> &d, const SplineBasisMatrix<T> &x)
{
	d.x[1] = x.x[0] * 3.0;
	d.x[2] = x.x[1] * 2.0;
	d.x[3] = x.x[2];
}


template <class T> inline T evaluateSpline(const SplineBasisMatrix<T> &x, double t, bool deriv)
{
	double t2 = t * t;

	if ( deriv )
	{
		return x.x[0] * t2 * 3.0  +  x.x[1] * t * 2.0  +  x.x[2];
	}
	else
	{
		double t3 = t2 * t;
		return x.x[0] * t3  +  x.x[1] * t2  +  x.x[2] * t  +  x.x[3];
	}
}


template <class T> inline T cubicBSpline(const T &a, const T &b, const T &c, const T &d, double t, bool deriv)
{
	SplineBasisMatrix<T> basis;
	cubicBSplineBasis( a, b, c, d, basis );
	return evaluateSpline( basis, t, deriv );
}

template <class T> inline T cubicCatmulRom(const T &a, const T &b, const T &c, const T &d, double t, bool deriv)
{
	SplineBasisMatrix<T> basis;
	cubicCatmulRomBasis( a, b, c, d, basis );
	return evaluateSpline( basis, t, deriv );
}

template <class T> inline T cubicBezier(const T &a, const T &b, const T &c, const T &d, double t, bool deriv)
{
	SplineBasisMatrix<T> basis;
	cubicBezierBasis( a, b, c, d, basis );
	return evaluateSpline( basis, t, deriv );
}



template <class T> inline T cubicBezier(const BezierControlPoints<T> &c, double t, bool deriv)
{
	return cubicBezier( c.v[0], c.v[1], c.v[2], c.v[3], t, deriv );
}




/*
THIS WORK IS BASED ON THE PAGE AT:

http:://www.ibiblio.org/e-notes/Splines/Bint.htm

NATURAL SPLINES
===============
Natural splines are C2 continuous splines that interpolate their control
points. Each spline segment is represented using a Bezier curve.

For a natural spline whose control points are P0  to P(N-1)    :

The control points Bezier segments i-1 to i+1 are:

			A					B					C					D

Bi-1:			Pi-1					Pi-1 + di-1			Pi - di				Pi

Bi:			Pi					Pi + di				Pi+1 - di+1			Pi+1

Bi+1:		Pi+1					Pi+1 + di+1			Pi+2 - di+2			Pi+2



Two adjacent Bezier curves f(t) and g(t) are represented by the control points:

f(t): A  B  C  D			( P0		P0  + d0		P1  - d1		P1  )

and

g(t): Q, R, S, T		( P1		P1  + d1		P2  - d2		P2  )


For C0 continuity:					D	=	Q
For C1 continuity:  				D - C	=	 R - Q  (by definition of Bezier curves)

For C2 continuity: 				f''(1)		=	g''(0)

Second derivative of bezier curve:
	f''(t) = t( 6D - 18C + 18B - 6A )  +  6C - 12B + 6A
similar for g''(t)

So:
f''(1) = 6D - 12C + 6B
g''(0) = 6S - 12R + 6Q

Therefore:
For C2 continuity:		6D - 12C + 6B	=	6S - 12R + 6Q


Solve the following:

			6D - 12C + 6B	=	6S - 12R + 6Q			(C2 continuity)
				D - C	=	R - Q				(C1 continuity)
					D	=	Q					(C0 continuity)

			6Q - 12C + 6B	=	6S - 12R + 6Q
				Q - C	=	R - Q

			6B - 12C		=	6S - 12R
					C	=	2Q - R

		6B - 24Q + 12R	=	6S - 12R

					6B	=	6S - 24R + 24Q

					B	=	S - 4R + 4Q

			P0  + d0		=	P2  - d2   -  4 ( P1  + d1  )  +  4P1

		d0  + 4d1  + d2	=	P2  - P0


Generalised for splines that meet at point Pi  :

		di-1 + 4di + di+1	=	Pi+1 - Pi-1


These banded equations must be solved to compute di

For a closed spline with 6 control points, this can be represented by the
matrix below:


[	4	1	0	0	0	1	]		[	d0	]		[	P1 - P5	]
[							]		[		]		[			]
[	1	4	1	0	0	0	]		[	d1	]		[	P2 - P0	]
[							]		[		]		[			]
[	0	1	4	1	0	0	]		[	d2	]		[	P3 - P1	]
[							]	X	[		]	=	[			]
[	0	0	1	4	1	0	]		[	d3	]		[	P4 - P2	]
[							]		[		]		[			]
[	0	0	0	1	4	1	]		[	d4	]		[	P5 - P3	]
[							]		[		]		[			]
[	1	0	0	0	1	4	]		[	d5	]		[	P0 - P4	]



For an open spline we use:

d0 = ( P1 - P0 )  /  3

dN-1 = ( PN-1  -  PN-2 ) / 3


So the matrix equation is:

[	1	0	0	0	0	0	]		[	d0	]		[	( P1 - P0 ) / 3		]
[							]		[		]		[					]
[	1	4	1	0	0	0	]		[	d1	]		[	P2 - P0			]
[							]		[		]		[					]
[	0	1	4	1	0	0	]		[	d2	]		[	P3 - P1			]
[							]	X	[		]	=	[					]
[	0	0	1	4	1	0	]		[	d3	]		[	P4 - P2			]
[							]		[		]		[					]
[	0	0	0	1	4	1	]		[	d4	]		[	P5 - P3			]
[							]		[		]		[					]
[	0	0	0	0	0	1	]		[	d5	]		[	( P5 - P4 ) / 3		]



To compute d0 through dN-1 use Gaussian Elimination. Due to the fact that
the matrix is sparse, an iterative algorithm that is significantly faster
than normal Gaussian Elimination, can be used.
*/


template <class T> inline void computeOpenNaturalSplineBeziers(const Array<T> &p, Array< BezierControlPoints<T> > &beziers)
{
	if ( p.size() > 2 )
	{
		int n = p.size();

		Array<double> b;
		Array<T> a, d;
		b.resize( n-2 );
		a.resize( n-2 );
		d.resize( n );

		//compute the first and last entries of d
		d.front() = ( p[1] - p[0] ) * ( 1.0 / 3.0 );
		d.back() = ( p[n-1] - p[n-2] ) * ( 1.0 / 3.0 );


		b[0] = -0.25;
		a[0] = ( p[2] - p[0] - d[0] ) * 0.25;

		for (int i = 2; i < (n-1); i++)
		{
			int abIndex = i - 1;

			b[abIndex] = -1.0 / ( 4.0 + b[abIndex-1] );
			a[abIndex] = -( p[i+1] - p[i-1] - a[abIndex-1] ) * b[abIndex];
		}

		for (int i = n - 2; i > 0; i--)
		{
			int abIndex = i - 1;

			d[i] = a[abIndex]  +  d[i+1] * b[abIndex];
		}

		//create the bezier curves
		beziers.resize( n-1 );
		for (int i = 0; i < (n-1); i++)
		{
			beziers[i].v[0] = p[i];
			beziers[i].v[1] = p[i] + d[i];
			beziers[i].v[2] = p[i+1] - d[i+1];
			beziers[i].v[3] = p[i+1];
		}
	}
	else if ( p.size() == 2 )
	{
		BezierControlPoints<T> bez;

		bez.v[0] = p[0];
		bez.v[1] = p[0] * ( 2.0 / 3.0 )  +  p[1] * ( 1.0 / 3.0 );
		bez.v[2] = p[0] * ( 1.0 / 3.0 )  +  p[1] * ( 2.0 / 3.0 );
		bez.v[3] = p[1];

		beziers.push_back( bez );
	}
}

template <class T> inline void computeClosedNaturalSplineBeziers(const Array<T> &p, Array< BezierControlPoints<T> > &beziers)
{
	if ( p.size() > 2 )
	{
		int n = p.size();

		//
		// OPTIMISED GAUSSIAN ELIMINATION
		//
		Array<double> m, c;
		Array<T> x;

		//multpipliers; the multiplier for each row, for normalizing it.
		m.resize( n );
		//last column; the last column of the matrix
		c.resize( n );
		//the data; d
		x.resize( n );


		// MAKE THE MATRIX UPPER TRIANGULAR
		//handle top left corner special case
		// Row 0
		m[0] = 0.25;								// 1 / 4
		c[0] = m[0];								// m0 * 1
		x[0] = ( p[1] - p[n-1] ) * m[0];					// Scale : matrix0,0 = 1

		// Rows 1 to n-2
		for (int i = 1; i < (n-1); i++)
		{
			T xi = p[i+1] - p[i-1];

			m[i] = 1.0  /  ( 4.0 - m[i-1] );				// compute scale factor
			c[i] = -c[i-1] * m[i];						// track value in last column
			x[i] = ( xi - x[i-1] )  *  m[i];				// eliminate and scale
		}

		// Row n-1
		T &xLast = x[n-1];							// get last row
		xLast = p[0] - p[n-2];						// fill it in
		// keep track of the value in the last column of row n-1
		// Substract row 0 from row n-1. This eliminates the value in column 0, but introduces one in column 1
		double lastColumn = 4.0 - c[0];				// keep track of last column of row n-1
		xLast -= x[0];								// use row 0 to eliminate...
		// Eliminate the value in each column up to n-3 (not that doing this introduces a value in column i+1, hence
		// iterating across allrows)
		for (int i = 1; i < (n-2); i++)
		{
			lastColumn += c[i] * c[i-1];				// keep tract of last column...
			xLast += x[i] * c[i-1];					// eliminate...
		}
		double secondLastColumn = 1.0 - c[n-3];		// value in second last column of row n-1
		xLast -= x[n-2] * secondLastColumn;			// eliminate it
		lastColumn -= ( m[n-2] + c[n-2] ) * secondLastColumn;		// keep track of last column
		m[n-1] = 1.0  /  lastColumn;					// compute the scale factor
		xLast *= m[n-1];							// last row is 0,0,0....,0,1


		// MAKE THE MATRIX DIAGONAL
		// The matrix contains the following:
		// - 1s along the diagonal
		// - m[i] to the right of the diagonal, where i is the row index
		// - c[i] in the last column of row i
		// (m[i] + c[i] in the last column of n-1)

		for (int i = n - 2; i > 0; i--)
		{
			//eliminate upwards
			x[i] = x[i]  -  ( x[i+1] * m[i] )  -  ( x[n-1] * c[i] );
		}
		// compute row 0
		x[0] = x[0]  -  ( x[1] + x[n-1] ) * m[0];


		//x contains the data for d



		//create the bezier curves
		beziers.resize( n );
		for (int i = 0; i < (n-1); i++)
		{
			beziers[i].v[0] = p[i];
			beziers[i].v[1] = p[i] + x[i];
			beziers[i].v[2] = p[i+1] - x[i+1];
			beziers[i].v[3] = p[i+1];
		}

		beziers[n-1].v[0] = p[n-1];
		beziers[n-1].v[1] = p[n-1] + x[n-1];
		beziers[n-1].v[2] = p[0] - x[0];
		beziers[n-1].v[3] = p[0];
	}
	else if ( p.size() == 2 )
	{
		BezierControlPoints<T> bez1, bez2;

		bez1.v[0] = p[0];
		bez1.v[1] = p[0] * ( 2.0 / 3.0 )  +  p[1] * ( 1.0 / 3.0 );
		bez1.v[2] = p[0] * ( 1.0 / 3.0 )  +  p[1] * ( 2.0 / 3.0 );
		bez1.v[3] = p[1];
		bez2.v[0] = bez1.v[3];
		bez2.v[1] = bez1.v[2];
		bez2.v[2] = bez1.v[1];
		bez2.v[3] = bez1.v[0];

		beziers.push_back( bez1 );
		beziers.push_back( bez2 );
	}
}

template <class T> inline void computeNaturalSplineBeziers(const Array<T> &p, bool closedFlag, Array< BezierControlPoints<T> > &beziers)
{
	if ( closedFlag )
	{
		computeClosedNaturalSplineBeziers( p, beziers );
	}
	else
	{
		computeOpenNaturalSplineBeziers( p, beziers );
	}
}


#endif
