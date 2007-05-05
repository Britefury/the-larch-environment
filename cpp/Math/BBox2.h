//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef BBOX2_H__
#define BBOX2_H__

#include <stdio.h>

#include <algorithm>

#include <Math/Point2.h>
#include <Math/Segment2.h>
#include <Math/Polygon2.h>
#include <Math/Side.h>
#include <Math/Axis.h>
#include <Math/Xform2.h>

#include <boost/python.hpp>



/*
2D BOUNDING BOX
*/




class _DllExport_ BBox2
{
public:
	//lower and upper corners
	Point2 l, u;

public:
	inline BBox2() : l( 1.0, 1.0 ), u( -1.0, -1.0 )
	{
	}

	inline BBox2(const Point2 &p) : l( p ), u( p )
	{
	}

	inline BBox2(const Point2 &lower, const Point2 &upper)
	{
		l = Point2::min( lower, upper );
		u = Point2::max( lower, upper );
	}

	inline BBox2(const Polygon2 &poly)
	{
		if ( poly.size() == 0 )
		{
			return;
		}

		l = poly[0];
		u = poly[0];

		for (int i = 1; i < poly.size(); i++)
		{
			addPoint( poly[i] );
		}
	}





	inline void read(FILE *f)
	{
		l.read( f );
		u.read( f );
	}

	inline void write(FILE *f) const
	{
		l.write( f );
		u.write( f );
	}




	//check if the box is empty
	inline bool isEmpty() const
	{
		return l.x > u.x  ||  l.y > u.y;
	}


	//if necessary, expand the box to include p
	inline void addPoint(const Point2 &p)
	{
		if ( isEmpty() )
		{
			l = u = p;
		}
		else
		{
			l = Point2::min( l, p );
			u = Point2::max( u, p );
		}
	}

	//if necessary, expand the box to include b
	inline void addBox(const BBox2 &b)
	{
		if ( !b.isEmpty() )
		{
			if ( isEmpty() )
			{
				*this = b;
			}
			else
			{
				l = Point2::min( l, b.l );
				u = Point2::max( u, b.u );
			}
		}
	}


	//detemine which side of @seg, this is on
	inline Side side(const Segment2 &seg) const
	{
		//get the normal of the line, and use it to obtain the leading and
		//trailing vertices of the box.
		Vector2 normal = seg.getNormal();
		Point2 leading = getLeadingVertex( normal );
		Point2 trailing = getTrailingVertex( normal );

		//normal points to the left of the line, so if the trailing vertex
		//is to the left of the line, so is the whole box
		if ( seg.onLeft( trailing ) )
		{
			return SIDE_POSITIVE;
		}
		else
		{
			//if the leading vertex is to the right of the line, then so is the
			//whole box
			if ( seg.onRight( leading ) )
			{
				return SIDE_NEGATIVE;
			}
			else
			{
				//the trailing and leading vertices of the box are on different
				//sides of the line, so the box lies on both sides of the line
				return SIDE_BOTH;
			}
		}
	}



	inline bool intersects(const Segment2 &seg) const
	{
		// Segment->box intersection code based on the algorithm used by David Eberly,
		// http://www.geometrictools.com

		double segExtents[2], diffExtents[2], rhs;

		Vector2 delta = seg.getMidPoint() - getCentre();
		Vector2 segDir = seg.getDirection()  *  0.5;
		Vector2 boxExtents = getSize() * 0.5;


		segExtents[0] = fabs( segDir.x );
		diffExtents[0] = fabs( delta.x );
		rhs = boxExtents.x + segExtents[0];
		if ( diffExtents[0] > rhs )
		{
			return false;
		}

		segExtents[1] = fabs( segDir.y );
		diffExtents[1] = fabs( delta.y );
		rhs = boxExtents.y + segExtents[1];
		if ( diffExtents[1] > rhs )
		{
			return false;
		}


		Vector2 perp = segDir.perpendicular();
		double lhs = fabs( perp.dot( delta ) );
		double part0 = fabs( perp.x );
		double part1 = fabs( perp.y );
		rhs = boxExtents.x * part0  +  boxExtents.y * part1;
		return lhs <= rhs;
	}

	//check if @this intersects @b
	inline bool intersects(const BBox2 &b) const
	{
		return l.x <= b.u.x  &&  u.x >= b.l.x   &&   l.y <= b.u.y  &&  u.y >= b.l.y;
	}

	inline bool py_intersectsSegment(const Segment2 &seg) const
	{
		return intersects( seg );
	}

	inline bool py_intersectsBox(const BBox2 &box) const
	{
		return intersects( box );
	}



	//compute the intersection of @this and @b
	inline BBox2 intersection(const BBox2 &b) const
	{
		BBox2 result;
		result.l = Point2::max( l, b.l );
		result.u = Point2::min( u, b.u );
		return result;
	}


	//compute the closest point in the box to @p
	inline Point2 closestPointTo(const Point2 &p) const
	{
		return Point2::min( Point2::max( p, l ), u );
	}

	inline double sqrDistanceTo(const Point2 &p) const
	{
		return closestPointTo( p ).sqrDistanceTo( p );
	}

	inline double distanceTo(const Point2 &p) const
	{
		return closestPointTo( p ).distanceTo( p );
	}


	//check if p is within the bounding box
	inline bool contains(const Point2 &p) const
	{
		return ( p.x >= l.x )  &&  ( p.x <= u.x )  &&
				 ( p.y >= l.y )  &&  ( p.y <= u.y );
	}

	//check if @seg is wholly within the bounding box
	inline bool containsAllOf(const Segment2 &seg) const
	{
		//if the end points are contained within this, then so is all of the line
		return contains( seg.a )  &&  contains( seg.b );
	}

	inline bool py_containsAllOfSegment(const Segment2 &seg) const
	{
		return containsAllOf( seg );
	}


	//check if @seg is partially within the bounding box
	inline bool containsPartOf(const Segment2 &seg) const
	{
		//if either end point is within this, then @seg is partially
		//within this
		if ( contains( seg.a )  ||  contains( seg.b ) )
		{
			return true;
		}

		//neither end point is within this, but, the segment may pass through
		//this: use intersects() to detect this
		return intersects( seg );
	}

	inline bool py_containsPartOfSegment(const Segment2 &seg) const
	{
		return containsPartOf( seg );
	}


	// Check if @polygon is wholly within the bounding box
	inline bool containsAllOf(const Polygon2 &polygon) const
	{
		// If any vertex of @polygon is outside @this, then @polygon is not wholly inside @this
		for (int polyI = 0; polyI < polygon.size(); polyI++)
		{
			if ( !contains( polygon[polyI] ) )
			{
				return false;
			}
		}

		// All vertices of @polygon are inside @this
		return true;
	}

	inline bool py_containsAllOfPolygon(const Polygon2 &polygon) const
	{
		return containsAllOf( polygon );
	}

	// Check if @polygon is partially within the bounding box
	inline bool containsPartOf(const Polygon2 &polygon) const
	{
		// If any vertex of @polygon is inside @this, then @polygon is partially inside @this
		for (int polyI = 0; polyI < polygon.size(); polyI++)
		{
			if ( contains( polygon[polyI] ) )
			{
				return true;
			}
		}

		// If any corner of @this is inside @polygon, then @polygon is partially inside @this
		for (int cornerI = 0; cornerI < 4; cornerI++)
		{
			if ( polygon.side( getPoint( cornerI ) )  !=  SIDE_NEGATIVE )
			{
				return true;
			}
		}

		// If any edge of @polygon is inside @this, then @polygon is partially inside @this
		int prevEdgeI = polygon.size() - 1;
		for (int edgeI = 0; edgeI < polygon.size(); edgeI++)
		{
			Segment2 edge( polygon[prevEdgeI], polygon[edgeI] );
			if ( containsPartOf( edge ) )
			{
				return true;
			}
		}

		// No part of @polygon is inside @this
		return false;
	}

	inline bool py_containsPartOfPolygon(const Polygon2 &polygon) const
	{
		return containsPartOf( polygon );
	}




	//get lower and upper corners, and centre
	inline Point2 getLower() const
	{
		return l;
	}

	inline Point2 getUpper() const
	{
		return u;
	}

	inline Point2 getCentre() const
	{
		return Point2( ( l.x + u.x )  *  0.5,
					( l.y + u.y )  *  0.5 );
	}


	//get one of the (four) corners
	inline Point2 getPoint(int i) const
	{
		switch( i )
		{
		case 0:
			return l;
		case 1:
			return Point2( u.x, l.y );
		case 2:
			return Point2( l.x, u.y );
		case 3:
			return u;
		default:
			return l;
		}
	}

	//get one of the four edges that define the box
	inline Segment2 getEdge(int i) const
	{
		switch( i )
		{
		case 0:
			return Segment2( getPoint( 0 ), getPoint( 1 ) );
		case 1:
			return Segment2( getPoint( 1 ), getPoint( 3 ) );
		case 2:
			return Segment2( getPoint( 3 ), getPoint( 2 ) );
		case 3:
			return Segment2( getPoint( 2 ), getPoint( 0 ) );
		default:
			return Segment2( getPoint( 0 ), getPoint( 1 ) );
		}
	}

	//get bounds
	inline double getBounds(Axis axis) const
	{
		switch( axis )
		{
		case AXIS_NEGATIVE_X:
			return l.x;
		case AXIS_X:
			return u.x;
		case AXIS_NEGATIVE_Y:
			return l.y;
		case AXIS_Y:
			return u.y;
		default:
			return 0.0;
		}
	}

	//leading and trailing vertices
	//LEADING VERTEX: the vertex of the box further along the direction vector
	inline Point2 getLeadingVertex(const Vector2 &direction) const
	{
		return Point2( direction.x < 0.0  ?  l.x : u.x,
					direction.y < 0.0  ?  l.y : u.y );
	}

	//TRAILING VERTEX: the opposite corner to the LEADING VERTEX
	inline Point2 getTrailingVertex(const Vector2 &direction) const
	{
		return Point2( direction.x < 0.0  ?  u.x : l.x,
					direction.y < 0.0  ?  u.y : l.y );
	}



	//get dimensions
	inline double getWidth() const
	{
		return u.x - l.x;
	}

	inline double getHeight() const
	{
		return u.y - l.y;
	}

	inline Vector2 getSize() const
	{
		return u - l;
	}

	inline Vector2 py_getSizeVector() const
	{
		return getSize();
	}

	inline double getSize(Axis axis) const
	{
		switch( axis )
		{
		case AXIS_NEGATIVE_X:
		case AXIS_X:
			return getWidth();
		case AXIS_NEGATIVE_Y:
		case AXIS_Y:
			return getHeight();
		default:
			return 0.0;
		}
	}

	inline double py_getSizeInAxis(Axis axis) const
	{
		return getSize( axis );
	}


	//split the box at x = position, place result boxes in lowerBox and
	//upper Box
	inline void splitX(double position, BBox2 &lowerBox, BBox2 &upperBox) const
	{
		lowerBox = *this;
		upperBox = *this;
		lowerBox.u.x = position;
		upperBox.l.x = position;
	}

	inline boost::python::tuple py_splitX(double position) const
	{
		BBox2 lowerBox, upperBox;
		splitX( position, lowerBox, upperBox );
		return boost::python::make_tuple( lowerBox, upperBox );
	}

	//split the box at y = position
	inline void splitY(double position, BBox2 &lowerBox, BBox2 &upperBox) const
	{
		lowerBox = *this;
		upperBox = *this;
		lowerBox.u.y = position;
		upperBox.l.y = position;
	}

	inline boost::python::tuple py_splitY(double position) const
	{
		BBox2 lowerBox, upperBox;
		splitY( position, lowerBox, upperBox );
		return boost::python::make_tuple( lowerBox, upperBox );
	}



	//get major and minor axis
	inline Axis getMajorAxis() const
	{
		Axis result = AXIS_X;
		double size = getWidth();

		double height = getHeight();

		if ( height > size )
		{
			size = height;
			result = AXIS_Y;
		}

		return result;
	}

	inline Axis getMinorAxis() const
	{
		Axis result = AXIS_X;
		double size = getWidth();

		double height = getHeight();

		if ( height < size )
		{
			size = height;
			result = AXIS_Y;
		}

		return result;
	}

	//get major and minor size
	inline double getMajorSize() const
	{
		double size = getWidth();

		double height = getHeight();

		if ( height > size )
		{
			size = height;
		}

		return size;
	}

	inline double getMinorSize() const
	{
		double size = getWidth();

		double height = getHeight();

		if ( height < size )
		{
			size = height;
		}

		return size;
	}


	friend BBox2 _DllExport_ operator*(const BBox2 &box, const Xform2 &x);
};


inline BBox2 _DllExport_ operator*(const BBox2 &box, const Xform2 &x)
{
	return BBox2( box.l * x, box.u * x );
}


#endif
