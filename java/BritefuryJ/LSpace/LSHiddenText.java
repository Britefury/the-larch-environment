//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
