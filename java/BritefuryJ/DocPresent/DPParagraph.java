//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeParagraph;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;



public class DPParagraph extends DPContainerSequence
{
	public static class CouldNotFindInsertionPointException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}



	
	public DPParagraph(ElementContext context)
	{
		this( context, ParagraphStyleSheet.defaultStyleSheet );
	}

	public DPParagraph(ElementContext context, ParagraphStyleSheet styleSheet)
	{
		super( context, styleSheet );
		
		layoutNode = new LayoutNodeParagraph( this );
	}
	
	//
	//
	// STYLESHEET METHODS
	//
	//


	protected PackingParams getDefaultPackingParams()
	{
		return null;
	}

	public double getSpacing()
	{
		return ((ParagraphStyleSheet)styleSheet).getSpacing();
	}

	public double getLineSpacing()
	{
		return ((ParagraphStyleSheet)styleSheet).getLineSpacing();
	}

	public double getIndentation()
	{
		return ((ParagraphStyleSheet)styleSheet).getIndentation();
	}
}
