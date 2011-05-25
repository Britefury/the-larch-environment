//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.StreamValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.TextSelection;

public abstract class AbstractStreamValueVisitor
{
	//
	// Stream value building
	//
	
	public StreamValue getStreamValue(DPElement root)
	{
		StreamValueBuilder builder = new StreamValueBuilder();
		buildStreamValue( root, builder );
		return builder.stream();
	}
	
	public StreamValue getStreamValueFromStartToMarker(DPElement root, Marker marker)
	{
		StreamValueBuilder builder = new StreamValueBuilder();
		DPContentLeafEditable leaf = marker.getElement();
		buildStreamValueFromStartOfRootToMarker( builder, leaf, marker, root );
		return builder.stream();
	}
	
	public StreamValue getStreamValueFromMarkerToEnd(DPElement root, Marker marker)
	{
		StreamValueBuilder builder = new StreamValueBuilder();
		DPContentLeafEditable leaf = marker.getElement();
		buildStreamValueFromMarkerToEndOfRoot( builder, leaf, marker, root );
		return builder.stream();
	}
	
	public StreamValue getStreamValueInTextSelection(TextSelection s)
	{
		DPContainer commonRoot = s.getCommonRootContainer();
		
		if ( commonRoot != null )
		{
			ArrayList<DPElement> startPath = s.getStartPathFromCommonRoot();
			ArrayList<DPElement> endPath = s.getEndPathFromCommonRoot();

			StreamValueBuilder builder = new StreamValueBuilder();

			buildStreamValueBetweenPaths( builder, commonRoot, s.getStartMarker(), startPath, s.getEndMarker(), endPath );
		
			return builder.stream();
		}
		else
		{
			Marker startMarker = s.getStartMarker();
			Marker endMarker = s.getEndMarker();
			DPContentLeafEditable leaf = startMarker.getElement();
			if ( endMarker.getElement() != leaf )
			{
				throw new RuntimeException( "No common root, but leaf elements do not match" );
			}
			
			StreamValueBuilder builder = new StreamValueBuilder();
			builder.appendTextValue( leaf.getTextRepresentation().substring( startMarker.getClampedIndex(), endMarker.getClampedIndex() ) );
			return builder.stream();
		}
	}
	

	
	protected void buildStreamValueFromStartOfRootToMarker(StreamValueBuilder builder, DPContentLeafEditable leaf, Marker marker, DPElement root)
	{
		DPContainer parent = leaf.getParent();
		if ( leaf != root  &&  parent != null )
		{
			buildStreamValueFromStartOfRootToMarkerFromChild( builder, parent, marker, root, leaf );
		}
		builder.appendTextValue( leaf.getTextRepresentation().substring( 0, marker.getClampedIndex() ) );
	}
	
	protected void buildStreamValueFromMarkerToEndOfRoot(StreamValueBuilder builder, DPContentLeafEditable leaf, Marker marker, DPElement root)
	{
		builder.appendTextValue( leaf.getTextRepresentation().substring( marker.getClampedIndex() ) );
		DPContainer parent = leaf.getParent();
		if ( leaf != root  &&  parent != null )
		{
			buildStreamValueFromMarkerToEndOfRootFromChild( builder, parent, marker, root, leaf );
		}
	}

	
	
	protected void buildStreamValueFromStartOfRootToMarkerFromChild(StreamValueBuilder builder, DPContainer subtree, Marker marker, DPElement root, DPElement fromChild)
	{
		DPContainer parent = subtree.getParent();
		if ( root != subtree  &&  parent != null )
		{
			buildStreamValueFromStartOfRootToMarkerFromChild( builder, parent, marker, root, subtree );
		}
		
		preOrderVisitElement( builder, subtree );

		for (DPElement child: subtree.getChildrenInSequentialOrder())
		{
			if ( child != fromChild )
			{
				buildStreamValue( child, builder );
			}
			else
			{
				break;
			}
		}
	}

	protected void buildStreamValueFromMarkerToEndOfRootFromChild(StreamValueBuilder builder, DPContainer subtree, Marker marker, DPElement root, DPElement fromChild)
	{
		List<DPElement> children = subtree.getChildrenInSequentialOrder();
		int childIndex = children.indexOf( fromChild );
		
		if ( (childIndex + 1) < children.size() )
		{
			for (DPElement child: children.subList( childIndex + 1, children.size() ))
			{
				buildStreamValue( child, builder );
			}
		}

		postOrderVisitElement( builder, subtree );

		DPContainer parent = subtree.getParent();
		if ( root != subtree  &&  parent != null )
		{
			buildStreamValueFromMarkerToEndOfRootFromChild( builder, parent, marker, root, subtree );
		}
	}
	
	
	protected void buildStreamValueBetweenPaths(StreamValueBuilder builder, DPContainer commonRoot, Marker startMarker, ArrayList<DPElement> startPath,
			Marker endMarker, ArrayList<DPElement> endPath)
	{
		List<DPElement> children = commonRoot.getChildrenInSequentialOrder();
		
	
		DPElement startChild = startPath.get( 1 );
		DPElement endChild = endPath.get( 1 );
		
		int startIndex = children.indexOf( startChild );
		int endIndex = children.indexOf( endChild );
	
	
		buildStreamValueFromMarkerToEndOfRoot( builder, startMarker.getElement(), startMarker, startChild );
		
		for (int i = startIndex + 1; i < endIndex; i++)
		{
			buildStreamValue( children.get( i ), builder );
		}

		buildStreamValueFromStartOfRootToMarker( builder, endMarker.getElement(), endMarker, endChild );
	}

	
	protected void buildStreamValue(DPElement root, StreamValueBuilder builder)
	{
		// Dual stack iterative pre and post order tree traversal
		Stack<DPElement> elementStack = new Stack<DPElement>();
		Stack<DPElement> activeStack = new Stack<DPElement>();

		elementStack.push( root );
		

		while ( !elementStack.isEmpty() )
		{
			// Get the next element to visit
			DPElement element = elementStack.lastElement();
			// Add to the active stack
			activeStack.add( element );

			
			// Pre-order visit
			preOrderVisitElement( builder, element );
			
			// In-order visit
			List<DPElement> children = inOrderVisitElement( builder, element );
			
			if ( children != null  &&  children.size() > 0 )
			{
				elementStack.addAll( children );
				Collections.reverse( elementStack.subList( elementStack.size() - children.size(), elementStack.size() ) );
			}
			
			// Pull off matching elements from elementStack and activeStack - these will be in post-order traversal order
			while ( !elementStack.isEmpty()  &&  !activeStack.isEmpty()  &&  elementStack.lastElement() == activeStack.lastElement() )
			{
				// Post-order visit
				postOrderVisitElement( builder, elementStack.lastElement() );

				elementStack.pop();
				activeStack.pop();
			}
		}
	}
	
	
	
	protected abstract void preOrderVisitElement(StreamValueBuilder builder, DPElement e);
	protected abstract List<DPElement> inOrderVisitElement(StreamValueBuilder builder, DPElement e);
	protected abstract void postOrderVisitElement(StreamValueBuilder builder, DPElement e);
}
