//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeFraction;
import BritefuryJ.LSpace.StyleParams.CaretSlotStyleParams;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.LSpace.StyleParams.FractionStyleParams;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.Math.Point2;

public class LSFraction extends LSContainerNonOverlayed
{
	public static class DPFractionBar extends LSContentLeafEditable
	{
		public DPFractionBar(String textRepresentation)
		{
			this( FractionStyleParams.BarStyleParams.defaultStyleParams, textRepresentation );
		}

		public DPFractionBar(FractionStyleParams.BarStyleParams styleParams, String textRepresentation)
		{
			super(styleParams, textRepresentation );
			
			layoutNode = new LayoutNodeFraction.LayoutNodeFractionBar( this );
		}
		
		
		
		public boolean isRedrawRequiredOnHover()
		{
			return super.isRedrawRequiredOnHover()  ||  getHoverBarPaint() != null;
		}
		

		protected void draw(Graphics2D graphics)
		{
			Shape s = new Rectangle2D.Double( 0.0, 0.0, getActualWidth(), getActualHeight() );
			Paint curPaint = graphics.getPaint();

			Paint barPaint;
			if ( isHoverActive() )
			{
				Paint hoverBarPaint = getHoverBarPaint();
				barPaint = hoverBarPaint != null  ?  hoverBarPaint  :  getBarPaint();
			}
			else
			{
				barPaint = getBarPaint();
			}

			graphics.setPaint( barPaint );
			graphics.fill( s );
			graphics.setPaint( curPaint );
		}
		
		
		//
		//
		// CARET METHODS
		//
		//
		
		private void drawCaretAtStart(Graphics2D graphics)
		{
			double height = getActualHeight();
			AffineTransform current = pushLocalToRootGraphicsTransform( graphics );
			graphics.draw( new Line2D.Double( 0.0, -2.0, 0.0, height + 2.0 ) );
			popGraphicsTransform( graphics, current );
		}

		private void drawCaretAtEnd(Graphics2D graphics)
		{
			double width = getActualWidth();
			double height = getActualHeight();
			AffineTransform current = pushLocalToRootGraphicsTransform( graphics );
			graphics.draw( new Line2D.Double( width, -2.0, width, height + 2.0 ) );
			popGraphicsTransform( graphics, current );
		}

		public void drawCaret(Graphics2D graphics, Caret c)
		{
			int index = c.getIndex();
			
			if ( index == 0 )
			{
				drawCaretAtStart( graphics );
			}
			else
			{
				drawCaretAtEnd( graphics );
			}
		}
		
		
		//
		//
		// TEXT SELECTION METHODS
		//
		//
		
		public void drawTextSelection(Graphics2D graphics, int startIndex, int endIndex)
		{
			double width = getActualWidth();
			double height = getActualHeight();
			AffineTransform current = pushLocalToRootGraphicsTransform( graphics );
			double startX = startIndex == 0  ?  0.0  :  width;
			double endX = endIndex == 0  ?  0.0  :  width;
			Rectangle2D.Double shape = new Rectangle2D.Double( startX, -2.0, endX - startX, height + 4.0 );
			graphics.fill( shape );
			popGraphicsTransform( graphics, current );
		}


		
		//
		// Marker methods
		//

		public int getMarkerRange()
		{
			return textRepresentation.length();
		}

		public int getMarkerPositonForPoint(Point2 localPos)
		{
			double width = getActualWidth();
			if ( localPos.x >= width * 0.5 )
			{
				return 1;
			}
			else
			{
				return 0;
			}
		}

	
		//
		// STYLESHEET METHODS
		//
		
		protected Paint getBarPaint()
		{
			return ((FractionStyleParams.BarStyleParams) styleParams).getBarPaint();
		}
		
		protected Paint getHoverBarPaint()
		{
			return ((FractionStyleParams.BarStyleParams) styleParams).getHoverBarPaint();
		}
	}
	
	
	
	
	public static int NUMERATOR = LayoutNodeFraction.NUMERATOR;
	public static int BAR = LayoutNodeFraction.BAR;
	public static int DENOMINATOR = LayoutNodeFraction.DENOMINATOR;

	public static int NUMCHILDREN = LayoutNodeFraction.NUMCHILDREN;
	
	protected LSElement children[];
	protected LSSegment segs[];
	protected LSParagraph paras[];
	CaretSlotStyleParams segmentCaretSlotStyleParams;

	
	
	
	public LSFraction(FractionStyleParams styleParams, CaretSlotStyleParams segmentCaretSlotStyleParams, LSElement numerator, LSElement bar, LSElement denominator)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeFraction( this );
		
		this.segmentCaretSlotStyleParams = segmentCaretSlotStyleParams;
		
		children = new LSElement[NUMCHILDREN];
		segs = new LSSegment[NUMCHILDREN];
		paras = new LSParagraph[NUMCHILDREN];
		
		if ( numerator != null )
		{
			LSSegment seg = new LSSegment( (ContainerStyleParams)getStyleParams(), segmentCaretSlotStyleParams, true, true, numerator );
			segs[NUMERATOR] = seg;
			LSParagraph para = new LSParagraph( new LSElement[] { seg } );
			paras[NUMERATOR] = para;
			
			registeredChildren.add( para );
			registerChild( para );

			children[NUMERATOR] = numerator;
		}
		
		if ( bar != null )
		{
			registeredChildren.add( bar );
			registerChild( bar );

			children[BAR] = bar;
		}

		if ( denominator != null )
		{
			LSSegment seg = new LSSegment( (ContainerStyleParams)getStyleParams(), segmentCaretSlotStyleParams, true, true, denominator );
			segs[DENOMINATOR] = seg;
			LSParagraph para = new LSParagraph( new LSElement[] { seg } );
			paras[DENOMINATOR] = para;
			
			registeredChildren.add( para );
			registerChild( para );

			children[DENOMINATOR] = denominator;
		}
	}
	
	
	
	//
	//
	// Child element access / modification
	//
	//
	
	public LSElement getChild(int slot)
	{
		return children[slot];
	}
	
	public LSElement getWrappedChild(int slot)
	{
		return slot == BAR  ?  children[slot]  :  paras[slot];
	}
	
	public void setChild(int slot, LSElement child)
	{
		LSElement existingChild = children[slot];
		if ( child != existingChild )
		{
			if ( slot == BAR )
			{
				if ( existingChild != null )
				{
					unregisterChild( existingChild );
					registeredChildren.remove( existingChild );
				}
				
				children[slot] = child;
				
				if ( child != null )
				{
					int insertIndex = children[0] != null  ?  1  :  0;
					registeredChildren.add( insertIndex, child );
					registerChild( child );
				}
			}
			else
			{
				boolean bSegmentRequired = child != null  &&  slot != BAR;
				boolean bSegmentPresent = existingChild != null  &&  slot != BAR;

				if ( bSegmentRequired  &&  !bSegmentPresent )
				{
					LSSegment seg = new LSSegment( (ContainerStyleParams)getStyleParams(), segmentCaretSlotStyleParams, true, true, child );
					segs[slot] = seg;
					LSParagraph para = new LSParagraph( new LSElement[] { seg } );
					paras[slot] = para;
					
					int insertIndex = 0;
					for (int i = 0; i < slot; i++)
					{
						if ( children[i] != null )
						{
							insertIndex++;
						}
					}
					
					registeredChildren.add( insertIndex, para );
					registerChild( para );
				}
	
				
				children[slot] = child;
				
				
				if ( bSegmentPresent  &&  !bSegmentRequired )
				{
					LSParagraph para = paras[slot];
					unregisterChild( para );
					registeredChildren.remove( para );
					segs[slot] = null;
					paras[slot] = null;
				}
			}
			
			onChildListModified();
			queueResize();
		}
	}
	
	
	
	
	public LSElement getNumeratorChild()
	{
		return getChild( NUMERATOR );
	}
	
	public LSElement getDenominatorChild()
	{
		return getChild( DENOMINATOR );
	}
	
	public LSElement getBarChild()
	{
		return getChild( BAR );
	}
	
	
	public LSElement getWrappedNumeratorChild()
	{
		return paras[NUMERATOR];
	}
	
	public LSElement getWrappedDenominatorChild()
	{
		return paras[DENOMINATOR];
	}
	

	
	public void setNumeratorChild(LSElement child)
	{
		setChild( NUMERATOR, child );
	}
	
	public void setDenominatorChild(LSElement child)
	{
		setChild( DENOMINATOR, child );
	}
	
	public void setBarChild(LSElement child)
	{
		setChild( BAR, child );
	}
	

	
	
	protected void replaceChildWithEmpty(LSElement child)
	{
		int slot = Arrays.asList( children ).indexOf( child );
		setChild( slot, null );
	}
	
	protected void replaceChild(LSElement child, LSElement replacement)
	{
		int slot = Arrays.asList( children ).indexOf( child );
		setChild( slot, replacement );
	}
	
	
	
	public List<LSElement> getChildren()
	{
		return registeredChildren;
	}

	
	public boolean isSingleElementContainer()
	{
		return false;
	}

	
	
	
	
	//
	// STYLESHEET METHODS
	//
	
	protected double getVSpacing()
	{
		return ((FractionStyleParams) styleParams).getVSpacing();
	}

	protected double getHPadding()
	{
		return ((FractionStyleParams) styleParams).getHPadding();
	}

	protected double getYOffset()
	{
		return ((FractionStyleParams) styleParams).getYOffset();
	}
}
