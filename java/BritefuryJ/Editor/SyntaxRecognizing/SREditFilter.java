//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import BritefuryJ.Editor.Sequential.EditFilter;
import BritefuryJ.Editor.Sequential.SequentialController;

public abstract class SREditFilter extends EditFilter
{
	protected abstract SyntaxRecognizingController getSyntaxRecognizingController();
	
	
	@Override
	protected SequentialController getSequentialController()
	{
		return getSyntaxRecognizingController();
	}
}
