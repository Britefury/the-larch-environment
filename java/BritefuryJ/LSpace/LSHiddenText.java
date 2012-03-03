//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeHiddenText;
import BritefuryJ.LSpace.StyleParams.ContentLeafStyleParams;

public class LSHiddenText extends LSContentLeaf
{
	public LSHiddenText()
	{
		this( ContentLeafStyleParams.defaultStyleParams, "" );
	}
	
	public LSHiddenText(String textRepresentation)
	{
		this( ContentLeafStyleParams.defaultStyleParams, textRepresentation );
	}
	
	public LSHiddenText(ContentLeafStyleParams styleParams)
	{
		this(styleParams, "" );
	}

	public LSHiddenText(ContentLeafStyleParams styleParams, String textRepresentation)
	{
		super( styleParams, textRepresentation );
		
		layoutNode = new LayoutNodeHiddenText( this );
	}
}
