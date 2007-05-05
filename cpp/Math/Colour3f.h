//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef COLOUR3F_H__
#define COLOUR3F_H__

#include <math.h>

#include <stdio.h>






class _DllExport_ Colour3f
{
public:
	float r, g, b;


	inline Colour3f()
	{
		r = g = b = 0.0;
	}

	inline Colour3f(float ir, float ig, float ib)
	{
		r = ir;
		g = ig;
		b = ib;
	}



	inline bool operator==(const Colour3f &c) const
	{
		return ( r == c.r )  &&  ( g == c.g )  &&  ( b == c.b );
	}

	inline bool operator!=(const Colour3f &c) const
	{
		return ( r != c.r )  ||  ( g != c.g )  ||  ( b != c.b );
	}





	inline Colour3f operator+(const Colour3f &c) const
	{
		return Colour3f( r + c.r,  g + c.g,  b + c.b );
	}

	inline void operator+=(const Colour3f &c)
	{
		r += c.r;
		g += c.g;
		b += c.b;
	}


	inline Colour3f operator-(const Colour3f &c) const
	{
		return Colour3f( r - c.r,  g -c.g,  b - c.b );
	}

	inline void operator-=(const Colour3f &c)
	{
		r -= c.r;
		g -= c.g;
		b -= c.b;
	}


	inline Colour3f operator*(float s) const
	{
		return Colour3f( r * s,  g * s,  b * s );
	}

	inline void operator*=(float s)
	{
		r *= s;
		g *= s;
		b *= s;
	}


	inline Colour3f operator*(const Colour3f &c) const
	{
		return Colour3f( r * c.r,  g * c.g,  b * c.b );
	}

	inline void operator*=(const Colour3f &c)
	{
		r *= c.r;
		g *= c.g;
		b *= c.b;
	}


	inline Colour3f operator-() const
	{
		return Colour3f( -r, -g, -b );
	}




	inline void read(FILE *f)
	{
		fread( &r, sizeof(float), 1, f );
		fread( &g, sizeof(float), 1, f );
		fread( &b, sizeof(float), 1, f );
	}

	inline void write(FILE *f) const
	{
		fwrite( &r, sizeof(float), 1, f );
		fwrite( &g, sizeof(float), 1, f );
		fwrite( &b, sizeof(float), 1, f );
	}
};


#endif
