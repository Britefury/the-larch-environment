//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef BEZIER2UTIL_CPP__
#define BEZIER2UTIL_CPP__

#include <Math/lerp.h>
#include <Math/Spline.h>

#include <Math/Bezier2Util.h>




_DllExport_ ConvexHull2 bezierCurveConvexHull(const Point2 &a, const Point2 &b, const Point2 &c, const Point2 &d)
{
	ConvexHull2 hull;
	hull.addPoint( a );
	hull.addPoint( b );
	hull.addPoint( c );
	hull.addPoint( d );
	return hull;
}




class BezierCurve
{
public:
	Point2 a, b, c, d;


	BezierCurve()
	{
	}

	BezierCurve(const Point2 &a, const Point2 &b, const Point2 &c, const Point2 &d)
				: a( a ), b( b ), c( c ), d( d )
	{
	}


	void split(BezierCurve &p, BezierCurve &q, double t) const
	{
		Point2 ab = lerp( a, b, t );
		Point2 bc = lerp( b, c, t );
		Point2 cd = lerp( c, d, t );
		Point2 abc = lerp( ab, bc, t );
		Point2 bcd = lerp( bc, cd, t );
		Point2 abcd = lerp( abc, bcd, t );

		p.a = a;
		p.b = ab;
		p.c = abc;
		p.d = abcd;
		q.a = abcd;
		q.b = bcd;
		q.c = cd;
		q.d = d;
	}


	void hull(ConvexHull2 &h)
	{
		h.addPoint( a );
		h.addPoint( b );
		h.addPoint( c );
		h.addPoint( d );
	}
};




static double sqrDistanceToBezierCurve(const Point2 &point, const BezierCurve &curve, double epsilon, double sqrEpsilon, double t0, double t1,
								const SplineBasisMatrix<Vector2> &secondDerivative)
{
	double deltaT = t1 - t0;
	// The value of the second derivative is effectively the 'straightness' of the curve, as it represents the change in velocity/direction.
	// If the length of the second derivative, multiplied by the @deltaT, is less than @epsilon, the this section of the curve can be treated as a straight line.
	Vector2 deltaVelocity0 = evaluateSpline( secondDerivative, t0, false ) * deltaT;
	Vector2 deltaVelocity1 = evaluateSpline( secondDerivative, t1, false ) * deltaT;
	if ( deltaVelocity0.sqrLength()  >  sqrEpsilon  ||  deltaVelocity1.sqrLength()  >  sqrEpsilon )
	{
		// Split @curve into two sub-curves, @p, and @q
		BezierCurve p, q;
		curve.split( p, q, 0.5 );
		// Compute their convex hulls
		ConvexHull2 pHull, qHull;
		p.hull( pHull );
		q.hull( qHull );
		// Compute the sqr-distances to the hulls
		double pSqrDist = pHull.sqrDistanceTo( point );
		double qSqrDist = qHull.sqrDistanceTo( point );

		double midT = lerp( t0, t1, 0.5 );

		// Process the sub-curve whose hull is closest first
		if ( pSqrDist < qSqrDist )
		{
			// @p is closest, process
			double sqrDist = sqrDistanceToBezierCurve( point, p, epsilon, sqrEpsilon, t0, midT, secondDerivative );

			// if the distace to @q's hull is closer than the now computed distance to @p, check @q
			if ( qSqrDist < sqrDist )
			{
				sqrDist = std::min( sqrDist, sqrDistanceToBezierCurve( point, q, epsilon, sqrEpsilon, midT, t1, secondDerivative ) );
			}

			return sqrDist;
		}
		else
		{
			// @q is closest, process
			double sqrDist = sqrDistanceToBezierCurve( point, q, epsilon, sqrEpsilon, midT, t1, secondDerivative );

			// if the distace to @p's hull is closer than the now computed distance to @q, check @p
			if ( pSqrDist < sqrDist )
			{
				sqrDist = std::min( sqrDist, sqrDistanceToBezierCurve( point, p, epsilon, sqrEpsilon, t0, midT, secondDerivative ) );
			}

			return sqrDist;
		}
	}
	else
	{
		// Don't split
		Segment2 seg( curve.a, curve.d );
		return seg.sqrDistanceTo( point );
	}
}



_DllExport_ double sqrDistanceToBezierCurve(const Point2 &point, const Point2 &a, const Point2 &b, const Point2 &c, const Point2 &d, double epsilon)
{
	// Make BezierCurve object
	BezierCurve curve( a, b, c, d );

	SplineBasisMatrix<Vector2> bezier, firstDerivative, secondDerivative;
	cubicBezierBasis( a.toVector2(), b.toVector2(), c.toVector2(), d.toVector2(), bezier );

	differentiateBasis( firstDerivative, bezier );
	differentiateBasis( secondDerivative, firstDerivative );

	return sqrDistanceToBezierCurve( point, curve, epsilon, epsilon * epsilon, 0.0, 1.0, secondDerivative );
}







#endif
