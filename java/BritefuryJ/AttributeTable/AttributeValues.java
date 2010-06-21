//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.AttributeTable;

import java.util.HashMap;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.View.GSymFragmentView;

public class AttributeValues implements Presentable
{
	public static final AttributeValues identity = new AttributeValues();
	
	protected HashMap<String, Object> values = new HashMap<String, Object>();

	
	public AttributeValues()
	{
	}
	
	public AttributeValues(Map<String, Object> vals)
	{
		values.putAll( vals );
	}
	
	public AttributeValues(String names[], Object values[])
	{
		if ( values.length != names.length )
		{
			throw new RuntimeException( "All arguments must have keywords" );
		}
		
		for (int i = 0; i < values.length; i++)
		{
			this.values.put( names[i], values[i] );
		}
	}
	
	public AttributeValues(PyObject values[], String names[])
	{
		if ( values.length != names.length )
		{
			throw new RuntimeException( "All arguments must have keywords" );
		}
		
		for (int i = 0; i < values.length; i++)
		{
			this.values.put( names[i], Py.tojava( values[i], Object.class ) );
		}
	}
	
	
	public Object get(String key)
	{
		return values.get( key );
	}
	
	public Object __getitem__(String key)
	{
		if ( values.containsKey( key ) )
		{
			return values.get( key );
		}
		else
		{
			throw Py.KeyError( "No attribute named " + key );
		}
	}
	
	
	public String toString()
	{
		return "AttributeSet( " + values.toString() + " )";
	}
	
	
	@Override
	public DPElement present(GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		DPElement valueField = styleSheet.verticalObjectField( "Values:", AttributeTable.presentAttributeMap( fragment, styleSheet, inheritedState, values ) );
		return styleSheet.objectBoxWithFields( getClass().getName(), new DPElement[] { valueField } );
	}
}
