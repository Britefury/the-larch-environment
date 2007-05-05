//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef PYMATH_CPP
#define PYMATH_CPP

#include <boost/python.hpp>
using namespace boost::python;



void export_Axis();
void export_BBox2();
void export_Colour3f();
void export_Point2();
void export_Polygon2();
void export_Segment2();
void export_Side();
void export_Vector2();
void export_Xform2();



BOOST_PYTHON_MODULE(Math)
{
	export_Axis();
	export_BBox2();
	export_Colour3f();
	export_Point2();
	export_Polygon2();
	export_Segment2();
	export_Side();
	export_Vector2();
	export_Xform2();
}



#endif
