//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class VerticalListViewLayoutStyleSheet extends ListViewLayoutStyleSheet
{
	private static class VerticalListViewLayoutParams
	{
		private double indentation;
		
		private VerticalListViewLayoutParams(double indentation)
		{
			this.indentation = indentation;
		}
	}

	
	
	private VerticalListViewLayoutParams layoutParams = null;
	
	public static VerticalListViewLayoutStyleSheet instance = new VerticalListViewLayoutStyleSheet();
	
	
	
	public VerticalListViewLayoutStyleSheet()
	{
		super();
		
		initAttr( "indentation", 0.0 );
	}


	protected StyleSheet newInstance()
	{
		return new VerticalListViewLayoutStyleSheet();
	}
	
	
	
	//
	// GENERAL
	//
	
	public VerticalListViewLayoutStyleSheet withIndentation(double indentation)
	{
		return (VerticalListViewLayoutStyleSheet)withAttr( "indentation", indentation );
	}


	
	//
	// PARAMS
	//
	private VerticalListViewLayoutParams getLayoutParams()
	{
		if ( layoutParams == null )
		{
			layoutParams = new VerticalListViewLayoutParams(
					get( "indentation", Double.class, 0.0 ) );
		}
		return layoutParams;
	}

	
	private DPWidget createLineParagraph(PrimitiveStyleSheet basicStyle, int index, DPWidget child, SeparatorElementFactory separator)
	{
		if ( separator != null )
		{
			return basicStyle.paragraph( Arrays.asList( new DPWidget[] { child, separator.createElement( basicStyle, index, child ) } ) );
		}
		else
		{
			return child;
		}
	}
	
	public DPWidget createListElement(List<DPWidget> children, PrimitiveStyleSheet primitiveStyle, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator,
			ElementFactory spacing, TrailingSeparator trailingSeparator)
	{
		VerticalListViewLayoutParams params = getLayoutParams();
		
		ArrayList<DPWidget> childElems = new ArrayList<DPWidget>();
		childElems.ensureCapacity( children.size() );
		
		if ( children.size() > 0 )
		{
			for (int i = 0; i < children.size() - 1; i++)
			{
				childElems.add( createLineParagraph( primitiveStyle, i, children.get( i ), separator ) );
			}

			if ( ListViewStyleSheet.trailingSeparatorRequired( children, trailingSeparator ) )
			{
				childElems.add( createLineParagraph( primitiveStyle, children.size() - 1, children.get( children.size() - 1 ), separator ) );
			}
			else
			{
				childElems.add( children.get( children.size() - 1 ) );
			}
		}

		DPVBox vbox = primitiveStyle.vbox( childElems );
		DPWidget indented = vbox.padX( params.indentation, 0.0 );
		
		
		if ( beginDelim != null  ||  endDelim != null )
		{
			ArrayList<DPWidget> outerChildElems = new ArrayList<DPWidget>();
			outerChildElems.ensureCapacity( 3 );
			
			if ( beginDelim != null )
			{
				outerChildElems.add( beginDelim.createElement( primitiveStyle ) );
			}
			
			outerChildElems.add( indented );
			
			if ( endDelim != null )
			{
				outerChildElems.add(  endDelim.createElement( primitiveStyle ) );
			}
			
			return primitiveStyle.vbox( outerChildElems );
		}
		else
		{
			return indented;
		}
	}
}
