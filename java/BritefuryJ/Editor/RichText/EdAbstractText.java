//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.RichText;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.ClipboardFilter.ClipboardCopierMemo;
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
	
	
	public void setModelContents(RichTextController controller, List<Object> modelContents)
	{
		List<Object> editorModelContents = controller.convertModelListToEditorModelList( modelContents );
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
	
	
	protected ArrayList<Object> copyContents(ClipboardCopierMemo memo)
	{
		ArrayList<Object> contentsCopy = new ArrayList<Object>();
		for (Object x: contents)
		{
			contentsCopy.add( memo.copy( x ) );
		}
		return contentsCopy;
	}
}
