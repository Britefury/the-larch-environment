//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef PYPOLYGON2_CPP
#define PYPOLYGON2_CPP

#include <Math/Polygon2.h>

#include <boost/python.hpp>
using namespace boost::python;


void export_Polygon2()
{
	class_<Polygon2>( "Polygon2", init<>() )
		.def( init<boost::python::list>() )
		.def( init<const Polygon2 &>() )
		.def( "__len__", &Polygon2::size )
		.def( "__getitem__", &Polygon2::py__getitem__, return_internal_reference<>() )
		.def( "__setitem__", &Polygon2::py__setitem__ )
		.def( "append", &Polygon2::addVertex )
		.def( "__delitem__", &Polygon2::removeVertex )
		.def( "clear", &Polygon2::clear )
		.def( "side", &Polygon2::side )
		.def( "contains", &Polygon2::contains )
		.def( "containsAllOf", &Polygon2::py_containsAllOfSegment )
		.def( "containsPartOf", &Polygon2::py_containsPartOfSegment )
		.def( "containsAllOf", &Polygon2::py_containsAllOfPolygon )
		.def( "containsPartOf", &Polygon2::py_containsPartOfPolygon )
		.def( "computeAreaX2", &Polygon2::computeAreaX2 )
		.def( "computeArea", &Polygon2::computeArea )
		.def( "isClockwise", &Polygon2::isClockwise )
		.def( "isConvex", &Polygon2::isConvex )
		.def( "isSelfIntersecting", &Polygon2::isSelfIntersecting );
}


#endif
