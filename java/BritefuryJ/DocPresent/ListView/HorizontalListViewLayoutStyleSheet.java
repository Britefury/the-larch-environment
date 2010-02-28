//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ListView;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPWidget;
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
	
	
	public DPWidget createListElement(List<DPWidget> children, PrimitiveStyleSheet primitiveStyle, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator,
			ElementFactory spacing, TrailingSeparator trailingSeparator)
	{
		ArrayList<DPWidget> childElems = new ArrayList<DPWidget>();
		childElems.ensureCapacity( children.size() + 2 );
		
		if ( beginDelim != null )
		{
			childElems.add( beginDelim.createElement( primitiveStyle ) );
		}
		
		if ( children.size() > 0 )
		{
			for (int i = 0; i < children.size() - 1; i++)
			{
				DPWidget child = children.get( i );
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

			DPWidget lastChild = children.get( children.size() - 1 );
			childElems.add( lastChild );
			
			if ( ListViewStyleSheet.trailingSeparatorRequired( children, trailingSeparator ) )
			{
				if ( separator != null )
				{
					childElems.add( separator.createElement( primitiveStyle, children.size() - 1, lastChild ) );
				}
			}
		}

		if ( endDelim != null )
		{
			childElems.add( endDelim.createElement( primitiveStyle ) );
		}
		

		return primitiveStyle.hbox( childElems  );
	}
}
