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

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.LayoutTree.BranchLayoutNode;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.DocPresent.StyleParams.VBoxStyleParams;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Xform2;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;




public abstract class DPContainer extends DPElement
{
	public static class CouldNotFindChildException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected final static int FLAGS_CONTAINER_END = FLAGS_ELEMENT_END;

	
	
	protected ArrayList<DPElement> registeredChildren = new ArrayList<DPElement>();			// Replace with array; operations like insert etc are hardly used at all
	public String cachedTextRep = null;
	
	
	
	
	//
	// Constructors
	//
	
	public DPContainer()
	{
		this( ContainerStyleParams.defaultStyleParams );
	}

	public DPContainer(ContainerStyleParams styleParams)
	{
		super(styleParams);
	}
	
	protected DPContainer(DPContainer element)
	{
		super( element );
	}
	
	
	
	//
	// Geometry methods
	//
	
	protected Xform2 getAllocationSpaceToLocalSpaceXform(DPElement child)
	{
		return Xform2.identity;
	}
	

	
	
	
	//
	// Child registration methods
	//
	
	protected DPElement registerChild(DPElement child)
	{
		child.unparent();
		
		child.setParent( this, rootElement );
		
		if ( isRealised() )
		{
			child.handleRealise();
		}
		
		return child;
	}
	
	protected void unregisterChild(DPElement child)
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
		onDebugPresentationStateChanged();
		
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
		for (DPElement child: registeredChildren)
		{
			subtreeSize += child.computeSubtreeSize();
		}
		return subtreeSize;
	}
	
	
	protected abstract void replaceChildWithEmpty(DPElement child);
	
	public boolean hasChild(DPElement c)
	{
		for (DPElement child: registeredChildren)
		{
			if ( c == child )
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	protected List<DPElement> getInternalChildren()
	{
		return registeredChildren;
	}
	
	public List<DPElement> getLayoutChildren()
	{
		return registeredChildren;
	}
	
	public abstract List<DPElement> getChildren();

	
	public boolean areChildrenInOrder(DPElement child0, DPElement child1)
	{
		List<DPElement> children = getInternalChildren();
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
	// LAYOUT METHODS
	//
	//
	
	public int getParagraphLinebreakCostModifier()
	{
		return 0;
	}
	
	
	
	
	//
	//
	// MARKER METHODS
	//
	//
	
	public Marker markerAtStart()
	{
		for (DPElement child: getChildren())
		{
			Marker m = child.markerAtStart();
			if ( m != null )
			{
				return m;
			}
		}
		
		return super.markerAtStart();
	}
	
	public Marker markerAtEnd()
	{
		List<DPElement> children = getChildren();
		for (int i = children.size() - 1; i >= 0; i--)
		{
			DPElement child = children.get( i );
			Marker m = child.markerAtEnd();
			if ( m != null )
			{
				return m;
			}
		}
		
		return super.markerAtEnd();
	}
	
	
	public void moveMarkerToStart(Marker m)
	{
		m.moveTo( markerAtStart() );
	}
	
	public void moveMarkerToEnd(Marker m)
	{
		m.moveTo( markerAtEnd() );
	}
	
	
	
	//
	//
	// SELECTION METHODS
	//
	//
	
	protected void drawSubtreeSelection(Graphics2D graphics, Marker startMarker, List<DPElement> startPath, Marker endMarker, List<DPElement> endPath)
	{
		List<DPElement> children = getInternalChildren();
		
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
	
	protected void onLeaveIntoChild(PointerMotionEvent event, DPElement child)
	{
	}
	
	protected void onEnterFromChild(PointerMotionEvent event, DPElement child)
	{
	}
	
	
	
	
	
	protected void childRedrawRequest(DPElement child, AABox2 childBox)
	{
		Xform2 childToContainer = child.getLocalToParentXform();
		AABox2 localBox = childToContainer.transform( childBox );
		AABox2 clipBox = getLocalClipBox();
		if ( clipBox != null )
		{
			localBox = localBox.intersection( getLocalAABox() );
		}
		if ( !localBox.isEmpty() )
		{
			queueRedraw( localBox );
		}
	}
	
	
	
	protected DPElement getFirstChildAtLocalPoint(Point2 localPos)
	{
		for (DPElement child: registeredChildren)
		{
			if ( child.containsParentSpacePoint( localPos ) )
			{
				return child;
			}
		}
		
		return null;
	}
	
	protected DPElement getLastChildAtLocalPoint(Point2 localPos)
	{
		for (int i = registeredChildren.size() - 1; i >= 0; i--)
		{
			DPElement child = registeredChildren.get( i );
			if ( child.containsParentSpacePoint( localPos ) )
			{
				return child;
			}
		}
		
		return null;
	}
	

	protected DPElement getFirstLowestChildAtLocalPoint(Point2 localPos)
	{
		DPElement x = this;
		Point2 p = localPos;
		
		while ( true )
		{
			DPElement child = x.getFirstChildAtLocalPoint( p );
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
	
	protected DPElement getLastLowestChildAtLocalPoint(Point2 localPos)
	{
		DPElement x = this;
		Point2 p = localPos;
		
		while ( true )
		{
			DPElement child = x.getLastChildAtLocalPoint( p );
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
		DPElement child = getFirstChildAtLocalPoint( localPos );
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
		for (DPElement child: registeredChildren)
		{
			child.handleRealise();
		}
	}
	
	protected void handleUnrealise(DPElement unrealiseRoot)
	{
		for (DPElement child: registeredChildren)
		{
			child.handleUnrealise( unrealiseRoot );
		}
		super.handleUnrealise( unrealiseRoot );
	}
	
	
	
	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
		super.handleDrawBackground( graphics, areaBox );
		
		AABox2 clipBox = getLocalClipBox();
		if ( clipBox != null )
		{
			areaBox = areaBox.intersection( clipBox );
		}
		
		if ( !areaBox.isEmpty() )
		{
			AffineTransform currentTransform = graphics.getTransform();
			for (DPElement child: registeredChildren)
			{
				if ( child.getAABoxInParentSpace().intersects( areaBox ) )
				{
					child.getLocalToParentXform().apply( graphics );
					child.handleDrawBackground( graphics, child.getParentToLocalXform().transform( areaBox ) );
					graphics.setTransform( currentTransform );
				}
			}
		}
	}
	
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		super.handleDraw( graphics, areaBox );
		
		AABox2 clipBox = getLocalClipBox();
		if ( clipBox != null )
		{
			areaBox = areaBox.intersection( clipBox );
		}
		
		if ( !areaBox.isEmpty() )
		{
			AffineTransform currentTransform = graphics.getTransform();
			for (DPElement child: registeredChildren)
			{
				if ( child.getAABoxInParentSpace().intersects( areaBox ) )
				{
					child.getLocalToParentXform().apply( graphics );
					child.handleDraw( graphics, child.getParentToLocalXform().transform( areaBox ) );
					graphics.setTransform( currentTransform );
				}
			}
		}
	}
	
	
	
	
	protected void setRootElement(PresentationComponent.RootElement area)
	{
		super.setRootElement( area );
		
		for (DPElement child: registeredChildren)
		{
			child.setRootElement( area );
		}
	}


	
	
	//
	//
	// CONTENT LEAF METHODS
	//
	//

	public DPContentLeaf getFirstLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( branchFilter == null  ||  branchFilter.testElement( this ) )
		{
			for (DPElement child: getInternalChildren())
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

	public DPContentLeaf getLastLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( branchFilter == null  ||  branchFilter.testElement( this ) )
		{
			List<DPElement> children = getInternalChildren();
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
	
	

	
	protected DPContentLeaf getPreviousLeafFromChild(DPElement child, ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( subtreeRootFilter == null  ||  subtreeRootFilter.testElement( this ) )
		{
			List<DPElement> children = getInternalChildren();
			int index = children.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index - 1; i >= 0; i--)
				{
					DPElement e = children.get( i );
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
	
	protected DPContentLeaf getNextLeafFromChild(DPElement child, ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( subtreeRootFilter == null  ||  subtreeRootFilter.testElement( this ) )
		{
			List<DPElement> children = getInternalChildren();
			int index = children.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index + 1; i < children.size(); i++)
				{
					DPElement e = children.get( i );
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
	
	protected DPContentLeaf getContentLeafToLeftFromChild(DPElement child)
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
	
	protected DPContentLeaf getContentLeafToRightFromChild(DPElement child)
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
	
	public DPContentLeafEditable getEditableContentLeafAboveOrBelowFromChild(DPElement child, boolean bBelow, Point2 localPos)
	{
		BranchLayoutNode branchLayout = (BranchLayoutNode)getValidLayoutNodeOfClass( BranchLayoutNode.class );
		if ( branchLayout != null )
		{
			return branchLayout.getEditableContentLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( branchLayout.getElement(), localPos ) );
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
		DPElement c = getChildAtTextRepresentationPosition( position );
		
		if ( c != null )
		{
			return c.getLeafAtTextRepresentationPosition( position - getTextRepresentationOffsetOfChild( c ) );
		}
		else
		{
			return null;
		}
	}

	public DPElement getChildAtTextRepresentationPosition(int position)
	{
		int offset = 0;
		for (DPElement c: getInternalChildren())
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

	
	public int getTextRepresentationOffsetOfChild(DPElement elem)
	{
		int offset = 0;
		for (DPElement c: getInternalChildren())
		{
			if ( c == elem )
			{
				return offset;
			}
			offset += c.getTextRepresentationLength();
		}
		
		throw new DPContainer.CouldNotFindChildException();
	}
	
	protected int getChildTextRepresentationOffsetInSubtree(DPElement child, DPContainer subtreeRoot)
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
		for (DPElement child: getInternalChildren())
		{
			builder.append( child.getTextRepresentation() );
		}
		return builder.toString();
	}
	
	
	
	public void getTextRepresentationFromStartToPath(StringBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
		DPElement pathChild = path.get( pathMyIndex + 1 );
		for (DPElement child: getInternalChildren())
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
	
	public void getTextRepresentationFromPathToEnd(StringBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
		List<DPElement> children = getInternalChildren();
		int pathChildIndex = pathMyIndex + 1;
		DPElement pathChild = path.get( pathChildIndex );
		int childIndex = children.indexOf( pathChild );
		
		pathChild.getTextRepresentationFromPathToEnd( builder, marker, path, pathChildIndex );

		if ( (childIndex + 1) < children.size() )
		{
			for (DPElement child: children.subList( childIndex + 1, children.size() ))
			{
				builder.append( child.getTextRepresentation() );
			}
		}
	}

	public void getTextRepresentationBetweenPaths(StringBuilder builder, Marker startMarker, ArrayList<DPElement> startPath, int startPathMyIndex,
			Marker endMarker, ArrayList<DPElement> endPath, int endPathMyIndex)
	{
		List<DPElement> children = getInternalChildren();
		
	
		int startPathChildIndex = startPathMyIndex + 1;
		int endPathChildIndex = endPathMyIndex + 1;
		
		DPElement startChild = startPath.get( startPathChildIndex );
		DPElement endChild = endPath.get( endPathChildIndex );
		
		int startIndex = children.indexOf( startChild );
		int endIndex = children.indexOf( endChild );
	
		
		startChild.getTextRepresentationFromPathToEnd( builder, startMarker, startPath, startPathChildIndex );
		
		for (int i = startIndex + 1; i < endIndex; i++)
		{
			builder.append( children.get( i ).getTextRepresentation() );
		}

		endChild.getTextRepresentationFromStartToPath( builder, endMarker, endPath, endPathChildIndex );
	}


	protected void getTextRepresentationFromStartOfRootToMarkerFromChild(StringBuilder builder, Marker marker, DPElement root, DPElement fromChild)
	{
		if ( root != this  &&  parent != null )
		{
			parent.getTextRepresentationFromStartOfRootToMarkerFromChild( builder, marker, root, this );
		}
		
		for (DPElement child: getInternalChildren())
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
	
	protected void getTextRepresentationFromMarkerToEndOfRootFromChild(StringBuilder builder, Marker marker, DPElement root, DPElement fromChild)
	{
		List<DPElement> children = getInternalChildren();
		int childIndex = children.indexOf( fromChild );
		
		if ( (childIndex + 1) < children.size() )
		{
			for (DPElement child: children.subList( childIndex + 1, children.size() ))
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
		for (DPElement child: getInternalChildren())
		{
			child.appendToLinearRepresentation( builder );
		}
	}
	
	
	

	public void getLinearRepresentationFromStartToPath(ItemStreamBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
		DPElement pathChild = path.get( pathMyIndex + 1 );
		for (DPElement child: getInternalChildren())
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
	
	public void getLinearRepresentationFromPathToEnd(ItemStreamBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
		List<DPElement> children = getInternalChildren();
		int pathChildIndex = pathMyIndex + 1;
		DPElement pathChild = path.get( pathChildIndex );
		int childIndex = children.indexOf( pathChild );
		
		pathChild.getLinearRepresentationFromPathToEnd( builder, marker, path, pathChildIndex );

		if ( (childIndex + 1) < children.size() )
		{
			for (DPElement child: children.subList( childIndex + 1, children.size() ))
			{
				child.appendToLinearRepresentation( builder );
			}
		}
	}

	public void getLinearRepresentationBetweenPaths(ItemStreamBuilder builder, Marker startMarker, ArrayList<DPElement> startPath, int startPathMyIndex,
			Marker endMarker, ArrayList<DPElement> endPath, int endPathMyIndex)
	{
		List<DPElement> children = getInternalChildren();
		
	
		int startPathChildIndex = startPathMyIndex + 1;
		int endPathChildIndex = endPathMyIndex + 1;
		
		DPElement startChild = startPath.get( startPathChildIndex );
		DPElement endChild = endPath.get( endPathChildIndex );
		
		int startIndex = children.indexOf( startChild );
		int endIndex = children.indexOf( endChild );
	
		
		startChild.getLinearRepresentationFromPathToEnd( builder, startMarker, startPath, startPathChildIndex );
		
		for (int i = startIndex + 1; i < endIndex; i++)
		{
			children.get( i ).appendToLinearRepresentation( builder );
		}

		endChild.getLinearRepresentationFromStartToPath( builder, endMarker, endPath, endPathChildIndex );
	}


	protected void getLinearRepresentationFromStartOfRootToMarkerFromChild(ItemStreamBuilder builder, Marker marker, DPElement root, DPElement fromChild)
	{
		if ( root != this  &&  parent != null )
		{
			parent.getLinearRepresentationFromStartOfRootToMarkerFromChild( builder, marker, root, this );
		}
		
		appendStructuralPrefixToLinearRepresentation( builder );

		for (DPElement child: getInternalChildren())
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
	
	protected void getLinearRepresentationFromMarkerToEndOfRootFromChild(ItemStreamBuilder builder, Marker marker, DPElement root, DPElement fromChild)
	{
		List<DPElement> children = getInternalChildren();
		int childIndex = children.indexOf( fromChild );
		
		if ( (childIndex + 1) < children.size() )
		{
			for (DPElement child: children.subList( childIndex + 1, children.size() ))
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
	
	static FilledBorder metaIndentBorder = new FilledBorder( 25.0, 0.0, 0.0, 0.0 );
	static VBoxStyleParams metaVBoxStyle = new VBoxStyleParams( null, null, null, 0.0 );
	
	public DPElement createMetaElement(GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state)
	{
		DPVBox metaChildrenVBox = new DPVBox( metaVBoxStyle );
		for (DPElement child: getChildren())
		{
			if ( child != null )
			{
				DPElement metaChild = ctx.presentFragmentWithGenerixcPerspective( child.treeExplorer(), state ); 
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
		metaVBox.append( createDebugPresentationHeader() );
		metaVBox.append( indentMetaChildren );
		
		return metaVBox;
	}
}
