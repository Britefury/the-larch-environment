//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.AttributeTable;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.Table;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBoxWithFields;
import BritefuryJ.GSym.GenericPerspective.PresCom.VerticalField;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Utils.HashUtils;

public class AttributeTable implements Presentable
{
	public static class AttributeDoesNotExistException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected static class AttributeValuesMultiple
	{
		protected HashMap<String, Object> values;
		protected int hash;
		
		
		public AttributeValuesMultiple(HashMap<String, Object> values)
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
			
			if ( x instanceof AttributeValuesMultiple )
			{
				return values.equals( ((AttributeValuesMultiple)x).values );
			}
			else
			{
				return false;
			}
		}
	}
	
	protected static class AttributeValueSingle
	{
		protected String fieldName;
		protected Object value;
		protected int hash;
		
		
		public AttributeValueSingle(String fieldName, Object value)
		{
			this.fieldName = fieldName;
			this.value = value;
			int valueHash = value != null  ?  value.hashCode()  :  0;
			this.hash = HashUtils.doubleHash( fieldName.hashCode(), valueHash );
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
			
			if ( x instanceof AttributeValueSingle )
			{
				AttributeValueSingle v = (AttributeValueSingle)x;
				
				return fieldName.equals( v.fieldName )  &&  ( value != null  ?  value.equals( v.value )  :  value == v.value );
			}
			else
			{
				return false;
			}
		}
	}
	
	protected static class DelAttribute
	{
		protected String fieldName;
		
		
		public DelAttribute(String fieldName)
		{
			this.fieldName = fieldName;
		}
		
		public int hashCode()
		{
			return fieldName.hashCode();
		}
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof DelAttribute )
			{
				DelAttribute v = (DelAttribute)x;
				
				return fieldName.equals( v.fieldName );
			}
			else
			{
				return false;
			}
		}
	}
	
	private static class AttributeTableSet 
	{
		private HashMap<AttributeValuesMultiple, WeakReference<AttributeTable>> attributeTableSet = new HashMap<AttributeValuesMultiple, WeakReference<AttributeTable>>();
	}
	
	
	protected static HashMap<Class<? extends AttributeTable>, AttributeTableSet> attributeTableSetsByClass = new HashMap<Class<? extends AttributeTable>, AttributeTableSet>();
	
	// 
	protected HashMap<String, Object> values = new HashMap<String, Object>();
	protected HashMap<AttributeValueSingle, WeakReference<AttributeTable>> singleValueDerivedAttributeTables = new HashMap<AttributeValueSingle, WeakReference<AttributeTable>>();
	protected HashMap<AttributeValuesMultiple, WeakReference<AttributeTable>> multiValueDerivedAttributeTables = new HashMap<AttributeValuesMultiple, WeakReference<AttributeTable>>();
	protected IdentityHashMap<AttributeValues, WeakReference<AttributeTable>> attribSetDerivedAttributeTables = new IdentityHashMap<AttributeValues, WeakReference<AttributeTable>>();
	protected IdentityHashMap<AttributeTable, WeakReference<AttributeTable>> attribTableDerivedAttributeTables = new IdentityHashMap<AttributeTable, WeakReference<AttributeTable>>();
	protected HashMap<DelAttribute, WeakReference<AttributeTable>> delDerivedAttributeTables = new HashMap<DelAttribute, WeakReference<AttributeTable>>();
	
	
	public static AttributeTable instance = new AttributeTable();
	
	
	protected AttributeTable()
	{
	}
	
	
	protected AttributeTable newInstance()
	{
		return new AttributeTable();
	}
	
	
	public Object get(String attrName)
	{
		if ( !values.containsKey( attrName ) )
		{
			throw new AttributeDoesNotExistException();
		}
		
		return values.get( attrName );
	}
	
	public Object getOptional(String attrName)
	{
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
	
	public <V extends Object> V getOptional(String attrName, Class<V> valueClass, V defaultValue)
	{
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
	
	public <V extends Object> V getOptionalNonNull(String attrName, Class<V> valueClass, V defaultValue)
	{
		if ( !values.containsKey( attrName ) )
		{
			return defaultValue;
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
	
	
	public AttributeTable withAttr(String fieldName, Object value)
	{
		AttributeValueSingle v = new AttributeValueSingle( fieldName, value );
		WeakReference<AttributeTable> derivedRef = singleValueDerivedAttributeTables.get( v );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			AttributeTable derived = newInstance();
			derived.values.putAll( values );
			derived.values.put( fieldName, value );
			derived = getUniqueAttributeTable( derived );
			derivedRef = new WeakReference<AttributeTable>( derived );
			singleValueDerivedAttributeTables.put( v, derivedRef );
		}
		return derivedRef.get();
	}
	
	public AttributeTable withAttrs(HashMap<String, Object> valuesMap)
	{
		if ( valuesMap.size() == 1 )
		{
			Map.Entry<String, Object> entry = valuesMap.entrySet().iterator().next();
			return withAttr( entry.getKey(), entry.getValue() );
		}
		else
		{
			AttributeValuesMultiple v = new AttributeValuesMultiple( valuesMap );
			WeakReference<AttributeTable> derivedRef = multiValueDerivedAttributeTables.get( v );
			if ( derivedRef == null  ||  derivedRef.get() == null )
			{
				AttributeTable derived = newInstance();
				derived.values.putAll( values );
				derived.values.putAll( valuesMap );
				derived = getUniqueAttributeTable( derived );
				derivedRef = new WeakReference<AttributeTable>( derived );
				multiValueDerivedAttributeTables.put( v, derivedRef );
			}
			return derivedRef.get();
		}
	}
		
	public AttributeTable withAttrs(PyObject[] pyVals, String[] names)
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
	
	public AttributeTable withAttrValues(AttributeValues attribs)
	{
		WeakReference<AttributeTable> derivedRef = attribSetDerivedAttributeTables.get( attribs );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			AttributeTable derived = withAttrs( attribs.values );
			
			derivedRef = new WeakReference<AttributeTable>( derived );

			attribSetDerivedAttributeTables.put( attribs, derivedRef );
		}
		return derivedRef.get();
	}
	
	public AttributeTable withAttrs(AttributeTable attribs)
	{
		WeakReference<AttributeTable> derivedRef = attribTableDerivedAttributeTables.get( attribs );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			AttributeTable derived = withAttrs( attribs.values );
			
			derivedRef = new WeakReference<AttributeTable>( derived );

			attribTableDerivedAttributeTables.put( attribs, derivedRef );
		}
		return derivedRef.get();
	}
	
	public AttributeTable withoutAttr(String fieldName)
	{
		DelAttribute v = new DelAttribute( fieldName );
		WeakReference<AttributeTable> derivedRef = delDerivedAttributeTables.get( v );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			AttributeTable derived = newInstance();
			derived.values.putAll( values );
			derived.values.remove( fieldName );
			derived = getUniqueAttributeTable( derived );
			derivedRef = new WeakReference<AttributeTable>( derived );
			delDerivedAttributeTables.put( v, derivedRef );
		}
		return derivedRef.get();
	}
	
		
	
	
	
	protected void initAttr(String name, Object value)
	{
		values.put( name, value );
	}
	
	
	private AttributeTableSet getAttributeTableTableForClass()
	{
		AttributeTableSet setsForClass = attributeTableSetsByClass.get( getClass() );
		if ( setsForClass == null )
		{
			setsForClass = new AttributeTableSet();
			attributeTableSetsByClass.put( getClass(), setsForClass );
		}
		return setsForClass;
	}
	
	private AttributeTable getUniqueAttributeTable(AttributeTable attribTable)
	{
		AttributeTableSet setsForClass = getAttributeTableTableForClass();
		
		AttributeValuesMultiple vals = attribTable.allValues();
		WeakReference<AttributeTable> uniqueRef = setsForClass.attributeTableSet.get( vals );
		if ( uniqueRef == null  ||  uniqueRef.get() == null )
		{
			uniqueRef = new WeakReference<AttributeTable>( attribTable );
			setsForClass.attributeTableSet.put( vals, uniqueRef ); 
		}
		
		return uniqueRef.get();
	}
	

	private AttributeValuesMultiple allValues()
	{
		return new AttributeValuesMultiple( values );
	}
	
	
	
	protected void notifyBadAttributeType(String attrName, Object value, Class<?> expectedType)
	{
		System.err.println( "WARNING: attrib table \"" + getClass().getName() + "\": attribute '" + attrName + "' should have value of type '" + expectedType.getName() + "', has value '" + value + "'; type '" + value.getClass().getName() + "'" );
	}

	protected void notifyAttributeShouldNotBeNull(String attrName, Class<?> expectedType)
	{
		System.err.println( "WARNING: attrib table \"" + getClass().getName() + "\": attribute '" + attrName + "' should not have a null value; type='" + expectedType.getName() + "'" );
	}


	
	
	protected static Pres presentAttributeMap(GSymFragmentView ctx, AttributeTable inheritedState, HashMap<String, Object> values)
	{
		Set<String> nameSet = values.keySet();
		String names[] = nameSet.toArray( new String[0] );
		Arrays.sort( names );
		Pres children[][] = new Pres[names.length+1][];
		
		children[0] = new Pres[] { attrTableStyle.applyTo( new StaticText( "Name" ) ), attrTableStyle.applyTo( new StaticText( "Value" ) ) };
		for (int i = 0; i < names.length; i++)
		{
			String name = names[i];
			Object value = values.get( name );
			DPElement valueView = ctx.presentFragment( value, StyleSheet.instance );
			children[i+1] = new Pres[] { new StaticText( name ), Pres.elementToPres( valueView ) };
		}
		
		return attrTableStyle.applyTo( new Table( children ) );
	}
	
	@Override
	public Pres present(GSymFragmentView fragment, AttributeTable inheritedState)
	{
		Pres valueField = new VerticalField( "Attributes:", presentAttributeMap( fragment, inheritedState, values ) );
		return new ObjectBoxWithFields( getClass().getName(), new Pres[] { valueField } );
	}
	
	
	// We have to initialise this style sheet on request, otherwise we can end up with a circular class initialisation problem
	private static final StyleSheet2 attrTableStyle = StyleSheet2.instance.withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontSize, 14 )
			.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.5f ) ).withAttr( Primitive.tableColumnSpacing, 10.0 );
}
