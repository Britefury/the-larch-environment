//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef BEZIER2UTIL_H__
#define BEZIER2UTIL_H__

#include <boost/python.hpp>





#include <Util/Array.h>

#include <Math/Point2.h>
#include <Math/Side.h>
#include <Math/ConvexHull2.h>

_DllExport_ ConvexHull2 bezierCurveConvexHull(const Point2 &a, const Point2 &b, const Point2 &c, const Point2 &d);


_DllExport_ double sqrDistanceToBezierCurve(const Point2 &point, const Point2 &a, const Point2 &b, const Point2 &c, const Point2 &d, double epsilon);


#endif
