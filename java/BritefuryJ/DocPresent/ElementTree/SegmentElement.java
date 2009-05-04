//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;

public class SegmentElement extends BranchElement implements CollatedElementInterface
{
	
	//
	// Utility classes
	//
	
	public static class SegmentFilter implements ElementFilter
	{
		private SegmentElement segment;
		
		
		public SegmentFilter(SegmentElement seg)
		{
			segment = seg;
		}
		
		public boolean test(Element element)
		{
			return element.getSegment() == segment;
		}
	}

	
	
	private static class ParagraphCollationFilter implements CollatableBranchFilter
	{
		public boolean test(CollatableBranchElement branch)
		{
			return branch.isParagraph()  ||  branch.isProxy();
		}
	}

		
	
	protected TextStyleSheet textStyleSheet;
	protected boolean bGuardBegin, bGuardEnd;
	protected Element beginGuard, endGuard;
	private ElementCollator collator;
	protected Element child;
	
	
	//
	// Constructor
	//
	
	public SegmentElement(boolean bGuardBegin, boolean bGuardEnd)
	{
		this( ParagraphStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet, bGuardBegin, bGuardEnd );
	}

	public SegmentElement(ParagraphStyleSheet styleSheet, boolean bGuardBegin, boolean bGuardEnd)
	{
		this( styleSheet, TextStyleSheet.defaultStyleSheet, bGuardBegin, bGuardEnd );
	}

	public SegmentElement(ParagraphStyleSheet styleSheet, TextStyleSheet textStyleSheet, boolean bGuardBegin, boolean bGuardEnd)
	{
		super( new DPParagraph( styleSheet ) );
		this.textStyleSheet = textStyleSheet;
		this.bGuardBegin = bGuardBegin;
		this.bGuardEnd = bGuardEnd;
		
		collator = new ElementCollator( this );
	}
	
	
	
	
	public void setGuardPolicy(boolean bGuardBegin, boolean bGuardEnd)
	{
		if ( bGuardBegin != this.bGuardBegin  ||  bGuardEnd != this.bGuardEnd )
		{
			this.bGuardBegin = bGuardBegin;
			this.bGuardEnd = bGuardEnd;
			onSubtreeStructureChanged();
		}
	}
	
	
	
	//
	// Widget
	//
	
	public DPParagraph getWidget()
	{
		return (DPParagraph)widget;
	}
	
	
	
	//
	// Container
	//
	
	public void setChild(Element child)
	{
		if ( child != this.child )
		{
			if ( this.child != null )
			{
				this.child.setParent( null );
				this.child.setElementTree( null );
			}
			this.child = child;
			if ( this.child != null )
			{
				this.child.setParent( this );
				this.child.setElementTree( tree );
			}
			
			onChildListChanged();
		}
	}
	
	public Element getChild()
	{
		return child;
	}

	
	
	public List<Element> getChildren()
	{
		ArrayList<Element> ch = new ArrayList<Element>();
		
		if ( beginGuard != null )
		{
			ch.add( beginGuard );
		}
		if ( child != null )
		{
			ch.add( child );
		}
		if ( endGuard != null )
		{
			ch.add( endGuard );
		}
		
		return ch;
	}

	
	
	//
	// Collation methods
	//
	
	private void refreshGuards()
	{
		boolean bBegin = false, bEnd = false;
		
		if ( child != null )
		{
			Element firstLeaf = child.getFirstLeafInSubtree();
			Element lastLeaf = child.getLastLeafInSubtree();
			
			if ( firstLeaf != null  &&  lastLeaf != null )
			{
				bBegin = firstLeaf.getSegment() != this;
				bEnd = lastLeaf.getSegment() != this;
			}
		}
		
		if ( bGuardBegin )
		{
			if ( bBegin  &&  !( beginGuard instanceof TextElement ) )
			{
				beginGuard = new TextElement( textStyleSheet, "" );
				beginGuard.setParent( this );
				beginGuard.setElementTree( tree );
			}
			
			if ( !bBegin  &&  !( beginGuard instanceof WhitespaceElement ) )
			{
				beginGuard = new WhitespaceElement( "" );
				beginGuard.setParent( this );
				beginGuard.setElementTree( tree );
			}
		}
		else if ( beginGuard != null )
		{
			beginGuard.setParent( null );
			beginGuard.setElementTree( null );
			beginGuard = null;
		}
		
		
		if ( bGuardEnd )
		{
			if ( bEnd  &&  !( endGuard instanceof TextElement ) )
			{
				endGuard = new TextElement( textStyleSheet, "" );
				endGuard.setParent( this );
				endGuard.setElementTree( tree );
			}
			
			if ( !bEnd  &&  !( endGuard instanceof WhitespaceElement ) )
			{
				endGuard = new WhitespaceElement( "" );
				endGuard.setParent( this );
				endGuard.setElementTree( tree );
			}
		}
		else if ( endGuard != null )
		{
			endGuard.setParent( null );
			endGuard.setElementTree( null );
			endGuard = null;
		}
	}

	public void collateSubtree(List<Element> childElementsOut, List<CollatableBranchElement> collatedBranchesOut)
	{
		CollatableBranchFilter collationFilter = new ParagraphCollationFilter();
		if ( beginGuard != null )
		{
			childElementsOut.add( beginGuard );
		}
		if ( child != null )
		{
			if ( child.isCollatableBranch()  &&  collationFilter.test( (CollatableBranchElement)child ) )
			{
				CollatableBranchElement b = (CollatableBranchElement)child;
				collatedBranchesOut.add( b );
				b.collateSubtree( childElementsOut, collatedBranchesOut, collationFilter );
			}
			else
			{
				childElementsOut.add( child );
			}
		}
		if ( endGuard != null )
		{
			childElementsOut.add( endGuard );
		}
	}
	
	public void setCollatedContainerChildWidgets(List<DPWidget> childWidgets)
	{
		getWidget().setChildren( childWidgets );
	}
	
	
	protected void onSubtreeStructureChanged()
	{
		super.onSubtreeStructureChanged();
		
		refreshGuards();
		collator.refreshContainerWidgetContents();
	}
	
	
	
	
	//
	// Text representation methods
	//
	
	protected String computeSubtreeTextRepresentation()
	{
		StringBuilder builder = new StringBuilder();
		List<Element> ch = getChildren();
		if ( ch.size() > 0 )
		{
			for (Element child: ch)
			{
				builder.append( child.getTextRepresentation() );
			}
		}
		return builder.toString();
	}
	
	
	
	
	
	public SegmentElement getSegment()
	{
		return this;
	}

	
	protected boolean isSegment()
	{
		return true;
	}
}
