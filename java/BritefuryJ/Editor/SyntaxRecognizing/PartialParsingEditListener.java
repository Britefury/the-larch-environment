//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.EditEvent;
import BritefuryJ.LSpace.StreamValue.StreamValue;
import BritefuryJ.Parser.ParserExpression;

public abstract class PartialParsingEditListener extends ParsingEditListener
{
	public PartialParsingEditListener(ParserExpression parser)
	{
		super( parser );
	}

	
	@Override
	protected HandleEditResult handleParseSuccess(LSElement element, LSElement sourceElement, FragmentView fragment, EditEvent event, Object model, StreamValue value, Object parsed)
	{
		event.getStreamValueVisitor().setElementFixedValue( element, parsed );
		return HandleEditResult.PASS_TO_PARENT;
	}
}
