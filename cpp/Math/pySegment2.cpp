//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef PYSEGMENT2_CPP
#define PYSEGMENT2_CPP

#include <Math/Segment2.h>

#include <boost/python.hpp>
using namespace boost::python;


void export_Segment2()
{
	class_<Segment2>( "Segment2", init<>() )
		.def( init<Point2, Point2>() )
		.def( init<Point2, Vector2>() )
		.def( init<const Segment2 &>() )
		.def_readwrite( "a", &Segment2::a )
		.def_readwrite( "b", &Segment2::b )
		.def( "getDirection", &Segment2::getDirection )
		.def( "getNormal", &Segment2::getNormal )
		.def( "getMidPoint", &Segment2::getMidPoint )
		.def( "getPoint", &Segment2::getPoint )
		.def( "closestPointTo", &Segment2::py_closestPointTo )
		.def( "sqrDistanceTo", &Segment2::sqrDistanceTo )
		.def( "distanceTo", &Segment2::distanceTo )
		.def( "onLeft", &Segment2::onLeft )
		.def( "onOrLeft", &Segment2::onOrLeft )
		.def( "onRight", &Segment2::onRight )
		.def( "onOrRight", &Segment2::onOrRight )
		.def( "on", &Segment2::on )
		.def( "separates", &Segment2::separates )
		.def( "boundsContain", &Segment2::boundsContain )
		.def( "separates", &Segment2::separates )
		.def( "intersect", &Segment2::py_intersect )
		.def( "intersects", &Segment2::intersects );
}


#endif
