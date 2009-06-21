//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Layout.FractionLayout;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LReqBox;
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
		}

	
		protected void draw(Graphics2D graphics)
		{
			Shape s = new Rectangle2D.Double( 0.0, 0.0, getAllocationX(), getAllocationY() );
			graphics.setColor( getColour() );
			graphics.fill( s );
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


		
		protected void updateRequisitionX()
		{
			layoutReqBox.clearRequisitionX();
		}

		protected void updateRequisitionY()
		{
			layoutReqBox.clearRequisitionY();
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
		
		protected Color getColour()
		{
			return ((FractionStyleSheet.BarStyleSheet)styleSheet).getColour();
		}
	}
	
	
	
	
	public static int NUMERATOR = 0;
	public static int BAR = 1;
	public static int DENOMINATOR = 2;

	public static int NUMCHILDREN = 3;
	
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
					registerChild( child, null );
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
					DPParagraph para = new DPParagraph();
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
					registerChild( para, null );
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
		return Arrays.asList( children );
	}

	
	
	
	protected void updateRequisitionX()
	{
		LReqBox boxes[] = new LReqBox[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( i != BAR )
			{
				boxes[i] = paras[i] != null  ?  paras[i].refreshRequisitionX().scaled( childScale )  :  null;
			}
			else
			{
				boxes[i] = children[i] != null  ?  children[i].refreshRequisitionX()  :  null;
			}
		}
		
		FractionLayout.computeRequisitionX( layoutReqBox, boxes[NUMERATOR], boxes[BAR], boxes[DENOMINATOR], getHPadding(), getVSpacing(), getYOffset() );
	}

	protected void updateRequisitionY()
	{
		LReqBox boxes[] = new LReqBox[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( i != BAR )
			{
				boxes[i] = paras[i] != null  ?  paras[i].refreshRequisitionY().scaled( childScale )  :  null;
			}
			else
			{
				boxes[i] = children[i] != null  ?  children[i].refreshRequisitionY()  :  null;
			}
		}
		
		FractionLayout.computeRequisitionY( layoutReqBox, boxes[NUMERATOR], boxes[BAR], boxes[DENOMINATOR], getHPadding(), getVSpacing(), getYOffset() );
	}
	

	
	protected void updateAllocationX()
	{
		super.updateAllocationX( );
		
		LReqBox reqBoxes[] = new LReqBox[NUMCHILDREN];
		LAllocBox allocBoxes[] = new LAllocBox[NUMCHILDREN];
		double prevChildWidths[] = new double[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( i != BAR )
			{
				reqBoxes[i] = paras[i] != null  ?  paras[i].layoutReqBox.scaled( childScale )  :  null;
				allocBoxes[i] = paras[i] != null  ?  paras[i].layoutAllocBox  :  null;
				prevChildWidths[i] = paras[i] != null  ?  paras[i].layoutAllocBox.getAllocationX()  :  0.0;
			}
			else
			{
				reqBoxes[i] = children[i] != null  ?  children[i].layoutReqBox  :  null;
				allocBoxes[i] = children[i] != null  ?  children[i].layoutAllocBox  :  null;
				prevChildWidths[i] = children[i] != null  ?  children[i].layoutAllocBox.getAllocationX()  :  0.0;
			}
		}
		
		FractionLayout.allocateX( layoutReqBox, reqBoxes[NUMERATOR], reqBoxes[BAR], reqBoxes[DENOMINATOR],
				layoutAllocBox, allocBoxes[NUMERATOR], allocBoxes[BAR], allocBoxes[DENOMINATOR], 
				getHPadding(), getVSpacing(), getYOffset() );
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( paras[i] != null )
			{
				if ( i != BAR )
				{
					allocBoxes[i].scaleAllocationX( 1.0 / childScale );
					paras[i].refreshAllocationX( prevChildWidths[i] );
				}
				else
				{
					children[i].refreshAllocationX( prevChildWidths[i] );
				}
			}
		}
	}

	
	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		LReqBox reqBoxes[] = new LReqBox[NUMCHILDREN];
		LAllocBox allocBoxes[] = new LAllocBox[NUMCHILDREN];
		double prevChildHeights[] = new double[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( i != BAR )
			{
				reqBoxes[i] = paras[i] != null  ?  paras[i].layoutReqBox.scaled( childScale )  :  null;
				allocBoxes[i] = paras[i] != null  ?  paras[i].layoutAllocBox  :  null;
				prevChildHeights[i] = paras[i] != null  ?  paras[i].layoutAllocBox.getAllocationY()  :  0.0;
			}
			else
			{
				reqBoxes[i] = children[i] != null  ?  children[i].layoutReqBox  :  null;
				allocBoxes[i] = children[i] != null  ?  children[i].layoutAllocBox  :  null;
				prevChildHeights[i] = children[i] != null  ?  children[i].layoutAllocBox.getAllocationY()  :  0.0;
			}
		}
		
		FractionLayout.allocateY( layoutReqBox, reqBoxes[NUMERATOR], reqBoxes[BAR], reqBoxes[DENOMINATOR],
				layoutAllocBox, allocBoxes[NUMERATOR], allocBoxes[BAR], allocBoxes[DENOMINATOR], 
				getHPadding(), getVSpacing(), getYOffset() );
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( paras[i] != null )
			{
				if ( i != BAR )
				{
					allocBoxes[i].scaleAllocationY( 1.0 / childScale );
					paras[i].refreshAllocationY( prevChildHeights[i] );
				}
				else
				{
					children[i].refreshAllocationY( prevChildHeights[i] );
				}
			}
		}
	}
	
	

	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( registeredChildren, localPos, filter );
	}


	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return verticalNavigationList();
	}

	protected List<DPWidget> verticalNavigationList()
	{
		return registeredChildren;
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
