//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText.EditorModel;

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
	
	
	public List<Object> getContents()
	{
		return contents;
	}
	
	public void setContents(List<Object> contents)
	{
		this.contents.clear();
		this.contents.addAll( contents );
	}
	
	
	@Override
	protected boolean isTextual()
	{
		for (Object x: contents)
		{
			if ( x instanceof EdAbstractText )
			{
				if ( !((EdAbstractText)x).isTextual() )
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
	
	
	protected Pres presentContents()
	{
		return new NormalText( contents );
	}
	
	
	protected abstract EdNode withContents(List<Object> contents);
}
