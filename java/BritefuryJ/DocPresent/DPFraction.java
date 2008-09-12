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
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.Metrics.VMetricsTypeset;
import BritefuryJ.DocPresent.StyleSheets.FractionStyleSheet;
import BritefuryJ.Math.Point2;

public class DPFraction extends DPContainer
{
	public static class DPFractionBar extends DPContentLeafEditable
	{
		public DPFractionBar()
		{
			this( FractionStyleSheet.BarStyleSheet.defaultStyleSheet, "/" );
		}

		public DPFractionBar(String content)
		{
			this( FractionStyleSheet.BarStyleSheet.defaultStyleSheet, content );
		}

		public DPFractionBar(FractionStyleSheet.BarStyleSheet styleSheet)
		{
			super( styleSheet, "/" );
		}

		public DPFractionBar(FractionStyleSheet.BarStyleSheet styleSheet, String content)
		{
			super( styleSheet, content );
		}

	
		protected void draw(Graphics2D graphics)
		{
			Shape s = new Rectangle2D.Double( 0.0, 0.0, allocation.x, allocation.y );
			graphics.setColor( getColour() );
			graphics.fill( s );
		}
		
		
		public void drawCaret(Graphics2D graphics, Caret c)
		{
			int index = c.getMarker().getIndex();
			
			if ( index == 0 )
			{
				graphics.draw( new Line2D.Double( 0.0, -2.0, 0.0, allocation.y + 2.0 ) );
			}
			else
			{
				graphics.draw( new Line2D.Double( allocation.x, -2.0, allocation.x, allocation.y + 2.0 ) );
			}
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

		
		public int getContentPositonForPoint(Point2 localPos)
		{
			if ( localPos.x >= allocation.x * 0.5 )
			{
				return getContentLength();
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
			if ( child != null )
			{
				child.unparent();
			}
			
			if ( existingChild != null )
			{
				ChildEntry entry = childToEntry.get( existingChild );
				unregisterChildEntry( entry );
				childEntries.remove( entry );
			}
			
			children[slot] = child;
			
			if ( child != null )
			{
				ChildEntry entry = new ChildEntry( child );
				childEntries.add( entry );
				registerChildEntry( entry );
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
	

	
	
	protected void removeChild(DPWidget child)
	{
		int slot = Arrays.asList( children ).indexOf( child );
		setChild( slot, null );
	}
	
	
	
	protected List<DPWidget> getChildren()
	{
		Vector<DPWidget> ch = new Vector<DPWidget>();
		
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
			double childWidth = Math.min( Math.max( children[NUMERATOR].prefH.width, children[DENOMINATOR].prefH.width ) + padding * 2.0, allocation );
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
	
	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return verticalNavigationList();
	}

	protected List<DPWidget> verticalNavigationList()
	{
		Vector<DPWidget> xs = new Vector<DPWidget>();
		
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




	//
	//
	// CONTENT METHODS
	//
	//

	public String getContent()
	{
		String xs = "";
		for (DPWidget child: children)
		{
			if ( child != null )
			{
				xs += child.getContent();
			}
		}
		return xs;
	}

	public int getContentLength()
	{
		int length = 0;
		for (DPWidget child: children)
		{
			if ( child != null )
			{
				length += child.getContentLength();
			}
		}
		return length;
	}
}
