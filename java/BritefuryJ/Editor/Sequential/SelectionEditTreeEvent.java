//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.EditEvent;

public class SelectionEditTreeEvent extends EditEvent
{
	protected SequentialController sequentialController;
	protected LSElement sourceElement;
	
	
	protected SelectionEditTreeEvent(SequentialController sequentialController, LSElement sourceElement)
	{
		this.sequentialController = sequentialController;
		this.sourceElement = sourceElement;
	}
	
	
	public SequentialController getSequentialEditor()
	{
		return sequentialController;
	}
	
	public LSElement getSourceElement()
	{
		return sourceElement;
	}
}
