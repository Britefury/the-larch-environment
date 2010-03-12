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

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeScript;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.DocPresent.StyleParams.ScriptStyleParams;
import BritefuryJ.DocPresent.StyleParams.TextStyleParams;


public class DPScript extends DPContainer
{
	public static int LEFTSUPER = 0;
	public static int LEFTSUB = 1;
	public static int MAIN = 2;
	public static int RIGHTSUPER = 3;
	public static int RIGHTSUB = 4;
	
	public static int NUMCHILDREN = 5;
	
	private static double childScale = 0.7;
	
	
	
	protected DPElement[] children;
	protected DPSegment segs[];
	protected DPParagraph paras[];
	TextStyleParams segmentTextStyleParams;


	public DPScript()
	{
		this( ScriptStyleParams.defaultStyleSheet, TextStyleParams.defaultStyleParams);
	}
	
	public DPScript(ScriptStyleParams styleSheet, TextStyleParams segmentTextStyleParams)
	{
		super( styleSheet );
		
		layoutNode = new LayoutNodeScript( this );
		
		this.segmentTextStyleParams = segmentTextStyleParams;
		
		children = new DPElement[NUMCHILDREN];
		segs = new DPSegment[NUMCHILDREN];
		paras = new DPParagraph[NUMCHILDREN];
	}

	
	
	public DPElement getChild(int slot)
	{
		return children[slot];
	}
	
	public DPElement getWrappedChild(int slot)
	{
		return paras[slot];
	}
	
	public void setChild(int slot, DPElement child)
	{
		DPElement existingChild = children[slot];
		if ( child != existingChild )
		{
			boolean bSegmentRequired = child != null;
			boolean bSegmentPresent = existingChild != null;

			if ( bSegmentRequired  &&  !bSegmentPresent )
			{
				DPSegment seg = new DPSegment( (ContainerStyleParams)getStyleParams(), segmentTextStyleParams, isBeginGuardRequired( slot ), isEndGuardRequired( slot ) );
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
	
	
	
	public DPElement getMainChild()
	{
		return getChild( MAIN );
	}
	
	public DPElement getLeftSuperscriptChild()
	{
		return getChild( LEFTSUPER );
	}
	
	public DPElement getLeftSubscriptChild()
	{
		return getChild( LEFTSUB );
	}
	
	public DPElement getRightSuperscriptChild()
	{
		return getChild( RIGHTSUPER );
	}
	
	public DPElement getRightSubscriptChild()
	{
		return getChild( RIGHTSUB );
	}
	
	
	public DPElement getWrappedMainChild()
	{
		return getWrappedChild( MAIN );
	}
	
	public DPElement getWrappedLeftSuperscriptChild()
	{
		return getWrappedChild( LEFTSUPER );
	}
	
	public DPElement getWrappedLeftSubscriptChild()
	{
		return getWrappedChild( LEFTSUB );
	}
	
	public DPElement getWrappedRightSuperscriptChild()
	{
		return getWrappedChild( RIGHTSUPER );
	}
	
	public DPElement getWrappedRightSubscriptChild()
	{
		return getWrappedChild( RIGHTSUB );
	}
	
	
	public void setMainChild(DPElement child)
	{
		setChild( MAIN, child );
	}
	
	public void setLeftSuperscriptChild(DPElement child)
	{
		setChild( LEFTSUPER, child );
	}
	
	public void setLeftSubscriptChild(DPElement child)
	{
		setChild( LEFTSUB, child );
	}
	
	public void setRightSuperscriptChild(DPElement child)
	{
		setChild( RIGHTSUPER, child );
	}
	
	public void setRightSubscriptChild(DPElement child)
	{
		setChild( RIGHTSUB, child );
	}
	

	
	
	protected double getInternalChildScale(DPElement child)
	{
		return child == paras[MAIN]  ?  1.0  :  childScale;
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
	

	
	public static double getChildScale()
	{
		return childScale;
	}

	

	//
	// STYLESHEET METHODS
	//
	
	protected double getColumnSpacing()
	{
		return ((ScriptStyleParams) styleParams).getColumnSpacing();
	}

	protected double getRowSpacing()
	{
		return ((ScriptStyleParams) styleParams).getRowSpacing();
	}
}
