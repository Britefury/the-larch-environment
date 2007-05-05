//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef PYVECTOR2_CPP
#define PYVECTOR2_CPP

#include <Math/Vector2.h>

#include <boost/python.hpp>
using namespace boost::python;


void export_Vector2()
{
	class_<Vector2>( "Vector2", init<>() )
		.def( init<double, double>() )
		.def( init<const Vector2 &>() )
		.def_readwrite( "x", &Vector2::x )
		.def_readwrite( "y", &Vector2::y )
		.def( self == self )
		.def( self != self )
		.def( self + self )
		.def( self += self )
		.def( self - self )
		.def( self -= self )
		.def( self * double() )
		.def( self *= double() )
		.def( -self )
		.def( "dot", &Vector2::dot )
		.def( "cross", &Vector2::cross )
		.def( "sqrLength", &Vector2::sqrLength )
		.def( "length", &Vector2::length )
		.def( "normalise", &Vector2::normalise )
		.def( "normaliseToLength", &Vector2::normaliseToLength )
		.def( "getNormalised", &Vector2::getNormalised )
		.def( "getNormalisedToLength", &Vector2::getNormalisedToLength )
		.def( "perpendicular", &Vector2::perpendicular )
		.def( "projectOntoUnitVector", &Vector2::projectOntoUnitVector )
		.def( "getRotated90CCW", &Vector2::getRotated90CCW )
		.def( "getRotated90CW", &Vector2::getRotated90CW )
		.def( "argPolar", &Vector2::argPolar )
		.def( "isParallelWith", &Vector2::isParallelWith )
		.def( "min", &Vector2::min ).staticmethod( "min" )
		.def( "max", &Vector2::max ).staticmethod( "max" );
}


#endif
