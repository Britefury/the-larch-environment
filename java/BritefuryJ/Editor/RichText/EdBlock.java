//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.RichText;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ClipboardFilter.ClipboardCopierMemo;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;

public class EdBlock extends EdNode
{
	private ArrayList<EdNode> contents = new ArrayList<EdNode>();
	
	
	protected EdBlock(List<EdNode> contents)
	{
		this.contents.addAll( contents );
	}
	
	
	public void setModelContents(RichTextController controller, List<Object> xs)
	{
		ArrayList<EdNode> ed = new ArrayList<EdNode>();
		ed.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			ed.add( controller.modelToEditorModel( x ) );
		}
		this.contents.clear();
		this.contents.addAll( ed );
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
	protected Object buildModel(RichTextController editor)
	{
		throw new RuntimeException( "Cannot build model for an EdBlock" );
	}


	@Override
	public Object clipboardCopy(ClipboardCopierMemo memo)
	{
		ArrayList<EdNode> contentsCopy = new ArrayList<EdNode>();
		for (EdNode node: contents)
		{
			contentsCopy.add( (EdNode)memo.copy( node ) );
		}
		return new EdBlock( contentsCopy );
	}


	@Override
	protected boolean isTextual()
	{
		for (EdNode e: contents)
		{
			if ( !e.isTextual() )
			{
				return false;
			}
		}
		return true;
	}

	
	@Override
	protected void buildTextualValue(StringBuilder builder)
	{
		for (EdNode e: contents)
		{
			e.buildTextualValue( builder );
		}
	}


	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new Column( contents.toArray() );
	}


	@Override
	public boolean equals(Object x)
	{
		if ( x instanceof EdBlock )
		{
			return contents.equals( ((EdBlock)x).contents );
		}
		else
		{
			return false;
		}
	}
}
