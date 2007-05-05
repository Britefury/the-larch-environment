//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef AXIS_H__
#define AXIS_H__


/*
An enumerated Axis identifier.

Identifies one of the three axes (X, Y, & Z), negative ones also.
*/




enum Axis
{
	AXIS_X,
	AXIS_Y,
	AXIS_Z,
	AXIS_NEGATIVE_X,
	AXIS_NEGATIVE_Y,
	AXIS_NEGATIVE_Z
};




_DllExport_ inline bool isAxisPositive(Axis a)
{
	return a == AXIS_X  ||  a == AXIS_Y  ||  a == AXIS_Z;
}

_DllExport_ inline bool isAxisNegative(Axis a)
{
	return a == AXIS_NEGATIVE_X  ||  a == AXIS_NEGATIVE_Y  ||  a == AXIS_NEGATIVE_Z;
}

_DllExport_ inline Axis absoluteAxis(Axis axis)
{
	switch( axis )
	{
	case AXIS_NEGATIVE_X:
		return AXIS_X;
	case AXIS_NEGATIVE_Y:
		return AXIS_Y;
	case AXIS_NEGATIVE_Z:
		return AXIS_Z;
	default:
		return axis;
	}
}

_DllExport_ inline Axis negateAxis(Axis axis)
{
	switch( axis )
	{
	case AXIS_X:
		return AXIS_NEGATIVE_X;
	case AXIS_NEGATIVE_X:
		return AXIS_X;
	case AXIS_Y:
		return AXIS_NEGATIVE_Y;
	case AXIS_NEGATIVE_Y:
		return AXIS_Y;
	case AXIS_Z:
		return AXIS_NEGATIVE_Z;
	case AXIS_NEGATIVE_Z:
		return AXIS_Z;
	default:
		return AXIS_Z;
	}
}


#endif
