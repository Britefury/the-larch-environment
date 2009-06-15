//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;


public class DPBorder extends DPBin
{
	public static EmptyBorder defaultBorder = new EmptyBorder( 0.0, 0.0, 0.0, 0.0 );
	
	protected Border border;
	
	
	
	public DPBorder()
	{
		this( defaultBorder, ContainerStyleSheet.defaultStyleSheet );
	}

	public DPBorder(Border border)
	{
		this( border, ContainerStyleSheet.defaultStyleSheet );
	}

	public DPBorder(ContainerStyleSheet styleSheet)
	{
		this( defaultBorder, styleSheet );
	}
	
	public DPBorder(Border border, ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
		
		this.border = border;
	}
	
	
	
	public Border getBorder()
	{
		return border;
	}
	
	public void setBorder(Border b)
	{
		if ( b != border )
		{
			if ( b.getLeftMargin() != border.getLeftMargin()  ||  b.getRightMargin() != border.getRightMargin()  ||  b.getTopMargin() != border.getTopMargin()  ||  b.getBottomMargin() != border.getBottomMargin() )
			{
				queueResize();
			}
			else
			{
				queueFullRedraw();
			}
			border = b;
		}
	}
	
	
	
	protected void drawBackground(Graphics2D graphics)
	{
		border.draw( graphics, 0.0, 0.0, layoutBox.getAllocationX(), layoutBox.getAllocationY() );
	}
	

	
	protected void updateRequisitionX()
	{
		if ( child != null )
		{
			layoutBox.setRequisitionX( child.refreshRequisitionX() );
		}
		else
		{
			layoutBox.clearRequisitionX();
		}
		layoutBox.borderX( border.getLeftMargin(), border.getRightMargin() );
	}

	protected void updateRequisitionY()
	{
		if ( child != null )
		{
			layoutBox.setRequisitionY( child.refreshRequisitionY() );
		}
		else
		{
			layoutBox.clearRequisitionY();
		}
		layoutBox.borderY( border.getTopMargin(), border.getBottomMargin() );
	}

	
	
	
	protected void updateAllocationX()
	{
		if ( child != null )
		{
			double hborder = border.getLeftMargin() + border.getRightMargin();
			double prevWidth = child.layoutBox.getAllocationX();
			layoutBox.allocateChildX( child.layoutBox, border.getLeftMargin(), layoutBox.getAllocationX() - hborder );
			child.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		if ( child != null )
		{
			double vborder = border.getTopMargin() + border.getBottomMargin();
			double prevHeight = child.layoutBox.getAllocationY();
			layoutBox.allocateChildY( child.layoutBox, border.getTopMargin(), layoutBox.getAllocationY() - vborder );
			child.refreshAllocationY( prevHeight );
		}
	}
}
