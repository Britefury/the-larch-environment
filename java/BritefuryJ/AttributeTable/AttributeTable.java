//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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

public class AttributeTable implements Presentable
{
	public static class AttributeDoesNotExistException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected static class AttributeValuesMultiple
	{
		protected HashMap<AttributeBase, Object> values;
		protected int hash;
		
		
		public AttributeValuesMultiple(HashMap<AttributeBase, Object> values)
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
		protected AttributeBase attribute;
		protected Object value;
		protected int hash;
		
		
		public AttributeValueSingle(AttributeBase attribute, Object value)
		{
			this.attribute = attribute;
			this.value = value;
			int valueHash = value != null  ?  value.hashCode()  :  0;
			this.hash = HashUtils.doubleHash( attribute.hashCode(), valueHash );
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
				
				return attribute.equals( v.attribute )  &&  ( value != null  ?  value.equals( v.value )  :  value == v.value );
			}
			else
			{
				return false;
			}
		}
	}
	
	protected static class DelAttribute
	{
		protected AttributeBase attribute;
		
		
		public DelAttribute(AttributeBase attribute)
		{
			this.attribute = attribute;
		}
		
		public int hashCode()
		{
			return attribute.hashCode();
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
				
				return attribute.equals( v.attribute );
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
	

	protected HashMap<AttributeBase, Object> values = new HashMap<AttributeBase, Object>();
	protected HashMap<AttributeValueSingle, WeakReference<AttributeTable>> singleValueDerivedAttributeTables = new HashMap<AttributeValueSingle, WeakReference<AttributeTable>>();
	protected HashMap<AttributeValuesMultiple, WeakReference<AttributeTable>> multiValueDerivedAttributeTables = new HashMap<AttributeValuesMultiple, WeakReference<AttributeTable>>();
	protected IdentityHashMap<AttributeTable, WeakReference<AttributeTable>> attribTableDerivedAttributeTables = new IdentityHashMap<AttributeTable, WeakReference<AttributeTable>>();
	protected HashMap<DelAttribute, WeakReference<AttributeTable>> delDerivedAttributeTables = new HashMap<DelAttribute, WeakReference<AttributeTable>>();
	
	

	protected AttributeTable()
	{
	}
	
	
	protected AttributeTable newInstance()
	{
		return new AttributeTable();
	}
	
	
	public Object get(AttributeBase attribute)
	{
		if ( !values.containsKey( attribute ) )
		{
			return attribute.getDefaultValue();
		}
		
		return values.get( attribute );
	}
	
	public Object getRequired(AttributeBase attribute)
	{
		if ( !values.containsKey( attribute ) )
		{
			throw new AttributeDoesNotExistException();
		}

		return values.get( attribute );
	}
	
	@SuppressWarnings("unchecked")
	public <V> V get(AttributeBase attribute, Class<V> valueClass)
	{
		if ( !values.containsKey( attribute ) )
		{
			return (V)attribute.getDefaultValue();
		}
		
		return (V)values.get( attribute );
	}
	
	@SuppressWarnings("unchecked")
	public <V> V getRequired(AttributeBase attribute, Class<V> valueClass)
	{
		if ( !values.containsKey( attribute ) )
		{
			throw new AttributeDoesNotExistException();
		}
		
		return (V)values.get( attribute );
	}
	
	
	public Object __getitem__(AttributeBase key)
	{
		if ( !values.containsKey( key ) )
		{
		        throw Py.KeyError( key.getNamespace() + "." + key.getName() );
		}
		else
		{
			return values.get( key );
		}
	}
	
	
	
	public static AttributeTable values(AttributeWithValue... values)
	{
		return instance.withValues( values );
	}
	
	public static AttributeTable values(PyObject values[])
	{
		return instance.withValues( values );
	}
	
	public AttributeTable withValues(AttributeWithValue... values)
	{
		AttributeTable t = this;
		for (AttributeWithValue pair: values)
		{
			t = t.withAttr( pair.getAttribute(), pair.getValue() );
		}
		return t;
	}
	
	public AttributeTable withValues(PyObject values[])
	{
		AttributeTable t = this;
		for (PyObject value: values)
		{
			AttributeWithValue pair = Py.tojava( value, AttributeWithValue.class );
			t = t.withAttr( pair.getAttribute(), pair.getValue() );
		}
		return t;
	}
	

	
	public AttributeTable withAttr(AttributeBase attribute, Object value)
	{
		AttributeValueSingle v = new AttributeValueSingle( attribute, value );
		WeakReference<AttributeTable> derivedRef = singleValueDerivedAttributeTables.get( v );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			AttributeTable derived = newInstance();
			derived.values.putAll( values );
			derived.values.put( attribute, attribute.checkValue( value ) );
			derived = getUniqueAttributeTable( derived );
			derivedRef = new WeakReference<AttributeTable>( derived );
			singleValueDerivedAttributeTables.put( v, derivedRef );
		}
		return derivedRef.get();
	}
	
	public AttributeTable withAttrs(HashMap<AttributeBase, Object> valuesMap)
	{
		if ( valuesMap.size() == 1 )
		{
			Map.Entry<AttributeBase, Object> entry = valuesMap.entrySet().iterator().next();
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
				for (Map.Entry<AttributeBase, Object> entry: valuesMap.entrySet())
				{
					AttributeBase attribute = entry.getKey();
					derived.values.put( attribute, attribute.checkValue( entry.getValue() ) );
				}
				derived = getUniqueAttributeTable( derived );
				derivedRef = new WeakReference<AttributeTable>( derived );
				multiValueDerivedAttributeTables.put( v, derivedRef );
			}
			return derivedRef.get();
		}
	}
		
	public AttributeTable withAttrs(AttributeTable attribs)
	{
		WeakReference<AttributeTable> derivedRef = attribTableDerivedAttributeTables.get( attribs );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			AttributeTable derived = newInstance();
			derived.values.putAll( values );
			derived.values.putAll( attribs.values );
			derived = getUniqueAttributeTable( derived );
			derivedRef = new WeakReference<AttributeTable>( derived );
			attribTableDerivedAttributeTables.put( attribs, derivedRef );
		}
		return derivedRef.get();
	}
	
	public AttributeTable withAttrFrom(AttributeBase destAttr, AttributeTable srcTable, AttributeBase srcAttr)
	{
		return withAttr( destAttr, srcTable.get( srcAttr ) );
	}
	
	public AttributeTable withAttrsFrom(AttributeTable srcTable, AttributeBase srcAttr)
	{
		AttributeTable attrs = srcTable.get( srcAttr, AttributeTable.class );
		return withAttrs( attrs );
	}
	
	public AttributeTable withoutAttr(AttributeBase attribute)
	{
		if ( !values.containsKey( attribute ) )
		{
			// No attribute to remove - no change
			return this;
		}
		
		DelAttribute v = new DelAttribute( attribute );
		WeakReference<AttributeTable> derivedRef = delDerivedAttributeTables.get( v );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			AttributeTable derived = newInstance();
			derived.values.putAll( values );
			derived.values.remove( attribute );
			derived = getUniqueAttributeTable( derived );
			derivedRef = new WeakReference<AttributeTable>( derived );
			delDerivedAttributeTables.put( v, derivedRef );
		}
		return derivedRef.get();
	}
	
	public AttributeTable useAttr(AttributeBase attribute)
	{
		return attribute.use( this );
	}
	
	public AttributeTable remapAttr(AttributeBase destAttribute, AttributeBase sourceAttribute)
	{
		Object value = get( sourceAttribute );
		AttributeValueSingle v = new AttributeValueSingle( destAttribute, value );
		WeakReference<AttributeTable> derivedRef = singleValueDerivedAttributeTables.get( v );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			AttributeTable derived = newInstance();
			derived.values.putAll( values );
			derived.values.put( destAttribute, destAttribute.checkValue( value ) );
			derived = getUniqueAttributeTable( derived );
			derivedRef = new WeakReference<AttributeTable>( derived );
			singleValueDerivedAttributeTables.put( v, derivedRef );
		}
		return derivedRef.get();
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
	
	
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( "{ " );
		for (Map.Entry<AttributeBase, Object> entry: values.entrySet())
		{
			builder.append( entry.getKey().toString() );
			builder.append( "=" );
			builder.append( entry.getValue().toString() );
			builder.append( ", " );
		}
		builder.append( " }" );
		return builder.toString();
	}
	
	
	
	protected static Pres presentAttributeMap(FragmentView fragment, SimpleAttributeTable inheritedState, HashMap<AttributeBase, Object> values)
	{
		Set<AttributeBase> attributeSet = values.keySet();
		AttributeBase attributes[] = attributeSet.toArray( new AttributeBase[attributeSet.size()] );
		Arrays.sort( attributes, new AttributeBase.AttributeNameComparator() );
		Pres children[][] = new Pres[attributes.length+1][];
		
		StyleSheet attrTableStyle = getAttrTableStyle();
		
		children[0] = new Pres[] { attrTableStyle.applyTo( new Label( "Name" ) ), attrTableStyle.applyTo( new Label( "Value" ) ) };
		for (int i = 0; i < attributes.length; i++)
		{
			AttributeBase attribute = attributes[i];
			Object value = values.get( attribute );
			children[i+1] = new Pres[] { new Label( attribute.getFullName() ), new InnerFragment( value ) };
		}
		
		return attrTableStyle.applyTo( new Table( children ) );
	}
	

	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres valueField = new VerticalField( "Attributes:", presentAttributeMap( fragment, inheritedState, values ) );
		return new ObjectBoxWithFields( getClass().getName(), new Pres[] { valueField } );
	}
	
	
	// We have to initialise this style sheet on request, otherwise we can end up with a circular class initialisation problem
	private static StyleSheet _attrTableStyle = null;
	
	private static StyleSheet getAttrTableStyle()
	{
		if ( _attrTableStyle == null )
		{
			_attrTableStyle = StyleSheet.style( Primitive.fontBold.as( true ), Primitive.fontSize.as( 14 ), Primitive.foreground.as( new Color( 0.0f, 0.0f, 0.5f ) ), Primitive.tableColumnSpacing.as( 10.0 ) );
		}
		return _attrTableStyle;
	}



	public static final AttributeTable instance = new AttributeTable();
}
