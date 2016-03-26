//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.StyleSheet;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.AttributeTable.AttributeBase;
import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.AttributeTable.AttributeWithValue;
import BritefuryJ.Pres.ApplyStyleSheet;

public class StyleSheet extends AttributeTable
{
	protected StyleSheet()
	{
		super();
	}
	
	
	protected StyleSheet newInstance()
	{
		return new StyleSheet();
	}
	
	
	public static StyleSheet style(AttributeWithValue... values)
	{
		return instance.withValues( values );
	}
	
	public static StyleSheet style(PyObject values[])
	{
		return instance.withValues( values );
	}
	
	public StyleSheet withValues(AttributeWithValue... values)
	{
		StyleSheet s = this;
		for (AttributeWithValue pair: values)
		{
			s = s.withAttr( pair.getAttribute(), pair.getValue() );
		}
		return s;
	}
	
	public StyleSheet withValues(PyObject values[])
	{
		StyleSheet s = this;
		for (PyObject value: values)
		{
			AttributeWithValue pair = Py.tojava( value, AttributeWithValue.class );
			s = s.withAttr( pair.getAttribute(), pair.getValue() );
		}
		return s;
	}

	
	public ApplyStyleSheet applyTo(Object child)
	{
		return new ApplyStyleSheet( this, child );
	}

	public ApplyStyleSheet __call__(Object child)
	{
		return new ApplyStyleSheet( this, child );
	}




	public StyleSheet withAttr(AttributeBase attribute, Object value)
	{
		return (StyleSheet)super.withAttr( attribute, value );
	}
	
	public StyleSheet withAttrs(AttributeTable attribs)
	{
		return (StyleSheet)super.withAttrs( attribs );
	}
		
	public StyleSheet withAttrFrom(AttributeBase destAttr, AttributeTable srcTable, AttributeBase srcAttr)
	{
		return (StyleSheet)super.withAttrFrom( destAttr, srcTable, srcAttr );
	}
	
	public StyleSheet withAttrsFrom(AttributeTable srcTable, AttributeBase srcAttr)
	{
		return (StyleSheet)super.withAttrsFrom( srcTable, srcAttr );
	}
	
	public StyleSheet withoutAttr(AttributeBase attribute)
	{
		return (StyleSheet)super.withoutAttr( attribute );
	}
	
	public StyleSheet remapAttr(AttributeBase destAttribute, AttributeBase sourceAttribute)
	{
		return (StyleSheet)super.remapAttr( destAttribute, sourceAttribute );
	}
	
	
	
	public static final StyleSheet instance = new StyleSheet();
}
