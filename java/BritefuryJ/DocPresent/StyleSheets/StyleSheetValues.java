//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.util.Arrays;
import java.util.HashMap;

import BritefuryJ.Utils.HashUtils;

public class StyleSheetValues
{
	private static class CascadeKey
	{
		private StyleSheetValues parent;
		private ElementStyleSheet elementSheet;
		private boolean bContainerCascade;
		private StyleSheetValueFieldSet usedFields;
		private int hash;
		
		
		public CascadeKey(StyleSheetValues parent, ElementStyleSheet elementSheet, boolean bContainerCascade, StyleSheetValueFieldSet usedFields)
		{
			this.parent = parent;
			this.elementSheet = elementSheet;
			this.bContainerCascade = bContainerCascade;
			this.usedFields = usedFields;
			hash = HashUtils.quadHash( parent.hashCode(), elementSheet.hashCode(), new Boolean( bContainerCascade ).hashCode(), this.usedFields.hashCode() );
		}
		
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof CascadeKey )
			{
				CascadeKey k = (CascadeKey)x;
				
				return parent == k.parent  &&  elementSheet == k.elementSheet  &&  bContainerCascade == k.bContainerCascade  &&  usedFields.equals( k.usedFields );
			}
			else
			{
				return false;
			}
		}
		
		public int hashCode()
		{
			return hash;
		}
	}
	
	
	
	protected static StyleSheetLayout<StyleSheetValueField> layout = new StyleSheetLayout<StyleSheetValueField>();
	protected static HashMap<CascadeKey, StyleSheetValues> cascadeTable = new HashMap<CascadeKey, StyleSheetValues>();
	
	
	private Object fieldValues[];
	private int hash;
	
	
	
	
	public StyleSheetValues()
	{
		fieldValues = new Object[layout.fields.size()];
		hash = HashUtils.hashArray( fieldValues );
		for (int i = 0; i < layout.fields.size(); i++)
		{
			fieldValues[i] = layout.fields.get( i ).getDefaultValue();
		}
	}
	
	private StyleSheetValues(Object values[])
	{
		if ( values.length != layout.fields.size() )
		{
			throw new RuntimeException( "Incorrect input value-array length" );
		}
		
		fieldValues = new Object[layout.fields.size()];
		System.arraycopy( values, 0, fieldValues, 0, values.length );
		hash = HashUtils.hashArray( fieldValues );
	}
	
	

	public boolean containsKey(String key)
	{
		return layout.nameToField.containsKey( key );
	}

	public Object get(String key)
	{
		StyleSheetValueField field = layout.nameToField.get( key );
		if ( field != null )
		{
			return get( field );
		}
		else
		{
			return null;
		}
	}
	
	public Object get(StyleSheetValueField field)
	{
		if ( field.index < fieldValues.length )
		{
			return fieldValues[field.index];
		}
		
		return field.getDefaultValue();
	}



	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof StyleSheetValues )
		{
			StyleSheetValues e = (StyleSheetValues)x;
			
			return Arrays.asList( fieldValues ).equals( Arrays.asList( e.fieldValues ) );
		}
		else
		{
			return false;
		}
	}
	
	public int hashCode()
	{
		return hash;
	}




	
	
	
	public static StyleSheetValues cascade(StyleSheetValues parentSheet, ElementStyleSheet elementSheet, StyleSheetValueFieldSet usedFields)
	{
		if ( elementSheet == null )
		{
			return parentSheet;
		}
		else
		{
			CascadeKey key = new CascadeKey( parentSheet, elementSheet, false, usedFields );
			StyleSheetValues sheetValues = cascadeTable.get( key );
			
			if ( sheetValues == null  )
			{
				Object values[] = new Object[layout.fields.size()];
				
				for (int i = 0; i < layout.fields.size(); i++)
				{
					StyleSheetValueField field = layout.fields.get( i );
					values[i] = field.cascadeValue( parentSheet, elementSheet, usedFields.contains( field ) );
				}
				
				sheetValues = new StyleSheetValues( values );
				if ( sheetValues.equals( parentSheet ) )
				{
					sheetValues = parentSheet;
				}
				cascadeTable.put( key, sheetValues );
			}
			
			return sheetValues;
		}
	}

	public static StyleSheetValues packingContainerCascade(StyleSheetValues parentSheet, ElementStyleSheet elementSheet, StyleSheetValueFieldSet usedFields)
	{
		if ( elementSheet == null )
		{
			return parentSheet;
		}
		else
		{
			CascadeKey key = new CascadeKey( parentSheet, elementSheet, true, usedFields );
			StyleSheetValues sheetValues = cascadeTable.get( key );
			
			if ( sheetValues == null  )
			{
				Object values[] = new Object[layout.fields.size()];
				
				for (int i = 0; i < layout.fields.size(); i++)
				{
					StyleSheetValueField field = layout.fields.get( i );
					values[i] = field.packingContainerCascadeValue( parentSheet, elementSheet, usedFields.contains( field ) );
				}
				
				sheetValues = new StyleSheetValues( values );
				if ( sheetValues.equals( parentSheet ) )
				{
					sheetValues = parentSheet;
				}
				cascadeTable.put( key, sheetValues );
			}
			
			return sheetValues;
		}
	}
}
