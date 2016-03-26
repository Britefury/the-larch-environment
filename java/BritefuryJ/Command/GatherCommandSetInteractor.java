//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Command;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;

public interface GatherCommandSetInteractor extends AbstractElementInteractor
{
	void gatherCommandSets(LSElement element, List<CommandSet> commandSets);
}
