//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ListView;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class HorizontalListViewLayoutStyleSheet extends ListViewLayoutStyleSheet
{
	public static HorizontalListViewLayoutStyleSheet instance = new HorizontalListViewLayoutStyleSheet();
	
	
	public HorizontalListViewLayoutStyleSheet()
	{
		super();
	}
	
	
	protected StyleSheet newInstance()
	{
		return new HorizontalListViewLayoutStyleSheet();
	}
	
	
	public DPElement createListElement(List<DPElement> children, PrimitiveStyleSheet primitiveStyle, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator,
			ElementFactory spacing, TrailingSeparator trailingSeparator)
	{
		ArrayList<DPElement> childElems = new ArrayList<DPElement>();
		childElems.ensureCapacity( children.size() + 2 );
		
		if ( beginDelim != null )
		{
			childElems.add( beginDelim.createElement( primitiveStyle ) );
		}
		
		if ( children.size() > 0 )
		{
			for (int i = 0; i < children.size() - 1; i++)
			{
				DPElement child = children.get( i );
				childElems.add( child );
				if ( separator != null )
				{
					childElems.add( separator.createElement( primitiveStyle, i, child ) );
				}
				if ( spacing != null )
				{
					childElems.add( spacing.createElement( primitiveStyle ) );
				}
			}

			DPElement lastChild = children.get( children.size() - 1 );
			childElems.add( lastChild );
			
			if ( separator != null  &&  ListViewStyleSheet.trailingSeparatorRequired( children, trailingSeparator ) )
			{
				childElems.add( separator.createElement( primitiveStyle, children.size() - 1, lastChild ) );
			}
		}

		if ( endDelim != null )
		{
			childElems.add( endDelim.createElement( primitiveStyle ) );
		}
		
		
		return primitiveStyle.hbox( childElems.toArray( new DPElement[0] ) );
	}
}
