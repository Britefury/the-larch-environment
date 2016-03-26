//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.AttributeTable;

import java.util.ArrayList;
import java.util.Arrays;

import org.python.core.Py;
import org.python.core.PyObject;

public class AttributeUsageSet
{
	// TODO : cache attribute table values
	private ArrayList<AttributeBase> attributes;
	
	
	public AttributeUsageSet(AttributeBase ...attributes)
	{
		this.attributes = new ArrayList<AttributeBase>();
		this.attributes.addAll( Arrays.asList( attributes ) );
	}

	public AttributeUsageSet(PyObject values[])
	{
		attributes = new ArrayList<AttributeBase>();
		attributes.ensureCapacity( values.length );
		for (PyObject v: values)
		{
			attributes.add( Py.tojava( v, AttributeBase.class ) );
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public <T extends AttributeTable> T useAttrs(T table)
	{
		for (AttributeBase attr: attributes)
		{
			table = (T)attr.use( table );
		}
		return table;
	}
}
