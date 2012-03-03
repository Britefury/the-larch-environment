//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeParagraphDedentMarker;
import BritefuryJ.LSpace.StyleParams.ElementStyleParams;

public class LSParagraphDedentMarker extends LSBlank
{
	public LSParagraphDedentMarker()
	{
		super( );
		
		layoutNode = new LayoutNodeParagraphDedentMarker( this );
	}
	
	public LSParagraphDedentMarker(ElementStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeParagraphDedentMarker( this );
	}
}
