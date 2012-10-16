//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import BritefuryJ.Editor.Sequential.EditFilter;
import BritefuryJ.Editor.Sequential.SequentialController;

public abstract class SREditFilter extends EditFilter
{
	protected abstract SyntaxRecognizingController getSyntaxRecognizingEditor();
	
	
	@Override
	protected SequentialController getSequentialEditor()
	{
		return getSyntaxRecognizingEditor();
	}
}
