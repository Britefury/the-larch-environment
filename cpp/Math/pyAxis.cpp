//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef PYAXIS_CPP
#define PYAXIS_CPP

#include <Math/Axis.h>

#include <boost/python.hpp>
using namespace boost::python;


void export_Axis()
{
	enum_<Axis>( "Axis" )
		.value( "AXIS_X", AXIS_X )
		.value( "AXIS_Y", AXIS_Y )
		.value( "AXIS_Z", AXIS_Z )
		.value( "AXIS_NEGATIVE_X", AXIS_NEGATIVE_X )
		.value( "AXIS_NEGATIVE_Y", AXIS_NEGATIVE_Y )
		.value( "AXIS_NEGATIVE_Z", AXIS_NEGATIVE_Z );

	def( "isAxisPositive", isAxisPositive );
	def( "isAxisNegative", isAxisNegative );
	def( "absoluteAxis", absoluteAxis );
	def( "negateAxis", negateAxis );
}


#endif
