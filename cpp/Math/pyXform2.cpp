//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef PYSEGMENT2_CPP
#define PYSEGMENT2_CPP

#include <Math/Xform2.h>

#include <boost/python.hpp>
using namespace boost::python;


void export_Xform2()
{
	class_<Xform2>( "Xform2", init<>() )
		.def( init<double>() )
		.def( init<const Vector2 &>() )
		.def( init<double, const Vector2 &>() )
		.def_readwrite( "scale", &Xform2::scale )
		.def_readwrite( "translation", &Xform2::translation )

		.def( "inverse", &Xform2::inverse )

		.def( Vector2() * self )
		.def( Point2() * self )
		.def( Segment2() * self )
		.def( self * self );
}


#endif
