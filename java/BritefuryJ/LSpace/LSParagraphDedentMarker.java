//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
