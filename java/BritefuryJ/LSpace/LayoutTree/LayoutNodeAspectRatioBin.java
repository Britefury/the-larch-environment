//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSAspectRatioBin;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;

public class LayoutNodeAspectRatioBin extends LayoutNodeBin
{
	public LayoutNodeAspectRatioBin(LSAspectRatioBin element)
	{
		super( element );
	}


	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSAspectRatioBin spacer = (LSAspectRatioBin)element;
		LSElement child = spacer.getChild();
		double minWidth = spacer.getMinWidth();
		if ( child != null )
		{
			LReqBoxInterface c = child.getLayoutNode().refreshRequisitionX();
			if ( minWidth >= 0.0 )
			{
				child.getLayoutNode().refreshRequisitionX();
				layoutReqBox.setRequisitionX( Math.max( minWidth, c.getReqMinWidth() ), Math.max( minWidth, c.getReqMinHAdvance() ), c.getReqPrefWidth(), c.getReqPrefHAdvance() );
			}
			else
			{
				layoutReqBox.setRequisitionX( c );
			}
		}
		else
		{
			minWidth = Math.max( minWidth, 0.0 );
			layoutReqBox.setRequisitionX( minWidth, minWidth );
		}
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSAspectRatioBin spacer = (LSAspectRatioBin)element;
		LSElement child = spacer.getChild();
		double width = spacer.getLayoutNode().getAllocWidth();
		double aspectRatio = spacer.getAspectRatio();
		double minHeight = width / aspectRatio;
		if ( child != null )
		{
			LReqBoxInterface c = child.getLayoutNode().refreshRequisitionY();
			if ( minHeight >= 0.0 )
			{
				child.getLayoutNode().refreshRequisitionY();
				if ( minHeight > c.getReqHeight() )
				{
					layoutReqBox.setRequisitionY( minHeight, 0.0 );
				}
				else
				{
					layoutReqBox.setRequisitionY( c.getReqHeight(), c.getReqVSpacing(), c.getReqRefY() );
				}
			}
			else
			{
				layoutReqBox.setRequisitionY( c );
			}
		}
		else
		{
			minHeight = Math.max( minHeight, 0.0 );
			layoutReqBox.setRequisitionY( minHeight, 0.0 );
		}
	}
}
