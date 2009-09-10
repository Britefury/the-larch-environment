//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementContext;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class VerticalInlineListViewLayout extends IndentedListViewLayout
{
	private VBoxStyleSheet styleSheet;
	private ParagraphStyleSheet lineParagraphStyleSheet;
	private TrailingSeparator trailingSeparator;
	
	
	public VerticalInlineListViewLayout(VBoxStyleSheet styleSheet, ParagraphStyleSheet lineParagraphStyleSheet, float indentation, TrailingSeparator trailingSeparator)
	{
		super( indentation );
		this.styleSheet = styleSheet;
		this.lineParagraphStyleSheet = lineParagraphStyleSheet;
		this.trailingSeparator = trailingSeparator;
	}
	
	
	private DPWidget createLineParagraph(ElementContext ctx, int index, DPWidget child, SeparatorElementFactory separator)
	{
		if ( separator != null )
		{
			DPParagraph paragraph = new DPParagraph( lineParagraphStyleSheet );
			paragraph.setContext( ctx );

			paragraph.setChildren( Arrays.asList( new DPWidget[] { child, separator.createElement( ctx, index, child ) } ) );
			return paragraph;
		}
		else
		{
			return child;
		}
	}
	

	public DPWidget createListElement(ElementContext ctx, List<DPWidget> children, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator)
	{
		if ( children.size() <= 1 )
		{
			// Paragraph with contents: [ beginDelim ] + children + [ endDelim ]
			DPParagraph paragraph = new DPParagraph( lineParagraphStyleSheet );
			paragraph.setContext( ctx );
			
			ArrayList<DPWidget> childElems = new ArrayList<DPWidget>();
			if ( beginDelim != null )
			{
				childElems.add( beginDelim.createElement( ctx ) );
			}
			
			childElems.addAll( children );
			
			if ( trailingSeparatorRequired( children, trailingSeparator )  &&  children.size() == 1 )
			{
				childElems.add( separator.createElement( ctx, 0, children.get( 0 ) ) );
			}
			
			if ( endDelim != null )
			{
				childElems.add( endDelim.createElement( ctx ) );
			}
			
			paragraph.setChildren( childElems );
			
			return paragraph;
		}
		else
		{
			// First line
			DPWidget first = null;
			if ( beginDelim != null  ||  separator != null )
			{
				DPParagraph firstPara = new DPParagraph( lineParagraphStyleSheet );
				firstPara.setContext( ctx );
				DPWidget child = children.get( 0 );
				ArrayList<DPWidget> firstChildElems = new ArrayList<DPWidget>();
				firstChildElems.ensureCapacity( 3 );
				if ( beginDelim != null )
				{
					firstChildElems.add( beginDelim.createElement( ctx ) );
				}
				firstChildElems.add( child );
				if ( separator != null )
				{
					firstChildElems.add( separator.createElement( ctx, 0, child ) );
				}
				firstPara.setChildren( firstChildElems );
				first = firstPara;
			}
			else
			{
				first = children.get( 0 );
			}
			
			
			// Middle lines
			ArrayList<DPWidget> childElems = new ArrayList<DPWidget>();
			childElems.ensureCapacity( children.size() );
			for (int i = 1; i < children.size() - 1; i++)
			{
				childElems.add( createLineParagraph( ctx, i, children.get( i ), separator ) );
			}
			
			// Last line
			if ( trailingSeparatorRequired( children, trailingSeparator ) )
			{
				childElems.add( createLineParagraph( ctx, children.size() - 1, children.get( children.size() - 1 ), separator ) );
			}
			else
			{
				childElems.add( createLineParagraph( ctx, children.size() - 1, children.get( children.size() - 1 ), null ) );
			}
			
			DPVBox middleVBox = new DPVBox( styleSheet );
			middleVBox.setContext( ctx );
			middleVBox.setChildren( childElems );
			DPWidget indent = indent( ctx, middleVBox );
			
			
			DPVBox mainVBox = new DPVBox( styleSheet );
			mainVBox.setContext( ctx );
			if ( endDelim != null )
			{
				mainVBox.setChildren( Arrays.asList( new DPWidget[] { first, indent, endDelim.createElement( ctx ) } ) );
			}
			else
			{
				mainVBox.setChildren( Arrays.asList( new DPWidget[] { first, indent } ) );
			}
			
			return mainVBox;
		}
	}
}
