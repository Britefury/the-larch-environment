//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSParagraphIndentMarker;
import BritefuryJ.LSpace.Layout.LReqBox;

public class LayoutNodeParagraphIndentMarker extends LayoutNodeEmptySharedReq
{
	protected static LReqBox indentReqBox = new LReqBox();
	
	static
	{
		indentReqBox.setParagraphIndentMarker();
	}
	
	
	
	public LayoutNodeParagraphIndentMarker(LSParagraphIndentMarker element)
	{
		super( element, indentReqBox );
	}
}
