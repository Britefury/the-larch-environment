//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSText;
import BritefuryJ.LSpace.Util.TextVisual;

public class LayoutNodeText extends EditableContentLeafLayoutNodeSharedReq
{
	public LayoutNodeText(LSText element)
	{
		super( element, element.getVisual().getRequisition() );
	}

	protected void updateRequisitionX()
	{
		LSText text = (LSText)element;
		layoutReqBox = text.getVisual().getRequisition();
	}

	protected void updateRequisitionY()
	{
		LSText text = (LSText)element;
		layoutReqBox = text.getVisual().getRequisition();
	}
	
	
	public void setVisual(TextVisual visual)
	{
		layoutReqBox = visual.getRequisition();
	}
}
