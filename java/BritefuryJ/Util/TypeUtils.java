//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Util;

import org.python.core.PyObject;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocModel.DMObject;

public class TypeUtils
{
	public enum ObjectKind
	{
		JAVA,
		PYTHON,
		DOCMODEL
	}
	
	
	public static ObjectKind getKindOfObject(Object x)
	{
		if ( x instanceof PyObject )
		{
			return ObjectKind.PYTHON;
		}
		else if ( x instanceof DMNode )
		{
			return ObjectKind.DOCMODEL;
		}
		else
		{
			return ObjectKind.JAVA;
		}
	}
	
	public static String nameOfTypeOf(Object x)
	{
		if ( x instanceof DMObject )
		{
			return ( (DMObject)x ).getDMNodeClass().getName();
		}
		else if ( x instanceof PyObject )
		{
			return ( (PyObject)x ).getType().getName();
		}
		else
		{
			Class<?> cls = x.getClass();
			return cls.getName();
		}
	}
}
