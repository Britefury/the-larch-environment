//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.List;
import java.util.Vector;

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

	
	public static interface CaretStopElementFactory
	{
		public Element createCaretStopElement();
	}
	
	public static class EmptyTextElementStopFactory implements CaretStopElementFactory
	{
		private TextStyleSheet styleSheet;
		
		public EmptyTextElementStopFactory()
		{
			this( TextStyleSheet.defaultStyleSheet );
		}
		
		public EmptyTextElementStopFactory(TextStyleSheet styleSheet)
		{
			this.styleSheet = styleSheet;
		}

		public Element createCaretStopElement()
		{
			return new TextElement( styleSheet, "" );
		}
	}
	
	
	public static EmptyTextElementStopFactory defaultStopFactory = new EmptyTextElementStopFactory();
	
	
	protected CaretStopElementFactory stopFactory;
	protected Element beginStop, endStop, endWhitespace;
	private ElementCollator collator;
	protected Element child;
	
	
	//
	// Constructor
	//
	
	public SegmentElement(CaretStopElementFactory stopFactory)
	{
		this( ParagraphStyleSheet.defaultStyleSheet, stopFactory );
	}

	public SegmentElement(ParagraphStyleSheet styleSheet, CaretStopElementFactory stopFactory)
	{
		super( new DPParagraph( styleSheet ) );
		
		collator = new ElementCollator( this );
		
		this.stopFactory = stopFactory;
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
		Vector<Element> ch = new Vector<Element>();
		
		if ( beginStop != null )
		{
			ch.add( beginStop );
		}
		if ( child != null )
		{
			ch.add( child );
		}
		if ( endStop != null )
		{
			ch.add( endStop );
		}
		else if ( endWhitespace != null )
		{
			ch.add( endWhitespace );
		}
		
		return ch;
	}

	
	
	//
	// Collation methods
	//
	
	private void refreshStops()
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
		
		if ( bBegin  &&  beginStop == null )
		{
			beginStop = stopFactory.createCaretStopElement();
			beginStop.setParent( this );
			beginStop.setElementTree( tree );
		}
		else if ( !bBegin  &&  beginStop != null )
		{
			beginStop.setParent( null );
			beginStop.setElementTree( null );
			beginStop = null;
		}
		
		if ( bEnd )
		{
			if ( endStop == null )
			{
				endStop = stopFactory.createCaretStopElement();
				endStop.setParent( this );
				endStop.setElementTree( tree );
			}
			
			if ( endWhitespace != null )
			{
				endWhitespace.setParent( null );
				endWhitespace.setElementTree( null );
				endWhitespace = null;
			}
		}
		else
		{
			if ( endStop != null )
			{
				endStop.setParent( null );
				endStop.setElementTree( null );
				endStop = null;
			}

			if ( endWhitespace == null )
			{
				endWhitespace = new WhitespaceElement( "" );
				endWhitespace.setParent( this );
				endWhitespace.setElementTree( tree );
			}

		}
	}

	public void collateSubtree(List<Element> childElementsOut, List<CollatableBranchElement> collatedBranchesOut)
	{
		CollatableBranchFilter collationFilter = new ParagraphCollationFilter();
		if ( beginStop != null )
		{
			childElementsOut.add( beginStop );
		}
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
		if ( endStop != null )
		{
			childElementsOut.add( endStop );
		}
		else if ( endWhitespace != null )
		{
			childElementsOut.add( endWhitespace );
		}
	}
	
	public void setCollatedContainerChildWidgets(List<DPWidget> childWidgets)
	{
		getWidget().setChildren( childWidgets );
	}
	
	
	protected void onSubtreeStructureChanged()
	{
		super.onSubtreeStructureChanged();
		
		refreshStops();
		collator.refreshContainerWidgetContents();
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
