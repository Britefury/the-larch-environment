//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeParagraph;
import BritefuryJ.LSpace.StyleParams.ParagraphStyleParams;


public class LSParagraph extends LSContainerSequence
{
	public LSParagraph(LSElement[] items)
	{
		this( ParagraphStyleParams.defaultStyleParams, items );
	}

	public LSParagraph(ParagraphStyleParams styleParams, LSElement[] items)
	{
		super( styleParams, items );
		
		layoutNode = new LayoutNodeParagraph( this );
	}
	
	
	
	//
	//
	// STYLESHEET METHODS
	//
	//

	public double getSpacing()
	{
		return ((ParagraphStyleParams) styleParams).getSpacing();
	}

	public double getLineSpacing()
	{
		return ((ParagraphStyleParams) styleParams).getLineSpacing();
	}

	public double getIndentation()
	{
		return ((ParagraphStyleParams) styleParams).getIndentation();
	}
}
