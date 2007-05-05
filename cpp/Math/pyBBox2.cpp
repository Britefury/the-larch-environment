//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef PYBBOX2_CPP
#define PYBBOX2_CPP

#include <Math/BBox2.h>

#include <boost/python.hpp>
using namespace boost::python;


void export_BBox2()
{
	class_<BBox2>( "BBox2", init<>() )
		.def( init<Point2>() )
		.def( init<Point2, Point2>() )
		.def( init<Polygon2>() )
		.def( init<const BBox2 &>() )
		.def_readwrite( "_lower", &BBox2::l )
		.def_readwrite( "_upper", &BBox2::u )
		.def( "isEmpty", &BBox2::isEmpty )
		.def( "addPoint", &BBox2::addPoint )
		.def( "addBox", &BBox2::addBox )
		.def( "side", &BBox2::side )
		.def( "intersects", &BBox2::py_intersectsSegment )
		.def( "intersects", &BBox2::py_intersectsBox )
		.def( "intersection", &BBox2::intersection )
		.def( "closestPointTo", &BBox2::closestPointTo )
		.def( "sqrDistanceTo", &BBox2::sqrDistanceTo )
		.def( "distanceTo", &BBox2::distanceTo )
		.def( "contains", &BBox2::contains )
		.def( "containsAllOf", &BBox2::py_containsAllOfSegment )
		.def( "containsPartOf", &BBox2::py_containsPartOfSegment )
		.def( "containsAllOf", &BBox2::py_containsAllOfPolygon )
		.def( "containsPartOf", &BBox2::py_containsPartOfPolygon )
		.def( "getLower", &BBox2::getLower )
		.def( "getUpper", &BBox2::getUpper )
		.def( "getCentre", &BBox2::getCentre )
		.def( "getPoint", &BBox2::getPoint )
		.def( "getEdge", &BBox2::getEdge )
		.def( "getBounds", &BBox2::getBounds )
		.def( "getLeadingVertex", &BBox2::getLeadingVertex )
		.def( "getTrailingVertex", &BBox2::getTrailingVertex )
		.def( "getWidth", &BBox2::getWidth )
		.def( "getHeight", &BBox2::getHeight )
		.def( "getSize", &BBox2::py_getSizeVector )
		.def( "getSizeInAxis", &BBox2::py_getSizeInAxis )
		.def( "splitX", &BBox2::py_splitX )
		.def( "splitY", &BBox2::py_splitY )
		.def( "getMajorAxis", &BBox2::getMajorAxis )
		.def( "getMinorAxis", &BBox2::getMinorAxis )
		.def( "getMajorSize", &BBox2::getMajorAxis )
		.def( "getMinorSize", &BBox2::getMinorAxis )
		.def( self * Xform2() );
}


#endif
