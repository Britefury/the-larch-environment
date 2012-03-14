//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.TextSelection;
import BritefuryJ.Util.RichString.RichString;
import BritefuryJ.Util.RichString.RichStringBuilder;

public abstract class AbstractRichStringVisitor
{
	private class Visitor extends ElementTreeVisitor
	{
		private RichStringBuilder builder = new RichStringBuilder();
		
		
		protected RichString richString()
		{
			return builder.richString();
		}


		@Override
		protected void preOrderVisitElement(LSElement e, boolean complete)
		{
			AbstractRichStringVisitor.this.preOrderVisitElement( builder, e );
		}

		@Override
		protected void inOrderCompletelyVisitElement(LSElement e)
		{
			AbstractRichStringVisitor.this.inOrderVisitElement( builder, e );
		}

		@Override
		protected void postOrderVisitElement(LSElement e, boolean complete)
		{
			AbstractRichStringVisitor.this.postOrderVisitElement( builder, e );
		}

		@Override
		protected void inOrderVisitPartialContentLeafEditable(LSContentLeafEditable e, int startIndex, int endIndex)
		{
			AbstractRichStringVisitor.this.inOrderVisitPartialContentLeafEditable( builder, e, startIndex, endIndex );
		}
		
		@Override
		protected boolean shouldVisitChildrenOfElement(LSElement e, boolean completeVisit)
		{
			return AbstractRichStringVisitor.this.shouldVisitChildrenOfElement( e, completeVisit );
		}
	}
	
	
	//
	// Rich string building
	//
	
	public RichString getRichString(LSElement root)
	{
		Visitor visitor = new Visitor();
		visitor.visitSubtree( root );
		return visitor.richString();
	}
	
	public RichString getRichStringFromStartToMarker(LSElement root, Marker marker)
	{
		Visitor visitor = new Visitor();
		visitor.visitFromStartOfRootToMarker( marker, root );
		return visitor.richString();
	}
	
	public RichString getRichStringFromMarkerToEnd(LSElement root, Marker marker)
	{
		Visitor visitor = new Visitor();
		visitor.visitFromMarkerToEndOfRoot( marker, root );
		return visitor.richString();
	}
	
	public RichString getRichStringInTextSelection(TextSelection s)
	{
		Visitor visitor = new Visitor();
		visitor.visitTextSelection( s );
		return visitor.richString();
	}

	
	
	protected abstract void preOrderVisitElement(RichStringBuilder builder, LSElement e);
	protected abstract void inOrderVisitElement(RichStringBuilder builder, LSElement e);
	protected abstract void postOrderVisitElement(RichStringBuilder builder, LSElement e);
	protected abstract void inOrderVisitPartialContentLeafEditable(RichStringBuilder builder, LSContentLeafEditable e, int startIndex, int endIndex);
	public abstract boolean shouldVisitChildrenOfElement(LSElement e, boolean completeVisit);
}
