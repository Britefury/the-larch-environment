//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSSpaceBin;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;

public class LayoutNodeSpaceBin extends LayoutNodeBin
{
	public LayoutNodeSpaceBin(LSSpaceBin element)
	{
		super( element );
	}


	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSSpaceBin spacer = (LSSpaceBin)element;
		LSElement child = spacer.getChild();
		double width = spacer.getWidth();
		if ( child != null )
		{
			LReqBoxInterface c = child.getLayoutNode().refreshRequisitionX();
			if ( width >= 0.0 )
			{
				child.getLayoutNode().refreshRequisitionX();
				if ( spacer.isMaxSize() )
				{
					layoutReqBox.setRequisitionX( Math.min( width, c.getReqMinWidth() ), Math.min( width, c.getReqPrefWidth() ),
							Math.min( width, c.getReqMinHAdvance() ), Math.min( width, c.getReqPrefHAdvance() ) );
				}
				else
				{
					layoutReqBox.setRequisitionX( Math.max( width, c.getReqMinWidth() ), Math.max( width, c.getReqPrefWidth() ),
							Math.max( width, c.getReqMinHAdvance() ), Math.max( width, c.getReqPrefHAdvance() ) );
				}
			}
			else
			{
				layoutReqBox.setRequisitionX( c );
			}
		}
		else
		{
			if ( !spacer.isMaxSize() )
			{
				width = Math.max( width, 0.0 );
				layoutReqBox.setRequisitionX( width, width );
			}
		}
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSSpaceBin spacer = (LSSpaceBin)element;
		LSElement child = spacer.getChild();
		double height = spacer.getHeight();
		if ( child != null )
		{
			LReqBoxInterface c = child.getLayoutNode().refreshRequisitionY();
			if ( height >= 0.0 )
			{
				child.getLayoutNode().refreshRequisitionY();
				if ( spacer.isMaxSize() )
				{
					if ( height < c.getReqHeight() )
					{
						double halfPadding = ( height - c.getReqHeight() ) * 0.5;
						layoutReqBox.setRequisitionY( height, c.getReqVSpacing(), c.getReqRefY() + halfPadding );
					}
					else
					{
						layoutReqBox.setRequisitionY( c.getReqHeight(), c.getReqVSpacing(), c.getReqRefY() );
					}
				}
				else
				{
					if ( height > c.getReqHeight() )
					{
						double halfPadding = ( height - c.getReqHeight() ) * 0.5;
						layoutReqBox.setRequisitionY( height, c.getReqVSpacing(), c.getReqRefY() + halfPadding );
					}
					else
					{
						layoutReqBox.setRequisitionY( c.getReqHeight(), c.getReqVSpacing(), c.getReqRefY() );
					}
				}
			}
			else
			{
				layoutReqBox.setRequisitionY( c );
			}
		}
		else
		{
			if ( !spacer.isMaxSize() )
			{
				height = Math.max( height, 0.0 );
				layoutReqBox.setRequisitionY( height, 0.0 );
			}
		}
	}
}
