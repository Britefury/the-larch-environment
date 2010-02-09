//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPParagraphDedentMarker;
import BritefuryJ.DocPresent.Layout.LReqBox;

public class LayoutNodeParagraphDedentMarker extends LayoutNodeParagraphMarker
{
	protected static LReqBox dedentReqBox = new LReqBox();
	
	static
	{
		dedentReqBox.setParagraphDedentMarker();
	}
	
	
	
	public LayoutNodeParagraphDedentMarker(DPParagraphDedentMarker element)
	{
		super( element, dedentReqBox );
	}
}
