//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.RichText.NormalText;

public abstract class EdAbstractText extends EdNode
{
	protected ArrayList<Object> contents = new ArrayList<Object>();
	
	
	protected EdAbstractText(List<Object> contents)
	{
		this.contents.addAll( contents );
	}
	
	protected EdAbstractText()
	{
	}
	
	
	public void setModelContents(RichTextEditor editor, List<Object> modelContents)
	{
		List<Object> editorModelContents = editor.convertModelListToEditorModelList( modelContents );
		this.contents.clear();
		this.contents.addAll( editorModelContents );
	}
	

	protected List<Object> getContents()
	{
		return contents;
	}
	
	
	@Override
	protected boolean isTextual()
	{
		for (Object x: contents)
		{
			if ( x instanceof EdNode )
			{
				if ( !((EdNode)x).isTextual() )
				{
					return false;
				}
			}
			else if ( !( x instanceof String ) )
			{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	protected void buildTextualValue(StringBuilder builder)
	{
		for (Object x: contents)
		{
			if ( x instanceof EdNode )
			{
				( (EdNode)x ).buildTextualValue( builder );
			}
			else if ( x instanceof String )
			{
				builder.append( (String)x );
			}
			else
			{
				throw new RuntimeException( "Contents are not purely textual" );
			}
		}
	}

	
	
	
	protected Pres presentContents()
	{
		return new NormalText( contents );
	}
	
	
	protected ArrayList<Object> deepCopyContents(RichTextEditor editor)
	{
		ArrayList<Object> contentsCopy = new ArrayList<Object>();
		for (Object x: contents)
		{
			if ( x instanceof EdNode )
			{
				contentsCopy.add( ( (EdNode)x ).deepCopy( editor ) );
			}
			else
			{
				contentsCopy.add( x );
			}
		}
		return contentsCopy;
	}
	
	
	protected abstract EdNode withContents(List<Object> contents);
}