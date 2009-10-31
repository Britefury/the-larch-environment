//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;

public class DPParagraphDedentMarker extends DPParagraphMarker
{
	public DPParagraphDedentMarker(ElementContext context)
	{
		super( context );
	}
	
	public DPParagraphDedentMarker(ElementContext context, WidgetStyleSheet styleSheet)
	{
		super( context, styleSheet );
	}



	protected void updateRequisitionX()
	{
		super.updateRequisitionX();
		layoutReqBox.setParagraphDedentMarker();
	}

	protected void updateRequisitionY()
	{
		super.updateRequisitionY();
		layoutReqBox.setParagraphDedentMarker();
	}
}
