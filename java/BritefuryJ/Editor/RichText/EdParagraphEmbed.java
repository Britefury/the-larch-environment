//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import BritefuryJ.ClipboardFilter.ClipboardCopierMemo;


public class EdParagraphEmbed extends EdEmbed
{
	protected EdParagraphEmbed(Object value)
	{
		super( value );
	}


	@Override
	public Object clipboardCopy(ClipboardCopierMemo memo)
	{
		return new EdParagraphEmbed( memo.copy( value ) );
	}


	@Override
	protected Object buildModel(RichTextController controller)
	{
		return controller.buildParagraphEmbed( value );
	}


	@Override
	protected boolean isParagraph()
	{
		return true;
	}
}
