//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import BritefuryJ.ClipboardFilter.ClipboardCopierMemo;


public class EdInlineEmbed extends EdEmbed
{
	protected EdInlineEmbed(Object value)
	{
		super( value );
	}


	@Override
	public Object clipboardCopy(ClipboardCopierMemo memo)
	{
		return new EdInlineEmbed( memo.copy( value ) );
	}


	@Override
	protected Object buildModel(RichTextController editor)
	{
		return editor.buildInlineEmbed( value );
	}
}
