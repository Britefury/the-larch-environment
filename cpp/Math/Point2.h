//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef POINT2_H__
#define POINT2_H__

#include <Math/epsilon.h>

#include <Math/Vector2.h>



class _DllExport_ Point2
{
public:
	double x, y;


	inline Point2() : x( 0.0 ), y( 0.0 )
	{
	}

	inline Point2(double ix, double iy) : x( ix ), y( iy )
	{
	}

	inline Point2(const Vector2 &v) : x( v.x ), y( v.y )
	{
	}




	inline bool operator==(const Point2 &p) const
	{
		return ( x == p.x )  &&  ( y == p.y );
	}

	inline bool operator!=(const Point2 &p) const
	{
		return ( x != p.x )  ||  ( y != p.y );
	}

	inline Point2 operator+(const Vector2 &v) const
	{
		return Point2( x + v.x,  y + v.y );
	}

	inline void operator+=(const Vector2 &v)
	{
		x += v.x;
		y += v.y;
	}

	inline Point2 operator-(const Vector2 &v) const
	{
		return Point2( x - v.x,  y - v.y );
	}

	inline void operator-=(const Vector2 &v)
	{
		x -= v.x;
		y -= v.y;
	}

	inline Vector2 operator-(const Point2 &v) const
	{
		return Vector2( x - v.x,  y - v.y );
	}

	inline Vector2 toVector2() const
	{
		return Vector2( x, y );
	}

	inline double dot(const Vector2 &v) const
	{
		return ( x * v.x ) + ( y * v.y );
	}

	inline double sqrDistanceTo(const Point2 &p) const
	{
		return ( *this - p ).sqrLength();
	}

	inline double distanceTo(const Point2 &p) const
	{
		return ( *this - p ).length();
	}


	inline static double areaOfTrianglex2(const Point2 &a, const Point2 &b, const Point2 &c)
	{
		return ( b - a ).cross( c - a );
	}

	inline bool isOnLeft(const Point2 &a, const Point2 &b)
	{
		return areaOfTrianglex2( a, b, *this )  >  0.0;
	}

	inline bool isOnRight(const Point2 &a, const Point2 &b)
	{
		return areaOfTrianglex2( a, b, *this )  <  0.0;
	}

	inline static bool separates(const Point2 &lineA, const Point2 &lineB, const Point2 &p, const Point2 &q)
	{
		double sqrDist = lineA.sqrDistanceTo( lineB );
		double tolerence = sqrDist * EPSILON;

		double pArea = Point2::areaOfTrianglex2( lineA, lineB, p );
		double qArea = Point2::areaOfTrianglex2( lineA, lineB, q );

		if ( ( pArea * pArea )  <  tolerence  ||
			  ( qArea * qArea )  <  tolerence )
		{
			//within tolerence; no separation
			return false;
		}
		else
		{
			//check if they are on either side of the line
			return ( pArea < 0.0  &&  qArea > 0.0 )  ||
					 ( pArea > 0.0  &&  qArea < 0.0 );
		}
	}

	inline static bool segmentsIntersect(const Point2 &a, const Point2 &b, const Point2 &p, const Point2 &q)
	{
		return separates( a, b, p, q )  &&  separates( p, q, a, b );
	}




	inline static Point2 mul(const Point2 &p, double s)
	{
		return Point2( p.x * s,  p.y * s );
	}



	inline static Point2 sum(const Point2 &a, const Point2 &b)
	{
		return Point2( a.x + b.x,
					a.y + b.y );
	}

	inline static Point2 sum(const Point2 &a, const Point2 &b,
									 const Point2 &c)
	{
		return Point2( a.x + b.x + c.x,
					a.y + b.y + c.y );
	}

	inline static Point2 sum(const Point2 &a, const Point2 &b,
									 const Point2 &c, const Point2 &d)
	{
		return Point2( a.x + b.x + c.x + d.x,
					a.y + b.y + c.y + d.y );
	}


	inline static Point2 py_sum2(const Point2 &a, const Point2 &b)
	{
		return sum( a, b );
	}

	inline static Point2 py_sum3(const Point2 &a, const Point2 &b, const Point2 &c)
	{
		return sum( a, b, c );
	}

	inline static Point2 py_sum4(const Point2 &a, const Point2 &b, const Point2 &c, const Point2 &d)
	{
		return sum( a, b, c, d );
	}



	inline static Point2 min(const Point2 &a, const Point2 &b)
	{
		return Point2( std::min( a.x, b.x ), std::min( a.y, b.y ) );
	}

	inline static Point2 max(const Point2 &a, const Point2 &b)
	{
		return Point2( std::max( a.x, b.x ), std::max( a.y, b.y ) );
	}



	inline static Point2 average(const Point2 &a, const Point2 &b)
	{
		return mul( sum( a, b ), 0.5 );
	}

	inline static Point2 average(const Point2 &a, const Point2 &b, const Point2 &c)
	{
		return mul( sum( a, b, c ), 1.0 / 3.0 );
	}

	inline static Point2 average(const Point2 &a, const Point2 &b, const Point2 &c, const Point2 &d)
	{
		return mul( sum( a, b, c, d ), 0.25 );
	}


	inline static Point2 py_average2(const Point2 &a, const Point2 &b)
	{
		return average( a, b );
	}

	inline static Point2 py_average3(const Point2 &a, const Point2 &b, const Point2 &c)
	{
		return average( a, b, c );
	}

	inline static Point2 py_average4(const Point2 &a, const Point2 &b, const Point2 &c, const Point2 &d)
	{
		return average( a, b, c, d );
	}



	inline static Point2 weightedAverage(const Point2 &a, double wa, const Point2 &b, double wb)
	{
		return Point2( a.x * wa  +  b.x * wb,
					a.y * wa  +  b.y * wb );
	}

	inline static Point2 weightedAverage(const Point2 &a, double wa, const Point2 &b, double wb, const Point2 &c, double wc)
	{
		return Point2( a.x * wa  +  b.x * wb  +  c.x * wc,
					a.y * wa  +  b.y * wb  +  c.y * wc );
	}

	inline static Point2 weightedAverage(const Point2 &a, double wa, const Point2 &b, double wb, const Point2 &c, double wc, const Point2 &d, double wd)
	{
		return Point2( a.x * wa  +  b.x * wb  +  c.x * wc  +  d.x * wd,
					a.y * wa  +  b.y * wb  +  c.y * wc  +  d.y * wd );
	}


	inline static Point2 py_weightedAverage2(const Point2 &a, double wa, const Point2 &b, double wb)
	{
		return weightedAverage( a, wa, b, wb );
	}

	inline static Point2 py_weightedAverage3(const Point2 &a, double wa, const Point2 &b, double wb, const Point2 &c, double wc)
	{
		return weightedAverage( a, wa, b, wb, c, wc );
	}

	inline static Point2 py_weightedAverage4(const Point2 &a, double wa, const Point2 &b, double wb, const Point2 &c, double wc, const Point2 &d, double wd)
	{
		return weightedAverage( a, wa, b, wb, c, wc, d, wd );
	}



	inline static Point2 normalisedWeightedAverage(const Point2 &a, double wa, const Point2 &b, double wb)
	{
		double oneOverWTotal = 1.0 / ( wa + wb );
		return weightedAverage( a, wa * oneOverWTotal,
							b, wb * oneOverWTotal );
	}

	inline static Point2 normalisedWeightedAverage(const Point2 &a, double wa, const Point2 &b, double wb, const Point2 &c, double wc)
	{
		double oneOverWTotal = 1.0 / ( wa + wb + wc );
		return weightedAverage( a, wa * oneOverWTotal,
							b, wb * oneOverWTotal,
							c, wc * oneOverWTotal );
	}

	inline static Point2 normalisedWeightedAverage(const Point2 &a, double wa, const Point2 &b, double wb, const Point2 &c, double wc, const Point2 &d, double wd)
	{
		double oneOverWTotal = 1.0 / ( wa + wb + wc + wd );
		return weightedAverage( a, wa * oneOverWTotal,
							b, wb * oneOverWTotal,
							c, wc * oneOverWTotal,
							d, wd * oneOverWTotal );
	}


	inline static Point2 py_normalisedWeightedAverage2(const Point2 &a, double wa, const Point2 &b, double wb)
	{
		return normalisedWeightedAverage( a, wa, b, wb );
	}

	inline static Point2 py_normalisedWeightedAverage3(const Point2 &a, double wa, const Point2 &b, double wb, const Point2 &c, double wc)
	{
		return normalisedWeightedAverage( a, wa, b, wb, c, wc );
	}

	inline static Point2 py_normalisedWeightedAverage4(const Point2 &a, double wa, const Point2 &b, double wb, const Point2 &c, double wc, const Point2 &d, double wd)
	{
		return normalisedWeightedAverage( a, wa, b, wb, c, wc, d, wd );
	}



	inline static Point2 lerp(const Point2 &a, const Point2 &b, double t)
	{
		return a  +  ( b - a ) * t;
	}



	inline void read(FILE *f)
	{
		fread( &x, sizeof(double), 1, f );
		fread( &y, sizeof(double), 1, f );
	}

	inline void write(FILE *f) const
	{
		fwrite( &x, sizeof(double), 1, f );
		fwrite( &y, sizeof(double), 1, f );
	}
};



#endif
