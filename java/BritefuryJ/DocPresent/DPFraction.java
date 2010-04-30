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
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.DocPresent.StyleParams.FractionStyleParams;
import BritefuryJ.DocPresent.StyleParams.TextStyleParams;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Xform2;

public class DPFraction extends DPContainer
{
	private static double childScale = 0.85;
	private static final Xform2 childScaleXform = new Xform2( childScale );
	private static final Xform2 childScaleXformInv = childScaleXform.inverse();


	public static class DPFractionBar extends DPContentLeafEditable
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
		
		protected DPFractionBar(DPFractionBar element)
		{
			super( element );
			
			layoutNode = new LayoutNodeFraction.LayoutNodeFractionBar( this );
		}
		
		
		
		//
		//
		// Presentation tree cloning
		//
		//
		
		public DPElement clonePresentationSubtree()
		{
			DPFractionBar clone = new DPFractionBar( this );
			clone.clonePostConstuct( this );
			return clone;
		}

	

		public boolean isRedrawRequiredOnHover()
		{
			return super.isRedrawRequiredOnHover()  ||  getHoverBarPaint() != null;
		}
		

		protected void draw(Graphics2D graphics)
		{
			Shape s = new Rectangle2D.Double( 0.0, 0.0, getAllocationX(), getAllocationY() );
			Paint curPaint = graphics.getPaint();

			Paint barPaint;
			if ( testFlag( FLAG_HOVER ) )
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
			double allocationY = getAllocationY();
			AffineTransform current = pushGraphicsTransform( graphics );
			graphics.draw( new Line2D.Double( 0.0, -2.0, 0.0, allocationY + 2.0 ) );
			popGraphicsTransform( graphics, current );
		}

		private void drawCaretAtEnd(Graphics2D graphics)
		{
			double allocationX = getAllocationX();
			double allocationY = getAllocationY();
			AffineTransform current = pushGraphicsTransform( graphics );
			graphics.draw( new Line2D.Double( allocationX, -2.0, allocationX, allocationY + 2.0 ) );
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
			return textRepresentation.length();
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
	
	protected DPElement children[];
	protected DPSegment segs[];
	protected DPParagraph paras[];
	TextStyleParams segmentTextStyleParams;

	
	
	
	public DPFraction()
	{
		this( FractionStyleParams.defaultStyleParams, TextStyleParams.defaultStyleParams, "/" );
	}
	
	public DPFraction(String barTextRepresentation)
	{
		this( FractionStyleParams.defaultStyleParams, TextStyleParams.defaultStyleParams, barTextRepresentation );
	}
	
	public DPFraction(FractionStyleParams styleParams, TextStyleParams segmentTextStyleParams)
	{
		this(styleParams, segmentTextStyleParams, "/" );
	}
	
	public DPFraction(FractionStyleParams styleParams, TextStyleParams segmentTextStyleParams, String barTextRepresentation)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeFraction( this );
		
		this.segmentTextStyleParams = segmentTextStyleParams;
		
		children = new DPElement[NUMCHILDREN];
		segs = new DPSegment[NUMCHILDREN];
		paras = new DPParagraph[NUMCHILDREN];
		
		setChild( BAR, new DPFractionBar( styleParams.getBarStyleSheet(), barTextRepresentation ) );
	}
	
	protected DPFraction(DPFraction element)
	{
		super( element );
		
		layoutNode = new LayoutNodeFraction( this );
		
		this.segmentTextStyleParams = element.segmentTextStyleParams;
		
		children = new DPElement[NUMCHILDREN];
		segs = new DPSegment[NUMCHILDREN];
		paras = new DPParagraph[NUMCHILDREN];
	}
	
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	protected void clonePostConstuct(DPElement src)
	{
		super.clonePostConstuct( src );
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPElement child = ((DPFraction)src).getChild( i );
			if ( child != null )
			{
				setChild( i, child.clonePresentationSubtree() );
			}
		}
	}
	
	public DPElement clonePresentationSubtree()
	{
		DPFraction clone = new DPFraction( this );
		clone.clonePostConstuct( this );
		return clone;
	}

	
	
	//
	//
	// Child element access / modification
	//
	//
	
	public DPElement getChild(int slot)
	{
		return children[slot];
	}
	
	public DPElement getWrappedChild(int slot)
	{
		return slot == BAR  ?  children[slot]  :  paras[slot];
	}
	
	public void setChild(int slot, DPElement child)
	{
		DPElement existingChild = children[slot];
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
					DPSegment seg = new DPSegment( (ContainerStyleParams)getStyleParams(), segmentTextStyleParams, true, true );
					segs[slot] = seg;
					DPParagraph para = new DPParagraph( );
					para.setChildren( Arrays.asList( new DPElement[] { seg } ) );
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
	
	
	
	
	public DPElement getNumeratorChild()
	{
		return getChild( NUMERATOR );
	}
	
	public DPElement getDenominatorChild()
	{
		return getChild( DENOMINATOR );
	}
	
	public DPElement getBarChild()
	{
		return getChild( BAR );
	}
	
	
	public DPElement getWrappedNumeratorChild()
	{
		return paras[NUMERATOR];
	}
	
	public DPElement getWrappedDenominatorChild()
	{
		return paras[DENOMINATOR];
	}
	

	
	public void setNumeratorChild(DPElement child)
	{
		setChild( NUMERATOR, child );
	}
	
	public void setDenominatorChild(DPElement child)
	{
		setChild( DENOMINATOR, child );
	}
	
	public void setBarChild(DPElement child)
	{
		setChild( BAR, child );
	}
	

	
	
	protected Xform2 getAllocationSpaceToLocalSpaceXform(DPElement child)
	{
		return child == children[BAR]  ?  Xform2.identity  :  new Xform2( childScale );
	}
	

	
	
	protected void replaceChildWithEmpty(DPElement child)
	{
		int slot = Arrays.asList( children ).indexOf( child );
		setChild( slot, null );
	}
	
	
	
	public List<DPElement> getChildren()
	{
		return registeredChildren;
	}

	
	
	
	
	public static Xform2 getScriptChildXform()
	{
		return childScaleXform;
	}

	public static Xform2 getInverseScriptChildXform()
	{
		return childScaleXformInv;
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
