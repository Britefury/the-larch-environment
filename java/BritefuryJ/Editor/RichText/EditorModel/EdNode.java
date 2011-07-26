//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText.EditorModel;

import java.util.List;

import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Editor.RichText.Tags.Tag;

public abstract class EdNode implements Presentable
{
	public Tag startTag()
	{
		return null;
	}

	public Tag endTag()
	{
		return null;
	}

	public Tag prefixTag()
	{
		return null;
	}

	public Tag suffixTag()
	{
		return null;
	}

	public abstract void buildTagList(List<Object> tags);
	
	
	public abstract boolean isTextual();
	
	public void buildTextualValue(StringBuilder builder)
	{
		throw new RuntimeException( "Contents are not purely textual" );
	}
	
	
	public boolean isParagraph()
	{
		return false;
	}
}
