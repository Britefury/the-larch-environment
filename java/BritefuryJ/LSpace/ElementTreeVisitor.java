//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.TextSelection;

public abstract class ElementTreeVisitor
{
	public void visitTextSelection(TextSelection s)
	{
		LSContainer commonRoot = s.getCommonRootContainer();
		
		if ( commonRoot != null )
		{
			ArrayList<LSElement> startPath = s.getStartPathFromCommonRoot();
			ArrayList<LSElement> endPath = s.getEndPathFromCommonRoot();

			visitBetweenPaths( commonRoot, s.getStartMarker(), startPath, s.getEndMarker(), endPath );
		}
		else
		{
			Marker startMarker = s.getStartMarker();
			Marker endMarker = s.getEndMarker();
			LSContentLeafEditable leaf = startMarker.getElement();
			if ( endMarker.getElement() != leaf )
			{
				throw new RuntimeException( "No common root, but leaf elements do not match" );
			}
			
			int start = startMarker.getClampedIndex(), end = endMarker.getClampedIndex();
			inOrderVisitPartialContentLeafEditable( leaf, start, end );
		}
	}
	

	
	public void visitFromStartOfRootToElement(LSElement elem, LSElement root)
	{
		if ( elem != root )
		{
			List<LSElement> path = elem.getElementPathFromAncestor( (LSContainer)root );
			
			// Start one element down from root, stop before last element (leaf)
			int indexOfLeafInPath = path.size() - 1;
			for (int i = 0; i < indexOfLeafInPath; i++)
			{
				LSElement e = path.get( i );
				
				preOrderVisitElement( e, false );

				if ( shouldVisitChildrenOfElement( e, false ) )
				{
					for (LSElement child: e.getChildrenInSequentialOrder())
					{
						if ( child != path.get( i + 1 ) )
						{
							visitSubtree( child );
						}
						else
						{
							break;
						}
					}
				}
				else
				{
					break;
				}
			}
		}
	}
	
	public void visitFromStartOfRootToMarker(Marker marker, LSElement root)
	{
		LSContentLeafEditable leaf = marker.getElement();
		
		if ( leaf == root )
		{
			preOrderVisitElement( leaf, false );
			inOrderVisitPartialContentLeafEditable( leaf, 0, marker.getClampedIndex() );
		}
		else
		{
			List<LSElement> path = leaf.getElementPathFromAncestor( (LSContainer)root );
			
			// If we stop before we get to @leaf, don't visit it
			boolean visitLeaf = true;
			// Start one element down from root, stop before last element (leaf)
			int indexOfLeafInPath = path.size() - 1;
			for (int i = 0; i < indexOfLeafInPath; i++)
			{
				LSElement e = path.get( i );
				
				preOrderVisitElement( e, false );

				if ( shouldVisitChildrenOfElement( e, false ) )
				{
					for (LSElement child: e.getChildrenInSequentialOrder())
					{
						if ( child != path.get( i + 1 ) )
						{
							visitSubtree( child );
						}
						else
						{
							break;
						}
					}
				}
				else
				{
					// Stop here
					visitLeaf = false;
					break;
				}
			}
			
			if ( visitLeaf )
			{
				preOrderVisitElement( leaf, false );
				inOrderVisitPartialContentLeafEditable( leaf, 0, marker.getClampedIndex() );
			}
		}
	}
	

	
	
	public void visitFromElementToEndOfRoot(LSElement elem, LSElement root)
	{
		if ( elem != root )
		{
			if ( shouldVisitChildrenOfElement( root , false ) )
			{
				List<LSElement> path = elem.getElementPathFromAncestor( (LSContainer)root );
				
				// First, scan through the path, and find the range of elements we should visit - we must stop at any element for which shouldVisitChildrenOfElement() returns false
				int startIndex = 1;
				for (int i = 0; i < path.size() - 1; i++)
				{
					if ( !shouldVisitChildrenOfElement( path.get( i ), false ) )
					{
						break;
					}
					startIndex = i + 1;
				}
				
				if ( startIndex == path.size() - 1 )
				{
					// Skip @elem
					startIndex--;
				}
				for (int i = startIndex; i >= 0; i--)
				{
					LSElement e = path.get( i );

					List<LSElement> children = e.getChildrenInSequentialOrder();
					int childIndex = children.indexOf( path.get( i + 1 ) );
					
					if ( (childIndex + 1) < children.size() )
					{
						for (LSElement child: children.subList( childIndex + 1, children.size() ))
						{
							visitSubtree( child );
						}
					}

					postOrderVisitElement( e, false );
				}
			}
		}
	}

	public void visitFromMarkerToEndOfRoot(Marker marker, LSElement root)
	{
		LSContentLeafEditable leaf = marker.getElement();
		
		if ( leaf == root )
		{
			inOrderVisitPartialContentLeafEditable( leaf, marker.getClampedIndex(), leaf.getTextRepresentationLength() );
			postOrderVisitElement( leaf, false );
		}
		else
		{
			if ( shouldVisitChildrenOfElement( root , false ) )
			{
				List<LSElement> path = leaf.getElementPathFromAncestor( (LSContainer)root );
				
				// First, scan through the path, and find the range of elements we should visit - we must stop at any element for which shouldVisitChildrenOfElement() returns false
				int startIndex = 1;
				for (int i = 0; i < path.size() - 1; i++)
				{
					if ( !shouldVisitChildrenOfElement( path.get( i ), false ) )
					{
						break;
					}
					startIndex = i + 1;
				}
				
				if ( startIndex == path.size() - 1 )
				{
					// We are visiting the last element (leaf)
					inOrderVisitPartialContentLeafEditable( leaf, marker.getClampedIndex(), leaf.getTextRepresentationLength() );
					postOrderVisitElement( leaf, false );
					
					// We have visited the leaf
					startIndex--;
				}
				for (int i = startIndex; i >= 0; i--)
				{
					LSElement e = path.get( i );

					List<LSElement> children = e.getChildrenInSequentialOrder();
					int childIndex = children.indexOf( path.get( i + 1 ) );
					
					if ( (childIndex + 1) < children.size() )
					{
						for (LSElement child: children.subList( childIndex + 1, children.size() ))
						{
							visitSubtree( child );
						}
					}

					postOrderVisitElement( e, false );
				}
			}
		}
	}
	

	
	
	protected void visitBetweenPaths(LSContainer commonRoot, Marker startMarker, ArrayList<LSElement> startPath,
			Marker endMarker, ArrayList<LSElement> endPath)
	{
		if ( shouldVisitChildrenOfElement( commonRoot, false ) )
		{
			List<LSElement> children = commonRoot.getChildrenInSequentialOrder();
			
		
			LSElement startChild = startPath.get( 1 );
			LSElement endChild = endPath.get( 1 );
			
			int startIndex = children.indexOf( startChild );
			int endIndex = children.indexOf( endChild );
		
		
			visitFromMarkerToEndOfRoot( startMarker, startChild );
			
			for (int i = startIndex + 1; i < endIndex; i++)
			{
				visitSubtree( children.get( i ) );
			}
	
			visitFromStartOfRootToMarker( endMarker, endChild );
		}
	}

	
	public void visitSubtree(LSElement root)
	{
		// Dual stack iterative pre and post order tree traversal
		Stack<LSElement> elementStack = new Stack<LSElement>();
		Stack<LSElement> activeStack = new Stack<LSElement>();

		elementStack.push( root );
		

		while ( !elementStack.isEmpty() )
		{
			// Get the next element to visit
			LSElement element = elementStack.lastElement();
			// Add to the active stack
			activeStack.add( element );

			
			// Pre-order visit
			preOrderVisitElement( element, true );
			
			// In-order visit
			inOrderCompletelyVisitElement( element );
			if ( shouldVisitChildrenOfElement( element, true ) )
			{
				List<LSElement> children = element.getChildrenInSequentialOrder();

				if ( children.size() > 0 )
				{
					elementStack.addAll( children );
					Collections.reverse( elementStack.subList( elementStack.size() - children.size(), elementStack.size() ) );
				}
			}
			
			// Pull off matching elements from elementStack and activeStack - these will be in post-order traversal order
			while ( !elementStack.isEmpty()  &&  !activeStack.isEmpty()  &&  elementStack.lastElement() == activeStack.lastElement() )
			{
				// Post-order visit
				postOrderVisitElement( elementStack.lastElement(), true );

				elementStack.pop();
				activeStack.pop();
			}
		}
	}
	
	
	
	protected abstract void preOrderVisitElement(LSElement e, boolean complete);
	protected abstract void inOrderCompletelyVisitElement(LSElement e);
	protected abstract void postOrderVisitElement(LSElement e, boolean complete);
	protected abstract void inOrderVisitPartialContentLeafEditable(LSContentLeafEditable e, int startIndex, int endIndex);
	protected abstract boolean shouldVisitChildrenOfElement(LSElement e, boolean completeVisit);
}
