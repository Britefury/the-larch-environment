//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeParagraph;
import BritefuryJ.DocPresent.StyleParams.ParagraphStyleParams;


public class DPParagraph extends DPContainerSequence
{
	public DPParagraph()
	{
		this( ParagraphStyleParams.defaultStyleParams);
	}

	public DPParagraph(ParagraphStyleParams styleParams)
	{
		super(styleParams);
		
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
