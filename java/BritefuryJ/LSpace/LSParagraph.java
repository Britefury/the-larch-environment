//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
