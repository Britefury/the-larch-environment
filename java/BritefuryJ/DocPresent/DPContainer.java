//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.LayoutTree.BranchLayoutNode;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;




public abstract class DPContainer extends DPWidget
{
	public static class CouldNotFindChildException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected ArrayList<DPWidget> registeredChildren;				// Replace with array; operations like insert etc are hardly used at all
	public String cachedTextRep;								// Move to 'waypoint' element
	
	
	
	
	//
	// Constructors
	//
	
	public DPContainer()
	{
		this( ContainerStyleSheet.defaultStyleSheet );
	}

	public DPContainer(ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
		
		registeredChildren = new ArrayList<DPWidget>();
		cachedTextRep = null;
	}
	
	
	
	//
	// Geometry methods
	//
	
	protected double getInternalChildScale(DPWidget child)
	{
		return 1.0;
	}
	

	
	
	
	//
	// Child registration methods
	//
	
	protected DPWidget registerChild(DPWidget child)
	{
		child.unparent();
		
		child.setParent( this, presentationArea );
		
		if ( isRealised() )
		{
			child.handleRealise();
		}
		
		return child;
	}
	
	protected void unregisterChild(DPWidget child)
	{
		if ( isRealised() )
		{
			child.handleUnrealise( child );
		}
		
		child.setParent( null, null );
	}
	
	
	protected void onChildListModified()
	{
		onSubtreeStructureChanged();
		refreshMetaElement();
		
		BranchLayoutNode branchLayout = (BranchLayoutNode)getValidLayoutNodeOfClass( BranchLayoutNode.class );
		if ( branchLayout != null )
		{
			branchLayout.onChildListModified();
		}
	}
	
	
	
	
	
	//
	// Tree structure methods
	//
	
	
	public int computeSubtreeSize()
	{
		int subtreeSize = 1;
		for (DPWidget child: registeredChildren)
		{
			subtreeSize += child.computeSubtreeSize();
		}
		return subtreeSize;
	}
	
	
	protected abstract void replaceChildWithEmpty(DPWidget child);
	
	public boolean hasChild(DPWidget c)
	{
		for (DPWidget child: registeredChildren)
		{
			if ( c == child )
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	protected List<DPWidget> getInternalChildren()
	{
		return registeredChildren;
	}
	
	public List<DPWidget> getLayoutChildren()
	{
		return registeredChildren;
	}
	
	public abstract List<DPWidget> getChildren();

	
	public boolean areChildrenInOrder(DPWidget child0, DPWidget child1)
	{
		List<DPWidget> children = getInternalChildren();
		int index0 = children.indexOf( child0 );
		int index1 = children.indexOf( child1 );
		
		if ( index0 != -1  &&  index1 != -1 )
		{
			return index0 < index1;
		}
		else
		{
			throw new CouldNotFindChildException();
		}
	}
	
	
	
	
	
	protected void onSubtreeStructureChanged()
	{
		cachedTextRep = null;
		
		if ( parent != null )
		{
			parent.onSubtreeStructureChanged();
		}
	}
	
	
	
	
	//
	//
	// SELECTION METHODS
	//
	//
	
	protected void drawSubtreeSelection(Graphics2D graphics, Marker startMarker, List<DPWidget> startPath, Marker endMarker, List<DPWidget> endPath)
	{
		List<DPWidget> children = getInternalChildren();
		
		int startIndex = startMarker != null  ?  children.indexOf( startPath.get( 1 ) )  :  0;
		int endIndex = endMarker != null  ?  children.indexOf( endPath.get( 1) )  :  children.size() - 1;
		
		for (int i = startIndex; i <= endIndex; i++)
		{
			if ( i == startIndex  &&  startMarker != null )
			{
				children.get( i ).drawSubtreeSelection( graphics, startMarker, startPath.subList( 1, startPath.size() ), null, null );
			}
			else if ( i == endIndex  &&  endMarker != null )
			{
				children.get( i ).drawSubtreeSelection( graphics, null, null, endMarker, endPath.subList( 1, endPath.size() ) );
			}
			else
			{
				children.get( i ).drawSubtreeSelection( graphics, null, null, null, null );
			}
		}
	}
	
	
	
	
	
	//
	// Event handling methods
	//
	
	protected void onLeaveIntoChild(PointerMotionEvent event, DPWidget child)
	{
	}
	
	protected void onEnterFromChild(PointerMotionEvent event, DPWidget child)
	{
	}
	
	
	
	
	
	protected void childRedrawRequest(DPWidget child, Point2 childPos, Vector2 childSize)
	{
		Xform2 childToContainer = child.getLocalToParentXform();
		Point2 localPos = childToContainer.transform( childPos );
		Vector2 localSize = childToContainer.transform( childSize );
		queueRedraw( localPos, localSize );
	}
	
	
	
	protected DPWidget getFirstChildAtLocalPoint(Point2 localPos)
	{
		for (DPWidget child: registeredChildren)
		{
			if ( child.containsParentSpacePoint( localPos ) )
			{
				return child;
			}
		}
		
		return null;
	}
	
	protected DPWidget getLastChildAtLocalPoint(Point2 localPos)
	{
		for (int i = registeredChildren.size() - 1; i >= 0; i--)
		{
			DPWidget child = registeredChildren.get( i );
			if ( child.containsParentSpacePoint( localPos ) )
			{
				return child;
			}
		}
		
		return null;
	}
	

	protected DPWidget getFirstLowestChildAtLocalPoint(Point2 localPos)
	{
		DPWidget x = this;
		Point2 p = localPos;
		
		while ( true )
		{
			DPWidget child = x.getFirstChildAtLocalPoint( p );
			if ( child != null )
			{
				p = child.getParentToLocalXform().transform( p );
				x = child;
			}
			else
			{
				break;
			}
		}
		
		return x;
	}
	
	protected DPWidget getLastLowestChildAtLocalPoint(Point2 localPos)
	{
		DPWidget x = this;
		Point2 p = localPos;
		
		while ( true )
		{
			DPWidget child = x.getLastChildAtLocalPoint( p );
			if ( child != null )
			{
				p = child.getParentToLocalXform().transform( p );
				x = child;
			}
			else
			{
				break;
			}
		}
		
		return x;
	}
	
	
	
	
	//
	//
	// DRAG AND DROP METHODS
	//
	//
	
	public PointerInputElement getDndElement(Point2 localPos, Point2 targetPos[])
	{
		DPWidget child = getFirstChildAtLocalPoint( localPos );
		if ( child != null )
		{
			PointerInputElement element = child.getDndElement( child.getParentToLocalXform().transform( localPos ), targetPos );
			if ( element != null )
			{
				if ( targetPos != null )
				{
					targetPos[0] = child.getLocalToParentXform().transform( targetPos[0] );
				}
				return element;
			}
		}
		
		return super.getDndElement( localPos, targetPos );
	}

	
	
	
	
	
	
	//
	// Regular events
	//
	
	
	protected void handleRealise()
	{
		super.handleRealise();
		for (DPWidget child: registeredChildren)
		{
			child.handleRealise();
		}
	}
	
	protected void handleUnrealise(DPWidget unrealiseRoot)
	{
		for (DPWidget child: registeredChildren)
		{
			child.handleUnrealise( unrealiseRoot );
		}
		super.handleUnrealise( unrealiseRoot );
	}
	
	
	
	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
		super.handleDrawBackground( graphics, areaBox );
		
		AffineTransform currentTransform = graphics.getTransform();
		for (DPWidget child: registeredChildren)
		{
			if ( child.getAABoxInParentSpace().intersects( areaBox ) )
			{
				child.getLocalToParentXform().apply( graphics );
				child.handleDrawBackground( graphics, child.getParentToLocalXform().transform( areaBox ) );
				graphics.setTransform( currentTransform );
			}
		}
	}
	
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		super.handleDraw( graphics, areaBox );
		
		AffineTransform currentTransform = graphics.getTransform();
		for (DPWidget child: registeredChildren)
		{
			if ( child.getAABoxInParentSpace().intersects( areaBox ) )
			{
				child.getLocalToParentXform().apply( graphics );
				child.handleDraw( graphics, child.getParentToLocalXform().transform( areaBox ) );
				graphics.setTransform( currentTransform );
			}
		}
	}
	
	
	
	
	protected void setPresentationArea(DPPresentationArea area)
	{
		super.setPresentationArea( area );
		
		for (DPWidget child: registeredChildren)
		{
			child.setPresentationArea( area );
		}
	}


	
	
	//
	//
	// CONTENT LEAF METHODS
	//
	//

	public DPContentLeaf getFirstLeafInSubtree(WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( branchFilter == null  ||  branchFilter.testElement( this ) )
		{
			for (DPWidget child: getInternalChildren())
			{
				DPContentLeaf leaf = child.getFirstLeafInSubtree( branchFilter, leafFilter );
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

	public DPContentLeaf getLastLeafInSubtree(WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( branchFilter == null  ||  branchFilter.testElement( this ) )
		{
			List<DPWidget> children = getInternalChildren();
			for (int i = children.size() - 1; i >= 0; i--)
			{
				DPContentLeaf leaf = children.get( i ).getLastLeafInSubtree( branchFilter, leafFilter );
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
	
	

	
	protected DPContentLeaf getPreviousLeafFromChild(DPWidget child, WidgetFilter subtreeRootFilter, WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( subtreeRootFilter == null  ||  subtreeRootFilter.testElement( this ) )
		{
			List<DPWidget> children = getInternalChildren();
			int index = children.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index - 1; i >= 0; i--)
				{
					DPWidget e = children.get( i );
					DPContentLeaf l = e.getLastLeafInSubtree( branchFilter, leafFilter );
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
	
	protected DPContentLeaf getNextLeafFromChild(DPWidget child, WidgetFilter subtreeRootFilter, WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( subtreeRootFilter == null  ||  subtreeRootFilter.testElement( this ) )
		{
			List<DPWidget> children = getInternalChildren();
			int index = children.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index + 1; i < children.size(); i++)
				{
					DPWidget e = children.get( i );
					DPContentLeaf l = e.getFirstLeafInSubtree( branchFilter, leafFilter );
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
	

	
	//
	// CONTENT LEAF NAVIGATION METHODS
	//
	
	protected DPContentLeaf getContentLeafToLeftFromChild(DPWidget child)
	{
		BranchLayoutNode branchLayout = (BranchLayoutNode)getValidLayoutNodeOfClass( BranchLayoutNode.class );
		if ( branchLayout != null )
		{
			return branchLayout.getContentLeafToLeftFromChild( child );
		}
		else
		{
			return null;
		}
	}
	
	protected DPContentLeaf getContentLeafToRightFromChild(DPWidget child)
	{
		BranchLayoutNode branchLayout = (BranchLayoutNode)getValidLayoutNodeOfClass( BranchLayoutNode.class );
		if ( branchLayout != null )
		{
			return branchLayout.getContentLeafToRightFromChild( child );
		}
		else
		{
			return null;
		}
	}
	
	public DPContentLeaf getContentLeafAboveOrBelowFromChild(DPWidget child, boolean bBelow, Point2 localPos, boolean bSkipWhitespace)
	{
		BranchLayoutNode branchLayout = (BranchLayoutNode)getValidLayoutNodeOfClass( BranchLayoutNode.class );
		if ( branchLayout != null )
		{
			return branchLayout.getContentLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( branchLayout.getElement(), localPos ), bSkipWhitespace );
		}
		else
		{
			return null;
		}
	}
	
	
	//
	//
	// TEXT REPRESENTATION METHODS
	//
	//
	
	public DPContentLeaf getLeafAtTextRepresentationPosition(int position)
	{
		DPWidget c = getChildAtTextRepresentationPosition( position );
		
		if ( c != null )
		{
			return c.getLeafAtTextRepresentationPosition( position - getTextRepresentationOffsetOfChild( c ) );
		}
		else
		{
			return null;
		}
	}

	public DPWidget getChildAtTextRepresentationPosition(int position)
	{
		int offset = 0;
		for (DPWidget c: getInternalChildren())
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

	
	public int getTextRepresentationOffsetOfChild(DPWidget elem)
	{
		int offset = 0;
		for (DPWidget c: getInternalChildren())
		{
			if ( c == elem )
			{
				return offset;
			}
			offset += c.getTextRepresentationLength();
		}
		
		throw new DPContainer.CouldNotFindChildException();
	}
	
	protected int getChildTextRepresentationOffsetInSubtree(DPWidget child, DPContainer subtreeRoot)
	{
		return getTextRepresentationOffsetOfChild( child )  +  getTextRepresentationOffsetInSubtree( subtreeRoot );
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
	
	
	

	protected String computeSubtreeTextRepresentation()
	{
		StringBuilder builder = new StringBuilder();
		for (DPWidget child: getInternalChildren())
		{
			builder.append( child.getTextRepresentation() );
		}
		return builder.toString();
	}
	
	
	
	public void getTextRepresentationFromStartToPath(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
		DPWidget pathChild = path.get( pathMyIndex + 1 );
		for (DPWidget child: getInternalChildren())
		{
			if ( child != pathChild )
			{
				builder.append( child.getTextRepresentation() );
			}
			else
			{
				child.getTextRepresentationFromStartToPath( builder, marker, path, pathMyIndex + 1 );
				break;
			}
		}
	}
	
	public void getTextRepresentationFromPathToEnd(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
		List<DPWidget> children = getInternalChildren();
		int pathChildIndex = pathMyIndex + 1;
		DPWidget pathChild = path.get( pathChildIndex );
		int childIndex = children.indexOf( pathChild );
		
		pathChild.getTextRepresentationFromPathToEnd( builder, marker, path, pathChildIndex );

		if ( (childIndex + 1) < children.size() )
		{
			for (DPWidget child: children.subList( childIndex + 1, children.size() ))
			{
				builder.append( child.getTextRepresentation() );
			}
		}
	}

	public void getTextRepresentationBetweenPaths(StringBuilder builder, Marker startMarker, ArrayList<DPWidget> startPath, int startPathMyIndex,
			Marker endMarker, ArrayList<DPWidget> endPath, int endPathMyIndex)
	{
		List<DPWidget> children = getInternalChildren();
		
	
		int startPathChildIndex = startPathMyIndex + 1;
		int endPathChildIndex = endPathMyIndex + 1;
		
		DPWidget startChild = startPath.get( startPathChildIndex );
		DPWidget endChild = endPath.get( endPathChildIndex );
		
		int startIndex = children.indexOf( startChild );
		int endIndex = children.indexOf( endChild );
	
		
		startChild.getTextRepresentationFromPathToEnd( builder, startMarker, startPath, startPathChildIndex );
		
		for (int i = startIndex + 1; i < endIndex; i++)
		{
			builder.append( children.get( i ).getTextRepresentation() );
		}

		endChild.getTextRepresentationFromStartToPath( builder, endMarker, endPath, endPathChildIndex );
	}


	protected void getTextRepresentationFromStartOfRootToMarkerFromChild(StringBuilder builder, Marker marker, DPWidget root, DPWidget fromChild)
	{
		if ( root != this  &&  parent != null )
		{
			parent.getTextRepresentationFromStartOfRootToMarkerFromChild( builder, marker, root, this );
		}
		
		for (DPWidget child: getInternalChildren())
		{
			if ( child != fromChild )
			{
				builder.append( child.getTextRepresentation() );
			}
			else
			{
				break;
			}
		}
	}
	
	protected void getTextRepresentationFromMarkerToEndOfRootFromChild(StringBuilder builder, Marker marker, DPWidget root, DPWidget fromChild)
	{
		List<DPWidget> children = getInternalChildren();
		int childIndex = children.indexOf( fromChild );
		
		if ( (childIndex + 1) < children.size() )
		{
			for (DPWidget child: children.subList( childIndex + 1, children.size() ))
			{
				builder.append( child.getTextRepresentation() );
			}
		}

		if ( root != this  &&  parent != null )
		{
			parent.getTextRepresentationFromMarkerToEndOfRootFromChild( builder, marker, root, this );
		}
	}

	
	
	
	
	//
	//
	// LINEAR REPRESENTATION METHODS
	//
	//
	
	public void buildLinearRepresentation(ItemStreamBuilder builder)
	{
		for (DPWidget child: getInternalChildren())
		{
			child.appendToLinearRepresentation( builder );
		}
	}
	
	
	

	public void getLinearRepresentationFromStartToPath(ItemStreamBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
		DPWidget pathChild = path.get( pathMyIndex + 1 );
		for (DPWidget child: getInternalChildren())
		{
			if ( child != pathChild )
			{
				child.appendToLinearRepresentation( builder );
			}
			else
			{
				child.getLinearRepresentationFromStartToPath( builder, marker, path, pathMyIndex + 1 );
				break;
			}
		}
	}
	
	public void getLinearRepresentationFromPathToEnd(ItemStreamBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
		List<DPWidget> children = getInternalChildren();
		int pathChildIndex = pathMyIndex + 1;
		DPWidget pathChild = path.get( pathChildIndex );
		int childIndex = children.indexOf( pathChild );
		
		pathChild.getLinearRepresentationFromPathToEnd( builder, marker, path, pathChildIndex );

		if ( (childIndex + 1) < children.size() )
		{
			for (DPWidget child: children.subList( childIndex + 1, children.size() ))
			{
				child.appendToLinearRepresentation( builder );
			}
		}
	}

	public void getLinearRepresentationBetweenPaths(ItemStreamBuilder builder, Marker startMarker, ArrayList<DPWidget> startPath, int startPathMyIndex,
			Marker endMarker, ArrayList<DPWidget> endPath, int endPathMyIndex)
	{
		List<DPWidget> children = getInternalChildren();
		
	
		int startPathChildIndex = startPathMyIndex + 1;
		int endPathChildIndex = endPathMyIndex + 1;
		
		DPWidget startChild = startPath.get( startPathChildIndex );
		DPWidget endChild = endPath.get( endPathChildIndex );
		
		int startIndex = children.indexOf( startChild );
		int endIndex = children.indexOf( endChild );
	
		
		startChild.getLinearRepresentationFromPathToEnd( builder, startMarker, startPath, startPathChildIndex );
		
		for (int i = startIndex + 1; i < endIndex; i++)
		{
			children.get( i ).appendToLinearRepresentation( builder );
		}

		endChild.getLinearRepresentationFromStartToPath( builder, endMarker, endPath, endPathChildIndex );
	}


	protected void getLinearRepresentationFromStartOfRootToMarkerFromChild(ItemStreamBuilder builder, Marker marker, DPWidget root, DPWidget fromChild)
	{
		if ( root != this  &&  parent != null )
		{
			parent.getLinearRepresentationFromStartOfRootToMarkerFromChild( builder, marker, root, this );
		}
		
		appendStructuralPrefixToLinearRepresentation( builder );

		for (DPWidget child: getInternalChildren())
		{
			if ( child != fromChild )
			{
				child.appendToLinearRepresentation( builder );
			}
			else
			{
				break;
			}
		}
	}
	
	protected void getLinearRepresentationFromMarkerToEndOfRootFromChild(ItemStreamBuilder builder, Marker marker, DPWidget root, DPWidget fromChild)
	{
		List<DPWidget> children = getInternalChildren();
		int childIndex = children.indexOf( fromChild );
		
		if ( (childIndex + 1) < children.size() )
		{
			for (DPWidget child: children.subList( childIndex + 1, children.size() ))
			{
				child.appendToLinearRepresentation( builder );
			}
		}

		appendStructuralSuffixToLinearRepresentation( builder );

		if ( root != this  &&  parent != null )
		{
			parent.getLinearRepresentationFromMarkerToEndOfRootFromChild( builder, marker, root, this );
		}
	}

	
	
	
	
	//
	// Meta-element
	//
	
	static EmptyBorder metaIndentBorder = new EmptyBorder( 25.0, 0.0, 0.0, 0.0 );
	static VBoxStyleSheet metaVBoxStyle = new VBoxStyleSheet( 0.0 );
	
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
		for (DPWidget child: getChildren())
		{
			if ( child != null )
			{
				DPWidget metaChild = child.initialiseMetaElement();
				metaChildrenVBox.append( metaChild );
			}
			else
			{
				System.out.println( "DPContainer.createMetaElement(): null child in " + getClass().getName() );
			}
		}
		metaChildrenVBox.setRefPointIndex( getChildren().size() - 1 );
		
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
			for (DPWidget child: getChildren())
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
			for (DPWidget child: getChildren())
			{
				child.shutdownMetaElement();
			}
		}
		super.shutdownMetaElement();
	}

	
	
	
	
	//
	//
	// STYLESHEET METHODS
	//
	//
	
	public ContainerStyleSheet getStyleSheet()
	{
		return (ContainerStyleSheet)styleSheet;
	}
}
