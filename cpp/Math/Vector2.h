//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef VECTOR2_H__
#define VECTOR2_H__

#include <math.h>

#include <complex>






class _DllExport_ Vector2
{
public:
	double x, y;


	inline Vector2() : x( 0.0 ), y( 0.0 )
	{
	}

	inline Vector2(double ix, double iy) : x( ix ), y( iy )
	{
	}



	inline bool operator==(const Vector2 &v) const
	{
		return ( x == v.x )  &&  ( y  ==  v.y );
	}

	inline bool operator!=(const Vector2 &v) const
	{
		return ( x != v.x )  ||  ( y != v.y );
	}


	inline Vector2 operator+(const Vector2 &v) const
	{
		return Vector2( x + v.x,  y + v.y );
	}

	inline void operator+=(const Vector2 &v)
	{
		x += v.x;
		y += v.y;
	}

	inline Vector2 operator-(const Vector2 &v) const
	{
		return Vector2( x - v.x,  y - v.y );
	}

	inline void operator-=(const Vector2 &v)
	{
		x -= v.x;
		y -= v.y;
	}

	inline Vector2 operator*(double s) const
	{
		return Vector2( x * s,  y * s );
	}

	inline void operator*=(double s)
	{
		x *= s;
		y *= s;
	}

	inline Vector2 operator-() const
	{
		return Vector2( -x, -y );
	}


	inline double dot(const Vector2 &v) const
	{
		return ( x * v.x )  +  ( y * v.y );
	}

	inline double cross(const Vector2 &v) const
	{
		return x * v.y  -  y * v.x;
	}

	inline double sqrLength() const
	{
		return dot( *this );
	}

	inline double length() const
	{
		return sqrt( sqrLength() );
	}

	inline void normalise()
	{
		double oneOverLength = 1.0 / length();
		x *= oneOverLength;
		y *= oneOverLength;
	}

	inline void normaliseToLength(double l)
	{
		double lOverLength = l / length();
		x *= lOverLength;
		y *= lOverLength;
	}

	inline Vector2 getNormalised() const
	{
		double oneOverLength = 1.0 / length();
		return Vector2( x * oneOverLength, y * oneOverLength );
	}

	inline Vector2 getNormalisedToLength(double l) const
	{
		double lOverLength = l / length();
		return Vector2( x * lOverLength, y * lOverLength );
	}


	inline Vector2 perpendicular() const
	{
		return Vector2( y, -x );
	}


	inline Vector2 projectOntoUnitVector(const Vector2 &unitVector) const
	{
		return unitVector  *  dot( unitVector );
	}


	inline Vector2 getRotated90CCW() const
	{
		return Vector2( -y, x );
	}

	inline Vector2 getRotated90CW() const
	{
		return Vector2( y, -x );
	}


	inline double argPolar() const
	{
		return std::arg( std::complex<double>( x, y ) );
	}

	inline bool isParallelWith(const Vector2 &v)
	{
		double d = dot( v );
		double sqrLenProduct = sqrLength() * v.sqrLength();

		return d == sqrLenProduct  ||  d == -sqrLenProduct;
	}



	inline static Vector2 min(const Vector2 &a, const Vector2 &b)
	{
		return Vector2( std::min( a.x, b.x ), std::min( a.y, b.y ) );
	}

	inline static Vector2 max(const Vector2 &a, const Vector2 &b)
	{
		return Vector2( std::max( a.x, b.x ), std::max( a.y, b.y ) );
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
