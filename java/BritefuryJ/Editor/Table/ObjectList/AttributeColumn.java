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

import BritefuryJ.Editor.Table.TableEditorStyle;
import BritefuryJ.Pres.Primitive.Label;

public class AttributeColumn extends AbstractColumn
{
	private String title;
	private PyString attrname;
	private PyObject valueConstructorFn, valueExportFn;
	private PyObject defaultValue, defaultValueCallable;
	
	
	public AttributeColumn(String title, PyString attrname, PyObject valueConstructorFn, PyObject valueExportFn, PyObject defaultValue)
	{
		super();
		this.title = title;
		this.attrname = __builtin__.intern( attrname );
		this.valueConstructorFn = valueConstructorFn;
		this.valueExportFn = valueExportFn;
		if ( defaultValue.isCallable() )
		{
			this.defaultValueCallable = defaultValue;
		}
		else
		{
			this.defaultValue = defaultValue;
		}
	}
	
	public AttributeColumn(String title, PyString attrname, PyObject valueConstructorFn, PyObject defaultValue)
	{
		this( title, attrname, valueConstructorFn, null, defaultValue );
	}
	
	public AttributeColumn(String title, PyString attrname)
	{
		this( title, attrname, null, null, Py.None );
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
		if ( defaultValueCallable != null  &&  defaultValueCallable != Py.None )
		{
			return defaultValueCallable.__call__();
		}
		else
		{
			return defaultValue;
		}
	}

	@Override
	public Object importValue(Object x)
	{
		if ( valueConstructorFn != null  &&  valueConstructorFn != Py.None )
		{
			return valueConstructorFn.__call__( Py.java2py( x ) );
		}
		else
		{
			return x;
		}
	}

	@Override
	public String exportValue(Object x)
	{
		if ( valueExportFn != null  &&  valueExportFn != Py.None )
		{
			return valueExportFn.__call__( Py.java2py( x ) ).toString();
		}
		else
		{
			return x.toString();
		}
	}



	@Override
	public Object presentHeader()
	{
		return new Label( title ).withStyleSheetFromAttr( TableEditorStyle.headerAttrs );
	}
}
