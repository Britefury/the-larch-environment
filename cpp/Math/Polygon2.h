//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef POLYGON2_H__
#define POLYGON2_H__

#include <vector>

#include <Math/Point2.h>
#include <Math/Segment2.h>
#include <Math/Side.h>
#include <Math/Xform2.h>

#include <boost/python.hpp>




class _DllExport_ Polygon2
{
private:
	std::vector<Point2> vertices;


public:
	Polygon2();
	Polygon2(const std::vector<Point2> &vts);
	Polygon2(boost::python::list verts);
	~Polygon2();


	int size() const;
	Point2 & operator[](int i);
	const Point2 & operator[](int i) const;

	Point2 & py__getitem__(int i);
	void py__setitem__(int i, const Point2 &v);

	Point2 & firstVertex();
	const Point2 & firstVertex() const;
	Point2 & lastVertex();
	const Point2 & lastVertex() const;
	const std::vector<Point2> & getVertices() const;

	void addVertex(const Point2 &v);
	void reserve(int n);
	void resize(int n);
	void removeVertex(int i);
	void clear();



private:
	bool checkEdgeIntersection(const Segment2 &seg) const;


public:
	// Determine the 'side' of @point
	Side side(const Point2 &point) const;


	bool contains(const Point2 &p) const;
	bool containsAllOf(const Segment2 &seg) const;
	bool py_containsAllOfSegment(const Segment2 &seg) const;
	bool containsPartOf(const Segment2 &seg) const;
	bool py_containsPartOfSegment(const Segment2 &seg) const;
	bool containsAllOf(const Polygon2 &polygon) const;
	bool py_containsAllOfPolygon(const Polygon2 &polygon) const;
	bool containsPartOf(const Polygon2 &polygon) const;
	bool py_containsPartOfPolygon(const Polygon2 &polygon) const;


	double computeAreaX2() const;

	double computeArea() const;
	bool isClockwise() const;

	bool isConvex() const;
	bool isSelfIntersecting() const;




	//transform functions need acess to private members
	friend _DllExport_ void operator*=(Polygon2 &p, const Xform2 &x);
	friend _DllExport_ Polygon2 operator*(const Polygon2 &p, const Xform2 &x);
};


_DllExport_ void operator*=(Polygon2 &p, const Xform2 &x);
_DllExport_ Polygon2 operator*(const Polygon2 &p, const Xform2 &x);


#endif
