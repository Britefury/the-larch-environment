//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheet;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.Utils.HashUtils;

public class StyleSheet
{
	public static class AttributeDoesNotExistException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected static class StyleSheetValuesMultiple
	{
		protected HashMap<String, Object> values;
		protected int hash;
		
		
		public StyleSheetValuesMultiple(HashMap<String, Object> values)
		{
			this.values = values;
			this.hash = values.hashCode();
		}
		
		public int hashCode()
		{
			return this.hash;
		}
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof StyleSheetValuesMultiple )
			{
				return values.equals( ((StyleSheetValuesMultiple)x).values );
			}
			else
			{
				return false;
			}
		}
	}
	
	protected static class StyleSheetValueSingle
	{
		protected String fieldName;
		protected Object value;
		protected int hash;
		
		
		public StyleSheetValueSingle(String fieldName, Object value)
		{
			this.fieldName = fieldName;
			this.value = value;
			this.hash = HashUtils.doubleHash( fieldName.hashCode(), value.hashCode() );
		}
		
		public int hashCode()
		{
			return this.hash;
		}
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof StyleSheetValueSingle )
			{
				StyleSheetValueSingle v = (StyleSheetValueSingle)x;
				return fieldName.equals( v.fieldName )  &&  value.equals( v.value );
			}
			else
			{
				return false;
			}
		}
	}
	
	private static class StyleSheetTable 
	{
		private HashMap<StyleSheetValuesMultiple, StyleSheet> styleSheets = new HashMap<StyleSheetValuesMultiple, StyleSheet>();
	}
	
	
	protected static HashMap<Class<? extends StyleSheet>, StyleSheetTable> styleSheetTablesByClass = new HashMap<Class<? extends StyleSheet>, StyleSheetTable>();
	
	protected HashMap<String, Object> values = new HashMap<String, Object>();
	protected HashMap<StyleSheetValueSingle, StyleSheet> singleValueDerivedStyleSheets = new HashMap<StyleSheetValueSingle, StyleSheet>();
	protected HashMap<StyleSheetValuesMultiple, StyleSheet> multiValueDerivedStyleSheets = new HashMap<StyleSheetValuesMultiple, StyleSheet>();
	protected IdentityHashMap<AttributeValues, StyleSheet> attribSetDerivedStyleSheets = new IdentityHashMap<AttributeValues, StyleSheet>();
	protected IdentityHashMap<StyleSheetDerivedPyAttrFn, PyObject> derivedAttributes = new IdentityHashMap<StyleSheetDerivedPyAttrFn, PyObject>();
	
	
	public static StyleSheet defaultStyleSheet = new StyleSheet();
	
	
	protected StyleSheet()
	{
	}
	
	
	protected StyleSheet newInstance()
	{
		return new StyleSheet();
	}
	
	
	public Object get(String attrName)
	{
		if ( !values.containsKey( attrName ) )
		{
			throw new AttributeDoesNotExistException();
		}
		
		return values.get( attrName );
	}
	
	public <V extends Object> V get(String attrName, Class<V> valueClass, V defaultValue)
	{
		if ( !values.containsKey( attrName ) )
		{
			throw new AttributeDoesNotExistException();
		}
		
		Object v = values.get( attrName );
		
		if ( v == null )
		{
			return null;
		}
		else
		{
			V typedV;
			try
			{
				typedV = valueClass.cast( v );
			}
			catch (ClassCastException e)
			{
				notifyBadAttributeType( attrName, v, valueClass );
				return defaultValue;
			}
			return typedV;
		}
	}
	
	public <V extends Object> V getNonNull(String attrName, Class<V> valueClass, V defaultValue)
	{
		if ( !values.containsKey( attrName ) )
		{
			throw new AttributeDoesNotExistException();
		}
		
		Object v = values.get( attrName );
		
		if ( v == null )
		{
			notifyAttributeShouldNotBeNull( attrName, valueClass );
			return defaultValue;
		}
		else
		{
			V typedV;
			try
			{
				typedV = valueClass.cast( v );
			}
			catch (ClassCastException e)
			{
				notifyBadAttributeType( attrName, v, valueClass );
				return defaultValue;
			}
			return typedV;
		}
	}
	
	public Object __getitem__(String key)
	{
		if ( !values.containsKey( key ) )
		{
		        throw Py.KeyError( key );
		}
		else
		{
			return values.get( key );
		}
	}
	
	
	public StyleSheet withAttr(String fieldName, Object value)
	{
		StyleSheetValueSingle v = new StyleSheetValueSingle( fieldName, value );
		StyleSheet derived = singleValueDerivedStyleSheets.get( v );
		if ( derived == null )
		{
			derived = (StyleSheet)newInstance();
			derived.values.putAll( values );
			derived.values.put( fieldName, value );
			derived = getUniqueStyleSheet( derived );
			singleValueDerivedStyleSheets.put( v, derived );
		}
		return derived;
	}
	
	public StyleSheet withAttrs(HashMap<String, Object> valuesMap)
	{
		if ( valuesMap.size() == 1 )
		{
			Map.Entry<String, Object> entry = valuesMap.entrySet().iterator().next();
			return withAttr( entry.getKey(), entry.getValue() );
		}
		else
		{
			StyleSheetValuesMultiple v = new StyleSheetValuesMultiple( valuesMap );
			StyleSheet derived = multiValueDerivedStyleSheets.get( v );
			if ( derived == null )
			{
				derived = (StyleSheet)newInstance();
				derived.values.putAll( values );
				derived.values.putAll( valuesMap );
				derived = getUniqueStyleSheet( derived );
				multiValueDerivedStyleSheets.put( v, derived );
			}
			return derived;
		}
	}
		
	public StyleSheet withAttrs(PyObject[] pyVals, String[] names)
	{
		if ( names.length != pyVals.length )
		{
			throw new RuntimeException( "All arguments must have keywords" );
		}
		
		if ( names.length == 1 )
		{
			return withAttr( names[0], Py.tojava( pyVals[0], Object.class ) );
		}
		else
		{
			HashMap<String, Object> valuesMap = new HashMap<String, Object>();
			for (int i = 0; i < names.length; i++)
			{
				valuesMap.put( names[i], Py.tojava( pyVals[i], Object.class ) );
			}
			
			return withAttrs( valuesMap );
		}
	}
	
	public StyleSheet withAttrValues(AttributeValues attribs)
	{
		StyleSheet derived = attribSetDerivedStyleSheets.get( attribs );
		if ( derived == null )
		{
			derived = withAttrs( attribs.values );
			
			attribSetDerivedStyleSheets.put( attribs, derived );
		}
		return derived;
	}
	
		
	
	
	
	protected void initAttr(String name, Object value)
	{
		values.put( name, value );
	}
	
	
	private StyleSheetTable getStyleSheetTableForClass()
	{
		StyleSheetTable table = styleSheetTablesByClass.get( getClass() );
		if ( table == null )
		{
			table = new StyleSheetTable();
			styleSheetTablesByClass.put( getClass(), table );
		}
		return table;
	}
	
	private StyleSheet getUniqueStyleSheet(StyleSheet styleSheet)
	{
		StyleSheetTable table = getStyleSheetTableForClass();
		
		StyleSheetValuesMultiple vals = styleSheet.allValues();
		StyleSheet unique = table.styleSheets.get( vals );
		if ( unique == null )
		{
			table.styleSheets.put( vals, styleSheet ); 
			unique = styleSheet;
		}
		
		return unique;
	}
	

	private StyleSheetValuesMultiple allValues()
	{
		return new StyleSheetValuesMultiple( values );
	}
	
	
	
	protected void notifyBadAttributeType(String attrName, Object value, Class<?> expectedType)
	{
		System.err.println( "WARNING: style sheet \"" + getClass().getName() + "\": attribute '" + attrName + "' should have value of type '" + expectedType.getName() + "', has value '" + value + "'; type '" + value.getClass().getName() + "'" );
	}

	protected void notifyAttributeShouldNotBeNull(String attrName, Class<?> expectedType)
	{
		System.err.println( "WARNING: style sheet \"" + getClass().getName() + "\": attribute '" + attrName + "' should not have a null value; type='" + expectedType.getName() + "'" );
	}
}
