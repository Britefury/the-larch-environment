//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef SEGMENT2_CPP__
#define SEGMENT2_CPP__

#include <Math/Segment2.h>



void Segment2::read(FILE *f)
{
//	a.read( f );
//	b.read( f );
}

void Segment2::write(FILE *f) const
{
//	a.write( f );
//	b.write( f );
}




bool Segment2::intersect(const Segment2 &seg, double &t, Point2 &intersection) const
{
	//need the normal of the other line, represent line as a normal
	//and distance from origin instead of two points
	Vector2 normal = seg.getNormal();


	//raytrace
	double nDotDirection = normal.dot( getDirection() );

	if ( nDotDirection == 0.0 )
	{
		return false;
	}

	//distance from line to origin
	double d = seg.a.dot( normal );

	t = ( d - a.dot( normal ) )  /  nDotDirection;
	intersection = a  +  ( getDirection() * t );

	//t must be between 0 and 1 (intersection between a and b)
	//check that the intersection is within the bounds of line
	return ( t >= 0.0 )  &&  ( t <= 1.0 )  &&
			 seg.boundsContain( intersection );
}


bool Segment2::intersects(const Segment2 &seg) const
{
	return Point2::segmentsIntersect( a, b, seg.a, seg.b );
}


#endif
