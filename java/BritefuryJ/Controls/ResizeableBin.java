//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2013.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Interactor.DragElementInteractor;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSSpaceBin;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentElement;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.*;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class ResizeableBin extends ControlPres
{
	private static enum Axis
	{
		AXIS_X,
		AXIS_Y
	}

	private static enum DragBar
	{
		DRAGBAR_LOWER,
		DRAGBAR_UPPER,
		DRAGBAR_NONE
	}

	private static class ResizeInteractor implements DragElementInteractor
	{
		private LSSpaceBin spaceBin;
		private Axis axis;
		private boolean invert;
		private double start;


		public ResizeInteractor(LSSpaceBin spaceBin, Axis axis, boolean invert)
		{
			this.spaceBin = spaceBin;
			this.axis = axis;
			this.invert = invert;
		}


		public boolean dragBegin(LSElement element, PointerButtonEvent event)
		{
			if ( event.getButton() == 1 )
			{
				start = axis == Axis.AXIS_X  ?  spaceBin.getWidth()  :  spaceBin.getHeight();
				return true;
			}
			else
			{
				return false;
			}
		}

		public void dragEnd(LSElement element, PointerButtonEvent event, Point2 dragStartPos, int dragButton)
		{
			update( dragStartPos, event.getLocalPointerPos() );
		}

		public void dragMotion(LSElement element, PointerMotionEvent event, Point2 dragStartPos, int dragButton)
		{
			update( dragStartPos, event.getLocalPointerPos() );
		}


		private void update(Point2 dragStartPos, Point2 dragCurrentPos)
		{
			double delta = axis == Axis.AXIS_X  ?  dragCurrentPos.x - dragStartPos.x  :  dragCurrentPos.y - dragStartPos.y;
			delta = invert  ?  -delta  :  delta;
			double value = Math.max( start + delta, 0.0 );
			if ( axis == Axis.AXIS_X )
			{
				spaceBin.setWidth( value );
			}
			else
			{
				spaceBin.setHeight( value );
			}
		}
	}



	public static class ResizeableBinControl extends Control
	{
		private LSElement element;

		private ResizeableBinControl(PresentationContext ctx, StyleValues style, LSElement element)
		{
			super( ctx, style );
			this.element = element;
		}

		public LSElement getElement()
		{
			return element;
		}
	}



	private Pres child;
	private DragBar hDrag, vDrag;
	private double width, height;


	private ResizeableBin(DragBar hDrag, DragBar vDrag, double width, double height, Object child)
	{
		this.child = Pres.coerce( child );
		this.hDrag = hDrag;
		this.vDrag = vDrag;
		this.width = width;
		this.height = height;
	}


	public ResizeableBin(Object child)
	{
		this( DragBar.DRAGBAR_NONE, DragBar.DRAGBAR_NONE, 0.0, 0.0, child );
	}




	private Pres dragBar(LSSpaceBin spaceBin, Axis axis, boolean invert, Arrow.Direction arrow1, Arrow.Direction arrow2, double arrowSize, StyleSheet arrowStyle, AbstractBorder dragBarBorder, double dragBarPadding)
	{
		ResizeInteractor interactor = new ResizeInteractor( spaceBin, axis, invert );
		Arrow a1 = new Arrow( arrow1, arrowSize );
		Arrow a2 = new Arrow( arrow2, arrowSize );
		Pres arrows = axis == Axis.AXIS_X  ?  new Column( new Pres[] { a1, a2 } )  :  new Row( new Pres[] { a1, a2 } );
		arrows = arrowStyle.applyTo( arrows ).alignHCentre().alignVCentre();
		Pres dragBar = dragBarBorder.surround( arrows ).withElementInteractor( interactor );
		dragBar = axis == Axis.AXIS_X  ?  dragBar.padX( dragBarPadding )  :  dragBar.padY(dragBarPadding);
		return dragBar;
	}

	private Pres leftDragBar(LSSpaceBin spaceBin, double arrowSize, StyleSheet arrowStyle, AbstractBorder dragBarBorder, double dragBarPadding)
	{
		return dragBar( spaceBin, Axis.AXIS_X, true, Arrow.Direction.LEFT, Arrow.Direction.RIGHT, arrowSize, arrowStyle, dragBarBorder, dragBarPadding ).alignHPack().alignVExpand();
	}

	private Pres rightDragBar(LSSpaceBin spaceBin, double arrowSize, StyleSheet arrowStyle, AbstractBorder dragBarBorder, double dragBarPadding)
	{
		return dragBar( spaceBin, Axis.AXIS_X, false, Arrow.Direction.RIGHT, Arrow.Direction.LEFT, arrowSize, arrowStyle, dragBarBorder, dragBarPadding ).alignHPack().alignVExpand();
	}

	private Pres topDragBar(LSSpaceBin spaceBin, double arrowSize, StyleSheet arrowStyle, AbstractBorder dragBarBorder, double dragBarPadding)
	{
		return dragBar( spaceBin, Axis.AXIS_Y, true, Arrow.Direction.UP, Arrow.Direction.DOWN, arrowSize, arrowStyle, dragBarBorder, dragBarPadding ).alignVRefY().alignHExpand();
	}

	private Pres bottomDragBar(LSSpaceBin spaceBin, double arrowSize, StyleSheet arrowStyle, AbstractBorder dragBarBorder, double dragBarPadding)
	{
		return dragBar( spaceBin, Axis.AXIS_Y, false, Arrow.Direction.DOWN, Arrow.Direction.UP, arrowSize, arrowStyle, dragBarBorder, dragBarPadding ).alignVRefY().alignHExpand();
	}



	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		double arrowSize = style.get( Controls.resizeableArrowSize, Double.class );
		StyleSheet arrowStyle = Controls.resizeableBinArrowStyle.get( style );
		AbstractBorder dragBarBorder = style.get( Controls.resizeableDragBarBorder, AbstractBorder.class );
		double dragBarPadding = style.get( Controls.resizeableDragBarPadding, Double.class );

		LSSpaceBin.SizeConstraint constraintX = hDrag != DragBar.DRAGBAR_NONE  ?  LSSpaceBin.SizeConstraint.FIXED  :  LSSpaceBin.SizeConstraint.NONE;
		LSSpaceBin.SizeConstraint constraintY = vDrag != DragBar.DRAGBAR_NONE  ?  LSSpaceBin.SizeConstraint.FIXED  :  LSSpaceBin.SizeConstraint.NONE;
		Pres bin = new SpaceBin( width, height, constraintX, constraintY, child );
		LSSpaceBin binElement = (LSSpaceBin)bin.present( ctx, Controls.useResizeableBinAttrs( style ) );

		Pres hDragBar = null, vDragBar = null;
		if ( hDrag == DragBar.DRAGBAR_LOWER )
		{
			hDragBar = leftDragBar( binElement, arrowSize, arrowStyle, dragBarBorder, dragBarPadding );
		}
		else if ( hDrag == DragBar.DRAGBAR_UPPER )
		{
			hDragBar = rightDragBar(binElement, arrowSize, arrowStyle, dragBarBorder, dragBarPadding);
		}

		if ( vDrag == DragBar.DRAGBAR_LOWER )
		{
			vDragBar = topDragBar(binElement, arrowSize, arrowStyle, dragBarBorder, dragBarPadding);
		}
		else if ( vDrag == DragBar.DRAGBAR_UPPER )
		{
			vDragBar = bottomDragBar(binElement, arrowSize, arrowStyle, dragBarBorder, dragBarPadding);
		}

		Pres contents = new PresentElement( binElement );

		Pres p = null;
		if ( hDrag != DragBar.DRAGBAR_NONE  &&  vDrag != DragBar.DRAGBAR_NONE )
		{
			Table table = new Table();
			int x = hDrag == DragBar.DRAGBAR_LOWER ? 1 : 0;
			int y = vDrag == DragBar.DRAGBAR_LOWER ? 1 : 0;
			if ( hDrag == DragBar.DRAGBAR_LOWER )
			{
				table.put( 0, y, hDragBar );
			}
			if ( hDrag == DragBar.DRAGBAR_UPPER )
			{
				table.put( x + 1, y, hDragBar );
			}
			if ( vDrag == DragBar.DRAGBAR_LOWER )
			{
				table.put( x, 0, vDragBar );
			}
			if ( vDrag == DragBar.DRAGBAR_UPPER )
			{
				table.put( x, y + 1, vDragBar );
			}
			table.put( x, y, contents );
			p = table;
		}
		else if ( hDrag != DragBar.DRAGBAR_NONE )
		{
			Pres items[] = null;
			if ( hDrag == DragBar.DRAGBAR_LOWER )
			{
				items = new Pres[] { hDragBar, contents };
			}
			else
			{
				items = new Pres[] { contents, hDragBar };
			}
			p = new Row( items );
		}
		else if ( vDrag != DragBar.DRAGBAR_NONE )
		{
			Pres items[] = null;
			if ( vDrag == DragBar.DRAGBAR_LOWER )
			{
				items = new Pres[] { vDragBar, contents };
			}
			else
			{
				items = new Pres[] { contents, vDragBar };
			}
			p = new Column( items );
		}

		LSElement element = p.present( ctx, style );
		return new ResizeableBinControl( ctx, style, element );
	}



	public ResizeableBin resizeLeft(double width)
	{
		return new ResizeableBin( DragBar.DRAGBAR_LOWER, vDrag, width, height, child );
	}

	public ResizeableBin resizeRight(double width)
	{
		return new ResizeableBin( DragBar.DRAGBAR_UPPER, vDrag, width, height, child );
	}

	public ResizeableBin resizeTop(double height)
	{
		return new ResizeableBin( hDrag, DragBar.DRAGBAR_LOWER, width, height, child );
	}

	public ResizeableBin resizeBottom(double height)
	{
		return new ResizeableBin( hDrag, DragBar.DRAGBAR_UPPER, width, height, child );
	}
}
