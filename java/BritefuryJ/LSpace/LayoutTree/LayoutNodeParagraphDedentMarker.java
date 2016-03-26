//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSParagraphDedentMarker;
import BritefuryJ.LSpace.Layout.LReqBox;

public class LayoutNodeParagraphDedentMarker extends LayoutNodeEmptySharedReq
{
	protected static LReqBox dedentReqBox = new LReqBox();
	
	static
	{
		dedentReqBox.setParagraphDedentMarker();
	}
	
	
	
	public LayoutNodeParagraphDedentMarker(LSParagraphDedentMarker element)
	{
		super( element, dedentReqBox );
	}
}
