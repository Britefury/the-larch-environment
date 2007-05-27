//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef PYCONVEXHULL2_CPP
#define PYCONVEXHULL2_CPP

#include <boost/python.hpp>
using namespace boost::python;


#include <Math/ConvexHull2.h>
void export_ConvexHull2()
{
	class_<ConvexHull2>( "ConvexHull2", init<>() )
		.def( init<boost::python::list>() )
		.def( init<const ConvexHull2 &>() )

	.def( "__len__", &ConvexHull2::size )
		.def( "__getitem__", &ConvexHull2::py__getitem__, return_internal_reference<>() )
		.def( "__setitem__", &ConvexHull2::py__setitem__ )
		.def( "addPoint", &ConvexHull2::addPoint )
		.def( "clear", &ConvexHull2::clear )
		.def( "sqrDistanceTo", &ConvexHull2::sqrDistanceTo )
		.def( "distanceTo", &ConvexHull2::distanceTo )
		.def( "side", &ConvexHull2::side )
		.def( "contains", &ConvexHull2::contains )
		.def( "containsAllOf", &ConvexHull2::py_containsAllOfSegment )
		.def( "containsPartOf", &ConvexHull2::py_containsPartOfSegment )
		.def( "computeAreaX2", &ConvexHull2::computeAreaX2 )
		.def( "computeArea", &ConvexHull2::computeArea )
		.def( "boundingBox", &ConvexHull2::boundingBox );
}


#endif
