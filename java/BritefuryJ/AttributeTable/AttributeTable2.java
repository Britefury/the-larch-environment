//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.AttributeTable;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.python.core.Py;

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

public class AttributeTable2 implements Presentable
{
	public static class AttributeDoesNotExistException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	
	
	//
	//
	// NAMESPACES
	//
	//
	
	private static ArrayList<AttributeNamespace> namespaces = new ArrayList<AttributeNamespace>();
	
	protected static int registerNamespace(AttributeNamespace namespace)
	{
		int id = namespaces.size();
		namespaces.add( namespace );
		return id;
	}
	
	
	
	
	private static class NamespaceTable
	{
		private Object derivedValues[][];
		
		private Object getDerivedValue(AttributeTable2 attribTable, DerivedValueTable<? extends Object> valueTable)
		{
			int id = valueTable.getIDWithinNamespace();
			
			if ( derivedValues == null )
			{
				derivedValues = new Object[id+1][];
			}
			else if ( id >= derivedValues.length )
			{
				Object newValues[][] = new Object[id+1][];
				System.arraycopy( derivedValues, 0, newValues, 0, derivedValues.length );
				derivedValues = newValues;
			}
			
			Object valueHolder[] = derivedValues[id];
			if ( valueHolder == null )
			{
				valueHolder = new Object[1];
				valueHolder[0] = valueTable.evaluate( attribTable );
			}
			
			return valueHolder[0];
		}
	}
	
	private NamespaceTable namespaceTables[] = null;
	
	
	private NamespaceTable getNamespaceTable(AttributeNamespace namespace)
	{
		int id = namespace.getID();
		
		if ( namespaceTables == null )
		{
			namespaceTables = new NamespaceTable[id+1];
		}
		else if ( id >= namespaceTables.length )
		{
			NamespaceTable newTables[] = new NamespaceTable[id+1];
			System.arraycopy( namespaceTables, 0, newTables, 0, namespaceTables.length );
			namespaceTables = newTables;
		}
		
		NamespaceTable table = namespaceTables[id];
		if ( table == null )
		{
			table = new NamespaceTable();
			namespaceTables[id] = table;
		}
		
		return table;
	}
	
	protected Object getDerivedValue(AttributeNamespace namespace, DerivedValueTable<? extends Object> valueTable)
	{
		NamespaceTable namespaceTable = getNamespaceTable( namespace );
		return namespaceTable.getDerivedValue( this, valueTable );
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
		private HashMap<AttributeValuesMultiple, WeakReference<AttributeTable2>> attributeTableSet = new HashMap<AttributeValuesMultiple, WeakReference<AttributeTable2>>();
	}
	
	
	
	protected static HashMap<Class<? extends AttributeTable2>, AttributeTableSet> attributeTableSetsByClass = new HashMap<Class<? extends AttributeTable2>, AttributeTableSet>();
	

	protected HashMap<AttributeBase, Object> values = new HashMap<AttributeBase, Object>();
	protected HashMap<AttributeValueSingle, WeakReference<AttributeTable2>> singleValueDerivedAttributeTables = new HashMap<AttributeValueSingle, WeakReference<AttributeTable2>>();
	protected HashMap<AttributeValuesMultiple, WeakReference<AttributeTable2>> multiValueDerivedAttributeTables = new HashMap<AttributeValuesMultiple, WeakReference<AttributeTable2>>();
	protected IdentityHashMap<AttributeTable2, WeakReference<AttributeTable2>> attribTableDerivedAttributeTables = new IdentityHashMap<AttributeTable2, WeakReference<AttributeTable2>>();
	protected HashMap<DelAttribute, WeakReference<AttributeTable2>> delDerivedAttributeTables = new HashMap<DelAttribute, WeakReference<AttributeTable2>>();
	
	
	public static AttributeTable2 instance = new AttributeTable2();
	
	
	protected AttributeTable2()
	{
	}
	
	
	protected AttributeTable2 newInstance()
	{
		return new AttributeTable2();
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
	public <V extends Object> V get(AttributeBase attribute, Class<V> valueClass)
	{
		if ( !values.containsKey( attribute ) )
		{
			return (V)attribute.getDefaultValue();
		}
		
		return (V)values.get( attribute );
	}
	
	@SuppressWarnings("unchecked")
	public <V extends Object> V getRequired(AttributeBase attribute, Class<V> valueClass)
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
	
	
	public AttributeTable2 withAttr(AttributeBase attribute, Object value)
	{
		AttributeValueSingle v = new AttributeValueSingle( attribute, value );
		WeakReference<AttributeTable2> derivedRef = singleValueDerivedAttributeTables.get( v );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			AttributeTable2 derived = newInstance();
			derived.values.putAll( values );
			derived.values.put( attribute, attribute.checkValue( value ) );
			derived = getUniqueAttributeTable( derived );
			derivedRef = new WeakReference<AttributeTable2>( derived );
			singleValueDerivedAttributeTables.put( v, derivedRef );
		}
		return derivedRef.get();
	}
	
	public AttributeTable2 withAttrs(HashMap<AttributeBase, Object> valuesMap)
	{
		if ( valuesMap.size() == 1 )
		{
			Map.Entry<AttributeBase, Object> entry = valuesMap.entrySet().iterator().next();
			return withAttr( entry.getKey(), entry.getValue() );
		}
		else
		{
			AttributeValuesMultiple v = new AttributeValuesMultiple( valuesMap );
			WeakReference<AttributeTable2> derivedRef = multiValueDerivedAttributeTables.get( v );
			if ( derivedRef == null  ||  derivedRef.get() == null )
			{
				AttributeTable2 derived = newInstance();
				derived.values.putAll( values );
				for (Map.Entry<AttributeBase, Object> entry: valuesMap.entrySet())
				{
					AttributeBase attribute = entry.getKey();
					derived.values.put( attribute, attribute.checkValue( entry.getValue() ) );
				}
				derived = getUniqueAttributeTable( derived );
				derivedRef = new WeakReference<AttributeTable2>( derived );
				multiValueDerivedAttributeTables.put( v, derivedRef );
			}
			return derivedRef.get();
		}
	}
		
	public AttributeTable2 withAttrs(AttributeTable2 attribs)
	{
		WeakReference<AttributeTable2> derivedRef = attribTableDerivedAttributeTables.get( attribs );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			AttributeTable2 derived = newInstance();
			derived.values.putAll( values );
			derived.values.putAll( attribs.values );
			derived = getUniqueAttributeTable( derived );
			derivedRef = new WeakReference<AttributeTable2>( derived );
			attribTableDerivedAttributeTables.put( attribs, derivedRef );
		}
		return derivedRef.get();
	}
	
	public AttributeTable2 withAttrFrom(AttributeBase destAttr, AttributeTable2 srcTable, AttributeBase srcAttr)
	{
		return withAttr( destAttr, srcTable.get( srcAttr ) );
	}
	
	public AttributeTable2 withAttrsFrom(AttributeTable2 srcTable, AttributeBase srcAttr)
	{
		AttributeTable2 attrs = srcTable.get( srcAttr, AttributeTable2.class );
		return withAttrs( attrs );
	}
	
	public AttributeTable2 withoutAttr(AttributeBase attribute)
	{
		if ( !values.containsKey( attribute ) )
		{
			// No attribute to remove - no change
			return this;
		}
		
		DelAttribute v = new DelAttribute( attribute );
		WeakReference<AttributeTable2> derivedRef = delDerivedAttributeTables.get( v );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			AttributeTable2 derived = newInstance();
			derived.values.putAll( values );
			derived.values.remove( attribute );
			derived = getUniqueAttributeTable( derived );
			derivedRef = new WeakReference<AttributeTable2>( derived );
			delDerivedAttributeTables.put( v, derivedRef );
		}
		return derivedRef.get();
	}
	
	public AttributeTable2 useAttr(AttributeBase attribute)
	{
		return attribute.use( this );
	}
	
	public AttributeTable2 remapAttr(AttributeBase destAttribute, AttributeBase sourceAttribute)
	{
		Object value = get( sourceAttribute );
		AttributeValueSingle v = new AttributeValueSingle( destAttribute, value );
		WeakReference<AttributeTable2> derivedRef = singleValueDerivedAttributeTables.get( v );
		if ( derivedRef == null  ||  derivedRef.get() == null )
		{
			AttributeTable2 derived = newInstance();
			derived.values.putAll( values );
			derived.values.put( destAttribute, destAttribute.checkValue( value ) );
			derived = getUniqueAttributeTable( derived );
			derivedRef = new WeakReference<AttributeTable2>( derived );
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
	
	private AttributeTable2 getUniqueAttributeTable(AttributeTable2 attribTable)
	{
		AttributeTableSet setsForClass = getAttributeTableTableForClass();
		
		AttributeValuesMultiple vals = attribTable.allValues();
		WeakReference<AttributeTable2> uniqueRef = setsForClass.attributeTableSet.get( vals );
		if ( uniqueRef == null  ||  uniqueRef.get() == null )
		{
			uniqueRef = new WeakReference<AttributeTable2>( attribTable );
			setsForClass.attributeTableSet.put( vals, uniqueRef ); 
		}
		
		return uniqueRef.get();
	}
	

	private AttributeValuesMultiple allValues()
	{
		return new AttributeValuesMultiple( values );
	}
	
	
	
	protected static Pres presentAttributeMap(GSymFragmentView fragment, AttributeTable inheritedState, HashMap<AttributeBase, Object> values)
	{
		Set<AttributeBase> attributeSet = values.keySet();
		AttributeBase attributes[] = attributeSet.toArray( new AttributeBase[0] );
		Arrays.sort( attributes, new AttributeBase.AttributeNameComparator() );
		Pres children[][] = new Pres[attributes.length+1][];
		
		StyleSheet2 attrTableStyle = getAttrTableStyle();
		
		children[0] = new Pres[] { attrTableStyle.applyTo( new StaticText( "Name" ) ), attrTableStyle.applyTo( new StaticText( "Value" ) ) };
		for (int i = 0; i < attributes.length; i++)
		{
			AttributeBase attribute = attributes[i];
			Object value = values.get( attribute );
			DPElement valueView = fragment.presentFragment( value, StyleSheet.instance );
			children[i+1] = new Pres[] { new StaticText( attribute.getFullName() ), Pres.elementToPres( valueView ) };
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
	private static StyleSheet2 _attrTableStyle = null;
	
	private static StyleSheet2 getAttrTableStyle()
	{
		if ( _attrTableStyle == null )
		{
			_attrTableStyle = StyleSheet2.instance.withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontSize, 14 )
					.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.5f ) ).withAttr( Primitive.tableColumnSpacing, 10.0 );
		}
		return _attrTableStyle;
	}
}
