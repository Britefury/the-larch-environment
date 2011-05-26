//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.StreamValue;

import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementTreeVisitor;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.TextSelection;

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
		protected void preOrderVisitElement(DPElement e, boolean complete)
		{
			AbstractStreamValueVisitor.this.preOrderVisitElement( builder, e );
		}

		@Override
		protected void inOrderVisitElement(DPElement e)
		{
			AbstractStreamValueVisitor.this.inOrderVisitElement( builder, e );
		}

		@Override
		protected void postOrderVisitElement(DPElement e, boolean complete)
		{
			AbstractStreamValueVisitor.this.postOrderVisitElement( builder, e );
		}

		@Override
		protected void inOrderVisitPartialContentLeafEditable(DPContentLeafEditable e, int startIndex, int endIndex)
		{
			AbstractStreamValueVisitor.this.inOrderVisitPartialContentLeafEditable( builder, e, startIndex, endIndex );
		}
		
		@Override
		protected boolean shouldVisitChildrenOfElement(DPElement e, boolean completeVisit)
		{
			return AbstractStreamValueVisitor.this.shouldVisitChildrenOfElement( e, completeVisit );
		}
	}
	
	
	//
	// Stream value building
	//
	
	public StreamValue getStreamValue(DPElement root)
	{
		Visitor visitor = new Visitor();
		visitor.visitSubtree( root );
		return visitor.stream();
	}
	
	public StreamValue getStreamValueFromStartToMarker(DPElement root, Marker marker)
	{
		Visitor visitor = new Visitor();
		visitor.visitFromStartOfRootToMarker( marker, root );
		return visitor.stream();
	}
	
	public StreamValue getStreamValueFromMarkerToEnd(DPElement root, Marker marker)
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

	
	
	protected abstract void preOrderVisitElement(StreamValueBuilder builder, DPElement e);
	protected abstract void inOrderVisitElement(StreamValueBuilder builder, DPElement e);
	protected abstract void postOrderVisitElement(StreamValueBuilder builder, DPElement e);
	protected abstract void inOrderVisitPartialContentLeafEditable(StreamValueBuilder builder, DPContentLeafEditable e, int startIndex, int endIndex);
	public abstract boolean shouldVisitChildrenOfElement(DPElement e, boolean completeVisit);
}
