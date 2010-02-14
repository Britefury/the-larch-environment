//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeFraction;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.FractionStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.Math.Point2;

public class DPFraction extends DPContainer
{
	private static double childScale = 0.85;
	
	
	
	
	public static class DPFractionBar extends DPContentLeafEditableEntry
	{
		public DPFractionBar(String textRepresentation)
		{
			this( FractionStyleSheet.BarStyleSheet.defaultStyleSheet, textRepresentation );
		}

		public DPFractionBar(FractionStyleSheet.BarStyleSheet styleSheet, String textRepresentation)
		{
			super( styleSheet, textRepresentation );
			
			layoutNode = new LayoutNodeFraction.LayoutNodeFractionBar( this );
		}

	
		protected void draw(Graphics2D graphics)
		{
			Shape s = new Rectangle2D.Double( 0.0, 0.0, getAllocationX(), getAllocationY() );
			Paint curPaint = graphics.getPaint();
			graphics.setPaint( getBarPaint() );
			graphics.fill( s );
			graphics.setPaint( curPaint );
		}
		
		
		//
		//
		// CARET METHODS
		//
		//
		
		public void drawCaret(Graphics2D graphics, Caret c)
		{
			int index = c.getMarker().getIndex();
			
			if ( index == 0 )
			{
				drawCaretAtStart( graphics );
			}
			else
			{
				drawCaretAtEnd( graphics );
			}
		}

		public void drawCaretAtStart(Graphics2D graphics)
		{
			double allocationY = getAllocationY();
			AffineTransform current = pushGraphicsTransform( graphics );
			graphics.draw( new Line2D.Double( 0.0, -2.0, 0.0, allocationY + 2.0 ) );
			popGraphicsTransform( graphics, current );
		}

		public void drawCaretAtEnd(Graphics2D graphics)
		{
			double allocationX = getAllocationX();
			double allocationY = getAllocationY();
			AffineTransform current = pushGraphicsTransform( graphics );
			graphics.draw( new Line2D.Double( allocationX, -2.0, allocationX, allocationY + 2.0 ) );
			popGraphicsTransform( graphics, current );
		}

		
		
		//
		//
		// SELECTION METHODS
		//
		//
		
		public void drawSelection(Graphics2D graphics, Marker from, Marker to)
		{
			double allocationX = getAllocationX();
			double allocationY = getAllocationY();
			AffineTransform current = pushGraphicsTransform( graphics );
			int startIndex = from != null  ?  from.getIndex()  :  0;
			int endIndex = to != null  ?  to.getIndex()  :  1;
			double startX = startIndex == 0  ?  0.0  :  allocationX;
			double endX = endIndex == 0  ?  0.0  :  allocationX;
			Rectangle2D.Double shape = new Rectangle2D.Double( startX, -2.0, endX - startX, allocationY + 4.0 );
			graphics.fill( shape );
			popGraphicsTransform( graphics, current );
		}


		
		//
		// Marker methods
		//

		public int getMarkerRange()
		{
			return 1;
		}

		public int getMarkerPositonForPoint(Point2 localPos)
		{
			double allocationX = getAllocationX();
			if ( localPos.x >= allocationX * 0.5 )
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
			return ((FractionStyleSheet.BarStyleSheet)styleSheet).getBarPaint();
		}
	}
	
	
	
	
	public static int NUMERATOR = LayoutNodeFraction.NUMERATOR;
	public static int BAR = LayoutNodeFraction.BAR;
	public static int DENOMINATOR = LayoutNodeFraction.DENOMINATOR;

	public static int NUMCHILDREN = LayoutNodeFraction.NUMCHILDREN;
	
	protected DPWidget children[];
	protected DPSegment segs[];
	protected DPParagraph paras[];
	TextStyleSheet segmentTextStyleSheet;

	
	
	
	public DPFraction()
	{
		this( FractionStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet, "/" );
	}
	
	public DPFraction(String barTextRepresentation)
	{
		this( FractionStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet, barTextRepresentation );
	}
	
	public DPFraction(FractionStyleSheet styleSheet, TextStyleSheet segmentTextStyleSheet)
	{
		this( styleSheet, segmentTextStyleSheet, "/" );
	}
	
	public DPFraction(FractionStyleSheet styleSheet, TextStyleSheet segmentTextStyleSheet, String barTextRepresentation)
	{
		super( styleSheet );
		
		layoutNode = new LayoutNodeFraction( this );
		
		this.segmentTextStyleSheet = segmentTextStyleSheet;
		
		children = new DPWidget[NUMCHILDREN];
		segs = new DPSegment[NUMCHILDREN];
		paras = new DPParagraph[NUMCHILDREN];
		
		setChild( BAR, new DPFractionBar( styleSheet.getBarStyleSheet(), barTextRepresentation ) );
	}

	
	
	public DPWidget getChild(int slot)
	{
		return children[slot];
	}
	
	public DPWidget getWrappedChild(int slot)
	{
		return slot == BAR  ?  children[slot]  :  paras[slot];
	}
	
	public void setChild(int slot, DPWidget child)
	{
		DPWidget existingChild = children[slot];
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
					DPSegment seg = new DPSegment( segmentTextStyleSheet, true, true );
					segs[slot] = seg;
					DPParagraph para = new DPParagraph( );
					para.setChildren( Arrays.asList( new DPWidget[] { seg } ) );
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
				if ( child != null )
				{
					segs[slot].setChild( child );
				}
				
				
				if ( bSegmentPresent  &&  !bSegmentRequired )
				{
					DPParagraph para = paras[slot];
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
	
	
	
	
	public DPWidget getNumeratorChild()
	{
		return getChild( NUMERATOR );
	}
	
	public DPWidget getDenominatorChild()
	{
		return getChild( DENOMINATOR );
	}
	
	public DPWidget getBarChild()
	{
		return getChild( BAR );
	}
	
	
	public DPWidget getWrappedNumeratorChild()
	{
		return paras[NUMERATOR];
	}
	
	public DPWidget getWrappedDenominatorChild()
	{
		return paras[DENOMINATOR];
	}
	

	
	public void setNumeratorChild(DPWidget child)
	{
		setChild( NUMERATOR, child );
	}
	
	public void setDenominatorChild(DPWidget child)
	{
		setChild( DENOMINATOR, child );
	}
	
	public void setBarChild(DPWidget child)
	{
		setChild( BAR, child );
	}
	

	
	
	protected double getInternalChildScale(DPWidget child)
	{
		return child == children[BAR]  ?  1.0  :  childScale;
	}
	

	
	
	protected void replaceChildWithEmpty(DPWidget child)
	{
		int slot = Arrays.asList( children ).indexOf( child );
		setChild( slot, null );
	}
	
	
	
	public List<DPWidget> getChildren()
	{
		return registeredChildren;
	}

	
	
	
	
	public static double getChildScale()
	{
		return childScale;
	}

	

	//
	// STYLESHEET METHODS
	//
	
	protected double getVSpacing()
	{
		return ((FractionStyleSheet)styleSheet).getVSpacing();
	}

	protected double getHPadding()
	{
		return ((FractionStyleSheet)styleSheet).getHPadding();
	}

	protected double getYOffset()
	{
		return ((FractionStyleSheet)styleSheet).getYOffset();
	}
}
