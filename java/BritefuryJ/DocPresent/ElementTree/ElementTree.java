//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.ElementTree.Caret.ElementCaret;
import BritefuryJ.DocPresent.ElementTree.TreeExplorer.ElementTreeExplorer;

public class ElementTree
{
	protected RootElement root;
	protected ElementCaret caret;
	protected ElementTree metaTree;
	protected ElementTreeExplorer explorer;
	
	
	public ElementTree()
	{
		root = new RootElement();
		root.setElementTree( this );
		caret = new ElementCaret( this, getPresentationArea().getCaret() );
		metaTree = null;
		explorer = null;
	}
	
	
	public RootElement getRoot()
	{
		return root;
	}
	
	public DPPresentationArea getPresentationArea()
	{
		return root.getWidget();
	}
	
	public ElementCaret getCaret()
	{
		return caret;
	}
	
	
	
	public ElementTree initialiseMetaTree()
	{
		if ( metaTree == null )
		{
			metaTree = new ElementTree();
			metaTree.getRoot().setChild( root.initialiseMetaElement() );
		}
		
		return metaTree;
	}
	
	public void shutdownMetaTree()
	{
		if ( metaTree != null )
		{
			root.shutdownMetaElement();
			metaTree = null;
		}
	}
	
	
	
	
	public ElementTreeExplorer createTreeExplorer()
	{
		if ( explorer == null  ||  !explorer.isVisible() )
		{
			explorer = new ElementTreeExplorer( this );
		}
		return explorer;
	}
}
