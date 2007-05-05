//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef POLYGON2_CPP__
#define POLYGON2_CPP__

#include <assert.h>

#include <Math/Polygon2.h>




Polygon2::Polygon2()
{
}

Polygon2::Polygon2(const std::vector<Point2> &vts)
				: vertices( vts )
{
}

Polygon2::Polygon2(boost::python::list verts)
{
	boost::python::object lenObj = verts.attr( "__len__" )();
	boost::python::extract<int> lenExtract( lenObj );
	if ( lenExtract.check() )
	{
		int numVerts = lenExtract;
		vertices.reserve( numVerts );
		for (int i = 0; i < numVerts; i++)
		{
			boost::python::object pointObj = verts[i];
			boost::python::extract<Point2&> pointExtract( pointObj );
			if ( pointExtract.check() )
			{
				vertices.push_back( pointExtract );
			}
		}
	}
}

Polygon2::~Polygon2()
{
}



int Polygon2::size() const
{
	return vertices.size();
}

Point2 & Polygon2::operator[](int i)
{
	return vertices[i];
}

const Point2 & Polygon2::operator[](int i) const
{
	return vertices[i];
}


Point2 & Polygon2::py__getitem__(int i)
{
	assert( i >= -size()  &&  i < size() );
	if ( i < 0 )
	{
		i += size();
	}
	return vertices[i];
}

void Polygon2::py__setitem__(int i, const Point2 &v)
{
	assert( i >= -size()  &&  i < size() );
	if ( i < 0 )
	{
		i += size();
	}
	vertices[i] = v;
}


Point2 & Polygon2::firstVertex()
{
	return vertices.front();
}

const Point2 & Polygon2::firstVertex() const
{
	return vertices.front();
}

Point2 & Polygon2::lastVertex()
{
	return vertices.back();
}

const Point2 & Polygon2::lastVertex() const
{
	return vertices.back();
}

const std::vector<Point2> & Polygon2::getVertices() const
{
	return vertices;
}


void Polygon2::addVertex(const Point2 &v)
{
	vertices.push_back( v );
}

void Polygon2::reserve(int n)
{
	vertices.reserve( n );
}

void Polygon2::resize(int n)
{
	vertices.resize( n );
}

void Polygon2::removeVertex(int i)
{
	vertices.erase( vertices.begin() + i );
}

void Polygon2::clear()
{
	vertices.clear();
}




bool Polygon2::checkEdgeIntersection(const Segment2 &seg) const
{
	int iPrev = vertices.size() - 1;
	for (int i = 0; i < size(); i++)
	{
		Segment2 edge( vertices[iPrev], vertices[i] );

		if ( edge.intersects( seg ) )
		{
			return true;
		}

		iPrev = i;
	}

	return false;
}


// Determine the 'side' of @point
Side Polygon2::side(const Point2 &point) const
{
	int rightCrossings = 0;
	int leftCrossings = 0;

	int iPrev = vertices.size() - 1;
	for (int i = 0; i < size(); i++)
	{
		//@point is inside if it lies on a vertex
		if ( point == vertices[i] )
		{
			return SIDE_ON;
		}

		//check if the edge at position @iPrev straddles the horizontal axis
		//that passes through @p
		bool rightStraddle = ( vertices[i].y > point.y )  !=  ( vertices[iPrev].y > point.y );
		bool leftStraddle = ( vertices[i].y < point.y )  !=  ( vertices[iPrev].y < point.y );

		if ( rightStraddle || leftStraddle )
		{
			double areax2 = Point2::areaOfTrianglex2( vertices[iPrev], vertices[i], point );

			bool edgePointsUp = vertices[i].y > vertices[iPrev].y;

			bool pOnLeft = edgePointsUp  ?  areax2 > 0.0  :  areax2 < 0.0;
			bool pOnRight = edgePointsUp  ?  areax2 < 0.0  :  areax2 > 0.0;

			if ( rightStraddle  &&  pOnLeft )
			{
				//right straddle AND @p on left of the edge; intersection between
				//the edge and the horizontal axis passing through @p lies
				//to the right of @p
				rightCrossings++;
			}
			if ( leftStraddle  &&  pOnRight )
			{
				//left straddle AND @p on right of the edge; intesection between
				//the edge and the horizontal axis passing through @o lies
				//to the left of @p
				leftCrossings++;
			}
		}

		iPrev = i;
	}


	//@p is on an edge if left and right cross counts do not have the
	//same parity
	if ( ( rightCrossings % 2 )  !=  ( leftCrossings %2 ) )
	{
		//@p is inside the boundary
		return SIDE_ON;
	}

	//@p is inside if the number of crossings is odd
	if ( ( rightCrossings % 2 )  ==  1 )
	{
		return SIDE_POSITIVE;
	}
	else
	{
		return SIDE_NEGATIVE;
	}
}



bool Polygon2::contains(const Point2 &p) const
{
	return side( p )  !=  SIDE_NEGATIVE;
}

bool Polygon2::containsAllOf(const Segment2 &seg) const
{
	//if the end points of @seg are contained within @this
	if ( contains( seg.a )  &&  contains( seg.b ) )
	{
		//if none of the edges of @this intersect @seg
		if ( !checkEdgeIntersection( seg ) )
		{
			//@seg is wholly contained by @this
			return true;
		}
	}

	return false;
}

bool Polygon2::py_containsAllOfSegment(const Segment2 &seg) const
{
	return containsAllOf( seg );
}

bool Polygon2::containsPartOf(const Segment2 &seg) const
{
	//if either end point of @seg is contained by @this, then @segs is partially
	//inside @this
	if ( contains( seg.a )  ||  contains( seg.b ) )
	{
		return true;
	}

	//if @seg intersects any of the edges of @this, then @seg is partially
	//inside @this
	if ( checkEdgeIntersection( seg ) )
	{
		return true;
	}

	return false;
}

bool Polygon2::py_containsPartOfSegment(const Segment2 &seg) const
{
	return containsPartOf( seg );
}

bool Polygon2::containsAllOf(const Polygon2 &polygon) const
{
	// If any vertex of @polygon is outside the boundary of @this, then
	// @polygon is not wholly inside @this
	for (int polyI = 0; polyI < polygon.size(); polyI++)
	{
		if ( !contains( polygon[polyI] ) )
		{
			return false;
		}
	}

	// If any edge of @polygon intersects @this, then
	// @polygon is not wholly contained within @this
	int edgeIPrev = polygon.size() - 1;
	for (int edgeI = 0; edgeI < polygon.size(); edgeI++)
	{
		Segment2 edge( polygon[edgeIPrev], polygon[edgeI] );

		if ( checkEdgeIntersection( edge ) )
		{
			return false;
		}

		edgeIPrev = edgeI;
	}

	return true;
}

bool Polygon2::py_containsAllOfPolygon(const Polygon2 &polygon) const
{
	return containsAllOf( polygon );
}

bool Polygon2::containsPartOf(const Polygon2 &polygon) const
{
	// If any vertex of @polygon is inside the boundary of @this, then
	// @polygon is partially inside @this
	for (int polyI = 0; polyI < polygon.size(); polyI++)
	{
		if ( contains( polygon[polyI] ) )
		{
			return true;
		}
	}

	// If any vertex of @this is inside the boundary of @polygon, then
	// @polygon is partially inside @this
	for (int vertexI = 0; vertexI < size(); vertexI++)
	{
		if ( polygon.contains( vertices[vertexI] ) )
		{
			return true;
		}
	}

	// If any edge of @polygon intersects @this, then
	// @polygon is partially inside @this
	int edgeIPrev = polygon.size() - 1;
	for (int edgeI = 0; edgeI < polygon.size(); edgeI++)
	{
		Segment2 edge( polygon[edgeIPrev], polygon[edgeI] );

		if ( checkEdgeIntersection( edge ) )
		{
			return true;
		}

		edgeIPrev = edgeI;
	}

	// No intersection at all
	return false;
}

bool Polygon2::py_containsPartOfPolygon(const Polygon2 &polygon) const
{
	return containsPartOf( polygon );
}



double Polygon2::computeAreaX2() const
{
	int iPrev = vertices.size() - 1;

	double areaX2 = 0.0;
	for (int i = 0; i < size(); i++)
	{
		//cross product of the vectors from the origin to:
		//previous vertex  AND  current vertex
		areaX2 += vertices[iPrev].x * vertices[i].y  -  vertices[iPrev].y * vertices[i].x;

		iPrev = i;
	}

	return areaX2;
}

double Polygon2::computeArea() const
{
	return computeAreaX2()  *  0.5;
}

bool Polygon2::isClockwise() const
{
	return computeAreaX2()  <  0.0;
}



bool Polygon2::isConvex() const
{
	//compare the direction of each triangle consisting of 3 consecutive vertices
	//this is identical to doing an 'on left' test, comparing each vertex to
	//the edge composed of the previous two, and checking for direction changes,
	//e.g. some on left, others on right

	//vertex indices
	int a = vertices.size() - 2, b = vertices.size() - 1, c = 0;
	//triangle edges from b to a, and b to c
	Vector2 edge0 = vertices[a] - vertices[b], edge1 = vertices[c] - vertices[b];

	//on left?
	bool onLeft = edge0.cross( edge1 )  >  0.0;

	//next
	a = b;
	b = c;
	edge0 = -edge1;		//next edge 0 is edge 1 reversed

	for (int c = 1; c < size(); c++)
	{
		//compute new edge 1
		edge1 = vertices[c] - vertices[b];
		//counter-clockwise?
		bool thisOnLeft = edge0.cross( edge1 )  >  0.0;

		//if it is not the same as last time, there has been a direction change,
		//and the polygon is not convex
		if ( thisOnLeft != onLeft )
		{
			return false;
		}

		a = b;
		b = c;
		edge0 = -edge1;
	}

	return true;
}

bool Polygon2::isSelfIntersecting() const
{
	if ( vertices.size() < 4 )
	{
		return false;
	}


	int edge0Prev = vertices.size() - 1;
	for (int edge0 = 0; edge0 < size(); edge0++)
	{
		int edge1Prev = vertices.size() - 1;
		for (int edge1 = 0; edge1 < size(); edge1++)
		{
			if ( edge1 != edge0Prev  &&  edge1Prev != edge0Prev  &&  edge1Prev != edge0 )
			{
				if ( Point2::segmentsIntersect( vertices[edge0Prev], vertices[edge0], vertices[edge1Prev], vertices[edge1] ) )
				{
					return true;
				}
			}

			edge1Prev = edge1;
		}

		edge0Prev = edge0;
	}

	return false;
}



_DllExport_ void operator*=(Polygon2 &p, const Xform2 &x)
{
	for (int i = 0; i < p.size(); i++)
	{
		p.vertices[i] *= x;
	}
}

_DllExport_ Polygon2 operator*(const Polygon2 &p, const Xform2 &x)
{
	Polygon2 result = p;
	result *= x;
	return result;
}


#endif
