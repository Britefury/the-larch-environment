//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.Metrics.VMetricsTypeset;
import BritefuryJ.DocPresent.StyleSheets.ScriptStyleSheet;
import BritefuryJ.Math.Point2;


public class DPScript extends DPContainer
{
	public static int LEFTSUPER = 0;
	public static int LEFTSUB = 1;
	public static int MAIN = 2;
	public static int RIGHTSUPER = 3;
	public static int RIGHTSUB = 4;
	
	public static int NUMCHILDREN = 5;
	
	private static int LEFTCOLUMN = 0;
	private static int MAINCOLUMN = 1;
	private static int RIGHTCOLUMN = 2;
	
	private static double childScale = 0.7;
	
	
	private static double superOffset = 0.333;
	private static double subOffset = 0.333;

	private static double superOffsetFraction = superOffset  /  ( superOffset + subOffset );
	private static double subOffsetFraction = subOffset  /  ( superOffset + subOffset );
	
	
	
	protected DPWidget[] children;
	protected HMetrics leftMinH, leftPrefH;
	protected HMetrics mainMinH, mainPrefH;
	protected HMetrics rightMinH, rightPrefH;
	protected double superscriptAscent, mainAscent, subscriptAscent;
	
	
	
	public DPScript()
	{
		this( ScriptStyleSheet.defaultStyleSheet );
	}
	
	public DPScript(ScriptStyleSheet styleSheet)
	{
		super( styleSheet );
		
		children = new DPWidget[NUMCHILDREN];
		leftMinH = new HMetrics();
		leftPrefH = new HMetrics();
		rightMinH = new HMetrics();
		rightPrefH = new HMetrics();
		superscriptAscent = 0.0;
		mainAscent = 0.0;
		subscriptAscent = 0.0;
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
				if ( slot != MAIN )
				{
					child.setScale( childScale, rootScale * childScale );
				}
			}
			
			queueResize();
		}
	}
	
	
	
	
	public DPWidget getMainChild()
	{
		return getChild( MAIN );
	}
	
	public DPWidget getLeftSuperscriptChild()
	{
		return getChild( LEFTSUPER );
	}
	
	public DPWidget getLeftSubscriptChild()
	{
		return getChild( LEFTSUB );
	}
	
	public DPWidget getRightSuperscriptChild()
	{
		return getChild( RIGHTSUPER );
	}
	
	public DPWidget getRightSubscriptChild()
	{
		return getChild( RIGHTSUB );
	}
	
	
	public void setMainChild(DPWidget child)
	{
		setChild( MAIN, child );
	}
	
	public void setLeftSuperscriptChild(DPWidget child)
	{
		setChild( LEFTSUPER, child );
	}
	
	public void setLeftSubscriptChild(DPWidget child)
	{
		setChild( LEFTSUB, child );
	}
	
	public void setRightSuperscriptChild(DPWidget child)
	{
		setChild( RIGHTSUPER, child );
	}
	
	public void setRightSubscriptChild(DPWidget child)
	{
		setChild( RIGHTSUB, child );
	}
	

	
	
	protected void removeChild(DPWidget child)
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
	
	

	
	
	
	
	private HMetrics[] getChildMinimumHMetrics()
	{
		HMetrics[] metrics = new HMetrics[NUMCHILDREN];
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( children[i] != null )
			{
				metrics[i] = children[i].minH;
			}
			else
			{
				metrics[i] = new HMetrics();
			}
		}
		
		return metrics;
	}
	
	private HMetrics[] getChildPreferredHMetrics()
	{
		HMetrics[] metrics = new HMetrics[NUMCHILDREN];
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( children[i] != null )
			{
				metrics[i] = children[i].prefH;
			}
			else
			{
				metrics[i] = new HMetrics();
			}
		}
		
		return metrics;
	}
	
	
	private VMetricsTypeset[] getChildMinimumVMetrics()
	{
		VMetricsTypeset[] metrics = new VMetricsTypeset[NUMCHILDREN];
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( children[i] != null )
			{
				VMetrics v = children[i].minV;
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
	
	private VMetricsTypeset[] getChildPreferredVMetrics()
	{
		VMetricsTypeset[] metrics = new VMetricsTypeset[NUMCHILDREN];
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( children[i] != null )
			{
				VMetrics v = children[i].prefV;
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
	
	

	
	
	
	private boolean hasMainChild()
	{
		return children[MAIN] != null;
	}
	
	private boolean hasLeftChild()
	{
		return children[LEFTSUB] != null  ||  children[LEFTSUPER] != null;
	}
	
	private boolean hasRightChild()
	{
		return children[RIGHTSUB] != null  ||  children[RIGHTSUPER] != null;
	}
	
	private boolean hasSuperscriptChild()
	{
		return children[LEFTSUPER] != null  ||  children[RIGHTSUPER] != null;
	}
	
	private boolean hasSubscriptChild()
	{
		return children[LEFTSUB] != null  ||  children[RIGHTSUB] != null;
	}
	
	
	
	private HMetrics[] combineHMetricsHorizontally(HMetrics[] childHMetrics)
	{
		HMetrics left = HMetrics.max( childHMetrics[LEFTSUB], childHMetrics[LEFTSUPER] );
		HMetrics main = childHMetrics[MAIN];
		HMetrics right = HMetrics.max( childHMetrics[RIGHTSUB], childHMetrics[RIGHTSUPER] );

		if ( hasLeftChild()   &&   ( hasMainChild()  ||  hasRightChild() ) )
		{
			left = left.minSpacing( getSpacing() );
		}
		
		if ( hasMainChild()  &&  hasRightChild() )
		{
			main = main.minSpacing( getSpacing() );
		}
		
		
		HMetrics sum = left.add( main ).add( right );
		HMetrics[] result = { left, main, right, sum };
		
		return result;
	}
	
	
	
	private VMetricsTypeset combineVMetricsVertically(VMetricsTypeset[] childVMetrics)
	{
		double Ma = childVMetrics[MAIN].ascent;
		double Md = childVMetrics[MAIN].descent;
		double Mh = childVMetrics[MAIN].height;

		double Pa = Math.max( childVMetrics[LEFTSUPER].ascent, childVMetrics[RIGHTSUPER].ascent );
		double Pd = Math.max( childVMetrics[LEFTSUPER].descent, childVMetrics[RIGHTSUPER].descent );

		double Ba = Math.max( childVMetrics[LEFTSUB].ascent, childVMetrics[RIGHTSUB].ascent );
		double Bd = Math.max( childVMetrics[LEFTSUB].descent, childVMetrics[RIGHTSUB].descent );
		
		
		// top: TOP
		// bottom: BOTTOM
		// All y-co-ordinates are relative to an arbitrary co-ordinate system until the end of the calculation
		//
		// q: spacing between super bottom and sub top
		// r: min distance between super baseline and main baseline (1/3 of Mh)
		// s: min distance between main baseline and sub baseline (1/3 of Mh)
		// r and s can be thought of as springs
		double superTop, mainTop, superBaseline, superBottom, subTop, mainBaseline, mainBottom, subBaseline, subBottom, bottom, top, q, r, s;
		
		if ( hasSuperscriptChild()  &&  hasSubscriptChild() )
		{
			q = getScriptSpacing();
			
			// Start with main-baseline = 0
			mainBaseline = 0.0;
			
			// We can compute main-top and main-bottom immediately
			mainTop = mainBaseline - Ma;
			mainBottom = mainBaseline + Md;
			
			// Next compute super-baseline and sub-baseline
			// The distance between c and h is max( Pd + Ba + q, r + s )
			r = Mh * superOffset;
			s = Mh * subOffset;
			double deltaBaseline = Math.max( Pd + Ba + q,   r + s );
		
			// Divide cToH between r and s according to their proportion
			r = deltaBaseline  *  superOffsetFraction;
			s = deltaBaseline  *  subOffsetFraction;
			
			// We can now compute superscript-baseline and subscript-baseline
			superBaseline = mainBaseline - r;
			subBaseline = mainBaseline + s;
			
			// We can compute super-top and super-bottom
			superTop = superBaseline - Pa;
			superBottom = superBaseline + Pd;
			
			// We can compute subscript-top and subscript-bottom
			subTop = subBaseline - Ba;
			subBottom = subBaseline + Bd;
			
			// We can now compute the top and the bottom
			top = Math.min( mainTop, superTop );
			bottom = Math.max( mainBottom, subBottom );
			
			superscriptAscent = superBaseline - top;
			mainAscent = mainBaseline - top;
			subscriptAscent = subBaseline - top;
		}
		else if ( hasSuperscriptChild() )
		{
			// Start with main-baseline = 0
			mainBaseline = 0.0;
			
			// We can compute main-top and main-bottom immediately
			mainTop = mainBaseline - Ma;
			mainBottom = mainBaseline + Md;
			
			// R
			r = Mh * superOffset;
			
			// Superscript-baseline
			superBaseline = mainBaseline - r;

			// We can compute super-top and super-bottom
			superTop = superBaseline - Pa;
			superBottom = superBaseline + Pd;
			
			// We can now compute the top and the bottom
			top = Math.min( mainTop, superTop );
			bottom = Math.max( mainBottom, superBottom );
			
			superscriptAscent = superBaseline - top;
			mainAscent = mainBaseline - top;
			subscriptAscent = mainBaseline - top;
		}
		else if ( hasSubscriptChild() )
		{
			// Start with main-baseline = 0
			mainBaseline = 0.0;
			
			// We can compute main-top and main-bottom immediately
			mainTop = mainBaseline - Ma;
			mainBottom = mainBaseline + Md;
			
			// S
			s = Mh * subOffset;
			
			// We can now compute subscript-baseline
			subBaseline = mainBaseline + s;
			
			// We can compute sub-top and sub-bottom 
			subTop = subBaseline - Ba;
			subBottom = subBaseline + Bd;
			
			// We can now compute the top and the bottom
			top = Math.min( mainTop, subTop );
			bottom = Math.max( mainBottom, subBottom );
			
			superscriptAscent = mainBaseline - top;
			mainAscent = mainBaseline - top;
			subscriptAscent = subBaseline - top;
		}
		else
		{
			mainBaseline = 0.0;
			
			top = -Ma;
			bottom = Md;
			
			superscriptAscent = mainBaseline - top;
			mainAscent = mainBaseline - top;
			subscriptAscent = mainBaseline - top;
		}
		
		
		double height = bottom - top;
		
		double descent = height - mainAscent;
		return new VMetricsTypeset( mainAscent, descent, getSpacing() );
	}
	

	protected HMetrics computeMinimumHMetrics()
	{
		HMetrics[] xs = combineHMetricsHorizontally( getChildRefreshedMinimumHMetrics() );
		
		leftMinH = xs[0];
		mainMinH = xs[1];
		rightMinH = xs[2];
		
		return xs[3];
	}

	protected HMetrics computePreferredHMetrics()
	{
		HMetrics[] xs = combineHMetricsHorizontally( getChildRefreshedPreferredHMetrics() );
		
		leftPrefH = xs[0];
		mainPrefH = xs[1];
		rightPrefH = xs[2];
		
		return xs[3];
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
		
		double t;
		if ( prefH.width > minH.width )
		{
			t = ( allocation - minH.width )  /  ( prefH.width - minH.width );
			t = Math.max( t, 0.0 );
			t = Math.min( t, 1.0 );
		}
		else
		{
			t = 1.0;
		}
		
		HMetrics[] childMinH = getChildMinimumHMetrics();
		HMetrics[] childPrefH = getChildPreferredHMetrics();
		
		HMetrics overallH = HMetrics.lerp( minH, prefH, t );
		HMetrics leftH = HMetrics.lerp( leftMinH, leftPrefH, t );
		HMetrics mainH = HMetrics.lerp( mainMinH, mainPrefH, t );
	
		double padding = Math.max( ( allocation - overallH.width )  *  0.5, 0.0 );
		double x = padding;
		
		// Allocate left children
		if ( children[LEFTSUPER] != null )
		{
			HMetrics childH = HMetrics.lerp( childMinH[LEFTSUPER], childPrefH[LEFTSUPER], t );
			allocateChildX( children[LEFTSUPER], x  +  ( leftH.width - childH.width ), childH.width );
		}
		if ( children[LEFTSUB] != null )
		{
			HMetrics childH = HMetrics.lerp( childMinH[LEFTSUB], childPrefH[LEFTSUB], t );
			allocateChildX( children[LEFTSUB], x  +  ( leftH.width - childH.width ), childH.width ); 
		}
		
		if ( hasLeftChild() )
		{
			leftH = leftH.minSpacing( getSpacing() );
		}
		
		x += leftH.width + leftH.hspacing;
		
		
		// Allocate main child
		if ( children[MAIN] != null )
		{
			allocateChildX( children[MAIN], x, mainH.width );
			mainH = mainH.minSpacing( getSpacing() );
		}
		
		x += mainH.width + mainH.hspacing;
		
		
		// Allocate right children
		if ( children[RIGHTSUPER] != null )
		{
			HMetrics childH = HMetrics.lerp( childMinH[RIGHTSUPER], childPrefH[RIGHTSUPER], t );
			allocateChildX( children[RIGHTSUPER], x, childH.width );
		}
		if ( children[RIGHTSUB] != null )
		{
			HMetrics childH = HMetrics.lerp( childMinH[RIGHTSUB], childPrefH[RIGHTSUB], t );
			allocateChildX( children[RIGHTSUB], x, childH.width ); 
		}
	}

	
	protected void allocateContentsY(double allocation)
	{
		super.allocateContentsY( allocation );

		double padding = Math.max( ( allocation - prefV.height ) * 0.5, 0.0 );
		
		
		VMetricsTypeset[] childrenV;
		if ( allocation >= prefV.height )
		{
			childrenV = getChildPreferredVMetrics();
		}
		else
		{
			childrenV = getChildMinimumVMetrics();
		}

		
		// Allocate superscript children
		double y = padding + superscriptAscent;
		if ( children[LEFTSUPER] != null )
		{
			VMetricsTypeset childV = childrenV[LEFTSUPER];
			allocateChildY( children[LEFTSUPER], y - childV.ascent, childV.height );
		}
		if ( children[RIGHTSUPER] != null )
		{
			VMetricsTypeset childV = childrenV[RIGHTSUPER];
			allocateChildY( children[RIGHTSUPER], y - childV.ascent, childV.height );
		}
		
		
		// Allocate main child
		y = padding + mainAscent;
		if ( children[MAIN] != null )
		{
			VMetricsTypeset childV = childrenV[MAIN];
			allocateChildY( children[MAIN], y - childV.ascent, childV.height );
		}
		

		// Allocate subscript children
		y = padding + subscriptAscent;
		if ( children[LEFTSUB] != null )
		{
			VMetricsTypeset childV = childrenV[LEFTSUB];
			allocateChildY( children[LEFTSUB], y - childV.ascent, childV.height );
		}
		if ( children[RIGHTSUB] != null )
		{
			VMetricsTypeset childV = childrenV[RIGHTSUB];
			allocateChildY( children[RIGHTSUB], y - childV.ascent, childV.height );
		}
	}
	

	
	
	private ArrayList<ChildEntry> getChildEntries(int[] slots)
	{
		ArrayList<ChildEntry> entries = new ArrayList<ChildEntry>();
		for (int slot: slots)
		{
			if ( children[slot] != null )
			{
				entries.add( childToEntry.get( children[slot] ) );
			}
		}
		
		return entries;
	}
	
	private ArrayList<ChildEntry> getChildEntriesInColumn(int column)
	{
		if ( column == LEFTCOLUMN )
		{
			int slots[] = { LEFTSUPER, LEFTSUB };
			return getChildEntries( slots );
		}
		else if ( column == MAINCOLUMN )
		{
			int slots[] = { MAIN };
			return getChildEntries( slots );
		}
		else if ( column == RIGHTCOLUMN )
		{
			int slots[] = { RIGHTSUPER, RIGHTSUB };
			return getChildEntries( slots );
		}
		else
		{
			throw new RuntimeException();
		}
	}
	
	
	private double getColumnXEdge(ArrayList<ChildEntry> column, boolean bRightEdge)
	{
		double columnEdgeX = 0.0;
		for (int i = 0; i < column.size(); i++)
		{
			ChildEntry entry = column.get( i );
			
			double edgeX = 0.0;
			if ( bRightEdge )
			{
				edgeX = entry.pos.x + entry.size.x;
			}
			else
			{
				edgeX = entry.pos.x;
			}
			
			if ( i > 0 )
			{
				if ( bRightEdge )
				{
					columnEdgeX = Math.max( columnEdgeX, edgeX );
				}
				else
				{
					columnEdgeX = Math.min( columnEdgeX, edgeX );
				}
			}
			else
			{
				columnEdgeX= edgeX;
			}
		}
		
		return columnEdgeX;
	}
	
	
	protected ChildEntry getChildEntryClosestToLocalPoint(Point2 localPos)
	{
		if ( childEntries.size() == 0 )
		{
			// No children
			return null;
		}
		else if ( childEntries.size() == 1 )
		{
			// Only 1 child
			return childEntries.get( 0 );
		}
		else
		{
			// Group children by column
			ArrayList< ArrayList<ChildEntry> > childEntriesByColumn = new ArrayList< ArrayList<ChildEntry> >();
			
			int[] columns = { LEFTCOLUMN, MAINCOLUMN, RIGHTCOLUMN };
			
			for (int col: columns)
			{
				ArrayList<ChildEntry> childEntries = getChildEntriesInColumn( col );
				
				if ( childEntries.size() > 0 )
				{
					childEntriesByColumn.add( childEntries );
				}
			}
			
			
			// Determine which column is closest
			ArrayList<ChildEntry> closestColumn = null;
			
			if ( childEntriesByColumn.size() == 1 )
			{
				closestColumn = childEntriesByColumn.get( 0 );
			}
			else
			{
				ArrayList<ChildEntry> colI = childEntriesByColumn.get( 0 );
				for (int i = 0; i < childEntriesByColumn.size() - 1; i++)
				{
					ArrayList<ChildEntry> colJ = childEntriesByColumn.get( i + 1 );
					double rightEdgeI = getColumnXEdge( colI, true );
					double leftEdgeJ = getColumnXEdge( colJ, false );
					
					double midPoint = ( rightEdgeI + leftEdgeJ ) * 0.5;
					
					if ( localPos.x < midPoint )
					{
						closestColumn = colI;
						break;
					}
				}
				
				if ( closestColumn == null )
				{
					closestColumn = childEntriesByColumn.get( childEntriesByColumn.size() - 1 );
				}
			}
			
			
			
			// Now determine which child entry is the closest
			if ( closestColumn.size() == 1 )
			{
				// One entry; only 1 choice
				return closestColumn.get( 0 );
			}
			else if ( closestColumn.size() == 2 )
			{
				ChildEntry entryI = closestColumn.get( 0 );
				ChildEntry entryJ = closestColumn.get( closestColumn.size() - 1 );
				double iUpperY = entryI.pos.y + entryI.size.y;
				double jLowerY = entryJ.pos.y;
					
				double midY = ( iUpperY + jLowerY ) * 0.5;
				
				return localPos.y < midY  ?  entryI  :  entryJ;
			}
			else
			{
				throw new RuntimeException();
			}
		}
	}

	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
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
	
	protected double getSpacing()
	{
		return ((ScriptStyleSheet)styleSheet).getSpacing();
	}

	protected double getScriptSpacing()
	{
		return ((ScriptStyleSheet)styleSheet).getScriptSpacing();
	}
}
