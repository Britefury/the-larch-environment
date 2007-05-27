//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef GRAPHVIEWLINKCURVETABLE_CPP__
#define GRAPHVIEWLINKCURVETABLE_CPP__

#include <Util/Array.h>

#include <Math/BBox2.h>
#include <Math/ConvexHull2.h>
#include <Math/Bezier2Util.h>

#include <GraphViewHelper/GraphViewLinkCurveTable.h>



GraphViewLinkCurveTable::TableEntry::TableEntry() : bValid( false )
{
}

GraphViewLinkCurveTable::TableEntry::TableEntry(const Point2 &a, const Point2 &b, const Point2 &c, const Point2 &d)
										: a( a ), b( b ), c( c ), d( d )
{
	hull = bezierCurveConvexHull( a, b, c, d );
	box = BBox2();
	box.addPoint( a );
	box.addPoint( b );
	box.addPoint( c );
	box.addPoint( d );
	bValid = true;
}






GraphViewLinkCurveTable::GraphViewLinkCurveTable()
{
}


int GraphViewLinkCurveTable::addLinkCurve(const Point2 &a, const Point2 &b, const Point2 &c, const Point2 &d)
{
	int linkId = -1;

	if ( freeLinkIds.size() == 0 )
	{
		linkId = table.size();
		table.push_back();
	}
	else
	{
		linkId = freeLinkIds.back();
		freeLinkIds.pop_back();
	}

	table[linkId] = TableEntry( a, b, c, d );
	return linkId;
}

void GraphViewLinkCurveTable::setLinkCurve(int linkId, const Point2 &a, const Point2 &b, const Point2 &c, const Point2 &d)
{
	table[linkId] = TableEntry( a, b, c, d );
}

void GraphViewLinkCurveTable::removeLinkCurve(int linkId)
{
	table[linkId].bValid = false;
	freeLinkIds.push_back( linkId );
}



boost::python::list GraphViewLinkCurveTable::getIntersectingLinkList(const BBox2 &box) const
{
	boost::python::list result;

	for (int entryI = 0; entryI < table.size(); entryI++)
	{
		const TableEntry &entry = table[entryI];
		if ( entry.bValid )
		{
			if ( entry.box.intersects( box ) )
			{
				if ( entry.hull.intersects( box ) )
				{
					result.append( entryI );
				}
			}
		}
	}

	return result;
}

int GraphViewLinkCurveTable::getLinkClosestToPoint(const Point2 &point, double maxDist, double epsilon) const
{
	boost::python::list result;

	double bestSqrDist = maxDist * maxDist;
	int bestIndex = -1;

	for (int entryI = table.size() - 1; entryI >= 0; entryI--)
	{
		const TableEntry &entry = table[entryI];
		if ( entry.bValid )
		{
			if ( entry.box.sqrDistanceTo( point )  <  bestSqrDist )
			{
				if ( entry.hull.sqrDistanceTo( point )  <  bestSqrDist )
				{
					double sqrDist = sqrDistanceToBezierCurve( point, entry.a, entry.b, entry.c, entry.d, epsilon );
					if ( sqrDist < bestSqrDist )
					{
						bestSqrDist = sqrDist;
						bestIndex = entryI;
					}
				}
			}
		}
	}

	return bestIndex;
}






#endif // GRAPHVIEWLINKCURVETABLE_CPP__


