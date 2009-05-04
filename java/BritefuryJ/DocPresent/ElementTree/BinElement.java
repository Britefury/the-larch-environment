//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public class BinElement extends BranchElement
{
	protected Element child;
	
	
	public BinElement()
	{
		this( ContainerStyleSheet.defaultStyleSheet );
	}

	public BinElement(ContainerStyleSheet styleSheet)
	{
		this( new DPBin( styleSheet ) );
	}
	
	protected BinElement(DPBin bin)
	{
		super( bin );
	}

	
	public DPBin getWidget()
	{
		return (DPBin)widget;
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
	// Text representation methods
	//
	
	protected String computeSubtreeTextRepresentation()
	{
		return child != null  ?  child.getTextRepresentation()  :  "";
	}
}
