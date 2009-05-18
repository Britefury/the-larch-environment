//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;

public abstract class OrderedBranchElement extends BranchElement
{
	protected OrderedBranchElement(DPContainer container)
	{
		super( container );
	}

	
	public DPContainer getWidget()
	{
		return (DPContainer)widget;
	}

	
	
	//
	// Text representation methods
	//
	
	protected String computeSubtreeTextRepresentation()
	{
		StringBuilder builder = new StringBuilder();
		for (Element child: getChildren())
		{
			builder.append( child.getTextRepresentation() );
		}
		return builder.toString();
	}
	
	
	
	public void getTextRepresentationFromStartToPath(StringBuilder builder, ElementMarker marker, ArrayList<Element> path, int pathMyIndex)
	{
		Element pathChild = path.get( pathMyIndex + 1 );
		for (Element child: getChildren())
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
	
	public void getTextRepresentationFromPathToEnd(StringBuilder builder, ElementMarker marker, ArrayList<Element> path, int pathMyIndex)
	{
		List<Element> children = getChildren();
		int pathChildIndex = pathMyIndex + 1;
		Element pathChild = path.get( pathChildIndex );
		int childIndex = children.indexOf( pathChild );
		
		pathChild.getTextRepresentationFromPathToEnd( builder, marker, path, pathChildIndex );

		for (Element child: children.subList( childIndex + 1, children.size() ))
		{
			builder.append( child.getTextRepresentation() );
		}
	}

	protected void getTextRepresentationBetweenPaths(StringBuilder builder, ElementMarker startMarker, ArrayList<Element> startPath, int startPathMyIndex,
			ElementMarker endMarker, ArrayList<Element> endPath, int endPathMyIndex)
	{
		List<Element> children = getChildren();
		
	
		int startPathChildIndex = startPathMyIndex + 1;
		int endPathChildIndex = endPathMyIndex + 1;
		
		Element startChild = startPath.get( startPathChildIndex );
		Element endChild = endPath.get( endPathChildIndex );
		
		int startIndex = children.indexOf( startChild );
		int endIndex = children.indexOf( endChild );
	
		
		startChild.getTextRepresentationFromPathToEnd( builder, startMarker, startPath, startPathChildIndex );
		
		for (int i = startIndex + 1; i < endIndex; i++)
		{
			builder.append( children.get( i ).getTextRepresentation() );
		}

		endChild.getTextRepresentationFromStartToPath( builder, endMarker, endPath, endPathChildIndex );
	}
}
