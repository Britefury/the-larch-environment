//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText.EditorModel;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;

public class EdBlock extends EdNode
{
	private ArrayList<EdNode> contents = new ArrayList<EdNode>();
	
	
	public EdBlock(List<EdNode> contents)
	{
		this.contents.addAll( contents );
	}
	
	
	@Override
	protected void buildTagList(List<Object> tags)
	{
		for (EdNode node: contents)
		{
			node.buildTagList( tags );
		}
	}

	@Override
	protected boolean isTextual()
	{
		return false;
	}

	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new Column( contents.toArray() );
	}
}
