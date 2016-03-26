//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
	protected Object buildModel(RichTextController controller)
	{
		return controller.buildInlineEmbed( value );
	}



	@Override
	public boolean equals(Object x)
	{
		if ( x instanceof EdInlineEmbed )
		{
			return value.equals( ((EdInlineEmbed)x).value );
		}
		else
		{
			return false;
		}
	}
}
