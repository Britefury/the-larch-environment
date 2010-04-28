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
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class VerticalInlineListViewLayoutStyleSheet extends ListViewLayoutStyleSheet
{
	private static class VerticalInlineListViewLayoutParams
	{
		private double indentation;
		
		private VerticalInlineListViewLayoutParams(double indentation)
		{
			this.indentation = indentation;
		}
	}

	
	
	private VerticalInlineListViewLayoutParams layoutParams = null;
	
	public static VerticalInlineListViewLayoutStyleSheet instance = new VerticalInlineListViewLayoutStyleSheet();
	
	
	
	public VerticalInlineListViewLayoutStyleSheet()
	{
		super();
		
		initAttr( "indentation", 0.0 );
	}
	

	protected StyleSheet newInstance()
	{
		return new VerticalInlineListViewLayoutStyleSheet();
	}
	
	
	
	//
	// GENERAL
	//
	
	public VerticalInlineListViewLayoutStyleSheet withIndentation(double indentation)
	{
		return (VerticalInlineListViewLayoutStyleSheet)withAttr( "indentation", indentation );
	}


	
	//
	// PARAMS
	//
	private VerticalInlineListViewLayoutParams getLayoutParams()
	{
		if ( layoutParams == null )
		{
			layoutParams = new VerticalInlineListViewLayoutParams(
					getNonNull( "indentation", Double.class, 0.0 ) );
		}
		return layoutParams;
	}

	
	private DPElement createLineParagraph(PrimitiveStyleSheet basicStyle, int index, DPElement child, SeparatorElementFactory separator)
	{
		if ( separator != null )
		{
			return basicStyle.paragraph( Arrays.asList( new DPElement[] { child, separator.createElement( basicStyle, index, child ) } ) );
		}
		else
		{
			return child;
		}
	}
	
	public DPElement createListElement(List<DPElement> children, PrimitiveStyleSheet primitiveStyle, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator,
			ElementFactory spacing, TrailingSeparator trailingSeparator)
	{
		VerticalInlineListViewLayoutParams params = getLayoutParams();
		
		if ( children.size() <= 1 )
		{
			// Paragraph with contents: [ beginDelim ] + children + [ endDelim ]
			ArrayList<DPElement> childElems = new ArrayList<DPElement>();
			if ( beginDelim != null )
			{
				childElems.add( beginDelim.createElement( primitiveStyle ) );
			}
			
			childElems.addAll( children );
			
			if ( ListViewStyleSheet.trailingSeparatorRequired( children, trailingSeparator )  &&  children.size() == 1 )
			{
				childElems.add( separator.createElement( primitiveStyle, 0, children.get( 0 ) ) );
			}
			
			if ( endDelim != null )
			{
				childElems.add( endDelim.createElement( primitiveStyle ) );
			}
			
			return primitiveStyle.paragraph( childElems );
		}
		else
		{
			// First line
			DPElement first = null;
			if ( beginDelim != null  ||  separator != null )
			{
				DPElement child = children.get( 0 );
				ArrayList<DPElement> firstChildElems = new ArrayList<DPElement>();
				firstChildElems.ensureCapacity( 3 );
				if ( beginDelim != null )
				{
					firstChildElems.add( beginDelim.createElement( primitiveStyle ) );
				}
				firstChildElems.add( child );
				if ( separator != null )
				{
					firstChildElems.add( separator.createElement( primitiveStyle, 0, child ) );
				}
				first = primitiveStyle.paragraph( firstChildElems );
			}
			else
			{
				first = children.get( 0 );
			}
			
			
			// Middle lines
			ArrayList<DPElement> childElems = new ArrayList<DPElement>();
			childElems.ensureCapacity( children.size() );
			for (int i = 1; i < children.size() - 1; i++)
			{
				childElems.add( createLineParagraph( primitiveStyle, i, children.get( i ), separator ) );
			}
			
			// Last line
			if ( ListViewStyleSheet.trailingSeparatorRequired( children, trailingSeparator ) )
			{
				childElems.add( createLineParagraph( primitiveStyle, children.size() - 1, children.get( children.size() - 1 ), separator ) );
			}
			else
			{
				childElems.add( createLineParagraph( primitiveStyle, children.size() - 1, children.get( children.size() - 1 ), null ) );
			}
			
			DPVBox middleVBox = primitiveStyle.vbox( childElems );
			DPElement indent = middleVBox.padX( params.indentation );
			
			
			if ( endDelim != null )
			{
				return primitiveStyle.vbox( Arrays.asList( new DPElement[] { first, indent, endDelim.createElement( primitiveStyle ) } ), 0 );
			}
			else
			{
				return primitiveStyle.vbox( Arrays.asList( new DPElement[] { first, indent } ), 0 );
			}
		}
	}
}
