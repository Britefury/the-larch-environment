//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementPainter;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Interactor.DragElementInteractor;
import BritefuryJ.DocPresent.Interactor.PushElementInteractor;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.Util.Range;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

class ScrollBarHelper
{
	public enum Axis
	{
		HORIZONTAL,
		VERTICAL
	}


	protected static class ScrollBarArrowInteractor implements PushElementInteractor
	{
		public enum Direction
		{
			INCREASE,
			DECREASE
		}


		private Direction direction;
		private Range range;
		
		
		public ScrollBarArrowInteractor(Direction direction, Range range)
		{
			this.direction = direction;
			this.range = range;
		}
		
		
		public boolean buttonPress(PointerInputElement element, PointerButtonEvent event)
		{
			if ( event.getButton() == 1 )
			{
				if ( direction == Direction.INCREASE )
				{
					range.move( range.getStepSize() );
				}
				else if ( direction == Direction.DECREASE )
				{
					range.move( -range.getStepSize() );
				}
				return true;
			}
			else
			{
				return false;
			}
		}

		@Override
		public void buttonRelease(PointerInputElement element, PointerButtonEvent event)
		{
		}
	}

	protected static class ScrollBarDragBarInteractor implements PushElementInteractor, DragElementInteractor, ElementPainter, Range.RangeListener
	{
		private DPElement element;
		private Axis axis;
		private Range range;
		private double padding, rounding, minSize;
		private Painter dragBoxPainter;
		private double dragStartValue = 0.0;
		private AABox2 dragBox = null;
		
		
		public ScrollBarDragBarInteractor(DPElement element, Axis axis, Range range, double padding, double rounding, double minSize, Painter dragBoxPainter)
		{
			this.element = element;
			this.axis = axis;
			this.range = range;
			this.padding = padding;
			this.rounding = rounding;
			this.minSize = minSize;
			this.dragBoxPainter = dragBoxPainter;
			range.addListener( this );
		}
		
		
	
		@Override
		public boolean buttonPress(PointerInputElement element, PointerButtonEvent event)
		{
			if ( event.getButton() == 1 )
			{
				AABox2 dragBox = getRefreshedDragBox();
				
				Point2 localPos = event.getPointer().getLocalPos();
				
				if ( axis == Axis.HORIZONTAL  &&  localPos.x < dragBox.getLowerX()    ||  axis == Axis.VERTICAL  &&  localPos.y < dragBox.getLowerY() )
				{
					range.move( -range.getPageSize() );
					return true;
				}
				else if ( axis == Axis.HORIZONTAL  &&  localPos.x > dragBox.getUpperX()    ||  axis == Axis.VERTICAL  &&  localPos.y > dragBox.getUpperY() )
				{
					range.move( range.getPageSize() );
					return true;
				}
			}

			return false;
		}

		@Override
		public void buttonRelease(PointerInputElement element, PointerButtonEvent event)
		{
		}

		
		
		@Override
		public boolean dragBegin(PointerInputElement element, PointerButtonEvent event)
		{
			if ( event.getButton() == 1 )
			{
				AABox2 dragBox = getRefreshedDragBox();
				
				Point2 localPos = event.getPointer().getLocalPos();
				
				if ( dragBox.containsPoint( localPos ) )
				{
					dragStartValue = range.getBegin();
					return true;
				}
			}

			return false;
		}
		
		@Override
		public void dragEnd(PointerInputElement element, PointerButtonEvent event, Point2 dragStartPos, int dragButton)
		{
		}
		
		@Override
		public void dragMotion(PointerInputElement element, PointerMotionEvent event, Point2 dragStartPos, int dragButton)
		{
			DPElement dragbarElement = (DPElement)element;
			AABox2 box = dragbarElement.getLocalAABox();
			Point2 localPos = event.getPointer().getLocalPos();
			Vector2 deltaPos = localPos.sub( dragStartPos );
			
			double visibleRange, delta;
			if ( axis == Axis.HORIZONTAL )
			{
				visibleRange = box.getWidth()  -  padding * 2.0;
				delta = deltaPos.x;
			}
			else if ( axis == Axis.VERTICAL )
			{
				visibleRange = box.getHeight()  -  padding * 2.0;
				delta = deltaPos.y;
			}
			else
			{
				throw new RuntimeException( "Invalid direction" );
			}

			double scaleFactor = ( range.getMax() - range.getMin() ) / visibleRange;
			range.moveBeginTo( dragStartValue  +  delta * scaleFactor );
		}

		
		
		@Override
		public void drawBackground(DPElement element, Graphics2D graphics)
		{
		}
	
		@Override
		public void draw(DPElement element, Graphics2D graphics)
		{
			AABox2 dragBox = getRefreshedDragBox();
			
			RoundRectangle2D.Double shape = new RoundRectangle2D.Double( dragBox.getLowerX(), dragBox.getLowerY(), dragBox.getWidth(), dragBox.getHeight(), rounding, rounding );
			
			dragBoxPainter.drawShape( graphics, shape );
		}
		
		
		private AABox2 getRefreshedDragBox()
		{
			if ( dragBox == null )
			{
				AABox2 dragBarBox = element.getLocalAABox();
				double bounds[] = { 0.0, 0.0 };
				if ( axis == Axis.HORIZONTAL )
				{
					double visibleRange = dragBarBox.getWidth()  -  padding * 2.0;
					computeDragBoxBounds( visibleRange, bounds );
					dragBox = new AABox2( padding + bounds[0], padding, padding + bounds[1], dragBarBox.getUpperY() - padding );
				}
				else if ( axis == Axis.VERTICAL )
				{
					double visibleRange = dragBarBox.getHeight()  -  padding * 2.0;
					computeDragBoxBounds( visibleRange, bounds );
					dragBox = new AABox2( padding, padding + bounds[0], dragBarBox.getUpperX() - padding, padding + bounds[1] );
				}
				else
				{
					throw new RuntimeException( "Invalid direction" );
				}
			}
			return dragBox;
		}
		
		
		private void computeDragBoxBounds(double visibleRange, double bounds[])
		{
			double naturalScaleFactor = visibleRange / ( range.getMax() - range.getMin() );
			double value = Math.min( Math.max( range.getBegin(), range.getMin() ), range.getMax() );
			double end = Math.min( Math.max( range.getEnd(), range.getMin() ), range.getMax() );
			double dragBoxSize = ( end - value ) * naturalScaleFactor;
			if ( dragBoxSize < minSize )
			{
				dragBoxSize = minSize;
				double rangeSize = ( range.getMax() - range.getMin() ) - ( end - value );
				if ( rangeSize > 0.0 )
				{
					double actualScaleFactor = ( visibleRange - dragBoxSize )  /  rangeSize;
					bounds[0] = ( value - range.getMin() ) * actualScaleFactor;
					bounds[1] = bounds[0] + dragBoxSize;
					return;
				}
			}

			// Fallback - use natural scale factor
			bounds[0] = ( value - range.getMin() ) * naturalScaleFactor;
			bounds[1] = ( end - range.getMin() ) * naturalScaleFactor;
		}
	
	
	
		@Override
		public void onRangeModified(Range r)
		{
			dragBox = null;
			element.queueFullRedraw();
		}
	}

}
