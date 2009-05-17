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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.Metrics.VMetricsTypeset;
import BritefuryJ.DocPresent.StyleSheets.FractionStyleSheet;
import BritefuryJ.Math.Point2;

public class DPFraction extends DPContainer
{
	private static double childScale = 0.9;

	
	
	public static class DPFractionBar extends DPContentLeafEditableEntry
	{
		public DPFractionBar()
		{
			this( FractionStyleSheet.BarStyleSheet.defaultStyleSheet );
		}

		public DPFractionBar(FractionStyleSheet.BarStyleSheet styleSheet)
		{
			super( styleSheet );
		}

	
		protected void draw(Graphics2D graphics)
		{
			Shape s = new Rectangle2D.Double( 0.0, 0.0, allocationX, allocationY );
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
			AffineTransform current = pushGraphicsTransform( graphics );
			graphics.draw( new Line2D.Double( 0.0, -2.0, 0.0, allocationY + 2.0 ) );
			popGraphicsTransform( graphics, current );
		}

		public void drawCaretAtEnd(Graphics2D graphics)
		{
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
			AffineTransform current = pushGraphicsTransform( graphics );
			int startIndex = from != null  ?  from.getIndex()  :  0;
			int endIndex = to != null  ?  to.getIndex()  :  1;
			double startX = startIndex == 0  ?  0.0  :  allocationX;
			double endX = endIndex == 0  ?  0.0  :  allocationX;
			Rectangle2D.Double shape = new Rectangle2D.Double( startX, -2.0, endX - startX, allocationY + 4.0 );
			graphics.fill( shape );
			popGraphicsTransform( graphics, current );
		}


		
		protected HMetrics computeMinimumHMetrics()
		{
			return new HMetrics();
		}
		
		protected HMetrics computePreferredHMetrics()
		{
			return new HMetrics();
		}

		
		protected VMetrics computeMinimumVMetrics()
		{
			return new VMetrics();
		}

		protected VMetrics computePreferredVMetrics()
		{
			return new VMetrics();
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
	
	protected DPWidget[] children;

	
	
	
	public DPFraction()
	{
		this( FractionStyleSheet.defaultStyleSheet );
	}
	
	public DPFraction(FractionStyleSheet styleSheet)
	{
		super( styleSheet );
		
		children = new DPWidget[NUMCHILDREN];
		
		setChild( BAR, new DPFractionBar( styleSheet.getBarStyleSheet() ) );
	}

	
	
	public DPWidget getChild(int slot)
	{
		return children[slot];
	}
	
	public void setChild(int slot, DPWidget child)
	{
		DPWidget existingChild = children[slot];
		if ( child != existingChild )
		{
			if ( existingChild != null )
			{
				unregisterChild( existingChild );
				registeredChildren.remove( existingChild );
			}
			
			children[slot] = child;
			
			if ( child != null )
			{
				registerChild( child, null );
				if ( slot != BAR )
				{
					child.setScale( childScale, rootScale * childScale );
				}
				
				int insertIndex = 0;
				for (int i = 0; i < slot; i++)
				{
					if ( children[i] != null )
					{
						insertIndex++;
					}
				}
				
				registeredChildren.add( insertIndex, child );
			}
			
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
	

	
	
	protected void replaceChildWithEmpty(DPWidget child)
	{
		int slot = Arrays.asList( children ).indexOf( child );
		setChild( slot, null );
	}
	
	
	
	protected List<DPWidget> getChildren()
	{
		ArrayList<DPWidget> ch = new ArrayList<DPWidget>();
		
		for (int slot = 0; slot < NUMCHILDREN; slot++)
		{
			if ( children[slot] != null )
			{
				ch.add( children[slot] );
			}
		}
		
		return ch;
	}

	
	
	
	private HMetrics[] getChildRefreshedMinimumHMetrics()
	{
		HMetrics[] metrics = new HMetrics[NUMCHILDREN];
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( children[i] != null )
			{
				metrics[i] = children[i].refreshMinimumHMetrics();
			}
			else
			{
				metrics[i] = new HMetrics();
			}
		}
		
		return metrics;
	}
	
	private HMetrics[] getChildRefreshedPreferredHMetrics()
	{
		HMetrics[] metrics = new HMetrics[NUMCHILDREN];
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( children[i] != null )
			{
				metrics[i] = children[i].refreshPreferredHMetrics();
			}
			else
			{
				metrics[i] = new HMetrics();
			}
		}
		
		return metrics;
	}
	
	
	private VMetricsTypeset[] getChildRefreshedMinimumVMetrics()
	{
		VMetricsTypeset[] metrics = new VMetricsTypeset[NUMCHILDREN];
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( children[i] != null )
			{
				VMetrics v = children[i].refreshMinimumVMetrics();
				if ( v.isTypeset() )
				{
					metrics[i] = (VMetricsTypeset)v;
				}
				else
				{
					metrics[i] = new VMetricsTypeset( v.height, 0.0, v.vspacing );
				}
			}
			else
			{
				metrics[i] = new VMetricsTypeset();
			}
		}
		
		return metrics;
	}
	
	private VMetricsTypeset[] getChildRefreshedPreferredVMetrics()
	{
		VMetricsTypeset[] metrics = new VMetricsTypeset[NUMCHILDREN];
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( children[i] != null )
			{
				VMetrics v = children[i].refreshPreferredVMetrics();
				if ( v.isTypeset() )
				{
					metrics[i] = (VMetricsTypeset)v;
				}
				else
				{
					metrics[i] = new VMetricsTypeset( v.height, 0.0, v.vspacing );
				}
			}
			else
			{
				metrics[i] = new VMetricsTypeset();
			}
		}
		
		return metrics;
	}
	
	

	
	
	
	
	private double computeBarHeight()
	{
		return 1.5;
	}
	
	
	private HMetrics combineHMetricsHorizontally(HMetrics[] childHMetrics)
	{
		HMetrics m = HMetrics.max( childHMetrics[NUMERATOR], childHMetrics[DENOMINATOR] );
		return new HMetrics( m.width  +  getHPadding() * 2.0, m.hspacing );
	}
	
	
	private VMetrics combineVMetricsVertically(VMetrics[] childVMetrics)
	{
		double spacing = getVSpacing();
		double yOffset = getYOffset();
		
		// Accumulate the height required for all the children
		double barHeight = computeBarHeight() * 0.5;
		double ascent = childVMetrics[NUMERATOR].height + childVMetrics[NUMERATOR].minSpacing( spacing ).vspacing + barHeight  +  yOffset;
		double descent = barHeight + spacing + childVMetrics[DENOMINATOR].height  -  yOffset;
		

		return new VMetricsTypeset( ascent, descent, childVMetrics[DENOMINATOR].vspacing );
	}
	
	

	protected HMetrics computeMinimumHMetrics()
	{
		return combineHMetricsHorizontally( getChildRefreshedMinimumHMetrics() );
	}

	protected HMetrics computePreferredHMetrics()
	{
		return combineHMetricsHorizontally( getChildRefreshedPreferredHMetrics() );
	}

	
	protected VMetrics computeMinimumVMetrics()
	{
		return combineVMetricsVertically( getChildRefreshedMinimumVMetrics() );
	}

	protected VMetrics computePreferredVMetrics()
	{
		return combineVMetricsVertically( getChildRefreshedPreferredVMetrics() );
	}

	
	
	
	protected void allocateContentsX(double allocation)
	{
		super.allocateContentsX( allocation );
		
		double padding = getHPadding();
		
		double childrenAlloc = allocation - padding * 2.0;
		
		if ( children[NUMERATOR] != null )
		{
			double childWidth = Math.min( children[NUMERATOR].prefH.width, childrenAlloc );
			allocateChildX( children[NUMERATOR], padding + ( childrenAlloc - childWidth ) * 0.5, childWidth );
		}
		
		if ( children[BAR] != null )
		{
			double numeratorWidth = children[NUMERATOR] != null  ?  children[NUMERATOR].prefH.width  :  0.0;
			double denominatorWidth = children[DENOMINATOR] != null  ?  children[DENOMINATOR].prefH.width  :  0.0;
			double childWidth = Math.min( Math.max( numeratorWidth, denominatorWidth ) + padding * 2.0, allocation );
			allocateChildX( children[BAR], 0.0, childWidth );
		}
		
		if ( children[DENOMINATOR] != null )
		{
			double childWidth = Math.min( children[DENOMINATOR].prefH.width, childrenAlloc );
			allocateChildX( children[DENOMINATOR], padding + ( childrenAlloc - childWidth ) * 0.5, childWidth );
		}
	}

	
	protected void allocateContentsY(double allocation)
	{
		super.allocateContentsY( allocation );
		
		double y = 0.0;
		double spacing = getVSpacing();
		
		if ( children[NUMERATOR] != null )
		{
			double childHeight = Math.min( children[NUMERATOR].prefV.height, allocation );
			allocateChildY( children[NUMERATOR], y, childHeight );
			
			y += childHeight  +  children[NUMERATOR].prefV.minSpacing( spacing ).vspacing;
		}
		
		if ( children[BAR] != null )
		{
			double childHeight = computeBarHeight();
			allocateChildY( children[BAR], y, childHeight );
			
			y += childHeight  +  spacing;
		}
		
		if ( children[DENOMINATOR] != null )
		{
			double childHeight = Math.min( children[DENOMINATOR].prefV.height, allocation );
			allocateChildY( children[DENOMINATOR], y, childHeight );
		}
	}
	
	

	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( localPos, filter );
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
		ArrayList<DPWidget> xs = new ArrayList<DPWidget>();
		
		for (DPWidget x: children)
		{
			if ( x != null )
			{
				xs.add( x );
			}
		}
		
		return xs;
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
