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

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public abstract class BranchElement extends Element
{
	public String cachedTextRep;
	
	//
	// Constructor
	//
	
	protected BranchElement(DPContainer widget)
	{
		super( widget );
		cachedTextRep = null;
	}


	
	//
	// Widget
	//
	
	public DPContainer getWidget()
	{
		return (DPContainer)widget;
	}
	
	
	
	//
	// Element tree and parent methods
	//
	
	protected void setElementTree(ElementTree tree)
	{
		if ( tree != this.tree )
		{
			super.setElementTree( tree );
			
			for (Element c: getChildren())
			{
				c.setElementTree( tree );
			}
		}
	}
	

	
	//
	// Element tree structure methods
	//
	
	protected void onChildListChanged()
	{
		onSubtreeStructureChanged();
		refreshMetaElement();
	}
	
	protected void onSubtreeStructureChanged()
	{
		cachedTextRep = null;
		
		if ( parent != null )
		{
			parent.onSubtreeStructureChanged();
		}
	}

	public abstract List<Element> getChildren();
	

	
	public List<LeafElement> getLeavesInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		ArrayList<LeafElement> leaves = new ArrayList<LeafElement>();

		if ( branchFilter == null  ||  branchFilter.test( this ) )
		{
			for (Element ch: getChildren())
			{
				leaves.addAll( ch.getLeavesInSubtree( branchFilter, leafFilter ) );
			}
		}

		return leaves;
	}
	
	public LeafElement getFirstLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( branchFilter == null  ||  branchFilter.test( this ) )
		{
			for (Element child: getChildren())
			{
				LeafElement leaf = child.getFirstLeafInSubtree( branchFilter, leafFilter );
				if ( leaf != null )
				{
					return leaf;
				}
			}
			return null;
		}
		else
		{
			return null;
		}
	}

	public LeafElement getLastLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( branchFilter == null  ||  branchFilter.test( this ) )
		{
			List<Element> children = getChildren();
			for (int i = children.size() - 1; i >= 0; i--)
			{
				LeafElement leaf = children.get( i ).getLastLeafInSubtree( branchFilter, leafFilter );
				if ( leaf != null )
				{
					return leaf;
				}
			}
			return null;
		}
		else
		{
			return null;
		}
	}
	
	
	
	//
	// Text representation methods
	//
	
	public LeafElement getLeafAtTextRepresentationPosition(int position)
	{
		Element c = getChildAtTextRepresentationPosition( position );
		
		if ( c != null )
		{
			return c.getLeafAtTextRepresentationPosition( position - getTextRepresentationOffsetOfChild( c ) );
		}
		else
		{
			return null;
		}
	}

	public Element getChildAtTextRepresentationPosition(int position)
	{
		int offset = 0;
		for (Element c: getChildren())
		{
			int end = offset + c.getTextRepresentationLength();
			if ( position >= offset  &&  position < end )
			{
				return c;
			}
			offset = end;
		}
		
		return null;
	}

	
	protected LeafElement getPreviousLeafFromChild(Element child, ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( subtreeRootFilter == null  ||  subtreeRootFilter.test( this ) )
		{
			List<Element> children = getChildren();
			int index = children.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index - 1; i >= 0; i--)
				{
					Element e = children.get( i );
					LeafElement l = e.getLastLeafInSubtree( branchFilter, leafFilter );
					if ( l != null )
					{
						return l;
					}
				}
			}
			
			if ( parent != null )
			{
				return parent.getPreviousLeafFromChild( this, subtreeRootFilter, branchFilter, leafFilter );
			}
		}
		
		return null;
	}
	
	protected LeafElement getNextLeafFromChild(Element child, ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( subtreeRootFilter == null  ||  subtreeRootFilter.test( this ) )
		{
			List<Element> children = getChildren();
			int index = children.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index + 1; i < children.size(); i++)
				{
					Element e = children.get( i );
					LeafElement l = e.getFirstLeafInSubtree( branchFilter, leafFilter );
					if ( l != null )
					{
						return l;
					}
				}
			}
		
			if ( parent != null )
			{
				return parent.getNextLeafFromChild( this, subtreeRootFilter, branchFilter, leafFilter );
			}
		}

		return null;
	}
	

	
	public int getTextRepresentationOffsetOfChild(Element elem)
	{
		int offset = 0;
		for (Element c: getChildren())
		{
			if ( c == elem )
			{
				return offset;
			}
			offset += c.getTextRepresentationLength();
		}
		
		throw new DPContainer.CouldNotFindChildException();
	}
	
	protected int getChildTextRepresentationOffsetInSubtree(Element child, BranchElement subtreeRoot)
	{
		return getTextRepresentationOffsetOfChild( child )  +  getTextRepresentationOffsetInSubtree( subtreeRoot );
	}



	public SegmentElement getSegmentFromChild(Element element)
	{
		return getSegment();
	}
	
	
	protected boolean onChildTextRepresentationModifiedEvent(Element child)
	{
		return onTextRepresentationModifiedEvent();
	}
	
	
	public void onTextRepresentationModified()
	{
		cachedTextRep = null;
		super.onTextRepresentationModified();
	}
	
	
	public String getTextRepresentation()
	{
		if ( cachedTextRep == null )
		{
			cachedTextRep = computeSubtreeTextRepresentation();
		}
		return cachedTextRep;
	}
	
	public int getTextRepresentationLength()
	{
		return getTextRepresentation().length();
	}
	
	
	protected abstract String computeSubtreeTextRepresentation();
	
	
	
	protected abstract void getTextRepresentationBetweenPaths(StringBuilder builder, ElementMarker startMarker, ArrayList<Element> startPath, int startPathMyIndex,
			ElementMarker endMarker, ArrayList<Element> endPath, int endPathMyIndex);
	
	
	protected abstract void getTextRepresentationFromStartOfRootToMarkerFromChild(StringBuilder builder, ElementMarker marker, Element root, Element fromChild);
	protected abstract void getTextRepresentationFromMarkerToEndOfRootFromChild(StringBuilder builder, ElementMarker marker, Element root, Element fromChild);



	//
	// Meta-element
	//
	
	static EmptyBorder metaIndentBorder = new EmptyBorder( 25.0, 0.0, 0.0, 0.0 );
	static VBoxStyleSheet metaVBoxStyle = new VBoxStyleSheet( DPVBox.Typesetting.ALIGN_WITH_BOTTOM, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
	
	public DPBorder getMetaHeaderBorderWidget()
	{
		if ( metaElement != null )
		{
			DPVBox metaVBox = (DPVBox)metaElement;
			return (DPBorder)metaVBox.get( 0 );
		}
		else
		{
			return null;
		}
	}
	
	public DPWidget createMetaElement()
	{
		DPVBox metaChildrenVBox = new DPVBox( metaVBoxStyle );
		for (Element child: getChildren())
		{
			DPWidget metaChild = child.initialiseMetaElement();
			metaChildrenVBox.append( metaChild );
		}
		
		DPBorder indentMetaChildren = new DPBorder( metaIndentBorder );
		indentMetaChildren.setChild( metaChildrenVBox );
		
		DPVBox metaVBox = new DPVBox( metaVBoxStyle );
		metaVBox.append( createMetaHeader() );
		metaVBox.append( indentMetaChildren );
		
		return metaVBox;
	}
	
	public void refreshMetaElement()
	{
		if ( metaElement != null )
		{
			DPVBox metaVBox = (DPVBox)metaElement;
			
			DPBorder indentMetaChildren = (DPBorder)metaVBox.get( 1 );
			DPVBox metaChildrenVBox = (DPVBox)indentMetaChildren.getChild();

			ArrayList<DPWidget> childMetaElements = new ArrayList<DPWidget>();
			for (Element child: getChildren())
			{
				DPWidget metaChild = child.initialiseMetaElement();
				childMetaElements.add( metaChild );
			}
			metaChildrenVBox.setChildren( childMetaElements );
		}
	}

	public void shutdownMetaElement()
	{
		if ( metaElement != null )
		{
			for (Element child: getChildren())
			{
				child.shutdownMetaElement();
			}
		}
		super.shutdownMetaElement();
	}

	
	
	
	//
	// Element type methods
	//
	
	protected boolean isBranch()
	{
		return true;
	}
}
