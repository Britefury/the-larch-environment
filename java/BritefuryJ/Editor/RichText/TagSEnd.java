//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

class TagSEnd extends TagEnd
{
	@Override
	protected String getTagName()
	{
		return "style";
	}
	
	@Override
	public boolean equals(Object x)
	{
		return x instanceof TagSEnd;
	}

	@Override
	public String toString()
	{
		return "</style>";
	}
}
