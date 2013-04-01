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




	private Pres leftDragBar(LSSpaceBin spaceBin, double arrowSize, double arrowPadding, StyleSheet arrowStyle, StyleSheet dragBarStyle, double dragBarPadding, double dragBarEdgeThickness)
	{
		ResizeInteractor interactor = new ResizeInteractor( spaceBin, Axis.AXIS_X, true );
		Arrow a1 = new Arrow( Arrow.Direction.LEFT, arrowSize );
		Arrow a2 = new Arrow( Arrow.Direction.RIGHT, arrowSize );
		Pres arrows = new Column( new Pres[] { a1, a2 } );
		arrows = arrowStyle.applyTo( arrows ).pad( arrowPadding, arrowPadding ).alignHCentre().alignVCentre();
		Pres dragBar = new Row( new Pres[] { new Box( dragBarEdgeThickness, dragBarEdgeThickness ), new Bin( arrows ) } ).alignVExpand();
		dragBar = dragBarStyle.applyTo( dragBar ).withElementInteractor( interactor ).padX( 0.0, dragBarPadding );
		return dragBar;
	}

	private Pres rightDragBar(LSSpaceBin spaceBin, double arrowSize, double arrowPadding, StyleSheet arrowStyle, StyleSheet dragBarStyle, double dragBarPadding, double dragBarEdgeThickness)
	{
		ResizeInteractor interactor = new ResizeInteractor( spaceBin, Axis.AXIS_X, false );
		Arrow a1 = new Arrow( Arrow.Direction.RIGHT, arrowSize );
		Arrow a2 = new Arrow( Arrow.Direction.LEFT, arrowSize );
		Pres arrows = new Column( new Pres[] { a1, a2 } );
		arrows = arrowStyle.applyTo( arrows ).pad( arrowPadding, arrowPadding ).alignHCentre().alignVCentre();
		Pres dragBar = new Row( new Pres[] { new Bin( arrows ), new Box( dragBarEdgeThickness, dragBarEdgeThickness ) } ).alignVExpand();
		dragBar = dragBarStyle.applyTo( dragBar ).withElementInteractor( interactor ).padX( dragBarPadding, 0.0 );
		return dragBar;
	}

	private Pres topDragBar(LSSpaceBin spaceBin, double arrowSize, double arrowPadding, StyleSheet arrowStyle, StyleSheet dragBarStyle, double dragBarPadding, double dragBarEdgeThickness)
	{
		ResizeInteractor interactor = new ResizeInteractor( spaceBin, Axis.AXIS_Y, true );
		Arrow a1 = new Arrow( Arrow.Direction.UP, arrowSize );
		Arrow a2 = new Arrow( Arrow.Direction.DOWN, arrowSize );
		Pres arrows = new Row( new Pres[] { a1, a2 } );
		arrows = arrowStyle.applyTo( arrows ).pad( arrowPadding, arrowPadding ).alignHCentre().alignVCentre();
		Pres dragBar = new Column( new Pres[] { new Box( dragBarEdgeThickness, dragBarEdgeThickness ), new Bin( arrows ) } ).alignHExpand();
		dragBar = dragBarStyle.applyTo( dragBar ).withElementInteractor( interactor ).padY( 0.0, dragBarPadding );
		return dragBar;
	}

	private Pres bottomDragBar(LSSpaceBin spaceBin, double arrowSize, double arrowPadding, StyleSheet arrowStyle, StyleSheet dragBarStyle, double dragBarPadding, double dragBarEdgeThickness)
	{
		ResizeInteractor interactor = new ResizeInteractor( spaceBin, Axis.AXIS_Y, false );
		Arrow a1 = new Arrow( Arrow.Direction.DOWN, arrowSize );
		Arrow a2 = new Arrow( Arrow.Direction.UP, arrowSize );
		Pres arrows = new Row( new Pres[] { a1, a2 } );
		arrows = arrowStyle.applyTo( arrows ).pad( arrowPadding, arrowPadding ).alignHCentre().alignVCentre();
		Pres dragBar = new Column( new Pres[] { new Bin( arrows ), new Box( dragBarEdgeThickness, dragBarEdgeThickness ) } ).alignHExpand();
		dragBar = dragBarStyle.applyTo( dragBar ).withElementInteractor( interactor ).padY( dragBarPadding, 0.0 );
		return dragBar;
	}



	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		double arrowSize = style.get( Controls.resizeableArrowSize, Double.class );
		StyleSheet arrowStyle = Controls.resizeableBinArrowStyle.get( style );
		double arrowPadding = style.get( Controls.resizeableArrowPadding, Double.class );
		double dragBarEdgeThickness = style.get( Controls.resizeableDragBarEdgeThickness, Double.class );
		double dragBarPadding = style.get( Controls.resizeableDragBarPadding, Double.class );
		StyleSheet dragBarStyle = style.get( Controls.resizeableDragBarBodyStyle, StyleSheet.class );

		LSSpaceBin.SizeConstraint constraintX = hDrag == DragBar.DRAGBAR_NONE  ?  LSSpaceBin.SizeConstraint.NONE  :  LSSpaceBin.SizeConstraint.FIXED;
		LSSpaceBin.SizeConstraint constraintY = vDrag == DragBar.DRAGBAR_NONE  ?  LSSpaceBin.SizeConstraint.NONE  :  LSSpaceBin.SizeConstraint.FIXED;
		Pres bin = new SpaceBin( width, height, constraintX, constraintY, child );
		LSSpaceBin binElement = (LSSpaceBin)bin.present( ctx, Controls.useResizeableBinAttrs( style ) );

		Pres hDragBar = null, vDragBar = null;
		if ( hDrag == DragBar.DRAGBAR_LOWER )
		{
			hDragBar = leftDragBar( binElement, arrowSize, arrowPadding, arrowStyle, dragBarStyle, dragBarPadding, dragBarEdgeThickness );
		}
		else if ( hDrag == DragBar.DRAGBAR_UPPER )
		{
			hDragBar = rightDragBar( binElement, arrowSize, arrowPadding, arrowStyle, dragBarStyle, dragBarPadding, dragBarEdgeThickness );
		}

		if ( vDrag == DragBar.DRAGBAR_LOWER )
		{
			vDragBar = topDragBar( binElement, arrowSize, arrowPadding, arrowStyle, dragBarStyle, dragBarPadding, dragBarEdgeThickness );
		}
		else if ( vDrag == DragBar.DRAGBAR_UPPER )
		{
			vDragBar = bottomDragBar( binElement, arrowSize, arrowPadding, arrowStyle, dragBarStyle, dragBarPadding, dragBarEdgeThickness );
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
