//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.ObjectList;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.__builtin__;

public class AttributeColumn extends AbstractColumn
{
	private PyString attrname;
	private PyObject convertValueFn;
	private PyObject defaultValue, defaultValueCallable;
	
	
	public AttributeColumn(PyString attrname, PyObject convertValueFn, PyObject defaultValue)
	{
		super();
		this.attrname = __builtin__.intern( attrname );
		this.convertValueFn = convertValueFn;
		if ( defaultValue.isCallable() )
		{
			this.defaultValueCallable = defaultValue;
		}
		else
		{
			this.defaultValue = defaultValue;
		}
	}
	
	public AttributeColumn(PyString attrname)
	{
		this( attrname, null, Py.None );
	}
	
	
	@Override
	public Object get(Object modelRow)
	{
		PyObject pyRow = Py.java2py( modelRow );
		return Py.tojava( __builtin__.getattr( pyRow, attrname ), Object.class );
	}

	@Override
	public void set(Object modelRow, Object value)
	{
		PyObject pyRow = Py.java2py( modelRow );
		PyObject pyValue = Py.java2py( value );
		__builtin__.setattr( pyRow, attrname, pyValue );
	}

	@Override
	public Object defaultValue()
	{
		if ( defaultValueCallable != null )
		{
			return defaultValueCallable.__call__();
		}
		else
		{
			return defaultValue;
		}
	}

	@Override
	public Object convertValue(Object x)
	{
		if ( convertValueFn != null )
		{
			return convertValueFn.__call__( Py.java2py( x ) );
		}
		else
		{
			return x;
		}
	}
}
