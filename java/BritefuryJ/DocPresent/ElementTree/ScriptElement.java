//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPScript;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ScriptStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;

public class ScriptElement extends BranchElement
{
	public static class CouldNotFindChildException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	};
	
	
	public static int LEFTSUPER = DPScript.LEFTSUPER;
	public static int LEFTSUB = DPScript.LEFTSUB;
	public static int MAIN = DPScript.MAIN;
	public static int RIGHTSUPER = DPScript.RIGHTSUPER;
	public static int RIGHTSUB = DPScript.RIGHTSUB;
	
	public static int NUMCHILDREN = DPScript.NUMCHILDREN;
	
	
	protected ParagraphStyleSheet segmentParagraphStyleSheet;
	protected TextStyleSheet segmentTextStyleSheet; 
	protected Element children[];
	protected SegmentElement segments[];
	
	
	
	public ScriptElement()
	{
		this( ScriptStyleSheet.defaultStyleSheet, ParagraphStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet );
	}
	
	public ScriptElement(ScriptStyleSheet styleSheet)
	{
		this( styleSheet, ParagraphStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet );
	}
	
	public ScriptElement(ScriptStyleSheet styleSheet, ParagraphStyleSheet segmentParagraphStyleSheet, TextStyleSheet segmentTextStyleSheet)
	{
		super( new DPScript( styleSheet ) );
		
		this.segmentParagraphStyleSheet = segmentParagraphStyleSheet;
		this.segmentTextStyleSheet = segmentTextStyleSheet;
		children = new Element[NUMCHILDREN];
		segments = new SegmentElement[NUMCHILDREN];
	}




	public Element getChild(int slot)
	{
		return children[slot];
	}
	
	public void setChild(int slot, Element child)
	{
		Element existingChild = children[slot];
		if ( child != existingChild )
		{
			boolean bSegmentRequired = child != null;
			boolean bSegmentPresent = existingChild != null;
			
			if ( bSegmentRequired  &&  !bSegmentPresent )
			{
				SegmentElement seg = new SegmentElement( segmentParagraphStyleSheet, segmentTextStyleSheet, isBeginGuardRequired( slot ), isEndGuardRequired( slot ) );
				seg.setParent( this );
				seg.setElementTree( tree );
				segments[slot] = seg;
				DPWidget segmentWidget = seg.getWidget();
				getWidget().setChild( slot, segmentWidget );
			}
			
			children[slot] = child;
			if ( child != null )
			{
				segments[slot].setChild( child );
			}

			if ( bSegmentPresent  &&  !bSegmentRequired )
			{
				
				SegmentElement seg = segments[slot];
				seg.setParent( null );
				seg.setElementTree( null );
				segments[slot] = null;
				getWidget().setChild( slot, null );
			}
			
			refreshSegmentGuards();
			
			onChildListChanged();
		}
	}
	
	
	
	
	public Element getMainChild()
	{
		return getChild( MAIN );
	}
	
	public Element getLeftSuperscriptChild()
	{
		return getChild( LEFTSUPER );
	}
	
	public Element getLeftSubscriptChild()
	{
		return getChild( LEFTSUB );
	}
	
	public Element getRightSuperscriptChild()
	{
		return getChild( RIGHTSUPER );
	}
	
	public Element getRightSubscriptChild()
	{
		return getChild( RIGHTSUB );
	}
	
	
	public void setMainChild(Element child)
	{
		setChild( MAIN, child );
	}
	
	public void setLeftSuperscriptChild(Element child)
	{
		setChild( LEFTSUPER, child );
	}
	
	public void setLeftSubscriptChild(Element child)
	{
		setChild( LEFTSUB, child );
	}
	
	public void setRightSuperscriptChild(Element child)
	{
		setChild( RIGHTSUPER, child );
	}
	
	public void setRightSubscriptChild(Element child)
	{
		setChild( RIGHTSUB, child );
	}



	public DPScript getWidget()
	{
		return (DPScript)widget;
	}


	public List<Element> getChildren()
	{
		ArrayList<Element> xs = new ArrayList<Element>();
		
		for (int slot = 0; slot < NUMCHILDREN; slot++)
		{
			if ( segments[slot] != null )
			{
				xs.add( segments[slot] );
			}
		}
		
		return xs;
	}




	//
	// Content methods
	//
	
	public String getContent()
	{
		StringBuilder builder = new StringBuilder();
		
		for (int slot = 0; slot < NUMCHILDREN; slot++)
		{
			if ( segments[slot] != null )
			{
				builder.append( segments[slot].getContent() );
			}
		}
		
		return builder.toString();
	}
	
	public int getContentLength()
	{
		int length = 0;
		
		for (int slot = 0; slot < NUMCHILDREN; slot++)
		{
			if ( segments[slot] != null )
			{
				length += segments[slot].getContentLength();
			}
		}
		
		return length;
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
	
	
	private void refreshSegmentGuards()
	{
		if ( children[MAIN] != null )
		{
			segments[MAIN].setGuardPolicy( isBeginGuardRequired( MAIN ), isEndGuardRequired( MAIN ) );
		}
	}
}
