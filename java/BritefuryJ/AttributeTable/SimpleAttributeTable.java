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

import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectBoxWithFields;
import BritefuryJ.Pres.ObjectPres.VerticalField;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Table;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.Util.HashUtils;

public class SimpleAttributeTable implements Presentable
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
		private HashMap<AttributeValuesMultiple, WeakReference<SimpleAttributeTable>> attributeTableSet = new HashMap<AttributeValuesMultiple, WeakReference<SimpleAttributeTable>>();
	}
	
	
	protected static HashMap<Class<? extends SimpleAttributeTable>, AttributeTableSet> attributeTableSetsByClass = new HashMap<Class<? extends SimpleAttributeTable>, AttributeTableSet>();
	
	// 
	protected HashMap<String, Object> values = new HashMap<String, Object>();
	protected HashMap<AttributeValueSingle, WeakReference<SimpleAttributeTable>> singleValueDerivedAttributeTables = new HashMap<AttributeValueSingle, WeakReference<SimpleAttributeTable>>();
	protected HashMap<AttributeValuesMultiple, WeakReference<SimpleAttributeTable>> multiValueDerivedAttributeTables = new HashMap<AttributeValuesMultiple, WeakReference<SimpleAttributeTable>>();
	protected IdentityHashMap<SimpleAttributeTable, WeakReference<SimpleAttributeTable>> attribTableDerivedAttributeTables = new IdentityHashMap<SimpleAttributeTable, WeakReference<SimpleAttributeTable>>();
	protected HashMap<DelAttribute, WeakReference<SimpleAttributeTable>> delDerivedAttributeTables = new HashMap<DelAttribute, WeakReference<SimpleAttributeTable>>();
	
	
	protected SimpleAttributeTable()
	{
	}
	
	
	protected SimpleAttributeTable newInstance()
	{
		return new SimpleAttributeTable();
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
	
	
	public SimpleAttributeTable withAttr(String fieldName, Object value)
	{
		AttributeValueSingle v = new AttributeValueSingle( fieldName, value );
		WeakReference<SimpleAttributeTable> derivedRef = singleValueDerivedAttributeTables.get( v );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			SimpleAttributeTable derived = newInstance();
			derived.values.putAll( values );
			derived.values.put( fieldName, value );
			derived = getUniqueAttributeTable( derived );
			derivedRef = new WeakReference<SimpleAttributeTable>( derived );
			singleValueDerivedAttributeTables.put( v, derivedRef );
		}
		return derivedRef.get();
	}
	
	public SimpleAttributeTable withAttrs(Map<String, Object> valuesMap)
	{
		if ( valuesMap.size() == 1 )
		{
			Map.Entry<String, Object> entry = valuesMap.entrySet().iterator().next();
			return withAttr( entry.getKey(), entry.getValue() );
		}
		else
		{
			HashMap<String, Object> valuesHashMap = new HashMap<String, Object>();
			valuesHashMap.putAll( valuesMap );
			AttributeValuesMultiple v = new AttributeValuesMultiple( valuesHashMap );
			WeakReference<SimpleAttributeTable> derivedRef = multiValueDerivedAttributeTables.get( v );
			if ( derivedRef == null  ||  derivedRef.get() == null )
			{
				SimpleAttributeTable derived = newInstance();
				derived.values.putAll( values );
				derived.values.putAll( valuesHashMap );
				derived = getUniqueAttributeTable( derived );
				derivedRef = new WeakReference<SimpleAttributeTable>( derived );
				multiValueDerivedAttributeTables.put( v, derivedRef );
			}
			return derivedRef.get();
		}
	}
		
	public SimpleAttributeTable withAttrs(PyObject[] pyVals, String[] names)
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
	
	public SimpleAttributeTable withAttrs(SimpleAttributeTable attribs)
	{
		WeakReference<SimpleAttributeTable> derivedRef = attribTableDerivedAttributeTables.get( attribs );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			SimpleAttributeTable derived = withAttrs( attribs.values );
			
			derivedRef = new WeakReference<SimpleAttributeTable>( derived );

			attribTableDerivedAttributeTables.put( attribs, derivedRef );
		}
		return derivedRef.get();
	}
	
	public SimpleAttributeTable withoutAttr(String fieldName)
	{
		DelAttribute v = new DelAttribute( fieldName );
		WeakReference<SimpleAttributeTable> derivedRef = delDerivedAttributeTables.get( v );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			SimpleAttributeTable derived = newInstance();
			derived.values.putAll( values );
			derived.values.remove( fieldName );
			derived = getUniqueAttributeTable( derived );
			derivedRef = new WeakReference<SimpleAttributeTable>( derived );
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
	
	private SimpleAttributeTable getUniqueAttributeTable(SimpleAttributeTable attribTable)
	{
		AttributeTableSet setsForClass = getAttributeTableTableForClass();
		
		AttributeValuesMultiple vals = attribTable.allValues();
		WeakReference<SimpleAttributeTable> uniqueRef = setsForClass.attributeTableSet.get( vals );
		if ( uniqueRef == null  ||  uniqueRef.get() == null )
		{
			uniqueRef = new WeakReference<SimpleAttributeTable>( attribTable );
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


	
	
	protected static Pres presentAttributeMap(FragmentView ctx, SimpleAttributeTable inheritedState, HashMap<String, Object> values)
	{
		Set<String> nameSet = values.keySet();
		String names[] = nameSet.toArray( new String[nameSet.size()] );
		Arrays.sort( names );
		Pres children[][] = new Pres[names.length+1][];
		
		children[0] = new Pres[] { attrTableStyle.applyTo( new Label( "Name" ) ), attrTableStyle.applyTo( new Label( "Value" ) ) };
		for (int i = 0; i < names.length; i++)
		{
			String name = names[i];
			Object value = values.get( name );
			children[i+1] = new Pres[] { new Label( name ), new InnerFragment( value ) };
		}
		
		return attrTableStyle.applyTo( new Table( children ) );
	}
	
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres valueField = new VerticalField( "Attributes:", presentAttributeMap( fragment, inheritedState, values ) );
		return new ObjectBoxWithFields( getClass().getName(), new Pres[] { valueField } );
	}
	
	
	// We have to initialise this style sheet on request, otherwise we can end up with a circular class initialisation problem
	private static final StyleSheet attrTableStyle = StyleSheet.instance.withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontSize, 14 )
			.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.5f ) ).withAttr( Primitive.tableColumnSpacing, 10.0 );



	public static final SimpleAttributeTable instance = new SimpleAttributeTable();
}
