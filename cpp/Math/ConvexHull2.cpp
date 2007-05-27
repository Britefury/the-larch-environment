//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef CONVEXHULL2_CPP__
#define CONVEXHULL2_CPP__

#include <math.h>

#include <algorithm>

#include <Math/Segment2.h>

#include <Math/index.h>

#include <Math/ConvexHull2.h>




ConvexHull2::ConvexHull2()
{
}

ConvexHull2::ConvexHull2(const Array<Point2> &points)
{
	for (int pointI = 0; pointI < points.size(); pointI++)
	{
		addPoint( points[pointI] );
	}
}

ConvexHull2::ConvexHull2(boost::python::list points)
{
	boost::python::object lenObj = points.attr( "__len__" )();
	boost::python::extract<int> lenExtract( lenObj );
	if ( lenExtract.check() )
	{
		int numPoints = lenExtract;
		for (int pointI = 0; pointI < numPoints; pointI++)
		{
			boost::python::object pointObj = points[pointI];
			boost::python::extract<Point2&> pointExtract( pointObj );
			if ( pointExtract.check() )
			{
				addPoint( pointExtract );
			}
		}
	}
}

ConvexHull2::~ConvexHull2()
{
}



int ConvexHull2::size() const
{
	return vertices.size();
}

Point2 & ConvexHull2::operator[](int i)
{
	return vertices[i];
}

const Point2 & ConvexHull2::operator[](int i) const
{
	return vertices[i];
}


Point2 & ConvexHull2::py__getitem__(int i)
{
	gs_assert( i >= -size()  &&  i < size(), "ConvexHull2::py__getitem__(): index out of range\n" );
	if ( i < 0 )
	{
		i += size();
	}
	return vertices[i];
}

void ConvexHull2::py__setitem__(int i, const Point2 &v)
{
	gs_assert( i >= -size()  &&  i < size(), "ConvexHull2::py__setitem__(): index out of range\n" );
	if ( i < 0 )
	{
		i += size();
	}
	vertices[i] = v;
}


Point2 & ConvexHull2::firstVertex()
{
	return vertices.front();
}

const Point2 & ConvexHull2::firstVertex() const
{
	return vertices.front();
}

Point2 & ConvexHull2::lastVertex()
{
	return vertices.back();
}

const Point2 & ConvexHull2::lastVertex() const
{
	return vertices.back();
}

const Array<Point2> & ConvexHull2::getVertices() const
{
	return vertices;
}


void ConvexHull2::addPoint(const Point2 &p)
{
	if ( vertices.size() < 2 )
	{
		if ( !vertices.contains( p ) )
		{
			vertices.push_back( p );
		}
	}
	else if ( vertices.size() == 2 )
	{
		Segment2 seg( vertices[0], vertices[1] );
		if ( seg.onLeft( p ) )
		{
			vertices.push_back( p );
		}
		else if ( seg.onRight( p ) )
		{
			vertices.insert( 1, p );
		}
	}
	else
	{
		// Find the first edge for which @p is on the positive side of the line defined by the edge
		int firstOnOrLeft = -1;
		for (int vertI = 0; vertI < vertices.size(); vertI++)
		{
			int vertJ = nextIndex( vertI, vertices.size() );
			Segment2 seg( vertices[vertI], vertices[vertJ] );
			if ( seg.onOrLeft( p ) )
			{
				firstOnOrLeft = vertI;
				break;
			}
		}

		gs_assert( firstOnOrLeft != -1, "ConvexHull2::addPoint(): could not find first segment which has @p to the left\n" );


		// Starting with at the edge after the first edge for which @p is on the positive side (found in previous step),
		// find the first edge for which @p is on the negative side
		int vertI = nextIndex( firstOnOrLeft, vertices.size() );
		int firstOnRight = -1;
		for (int i = 0; i < vertices.size(); i++)
		{
			int vertJ = nextIndex( vertI, vertices.size() );
			Segment2 seg( vertices[vertI], vertices[vertJ] );
			if ( seg.onRight( p ) )
			{
				firstOnRight = vertI;
			}
			vertI = vertJ;
		}


		// If @firstOnRight is -1, then @p is on the left of all edges, meaning that it is inside the hull; do nothing in this case
		if ( firstOnRight != -1 )
		{
			// Starting the the first edge for which @p is on the negative side (found in previous step), look at the next
			// edge, and see if @p is on the negative side. If so, remove the vertex shared by both edges.
			int vertI = firstOnRight;
			int vertJ = nextIndex( vertI, vertices.size() );
			int vertK = nextIndex( vertJ, vertices.size() );
			Segment2 segB( vertices[vertJ], vertices[vertK] );

			while ( segB.onRight( p ) )
			{
				vertices.remove( vertJ );

				if ( vertJ < vertI )
				{
					vertI--;
				}
				vertJ = nextIndex( vertI, vertices.size() );
				vertK = nextIndex( vertJ, vertices.size() );
				segB = Segment2( vertices[vertJ], vertices[vertK] );
			}

			// Insert @p after @vertI
			vertices.insert( vertI + 1, p );
		}
	}
}

void ConvexHull2::clear()
{
	vertices.clear();
}




bool ConvexHull2::checkEdgeIntersection(const Segment2 &seg) const
{
	if ( vertices.size() < 2 )
	{
		return false;
	}
	else if ( vertices.size() == 2 )
	{
		return Segment2( vertices.front(), vertices.back() ).intersects( seg );
	}
	else
	{
		int iPrev = vertices.size() - 1;
		for (int i = 0; i < vertices.size(); i++)
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
}



double ConvexHull2::sqrDistanceTo(const Point2 &point) const
{
	if ( contains( point ) )
	{
		return 0.0;
	}
	else
	{
		double sqrDist = Segment2( vertices.back(), vertices.front() ).sqrDistanceTo( point );

		for (int i = 1; i < vertices.size(); i++)
		{
			double d2 = Segment2( vertices[i-1], vertices[i] ).sqrDistanceTo( point );
			sqrDist = std::min( sqrDist, d2 );
		}

		return sqrDist;
	}
}

double ConvexHull2::distanceTo(const Point2 &point) const
{
	return sqrt( sqrDistanceTo( point ) );
}


// Determine the 'side' of @point
Side ConvexHull2::side(const Point2 &point) const
{
	if ( vertices.size() == 0 )
	{
		return SIDE_NEGATIVE;
	}
	else if ( vertices.size() == 1 )
	{
		return point == vertices.front()  ?  SIDE_ON  :  SIDE_NEGATIVE;
	}
	else if ( vertices.size() == 2 )
	{
		return Segment2( vertices[0], vertices[1] ).on( point )  ?  SIDE_ON  :  SIDE_NEGATIVE;
	}
	else
	{
		int rightCrossings = 0;
		int leftCrossings = 0;

		int iPrev = vertices.size() - 1;
		for (int i = 0; i < vertices.size(); i++)
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
}



bool ConvexHull2::contains(const Point2 &p) const
{
	return side( p )  !=  SIDE_NEGATIVE;
}

bool ConvexHull2::containsAllOf(const Segment2 &seg) const
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

bool ConvexHull2::py_containsAllOfSegment(const Segment2 &seg) const
{
	return containsAllOf( seg );
}

bool ConvexHull2::containsPartOf(const Segment2 &seg) const
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

bool ConvexHull2::py_containsPartOfSegment(const Segment2 &seg) const
{
	return containsPartOf( seg );
}


bool ConvexHull2::intersects(const BBox2 &box) const
{
	int vertI = vertices.size() - 1;
	for (int vertJ = 0; vertJ < vertices.size(); vertJ++)
	{
		if ( box.contains( vertices[vertI] )   ||   box.intersects( Segment2( vertices[vertI], vertices[vertJ] ) ) )
		{
			return true;
		}

		vertI = vertJ;
	}


	return false;
}




double ConvexHull2::computeAreaX2() const
{
	if ( vertices.size() < 3 )
	{
		return 0.0;
	}
	else
	{
		int iPrev = vertices.size() - 1;

		double areaX2 = 0.0;
		for (int i = 0; i < vertices.size(); i++)
		{
			//cross product of the vectors from the origin to:
			//previous vertex  AND  current vertex
			areaX2 += vertices[iPrev].x * vertices[i].y  -  vertices[iPrev].y * vertices[i].x;

			iPrev = i;
		}

		return areaX2;
	}
}

double ConvexHull2::computeArea() const
{
	return computeAreaX2()  *  0.5;
}



BBox2 ConvexHull2::boundingBox() const
{
	BBox2 box;

	for (int vertI = 0; vertI < vertices.size(); vertI++)
	{
		box.addPoint( vertices[vertI] );
	}

	return box;
}



#endif
