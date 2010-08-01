//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import BritefuryJ.Cell.Cell;
import BritefuryJ.Cell.CellEvaluator;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.Util.Range;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

class ScrollBarHelper
{
	public enum Axis
	{
		HORIZONTAL,
		VERTICAL
	};
	
	
	protected static class ScrollBarArrowInteractor extends ElementInteractor
	{
		public enum Direction
		{
			INCREASE,
			DECREASE
		};
		
		
		private Direction direction;
		private Range range;
		
		
		public ScrollBarArrowInteractor(Direction direction, Range range)
		{
			this.direction = direction;
			this.range = range;
		}
		
		
		public boolean onButtonDown(DPElement element, PointerButtonEvent event)
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
		
		public boolean onButtonUp(DPElement element, PointerButtonEvent event)
		{
			return event.getButton() == 1;
		}
	}

	protected static class ScrollBarDragBarInteractor extends ElementInteractor implements IncrementalMonitorListener
	{
		private DPElement element;
		private Axis axis;
		private Range range;
		private double padding, rounding;
		private Painter dragBoxPainter;
		private PointerInterface dragPointer = null;
		private Point2 dragStartPos = null;
		private double dragStartValue = 0.0;
		private Cell dragBoxCell = new Cell();
		
		
		public ScrollBarDragBarInteractor(DPElement element, Axis axis, Range range, double padding, double rounding, Painter dragBoxPainter)
		{
			this.element = element;
			this.axis = axis;
			this.range = range;
			this.padding = padding;
			this.rounding = rounding;
			this.dragBoxPainter = dragBoxPainter;
			
			CellEvaluator dragBoxCellEval = new CellEvaluator()
			{
				@Override
				public Object evaluate()
				{
					return computeDragBox( ScrollBarDragBarInteractor.this.element );
				}
			};
			dragBoxCell.setEvaluator( dragBoxCellEval );
			dragBoxCell.addListener( this );
		}
		
		
	
		public boolean onButtonDown(DPElement element, PointerButtonEvent event)
		{
			if ( event.getButton() == 1 )
			{
				AABox2 dragBox = (AABox2)dragBoxCell.getValue();
				
				Point2 localPos = event.getPointer().getLocalPos();
				
				if ( axis == Axis.HORIZONTAL  &&  localPos.x < dragBox.getLowerX()    ||  axis == Axis.VERTICAL  &&  localPos.y < dragBox.getLowerY() )
				{
					range.move( -range.getPageSize() );
				}
				else if ( axis == Axis.HORIZONTAL  &&  localPos.x > dragBox.getUpperX()    ||  axis == Axis.VERTICAL  &&  localPos.y > dragBox.getUpperY() )
				{
					range.move( range.getPageSize() );
				}
				else
				{
					dragPointer = event.getPointer().concretePointer();
					dragStartPos = event.getPointer().getLocalPos();
					dragStartValue = range.getBegin();
				}
				
				return true;
			}
			else
			{
				return false;
			}
		}
		
		public boolean onButtonUp(DPElement element, PointerButtonEvent event)
		{
			dragPointer = null;
			return event.getButton() == 1;
		}
		
		public void onDrag(DPElement element, PointerMotionEvent event)
		{
			if ( event.getPointer().concretePointer() == dragPointer )
			{
				AABox2 box = element.getLocalAABox();
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
		}
		
		
		public void drawBackground(DPElement element, Graphics2D graphics)
		{
		}
	
		public void draw(DPElement element, Graphics2D graphics)
		{
			AABox2 dragBox = (AABox2)dragBoxCell.getValue();
			
			RoundRectangle2D.Double shape = new RoundRectangle2D.Double( dragBox.getLowerX(), dragBox.getLowerY(), dragBox.getWidth(), dragBox.getHeight(), rounding, rounding );
			
			dragBoxPainter.drawShape( graphics, shape );
		}
		
		
		private AABox2 computeDragBox(DPElement element)
		{
			AABox2 box = element.getLocalAABox();
			double value = Math.min( Math.max( range.getBegin(), range.getMin() ), range.getMax() );
			double end = Math.min( Math.max( range.getEnd(), range.getMin() ), range.getMax() );
			if ( axis == Axis.HORIZONTAL )
			{
				double visibleRange = box.getWidth()  -  padding * 2.0;
				double scaleFactor = visibleRange / ( range.getMax() - range.getMin() );
				return new AABox2( padding + value * scaleFactor, padding, padding + end * scaleFactor, box.getUpperY() - padding );
			}
			else if ( axis == Axis.VERTICAL )
			{
				double visibleRange = box.getHeight()  -  padding * 2.0;
				double scaleFactor = visibleRange / ( range.getMax() - range.getMin() );
				return new AABox2( padding, padding + value * scaleFactor, box.getUpperX() - padding, padding + end * scaleFactor );
			}
			else
			{
				throw new RuntimeException( "Invalid direction" );
			}
		}
	
	
	
		@Override
		public void onIncrementalMonitorChanged(IncrementalMonitor inc)
		{
			element.queueFullRedraw();
		}
	}

}