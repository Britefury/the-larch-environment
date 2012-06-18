//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.ObjectList;

import net.htmlparser.jericho.Segment;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyJavaType;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyType;
import org.python.core.__builtin__;

import BritefuryJ.ClipboardFilter.ClipboardHTMLImporter;
import BritefuryJ.ClipboardFilter.ClipboardPlainTextImporter;
import BritefuryJ.Editor.Table.TableEditorStyle;
import BritefuryJ.Pres.Primitive.Label;

public class AttributeColumn extends AbstractColumn
{
	private String title;
	private PyString attrname;
	private Class<?> cls;
	private PyType type;
	private PyObject defaultValue, defaultValueCallable;
	
	
	public AttributeColumn(String title, PyString attrname, PyType type, PyObject defaultValue)
	{
		super();
		this.title = title;
		this.attrname = __builtin__.intern( attrname );
		
		if ( type != null )
		{
			if ( type instanceof PyJavaType )
			{
				PyJavaType jtype = (PyJavaType)type;
				this.cls = Py.tojava( jtype, Class.class );
				this.type = null;
			}
			else
			{
				this.cls = null;
				this.type = type;
			}
		}
		else
		{
			this.cls = null;
			this.type = null;
		}
		if ( defaultValue.isCallable() )
		{
			this.defaultValueCallable = defaultValue;
		}
		else
		{
			this.defaultValue = defaultValue;
		}
	}
	
	public AttributeColumn(String title, PyString attrname, PyType type)
	{
		super();
		this.title = title;
		this.attrname = __builtin__.intern( attrname );
		
		if ( type != null )
		{
			if ( type instanceof PyJavaType )
			{
				PyJavaType jtype = (PyJavaType)type;
				this.cls = Py.tojava( jtype, Class.class );
				this.type = null;
			}
			else
			{
				this.cls = null;
				this.type = type;
			}
		}
		else
		{
			this.cls = null;
			this.type = null;
		}
		this.defaultValueCallable = type;
		this.defaultValue = null;
	}
	
	public AttributeColumn(String title, PyString attrname)
	{
		this( title, attrname, (PyType)null, Py.None );
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
		try
		{
			__builtin__.setattr( pyRow, attrname, pyValue );
		}
		catch (PyException e)
		{
			if ( e.match( Py.AttributeError ) )
			{
				System.out.println( "Warning: AttributeColumn.set() attempting to set read-only attribute" );
			}
			else
			{
				throw e;
			}
		}
	}

	@Override
	public Object defaultValue()
	{
		if ( defaultValueCallable != null  &&  defaultValueCallable != Py.None )
		{
			try
			{
				return defaultValueCallable.__call__();
			}
			catch (Throwable t)
			{
				return null;
			}
		}
		else
		{
			return defaultValue;
		}
	}

	@Override
	public Object importHTML(Segment importData)
	{
		Object x = null;
		if ( cls != null )
		{
			x = ClipboardHTMLImporter.instance.importObject( cls, importData );
		}
		else if ( type != null )
		{
			PyObject pyX = ClipboardHTMLImporter.instance.importObject( type, importData );
			x = pyX != null  ?  Py.tojava( pyX, Object.class )  :  null;
		}
		else
		{
			return null;
		}
		
		if ( x == null )
		{
			return importPlainText( importData.getTextExtractor().toString() );
		}
		else
		{
			return x;
		}
	}

	@Override
	public Object importPlainText(String importData)
	{
		if ( cls != null )
		{
			return ClipboardPlainTextImporter.instance.importObject( cls, importData );
		}
		else if ( type != null )
		{
			PyObject pyX = ClipboardPlainTextImporter.instance.importObject( type, importData );
			return pyX != null  ?  Py.tojava( pyX, Object.class )  :  null;
		}
		else
		{
			return null;
		}
	}



	@Override
	public Object presentHeader()
	{
		return new Label( title ).withStyleSheetFromAttr( TableEditorStyle.headerAttrs );
	}
}
