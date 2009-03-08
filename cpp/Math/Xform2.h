//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef XFORM2_H__
#define XFORM2_H__

#include <Math/Point2.h>
#include <Math/Vector2.h>
#include <Math/Segment2.h>

#include <boost/python.hpp>



/*
2D Transformation
*/




class _DllExport_ Xform2
{
public:
	double scale;
	Vector2 translation;

public:
	inline Xform2() : scale( 1.0 )
	{
	}

	inline Xform2(double scale) : scale( scale )
	{
	}

	inline Xform2(const Vector2 &translation) : scale( 1.0 ), translation( translation )
	{
	}

	inline Xform2(double scale, const Vector2 &translation) : scale( scale ), translation( translation )
	{
	}





	inline void read(FILE *f)
	{
		fread( &scale, sizeof(double), 1, f );
		translation.read( f );
	}

	inline void write(FILE *f) const
	{
		fwrite( &scale, sizeof(double), 1, f );
		translation.write( f );
	}


	inline Xform2 inverse() const
	{
		double invScale = 1.0 / scale;
		return Xform2( invScale, translation * -invScale );
	}
};


_DllExport_ inline Vector2 operator*(const Vector2 &v, const Xform2 &x)
{
	return v * x.scale;
}

_DllExport_ inline Vector2 & operator*=(Vector2 &v, const Xform2 &x)
{
	v *= x.scale;
	return v;
}

_DllExport_ inline Point2 operator*(const Point2 &v, const Xform2 &x)
{
	return Point2::mul( v, x.scale )  +  x.translation;
}

_DllExport_ inline Point2 & operator*=(Point2 &v, const Xform2 &x)
{
	v = Point2::mul( v, x.scale )  +  x.translation;
	return v;
}

_DllExport_ inline Segment2 operator*(const Segment2 &v, const Xform2 &x)
{
	return Segment2( v.a * x, v.b * x );
}

_DllExport_ inline Segment2 & operator*=(Segment2 &v, const Xform2 &x)
{
	v.a *= x;
	v.b *= x;
	return v;
}

_DllExport_ inline Xform2 operator*(const Xform2 &a, const Xform2 &b)
{
	return Xform2( a.scale * b.scale, a.translation * b.scale + b.translation );
}

_DllExport_ inline Xform2 & operator*=(Xform2 &a, const Xform2 &b)
{
	a.scale *= b.scale;
	a.translation *= b.scale;
	a.translation += b.translation;
	return a;
}





#endif
