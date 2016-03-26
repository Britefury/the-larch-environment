//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
