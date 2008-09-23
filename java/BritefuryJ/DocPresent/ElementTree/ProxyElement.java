//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public class ProxyElement extends CollatableBranchElement
{
	protected Element child;
	
	
	public ProxyElement()
	{
		this( ContainerStyleSheet.defaultStyleSheet );
	}

	public ProxyElement(ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
	}
	
	
	public DPBin getWidget()
	{
		return (DPBin)getContainer();
	}

	protected DPContainer createContainerWidget(ContainerStyleSheet styleSheet)
	{
		return new DPBin( styleSheet );
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
			this.child.setParent( this );
			this.child.setElementTree( tree );
			
			onChildListChanged();
		}
	}
	
	public Element getChild()
	{
		return child;
	}
	

	
	protected void refreshContainerWidgetContents()
	{
		DPWidget childWidget = null;
		if ( child != null )
		{
			childWidget = child.getWidget();
		}
		getWidget().setChild( childWidget );
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



	protected boolean isProxy()
	{
		return true;
	}
}
