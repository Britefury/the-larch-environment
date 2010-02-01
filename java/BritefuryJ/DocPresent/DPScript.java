//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeScript;
import BritefuryJ.DocPresent.StyleSheets.ScriptStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;


public class DPScript extends DPContainer
{
	public static int LEFTSUPER = 0;
	public static int LEFTSUB = 1;
	public static int MAIN = 2;
	public static int RIGHTSUPER = 3;
	public static int RIGHTSUB = 4;
	
	public static int NUMCHILDREN = 5;
	
	private static double childScale = 0.7;
	
	
	
	protected DPWidget[] children;
	protected DPSegment segs[];
	protected DPParagraph paras[];
	TextStyleSheet segmentTextStyleSheet;
	
	
	
	public DPScript(ElementContext context)
	{
		this( context, ScriptStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet );
	}
	
	public DPScript(ElementContext context, ScriptStyleSheet styleSheet, TextStyleSheet segmentTextStyleSheet)
	{
		super( context, styleSheet );
		
		layoutNode = new LayoutNodeScript( this );
		
		this.segmentTextStyleSheet = segmentTextStyleSheet;
		
		children = new DPWidget[NUMCHILDREN];
		segs = new DPSegment[NUMCHILDREN];
		paras = new DPParagraph[NUMCHILDREN];
	}

	
	
	public DPWidget getChild(int slot)
	{
		return children[slot];
	}
	
	public DPWidget getWrappedChild(int slot)
	{
		return paras[slot];
	}
	
	public void setChild(int slot, DPWidget child)
	{
		DPWidget existingChild = children[slot];
		if ( child != existingChild )
		{
			boolean bSegmentRequired = child != null;
			boolean bSegmentPresent = existingChild != null;

			if ( bSegmentRequired  &&  !bSegmentPresent )
			{
				DPSegment seg = new DPSegment( context, segmentTextStyleSheet, isBeginGuardRequired( slot ), isEndGuardRequired( slot ) );
				segs[slot] = seg;
				DPParagraph para = new DPParagraph( context );
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
			
			
			refreshSegmentGuards();				

			
			onChildListModified();
			queueResize();
		}
	}
	
	
	private void refreshSegmentGuards()
	{
		if ( children[MAIN] != null )
		{
			segs[MAIN].setGuardPolicy( isBeginGuardRequired( MAIN ), isEndGuardRequired( MAIN ) );
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
	
	
	public DPWidget getWrappedMainChild()
	{
		return getWrappedChild( MAIN );
	}
	
	public DPWidget getWrappedLeftSuperscriptChild()
	{
		return getWrappedChild( LEFTSUPER );
	}
	
	public DPWidget getWrappedLeftSubscriptChild()
	{
		return getWrappedChild( LEFTSUB );
	}
	
	public DPWidget getWrappedRightSuperscriptChild()
	{
		return getWrappedChild( RIGHTSUPER );
	}
	
	public DPWidget getWrappedRightSubscriptChild()
	{
		return getWrappedChild( RIGHTSUB );
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
	

	
	
	protected double getInternalChildScale(DPWidget child)
	{
		return child == paras[MAIN]  ?  1.0  :  childScale;
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

	
	
	
	
	protected boolean hasMainChild()
	{
		return children[MAIN] != null;
	}
	
	protected boolean hasLeftChild()
	{
		return children[LEFTSUB] != null  ||  children[LEFTSUPER] != null;
	}
	
	protected boolean hasRightChild()
	{
		return children[RIGHTSUB] != null  ||  children[RIGHTSUPER] != null;
	}
	
	protected boolean hasSuperscriptChild()
	{
		return children[LEFTSUPER] != null  ||  children[RIGHTSUPER] != null;
	}
	
	protected boolean hasSubscriptChild()
	{
		return children[LEFTSUB] != null  ||  children[RIGHTSUB] != null;
	}
	
	
	private boolean isBeginGuardRequired(int slot)
	{
		if ( slot == MAIN )
		{
			return hasLeftChild();
		}
		else
		{
			return true;
		}
	}

	private boolean isEndGuardRequired(int slot)
	{
		if ( slot == MAIN )
		{
			return hasRightChild();
		}
		else
		{
			return true;
		}
	}
	

	
	//
	// Packing parameters
	//
	
	protected PackingParams getDefaultPackingParams()
	{
		return null;
	}

	
	
	public static double getChildScale()
	{
		return childScale;
	}

	

	//
	// STYLESHEET METHODS
	//
	
	protected double getColumnSpacing()
	{
		return ((ScriptStyleSheet)styleSheet).getColumnSpacing();
	}

	protected double getRowSpacing()
	{
		return ((ScriptStyleSheet)styleSheet).getRowSpacing();
	}
}
