//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.StyleSheets.ButtonStyleSheet;

public class DPButton extends DPBin
{
	public interface ButtonListener
	{
		public void onButtonClicked(DPButton button);
	}
	
	
	
	protected ButtonListener listener;
	
	
	
	public DPButton()
	{
		this( ButtonStyleSheet.defaultStyleSheet, null );
	}

	public DPButton(ButtonListener listener)
	{
		this( ButtonStyleSheet.defaultStyleSheet, listener );
	}

	public DPButton(ButtonStyleSheet styleSheet)
	{
		this( styleSheet, null );
	}

	public DPButton(ButtonStyleSheet styleSheet, ButtonListener listener)
	{
		super( styleSheet );
		
		this.listener = listener;
	}
	
	
	
	public void setButtonListener(ButtonListener listener)
	{
		this.listener = listener;
	}
	
	
	
	protected void drawBackground(Graphics2D graphics)
	{
		ButtonStyleSheet buttonStyle = (ButtonStyleSheet)styleSheet;
		
		if ( pointersWithinBounds.size() > 0 )
		{
			buttonStyle.getHighlightBorder().draw( graphics, 0.0, 0.0, getAllocationX(), getAllocationY() );
		}
		else
		{
			buttonStyle.getBorder().draw( graphics, 0.0, 0.0, getAllocationX(), getAllocationY() );
		}
	}
	
	
	protected void onEnter(PointerMotionEvent event)
	{
		super.onEnter( event );
		queueFullRedraw();
	}

	protected void onLeave(PointerMotionEvent event)
	{
		queueFullRedraw();
		super.onLeave( event );
	}
	

	protected boolean onButtonDown(PointerButtonEvent event)
	{
		super.onButtonDown( event );
		
		if ( isRealised() )
		{
			if ( event.button == 1 )
			{
				if ( listener != null )
				{
					listener.onButtonClicked( this );
				}
				return true;
			}
		}
		return false;
	}

	
	
	protected void updateRequisitionX()
	{
		ButtonStyleSheet buttonStyle = (ButtonStyleSheet)styleSheet;
		SolidBorder border = buttonStyle.getBorder();
		
		DPWidget child = getChild();
		if ( child != null )
		{
			layoutReqBox.setRequisitionX( child.refreshRequisitionX() );
		}
		else
		{
			layoutReqBox.clearRequisitionX();
		}
		layoutReqBox.borderX( border.getLeftMargin(), border.getRightMargin() );
	}

	protected void updateRequisitionY()
	{
		ButtonStyleSheet buttonStyle = (ButtonStyleSheet)styleSheet;
		SolidBorder border = buttonStyle.getBorder();
		
		DPWidget child = getChild();
		if ( child != null )
		{
			layoutReqBox.setRequisitionY( child.refreshRequisitionY() );
		}
		else
		{
			layoutReqBox.clearRequisitionY();
		}
		layoutReqBox.borderY( border.getTopMargin(), border.getBottomMargin() );
	}

	
	
	
	protected void updateAllocationX()
	{
		ButtonStyleSheet buttonStyle = (ButtonStyleSheet)styleSheet;
		SolidBorder border = buttonStyle.getBorder();
		
		DPWidget child = getChild();
		if ( child != null )
		{
			double hborder = border.getLeftMargin() + border.getRightMargin();
			double prevWidth = child.layoutAllocBox.getAllocationX();
			layoutAllocBox.allocateChildX( child.layoutAllocBox, border.getLeftMargin(), layoutAllocBox.getAllocationX() - hborder );
			child.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		ButtonStyleSheet buttonStyle = (ButtonStyleSheet)styleSheet;
		SolidBorder border = buttonStyle.getBorder();
		
		DPWidget child = getChild();
		if ( child != null )
		{
			double vborder = border.getTopMargin() + border.getBottomMargin();
			double prevHeight = child.layoutAllocBox.getAllocationY();
			layoutAllocBox.allocateChildY( child.layoutAllocBox, border.getTopMargin(), layoutAllocBox.getAllocationY() - vborder );
			child.refreshAllocationY( prevHeight );
		}
	}
}
