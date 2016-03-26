//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeParagraphIndentMarker;
import BritefuryJ.LSpace.StyleParams.ElementStyleParams;

public class LSParagraphIndentMarker extends LSBlank
{
	public LSParagraphIndentMarker()
	{
		super( );
		
		layoutNode = new LayoutNodeParagraphIndentMarker( this );
	}
	
	public LSParagraphIndentMarker(ElementStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeParagraphIndentMarker( this );
	}
}
