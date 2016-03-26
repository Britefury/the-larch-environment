//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.RichText;

import BritefuryJ.ClipboardFilter.ClipboardCopierMemo;


public class EdParagraphEmbed extends EdEmbed
{
	private Object model;


	protected EdParagraphEmbed(Object model, Object value)
	{
		super( value );
	}


	@Override
	public Object clipboardCopy(ClipboardCopierMemo memo)
	{
		return new EdParagraphEmbed( null, memo.copy( value ) );
	}


	@Override
	protected Object buildModel(RichTextController controller)
	{
		if (model != null)
		{
			return model;
		}
		else
		{
			return controller.buildParagraphEmbed( value );
		}
	}


	@Override
	protected boolean isParagraph()
	{
		return true;
	}


	@Override
	public boolean equals(Object x)
	{
		if ( x instanceof EdParagraphEmbed )
		{
			return value.equals( ((EdParagraphEmbed)x).value );
		}
		else
		{
			return false;
		}
	}
}
