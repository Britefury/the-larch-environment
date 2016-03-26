//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.RichText;

import java.util.List;

import BritefuryJ.ClipboardFilter.ClipboardCopyable;
import BritefuryJ.DefaultPerspective.Presentable;

public abstract class EdNode implements Presentable, ClipboardCopyable
{
	protected Tag containingPrefixTag()
	{
		return null;
	}

	protected Tag containingSuffixTag()
	{
		return null;
	}

	protected Tag prefixTag()
	{
		return null;
	}

	protected Tag suffixTag()
	{
		return null;
	}

	protected abstract void buildTagList(List<Object> tags);
	
	
	protected abstract boolean isTextual();
	
	protected void buildTextualValue(StringBuilder builder)
	{
		throw new RuntimeException( "Contents are not purely textual" );
	}
	
	
	protected abstract Object buildModel(RichTextController controller);
	
	
	protected boolean isParagraph()
	{
		return false;
	}
}
