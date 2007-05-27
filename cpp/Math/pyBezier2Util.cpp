//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef PYBEZIER2UTIL_CPP
#define PYBEZIER2UTIL_CPP

#include <boost/python.hpp>
using namespace boost::python;


#include <Math/Bezier2Util.h>
void export_Bezier2Util()
{
	def( "bezierCurveConvexHull", &bezierCurveConvexHull );
	def( "sqrDistanceToBezierCurve", &sqrDistanceToBezierCurve );
}


#endif
