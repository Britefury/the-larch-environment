//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef SEGMENT2_H__
#define SEGMENT2_H__

#include <stdio.h>

#include <Math/clamp.h>

#include <Math/Point2.h>
#include <Math/Vector2.h>

#include <boost/python.hpp>



/*
2D LINE
*/


class _DllExport_ Segment2
{
public:
	Point2 a, b;


	inline Segment2()
	{
		a = b = Point2();
	}

	inline Segment2(const Point2 &p1, const Point2 &p2)
	{
		a = p1;
		b = p2;
	}

	inline Segment2(const Point2 &p, const Vector2 &direction)
	{
		a = p;
		b = p + direction;
	}

	inline Segment2(FILE *f)
	{
		read( f );
	}



public:
	//read and write methods
	void read(FILE *f);
	void write(FILE *f) const;



	//get direction of line
	inline Vector2 getDirection() const
	{
		return b - a;
	}

	//normal: if line is pointing upward, then the normal points to the left
	inline Vector2 getNormal() const
	{
		//return the direction, rotated left by 90 degrees
		Vector2 direction = getDirection();
		return Vector2( -direction.y, direction.x );
	}



	//get the mid-point
	inline Point2 getMidPoint() const
	{
		return Point2::average( a, b );
	}


	//get a point on the line
	inline Point2 getPoint(double t) const
	{
		return Point2::lerp( a, b, t );
	}


	//return the closest point on this, to p
	inline Point2 closestPointTo(const Point2 &p, double &t) const
	{
		Vector2 ab = b - a;
		Vector2 ap = p - a;

		//t is the position of p along ab
		//if t == 0: closest point is at a
		//if t == 1: closest point is at b
		t = ap.dot( ab )  /  ab.dot( ab );

		//clamp t to the region [0,1]; between a and b
		t = clamp( t, 0.0, 1.0 );
		return a  +  ab * t;
	}

	inline boost::python::tuple py_closestPointTo(const Point2 &p) const
	{
		double t = 0.0;
		Point2 point = closestPointTo( p, t );
		return boost::python::make_tuple( t, point );
	}

	//return the closest point on this, to p
	inline Point2 closestPointTo(const Point2 &p) const
	{
		double t;
		return closestPointTo( p, t );
	}

	//distance from the line to p
	inline double distanceTo(const Point2 &p) const
	{
		return p.distanceTo( closestPointTo( p ) );
	}

	//distance squared from the line to p
	inline double sqrDistanceTo(const Point2 &p) const
	{
		return p.sqrDistanceTo( closestPointTo( p ) );
	}


	//is p on the left side of the line?
	inline bool onLeft(const Point2 &p) const
	{
		return Point2::areaOfTrianglex2( a, b, p )  >  0.0;
	}

	//is p on the line or on the left side of the line?
	inline bool onOrLeft(const Point2 &p) const
	{
		return Point2::areaOfTrianglex2( a, b, p )  >=  0.0;
	}

	//is p on the right side of the line?
	inline bool onRight(const Point2 &p) const
	{
		return Point2::areaOfTrianglex2( a, b, p )  <  0.0;
	}

	//is p on the line or on the right side of the line?
	inline bool onOrRight(const Point2 &p) const
	{
		return Point2::areaOfTrianglex2( a, b, p )  <=  0.0;
	}

	//is p on the line?
	inline bool on(const Point2 &p) const
	{
		return Point2::areaOfTrianglex2( a, b, p )  ==  0.0;
	}

	//does the line separate p and q
	inline bool separates(const Point2 &p, const Point2 &q) const
	{
		return Point2::separates( a, b, p, q );
	}


	//see if p is within the bounds of the line
	inline bool boundsContain(const Point2 &p) const
	{
		//get the distance along the direction of p, a, and b
		Vector2 dir = getDirection();
		double pAlongLine = p.dot( dir );
		double aAlongLine = a.dot( dir );
		double bAlongLine = b.dot( dir );

		//p is between a and b
		return  ( pAlongLine >= aAlongLine  &&  pAlongLine <= bAlongLine )  ||
				  ( pAlongLine >= bAlongLine  &&  pAlongLine <= aAlongLine );
	}


	//compute the intersection between line and this
	//returns:
	//		true if intersection found

	//	t will contain the distance along the line of the intersection
	// intersection will contain the intersection point
	bool intersect(const Segment2 &seg, double &t, Point2 &intersection) const;

	inline boost::python::tuple py_intersect(const Segment2 &seg) const
	{
		double t = 0.0;
		Point2 intersection;
		bool success = intersect( seg, t, intersection );
		return boost::python::make_tuple( success, t, intersection );
	}

	bool intersects(const Segment2 &seg) const;
};


#endif
