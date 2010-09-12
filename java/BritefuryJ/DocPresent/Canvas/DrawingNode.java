//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.DocPresent.Input.DndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public abstract class DrawingNode extends PointerInputElement
{
	protected static class DrawContext
	{
		protected Paint strokePaint, fillPaint;
		
		
		public DrawContext()
		{
		}
		
		
		public void setStrokePaint(Paint strokePaint)
		{
			this.strokePaint = strokePaint;
		}
		
		public void setFillPaint(Paint fillPaint)
		{
			this.fillPaint = fillPaint;
		}
		
		public Paint getStrokePaint()
		{
			return strokePaint;
		}
		
		public Paint getFillPaint()
		{
			return fillPaint;
		}
	}
	
	
	protected DrawingOwner owner;
	
	
	
	protected DrawingNode()
	{
	}
	
	protected DrawingNode(DrawingNode n)
	{
	}
	
	
	
	public void realise(DrawingOwner owner)
	{
		this.owner = owner;
	}
	
	public void unrealise()
	{
		owner = null;
	}
	
	
	
	protected void queueRedraw()
	{
		if ( owner != null )
		{
			owner.drawingQueueRedraw();
		}
	}
	
	
	// User API
	public DrawingNode stroke(Stroke stroke)
	{
		return new StyleNode( this, stroke );
	}
	
	public DrawingNode paint(Paint paint)
	{
		return StyleNode.paintNode( this, paint );
	}
	
	public DrawingNode fillPaint(Paint paint)
	{
		return StyleNode.fillPaintNode( this, paint );
	}
	
	public DrawingNode hoverMonitor(HoverMonitor monitor)
	{
		return new InteractionNode( this, monitor );
	}
	
	public DrawingNode hoverHighlight(DrawingNode highlight)
	{
		return new InteractionNode( this, highlight );
	}
	
	public DrawingNode onInteraction(InteractionListener listener)
	{
		return new InteractionNode( this, listener );
	}

	public DrawingNode enableDnd(DndHandler dndHandler)
	{
		return new InteractionNode( this, dndHandler );
	}

	public DrawingNode transform(AffineTransform t)
	{
		return new TransformationNode( this, t );
	}
	
	
	public DrawingNode translate(double x, double y)
	{
		return transform( AffineTransform.getTranslateInstance( x, y ) );
	}

	public DrawingNode translate(Vector2 x)
	{
		return transform( AffineTransform.getTranslateInstance( x.x, x.y ) );
	}


	public DrawingNode scale(double x, double y)
	{
		return transform( AffineTransform.getScaleInstance( x, y ) );
	}

	public DrawingNode scale(Vector2 s)
	{
		return transform( AffineTransform.getScaleInstance( s.x, s.y ) );
	}

	public DrawingNode scale(double s)
	{
		return transform( AffineTransform.getScaleInstance( s, s ) );
	}

	
	public DrawingNode rotate(double r)
	{
		return transform( AffineTransform.getRotateInstance( r ) );
	}

	public DrawingNode rotateDegrees(double r)
	{
		return transform( AffineTransform.getRotateInstance( Math.toRadians( r ) ) );
	}

	
	
	// Interface API
	public abstract void draw(Graphics2D graphics, DrawContext context);


	public void draw(Graphics2D graphics)
	{
		DrawContext context = new DrawContext();

		Stroke s = graphics.getStroke();
		graphics.setStroke( new BasicStroke( 1.0f ) );

		context.setStrokePaint( Color.black );
		draw( graphics, context );
		
		graphics.setStroke( s );
	}

	
	
	public abstract AABox2 getParentSpaceBoundingBox();







	protected boolean handlePointerButtonDown(PointerButtonEvent event)
	{
		return false;
	}
	
	protected boolean handlePointerButtonUp(PointerButtonEvent event)
	{
		return false;
	}
	
	protected boolean handlePointerButtonClicked(PointerButtonClickedEvent event)
	{
		return false;
	}
	
	protected void handlePointerMotion(PointerMotionEvent event)
	{
	}
	
	protected void handlePointerDrag(PointerMotionEvent event)
	{
	}
	
	protected void handlePointerEnter(PointerMotionEvent event)
	{
	}
	
	protected void handlePointerLeave(PointerMotionEvent event)
	{
	}
	
	protected void handlePointerEnterFromChild(PointerMotionEvent event, PointerInputElement childElement)
	{
	}
	
	protected void handlePointerLeaveIntoChild(PointerMotionEvent event, PointerInputElement childElement)
	{
	}
	
	protected boolean handlePointerScroll(PointerScrollEvent event)
	{
		return false;
	}
	
	
	protected PointerInputElement getFirstPointerChildAtLocalPoint(Point2 localPos)
	{
		return null;
	}
	
	protected PointerInputElement getLastPointerChildAtLocalPoint(Point2 localPos)
	{
		return null;
	}
	
	protected PointerEvent transformParentToLocalEvent(PointerEvent event)
	{
		return event;
	}
	
	protected PointerInterface transformParentToLocalPointer(PointerInterface pointer)
	{
		return pointer;
	}
	
	public Point2 transformParentToLocalPoint(Point2 parentPos)
	{
		return parentPos;
	}
	
	

	public Iterable<AbstractElementInteractor> getElementInteractors()
	{
		return null;
	}

	public Iterable<AbstractElementInteractor> getElementInteractors(Class<?> interactorClass)
	{
		return null;
	}
	
	

	public boolean isPointerInputElementRealised()
	{
		return true;
	}
	
	public abstract boolean containsParentSpacePoint(Point2 parentPos);
	public abstract boolean containsLocalSpacePoint(Point2 localPos);
	
	
	public PointerInputElement getDndElement(Point2 localPos, Point2 targetPos[])				// targetPos is an output parameter
	{
		return null;
	}
	
	public DndHandler getDndHandler()
	{
		return null;
	}
}
