//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef GRAPHVIEWLINKCURVETABLE_H__
#define GRAPHVIEWLINKCURVETABLE_H__

#include <boost/python.hpp>
using namespace boost::python;



#include <Util/Array.h>

#include <Math/BBox2.h>
#include <Math/ConvexHull2.h>

class _DllExport_ GraphViewLinkCurveTable
{
private:
	class _DllExport_ TableEntry
	{
	public:
		Point2 a, b, c, d;
		ConvexHull2 hull;
		BBox2 box;
		bool bValid;

		TableEntry();
		TableEntry(const Point2 &a, const Point2 &b, const Point2 &c, const Point2 &d);
	};

	Array<TableEntry> table;
	Array<int> freeLinkIds;



public:
	GraphViewLinkCurveTable();


	int addLinkCurve(const Point2 &a, const Point2 &b, const Point2 &c, const Point2 &d);
	void setLinkCurve(int linkId, const Point2 &a, const Point2 &b, const Point2 &c, const Point2 &d);
	void removeLinkCurve(int linkId);


	boost::python::list getIntersectingLinkList(const BBox2 &box) const;
	int getLinkClosestToPoint(const Point2 &point, double maxDist, double epsilon) const;
};



#endif // GRAPHVIEWLINKCURVETABLE_H__


