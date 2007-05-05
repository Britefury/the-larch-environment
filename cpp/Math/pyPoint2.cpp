//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef PYPOINT2_CPP
#define PYPOINT2_CPP

#include <Math/Point2.h>

#include <boost/python.hpp>
using namespace boost::python;


void export_Point2()
{
	class_<Point2>( "Point2", init<>() )
		.def( init<double, double>() )
		.def( init<Vector2>() )
		.def( init<const Point2 &>() )
		.def_readwrite( "x", &Point2::x )
		.def_readwrite( "y", &Point2::y )
		.def( self == self )
		.def( self != self )
		.def( self + Vector2() )
		.def( self += Vector2() )
		.def( self - self )
		.def( self - Vector2() )
		.def( self -= Vector2() )
		.def( "toVector2", &Point2::toVector2 )
		.def( "dot", &Point2::dot )
		.def( "sqrDistanceTo", &Point2::sqrDistanceTo )
		.def( "distanceTo", &Point2::distanceTo )
		.def( "areaOfTrianglex2", &Point2::areaOfTrianglex2 ).staticmethod( "areaOfTrianglex2" )
		.def( "isOnLeft", &Point2::isOnLeft )
		.def( "isOnRight", &Point2::isOnRight )
		.def( "separates", &Point2::separates ).staticmethod( "separates" )
		.def( "segmentsIntersect", &Point2::segmentsIntersect ).staticmethod( "segmentsIntersect" )
		.def( "mul", &Point2::mul ).staticmethod( "mul" )
		.def( "sum", &Point2::py_sum2 ).staticmethod( "sum" )
		.def( "sum3", &Point2::py_sum3 ).staticmethod( "sum3" )
		.def( "sum4", &Point2::py_sum4 ).staticmethod( "sum4" )
		.def( "min", &Point2::min ).staticmethod( "min" )
		.def( "max", &Point2::max ).staticmethod( "max" )
		.def( "average", &Point2::py_average2 ).staticmethod( "average" )
		.def( "average3", &Point2::py_average3 ).staticmethod( "average3" )
		.def( "average4", &Point2::py_average4 ).staticmethod( "average4" )
		.def( "weightedAverage", &Point2::py_weightedAverage2 ).staticmethod( "weightedAverage" )
		.def( "weightedAverage3", &Point2::py_weightedAverage3 ).staticmethod( "weightedAverage3" )
		.def( "weightedAverage4", &Point2::py_weightedAverage4 ).staticmethod( "weightedAverage4" )
		.def( "normalisedWeightedAverage", &Point2::py_normalisedWeightedAverage2 ).staticmethod( "normalisedWeightedAverage" )
		.def( "normalisedWeightedAverage3", &Point2::py_normalisedWeightedAverage3 ).staticmethod( "normalisedWeightedAverage3" )
		.def( "normalisedWeightedAverage4", &Point2::py_normalisedWeightedAverage4 ).staticmethod( "normalisedWeightedAverage4" );
}


#endif
