//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.util.ArrayList;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheetField;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldDirect;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldSet;

public class DPButton extends DPBin
{
	protected static ElementStyleSheetField borderPaintField = ElementStyleSheetField.newField( "buttonBorderPaint", Paint.class );
	protected static ElementStyleSheetField backgPaintField = ElementStyleSheetField.newField( "buttonBackgroundPaint", Paint.class );
	protected static ElementStyleSheetField backgHighlightPaintField = ElementStyleSheetField.newField( "buttonBackgroundHighlightPaint", Paint.class );

	protected static StyleSheetValueFieldDirect borderPaintValueField = StyleSheetValueFieldDirect.newField( "buttonBorderPaint", Paint.class,
			new RadialGradientPaint( -10.0f, -10.0f, 100.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 0.2f, 0.3f, 0.5f ), new Color( 0.3f, 0.45f, 0.75f ) }, RadialGradientPaint.CycleMethod.NO_CYCLE ),
			borderPaintField );
	protected static StyleSheetValueFieldDirect backgPaintValueField = StyleSheetValueFieldDirect.newField( "buttonBackgroundPaint", Paint.class,
			new RadialGradientPaint( -10.0f, -10.0f, 100.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 0.9f, 0.92f, 1.0f ), new Color( 0.75f, 0.825f, 0.9f ) }, RadialGradientPaint.CycleMethod.NO_CYCLE ),
			backgPaintField );
	protected static StyleSheetValueFieldDirect backgHighlightPaintValueField = StyleSheetValueFieldDirect.newField( "buttonBackgroundHighlightPaint", Paint.class,
			new RadialGradientPaint( -10.0f, -10.0f, 100.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 1.0f, 1.0f, 1.0f ), new Color( 0.85f, 0.85f, 0.85f ) }, RadialGradientPaint.CycleMethod.NO_CYCLE ),
			backgHighlightPaintField );
	
	
	protected static StyleSheetValueFieldSet useStyleSheetFields_Button = useStyleSheetFields_Element.join( borderPaintValueField, backgPaintValueField, backgHighlightPaintValueField );

	
	
	public interface ButtonListener
	{
		public void onButtonClicked(DPButton button);
	}
	
	protected static class PyButtonListener implements ButtonListener
	{
		private PyObject callable;
		
		
		public PyButtonListener(PyObject callable)
		{
			this.callable = callable;
		}
		
		public void onButtonClicked(DPButton button)
		{
			callable.__call__( Py.java2py( button ) );
		}
	}
	
	
	
	protected ButtonListener listener;
	protected SolidBorder border, highlightBorder;
	
	
	
	public DPButton()
	{
		this( null, (ButtonListener)null );
	}

	public DPButton(ButtonListener listener)
	{
		this( null, listener );
	}

	public DPButton(PyObject listener)
	{
		this( null, new PyButtonListener( listener ) );
	}

	public DPButton(ElementStyleSheet styleSheet)
	{
		this( styleSheet, (ButtonListener)null );
	}

	public DPButton(ElementStyleSheet styleSheet, ButtonListener listener)
	{
		super( styleSheet );
		
		this.listener = listener;
		border = new SolidBorder( 1.0, 2.0, 10.0, 10.0, getBorderPaint(), getBackgroundPaint() );
		highlightBorder = new SolidBorder( 1.0, 2.0, 10.0, 10.0, getBorderPaint(), getBackgroundHighlightPaint() );
	}
	
	public DPButton(ElementStyleSheet styleSheet, PyObject listener)
	{
		this( styleSheet, new PyButtonListener( listener ) );
	}
	
	
	
	public void setButtonListener(ButtonListener listener)
	{
		this.listener = listener;
	}
	
	
	
	protected void drawBackground(Graphics2D graphics)
	{
		super.drawBackground( graphics );
		
		ArrayList<PointerInterface> pointersWithinBounds = getPointersWithinBounds();
		if ( pointersWithinBounds != null  &&  pointersWithinBounds.size() > 0 )
		{
			highlightBorder.draw( graphics, 0.0, 0.0, getAllocationX(), getAllocationY() );
		}
		else
		{
			border.draw( graphics, 0.0, 0.0, getAllocationX(), getAllocationY() );
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
		return event.button == 1;
	}

	protected boolean onButtonUp(PointerButtonEvent event)
	{
		super.onButtonUp( event );
		
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
		DPWidget child = getChild();
		if ( child != null )
		{
			double vborder = border.getTopMargin() + border.getBottomMargin();
			double prevHeight = child.layoutAllocBox.getAllocationY();
			layoutAllocBox.allocateChildY( child.layoutAllocBox, border.getTopMargin(), layoutAllocBox.getAllocationY() - vborder );
			child.refreshAllocationY( prevHeight );
		}
	}
	
	
	protected StyleSheetValueFieldSet getUsedStyleSheetValueFields()
	{
		return useStyleSheetFields_Button;
	}

	
	private Paint getBorderPaint()
	{
		return (Paint)styleSheetValues.get( borderPaintValueField );
	}
	
	private Paint getBackgroundPaint()
	{
		return (Paint)styleSheetValues.get( backgPaintValueField );
	}
	
	private Paint getBackgroundHighlightPaint()
	{
		return (Paint)styleSheetValues.get( backgHighlightPaintValueField );
	}
}
