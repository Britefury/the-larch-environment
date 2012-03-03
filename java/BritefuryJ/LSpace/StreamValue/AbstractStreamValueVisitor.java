//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.StreamValue;

import BritefuryJ.LSpace.LSContentLeafEditable;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.ElementTreeVisitor;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.Selection.TextSelection;

public abstract class AbstractStreamValueVisitor
{
	private class Visitor extends ElementTreeVisitor
	{
		private StreamValueBuilder builder = new StreamValueBuilder();
		
		
		protected StreamValue stream()
		{
			return builder.stream();
		}


		@Override
		protected void preOrderVisitElement(LSElement e, boolean complete)
		{
			AbstractStreamValueVisitor.this.preOrderVisitElement( builder, e );
		}

		@Override
		protected void inOrderCompletelyVisitElement(LSElement e)
		{
			AbstractStreamValueVisitor.this.inOrderVisitElement( builder, e );
		}

		@Override
		protected void postOrderVisitElement(LSElement e, boolean complete)
		{
			AbstractStreamValueVisitor.this.postOrderVisitElement( builder, e );
		}

		@Override
		protected void inOrderVisitPartialContentLeafEditable(LSContentLeafEditable e, int startIndex, int endIndex)
		{
			AbstractStreamValueVisitor.this.inOrderVisitPartialContentLeafEditable( builder, e, startIndex, endIndex );
		}
		
		@Override
		protected boolean shouldVisitChildrenOfElement(LSElement e, boolean completeVisit)
		{
			return AbstractStreamValueVisitor.this.shouldVisitChildrenOfElement( e, completeVisit );
		}
	}
	
	
	//
	// Stream value building
	//
	
	public StreamValue getStreamValue(LSElement root)
	{
		Visitor visitor = new Visitor();
		visitor.visitSubtree( root );
		return visitor.stream();
	}
	
	public StreamValue getStreamValueFromStartToMarker(LSElement root, Marker marker)
	{
		Visitor visitor = new Visitor();
		visitor.visitFromStartOfRootToMarker( marker, root );
		return visitor.stream();
	}
	
	public StreamValue getStreamValueFromMarkerToEnd(LSElement root, Marker marker)
	{
		Visitor visitor = new Visitor();
		visitor.visitFromMarkerToEndOfRoot( marker, root );
		return visitor.stream();
	}
	
	public StreamValue getStreamValueInTextSelection(TextSelection s)
	{
		Visitor visitor = new Visitor();
		visitor.visitTextSelection( s );
		return visitor.stream();
	}

	
	
	protected abstract void preOrderVisitElement(StreamValueBuilder builder, LSElement e);
	protected abstract void inOrderVisitElement(StreamValueBuilder builder, LSElement e);
	protected abstract void postOrderVisitElement(StreamValueBuilder builder, LSElement e);
	protected abstract void inOrderVisitPartialContentLeafEditable(StreamValueBuilder builder, LSContentLeafEditable e, int startIndex, int endIndex);
	public abstract boolean shouldVisitChildrenOfElement(LSElement e, boolean completeVisit);
}
