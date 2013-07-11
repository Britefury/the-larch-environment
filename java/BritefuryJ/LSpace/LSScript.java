//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeScript;
import BritefuryJ.LSpace.StyleParams.CaretSlotStyleParams;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.LSpace.StyleParams.ScriptStyleParams;


public class LSScript extends LSContainerNonOverlayed
{
	public static int LEFTSUPER = 0;
	public static int LEFTSUB = 1;
	public static int MAIN = 2;
	public static int RIGHTSUPER = 3;
	public static int RIGHTSUB = 4;
	
	public static int NUMCHILDREN = 5;
	
	
	
	protected LSElement[] children;
	protected LSSegment segs[];
	protected LSParagraph paras[];
	CaretSlotStyleParams segmentCaretSlotStyleParams;


	public LSScript(ScriptStyleParams styleSheet, CaretSlotStyleParams segmentCaretSlotStyleParams, LSElement leftSuper, LSElement leftSub, LSElement main, LSElement rightSuper, LSElement rightSub)
	{
		super( styleSheet );
		
		layoutNode = new LayoutNodeScript( this );
		
		this.segmentCaretSlotStyleParams = segmentCaretSlotStyleParams;
		
		children = new LSElement[NUMCHILDREN];
		segs = new LSSegment[NUMCHILDREN];
		paras = new LSParagraph[NUMCHILDREN];
		
		
		LSElement[] childElements = new LSElement[] { leftSuper, leftSub, main, rightSuper, rightSub };
		for (int slot = 0; slot < NUMCHILDREN; slot++)
		{
			LSElement child = childElements[slot];
			if ( child != null )
			{
				LSSegment seg = new LSSegment( (ContainerStyleParams)getStyleParams(), segmentCaretSlotStyleParams, isBeginGuardRequired( slot ), isEndGuardRequired( slot ), false, child );
				segs[slot] = seg;
				LSParagraph para = new LSParagraph( new LSElement[] { seg } );
				paras[slot] = para;
				
				registeredChildren.add( para );
				registerChild( para );
				
				children[slot] = child;
			}
		}
	}
	
	
	

	
	

	
	
	//
	//
	// Child access / modification
	//
	//
	
	public LSElement getChild(int slot)
	{
		return children[slot];
	}
	
	public LSElement getWrappedChild(int slot)
	{
		return paras[slot];
	}
	
	public void setChild(int slot, LSElement child)
	{
		LSElement existingChild = children[slot];
		if ( child != existingChild )
		{
			boolean bSegmentRequired = child != null;
			boolean bSegmentPresent = existingChild != null;

			if ( bSegmentRequired  &&  !bSegmentPresent )
			{
				LSSegment seg = new LSSegment( (ContainerStyleParams)getStyleParams(), segmentCaretSlotStyleParams,isBeginGuardRequired( slot ), isEndGuardRequired( slot ), false, child );
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
	
	
	
	public LSElement getMainChild()
	{
		return getChild( MAIN );
	}
	
	public LSElement getLeftSuperscriptChild()
	{
		return getChild( LEFTSUPER );
	}
	
	public LSElement getLeftSubscriptChild()
	{
		return getChild( LEFTSUB );
	}
	
	public LSElement getRightSuperscriptChild()
	{
		return getChild( RIGHTSUPER );
	}
	
	public LSElement getRightSubscriptChild()
	{
		return getChild( RIGHTSUB );
	}
	
	
	public LSElement getWrappedMainChild()
	{
		return getWrappedChild( MAIN );
	}
	
	public LSElement getWrappedLeftSuperscriptChild()
	{
		return getWrappedChild( LEFTSUPER );
	}
	
	public LSElement getWrappedLeftSubscriptChild()
	{
		return getWrappedChild( LEFTSUB );
	}
	
	public LSElement getWrappedRightSuperscriptChild()
	{
		return getWrappedChild( RIGHTSUPER );
	}
	
	public LSElement getWrappedRightSubscriptChild()
	{
		return getWrappedChild( RIGHTSUB );
	}
	
	
	public void setMainChild(LSElement child)
	{
		setChild( MAIN, child );
	}
	
	public void setLeftSuperscriptChild(LSElement child)
	{
		setChild( LEFTSUPER, child );
	}
	
	public void setLeftSubscriptChild(LSElement child)
	{
		setChild( LEFTSUB, child );
	}
	
	public void setRightSuperscriptChild(LSElement child)
	{
		setChild( RIGHTSUPER, child );
	}
	
	public void setRightSubscriptChild(LSElement child)
	{
		setChild( RIGHTSUB, child );
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
