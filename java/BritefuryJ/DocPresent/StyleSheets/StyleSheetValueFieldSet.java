//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class StyleSheetValueFieldSet
{
	private HashSet<StyleSheetValueField> fields;
	private int hash;
	
	
	public StyleSheetValueFieldSet()
	{
		fields = new HashSet<StyleSheetValueField>();
		hash = fields.hashCode();
	}

	public StyleSheetValueFieldSet(Collection<StyleSheetValueField> fields)
	{
		this.fields = new HashSet<StyleSheetValueField>();
		this.fields.addAll( fields );
		hash = fields.hashCode();
	}

	public StyleSheetValueFieldSet(StyleSheetValueField ... fields)
	{
		this( Arrays.asList( fields ) );
	}
	
	private StyleSheetValueFieldSet(Collection<StyleSheetValueField> a, Collection<StyleSheetValueField> b)
	{
		this.fields = new HashSet<StyleSheetValueField>();
		this.fields.addAll( a );
		this.fields.addAll( b );
		hash = fields.hashCode();
	}

	
	public boolean contains(StyleSheetValueField field)
	{
		return fields.contains( field );
	}
	
	
	public StyleSheetValueFieldSet join(Collection<StyleSheetValueField> b)
	{
		return new StyleSheetValueFieldSet( fields, b );
	}
	
	public StyleSheetValueFieldSet join(StyleSheetValueField ... b)
	{
		return new StyleSheetValueFieldSet( fields, Arrays.asList( b ) );
	}
	
	
	public int hashCode()
	{
		return hash;
	}
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof StyleSheetValueFieldSet )
		{
			StyleSheetValueFieldSet s = (StyleSheetValueFieldSet)x;
			
			if ( hash == s.hash )
			{
				return fields.equals( s.fields );
			}
		}

		return false;
	}
}
