//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.ArrayList;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.EditHandler;
import BritefuryJ.DocPresent.ElementTree.Caret.ElementCaret;
import BritefuryJ.DocPresent.ElementTree.Selection.ElementSelection;
import BritefuryJ.DocPresent.ElementTree.TreeExplorer.ElementTreeExplorer;

public class ElementTree
{
	protected RootElement root;
	protected ElementCaret caret;
	protected ElementSelection selection;
	protected DPPresentationArea metaArea;
	protected ElementTreeExplorer explorer;
	
	
	public ElementTree()
	{
		root = new RootElement();
		root.setElementTree( this );
		caret = new ElementCaret( this, getPresentationArea().getCaret() );
		selection = new ElementSelection( this, getPresentationArea().getSelection() );
		metaArea = null;
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
	
	public void setUndoListener(DPPresentationArea.UndoListener undoListener)
	{
		getPresentationArea().setUndoListener( undoListener );
	}
	
	
	
	
	//
	//
	// SELECTION METHODS
	//
	//
	
	public ElementSelection getSelection()
	{
		return selection;
	}
	
	public String getTextRepresentationInSelection(ElementSelection s)
	{
		if ( s.isEmpty() )
		{
			return null;
		}
		else
		{
			BranchElement commonRoot = s.getCommonRoot();
			ArrayList<Element> startPath = s.getStartPathFromCommonRoot();
			ArrayList<Element> endPath = s.getEndPathFromCommonRoot();
			
			if ( commonRoot != null )
			{
				StringBuilder builder = new StringBuilder();

				commonRoot.getTextRepresentationBetweenPaths( builder, s.getStartMarker(), startPath, 0, s.getEndMarker(), endPath, 0 );
			
				return builder.toString();
			}
			else
			{
				return ((LeafElement)startPath.get( 0 )).getTextRepresentationBetweenMarkers( s.getStartMarker(), s.getEndMarker() );
			}
		}
	}
	
	
	
	
	
	public DPPresentationArea initialiseMetaTree()
	{
		if ( metaArea == null )
		{
			metaArea = new DPPresentationArea();
			metaArea.disableHorizontalClamping();
			metaArea.setChild( root.initialiseMetaElement() );
		}
		
		return metaArea;
	}
	
	public void shutdownMetaTree()
	{
		if ( metaArea != null )
		{
			root.shutdownMetaElement();
			metaArea = null;
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
	
	
	
	public void setEditHandler(EditHandler handler)
	{
		getPresentationArea().setEditHandler( handler );
	}

	public EditHandler getEditHandler()
	{
		return getPresentationArea().getEditHandler();
	}
}
