//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef CONVEXHULL2_H__
#define CONVEXHULL2_H__

#include <boost/python.hpp>




#include <Util/Array.h>
#include <Util/gsassert.h>

#include <Math/Point2.h>
#include <Math/Side.h>
#include <Math/BBox2.h>



class _DllExport_ ConvexHull2
{
private:
	Array<Point2> vertices;


public:
	ConvexHull2();
	ConvexHull2(const Array<Point2> &points);
	ConvexHull2(boost::python::list points);
	~ConvexHull2();


	int size() const;
	Point2 & operator[](int i);
	const Point2 & operator[](int i) const;

	Point2 & py__getitem__(int i);
	void py__setitem__(int i, const Point2 &v);

	Point2 & firstVertex();
	const Point2 & firstVertex() const;
	Point2 & lastVertex();
	const Point2 & lastVertex() const;
	const Array<Point2> & getVertices() const;

	void addPoint(const Point2 &p);
	void clear();



private:
	bool checkEdgeIntersection(const Segment2 &seg) const;


public:
	double sqrDistanceTo(const Point2 &point) const;
	double distanceTo(const Point2 &point) const;


	// Determine the 'side' of @point
	Side side(const Point2 &point) const;


	bool contains(const Point2 &p) const;
	bool containsAllOf(const Segment2 &seg) const;
	bool py_containsAllOfSegment(const Segment2 &seg) const;
	bool containsPartOf(const Segment2 &seg) const;
	bool py_containsPartOfSegment(const Segment2 &seg) const;

	bool intersects(const BBox2 &box) const;


	double computeAreaX2() const;

	double computeArea() const;

	BBox2 boundingBox() const;
};


#endif
