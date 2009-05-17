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
		for (Element child: getChildren())
		{
			if ( child == path.get( pathMyIndex ) )
			{
				child.getTextRepresentationFromStartToPath( builder, marker, path, pathMyIndex + 1 );
				break;
			}
			else
			{
				builder.append( child.getTextRepresentation() );
			}
		}
	}
	
	public void getTextRepresentationFromPathToEnd(StringBuilder builder, ElementMarker marker, ArrayList<Element> path, int pathMyIndex)
	{
		List<Element> children = getChildren();
		int pathChildIndex = pathMyIndex + 1;
		Element startChild = path.get( pathChildIndex );
		int childIndex = children.indexOf( startChild );
		
		startChild.getTextRepresentationFromStartToPath( builder, marker, path, pathChildIndex );

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
		
		int startIndex = children.indexOf( startPath.get( startPathChildIndex ) );
		int endIndex = children.indexOf( endPath.get( endPathMyIndex + 1 ) );
	
		
		startPath.get( startPathChildIndex ).getTextRepresentationFromPathToEnd( builder, startMarker, startPath, startPathChildIndex );
		
		for (int i = startIndex + 1; i < endIndex; i++)
		{
			builder.append( children.get( i ).getTextRepresentation() );
		}

		endPath.get( endPathChildIndex ).getTextRepresentationFromStartToPath( builder, endMarker, endPath, endPathChildIndex );
	}
}
