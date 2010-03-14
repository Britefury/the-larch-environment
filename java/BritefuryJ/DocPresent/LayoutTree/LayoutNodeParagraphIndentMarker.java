//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPParagraphIndentMarker;
import BritefuryJ.DocPresent.Layout.LReqBox;

public class LayoutNodeParagraphIndentMarker extends LayoutNodeEmptySharedReq
{
	protected static LReqBox indentReqBox = new LReqBox();
	
	static
	{
		indentReqBox.setParagraphIndentMarker();
	}
	
	
	
	public LayoutNodeParagraphIndentMarker(DPParagraphIndentMarker element)
	{
		super( element, indentReqBox );
	}
}
