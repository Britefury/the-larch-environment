//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPMathRoot;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.MathRootStyleSheet;

public class MathRootElement extends BranchElement
{
	protected Element child;
	
	
	public MathRootElement()
	{
		this( MathRootStyleSheet.defaultStyleSheet );
	}

	public MathRootElement(MathRootStyleSheet styleSheet)
	{
		this( new DPMathRoot( styleSheet ) );
	}
	
	protected MathRootElement(DPMathRoot root)
	{
		super( root );
	}

	
	public DPMathRoot getWidget()
	{
		return (DPMathRoot)widget;
	}


	public void setChild(Element child)
	{
		if ( child != this.child )
		{
			if ( this.child != null )
			{
				this.child.setParent( null );
				this.child.setElementTree( null );
			}
			this.child = child;
			if ( this.child != null )
			{
				this.child.setParent( this );
				this.child.setElementTree( tree );
			}
			
			DPWidget childWidget = null;
			if ( child != null )
			{
				childWidget = child.getWidget();
			}
			getWidget().setChild( childWidget );
			
			onChildListChanged();
		}
	}
	
	public Element getChild()
	{
		return child;
	}
	

	
	public List<Element> getChildren()
	{
		if ( child == null )
		{
			Element[] ch = {};
			return Arrays.asList( ch );
		}
		else
		{
			Element[] ch = { child };
			return Arrays.asList( ch );
		}
	}
	
	
	
	
	//
	// Content methods
	//
	
	protected void computeSubtreeTextRepresentation(StringBuilder builder)
	{
		if ( child != null )
		{
			child.computeSubtreeTextRepresentation( builder );
		}
	}

	public int getTextRepresentationLength()
	{
		if ( child != null )
		{
			return child.getTextRepresentationLength();
		}
		else
		{
			return 0;
		}
	}
}
