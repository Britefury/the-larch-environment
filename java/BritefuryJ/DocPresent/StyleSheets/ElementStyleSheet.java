//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.util.Arrays;

import org.python.core.Py;
import org.python.core.PyObject;

public class ElementStyleSheet
{
	protected static StyleSheetLayout<ElementStyleSheetField> layout = new StyleSheetLayout<ElementStyleSheetField>();
	
	
	protected boolean fieldFlags[];
	protected Object fieldValues[];
	
	
	
	
	public ElementStyleSheet()
	{
		fieldFlags = new boolean[layout.fields.size()];
		fieldValues = new Object[layout.fields.size()];
		Arrays.fill( fieldFlags, false );
	}
	
	public ElementStyleSheet(String names[], Object values[])
	{
		fieldFlags = new boolean[layout.fields.size()];
		fieldValues = new Object[layout.fields.size()];
		Arrays.fill( fieldFlags, false );
		

		if ( names.length != values.length )
		{
			throw new RuntimeException( "ElementStyleSheet.<init>(): all parameters must have a keyword" );
		}
		
		
		for (int i = 0; i < names.length; i++)
		{
			StyleSheetField field = layout.nameToField.get( names[i] );
			if ( field == null )
			{
				throw new RuntimeException( "ElementStyleSheet.<init>(): no field named \"" + names[i] + "\"" );
			}
			else
			{
				if ( field.getValueClass().isInstance( values[i] )  ||  values[i] == null )
				{
					fieldValues[field.index] = values[i];
					fieldFlags[field.index] = true;
				}
				else
				{
					throw new StyleSheetField.ValueTypeException( "ElementStyleSheet.<init>(): value for field \"" + names[i] + "\" is of incorrect type" );
				}
			}
		}
	}
	
	public ElementStyleSheet(ElementStyleSheet baseSheet, String names[], Object values[])
	{
		fieldFlags = new boolean[layout.fields.size()];
		fieldValues = new Object[layout.fields.size()];
		System.arraycopy( baseSheet.fieldFlags, 0, fieldFlags, 0, baseSheet.fieldFlags.length );
		System.arraycopy( baseSheet.fieldValues, 0, fieldValues, 0, baseSheet.fieldValues.length );

		if ( names.length != values.length )
		{
			throw new RuntimeException( "ElementStyleSheet.<init>(): all parameters must have a keyword" );
		}
		
		
		for (int i = 0; i < names.length; i++)
		{
			StyleSheetField field = layout.nameToField.get( names[i] );
			if ( field == null )
			{
				throw new RuntimeException( "ElementStyleSheet.<init>(): no field named \"" + names[i] + "\"" );
			}
			else
			{
				if ( field.getValueClass().isInstance( values[i] ) )
				{
					fieldValues[field.index] = values[i];
					fieldFlags[field.index] = true;
				}
				else
				{
					throw new StyleSheetField.ValueTypeException( "ElementStyleSheet.<init>(): value for field \"" + names[i] + "\" is of incorrect type" );
				}
			}
		}
	}
	
	public ElementStyleSheet(PyObject values[], String names[])
	{
		System.out.println( "From python " + values.length + " " + names.length );
		fieldFlags = new boolean[layout.fields.size()];
		fieldValues = new Object[layout.fields.size()];

		int valuesOffset = 0;
		if ( values.length  ==  names.length + 1 )
		{
			// 1 extra parameter - a base element style sheet
			// Initialise to the values from the base sheet
			ElementStyleSheet baseSheet = Py.tojava( values[0], ElementStyleSheet.class );
			System.arraycopy( baseSheet.fieldFlags, 0, fieldFlags, 0, baseSheet.fieldFlags.length );
			System.arraycopy( baseSheet.fieldValues, 0, fieldValues, 0, baseSheet.fieldValues.length );
			valuesOffset = 1;
		} 
		else if ( names.length == values.length )
		{
			Arrays.fill( fieldFlags, false );
		}
		else
		{
			throw new RuntimeException( "ElementStyleSheet.<init>(): all parameters must have a keyword" );
		}
		
		
		for (int i = 0; i < names.length; i++)
		{
			StyleSheetField field = layout.nameToField.get( names[i] );
			if ( field == null )
			{
				throw Py.KeyError( names[i] );
			}
			else
			{
				Object value = Py.tojava( values[i+valuesOffset], field.getValueClass() );
				System.out.println( value );
				fieldValues[field.index] = value;
				fieldFlags[field.index] = true;
			}
		}
	}
	
	

	public boolean containsKey(String key)
	{
		ElementStyleSheetField field = layout.nameToField.get( key );
		if ( field != null )
		{
			return containsKey( field );
		}
		else
		{
			return false;
		}
	}

	public boolean containsKey(ElementStyleSheetField field)
	{
		if ( field.index < fieldValues.length )
		{
			return fieldFlags[field.index];
		}
		else
		{
			return false;
		}
	}

	public Object get(String key)
	{
		ElementStyleSheetField field = layout.nameToField.get( key );
		if ( field != null )
		{
			return get( field );
		}
		else
		{
			return null;
		}
	}

	public Object get(ElementStyleSheetField field)
	{
		if ( field.index < fieldValues.length )
		{
			return fieldValues[field.index];
		}
		else
		{
			return null;
		}
	}
	
	
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof ElementStyleSheet )
		{
			ElementStyleSheet e = (ElementStyleSheet)x;
			
			return Arrays.asList( fieldValues ).equals( Arrays.asList( e.fieldValues ) );
		}
		else
		{
			return false;
		}
	}
	
	
	public static ElementStyleSheet newSheet(PyObject values[], String names[])
	{
		return new ElementStyleSheet( values, names );
	}
}
