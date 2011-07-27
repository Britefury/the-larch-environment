//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText.EditorModel;

import BritefuryJ.Editor.RichText.RichTextEditor.EditorModel_Accessor;

public class EdParagraphEmbed extends EdEmbed
{
	public EdParagraphEmbed(Object value)
	{
		super( value );
	}


	@Override
	public EdNode deepCopy(EditorModel_Accessor accessor)
	{
		return new EdParagraphEmbed( accessor.deepCopyParagraphEmbedValue( value ) );
	}


	@Override
	public Object buildModel(EditorModel_Accessor accessor)
	{
		return accessor.buildParagraphEmbed( value );
	}


	@Override
	public boolean isParagraph()
	{
		return true;
	}
}
