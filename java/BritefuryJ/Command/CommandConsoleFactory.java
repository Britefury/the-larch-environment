//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Command;

import BritefuryJ.Browser.Browser;
import BritefuryJ.LSpace.PresentationComponent;

public interface CommandConsoleFactory
{
	public AbstractCommandConsole createCommandConsole(PresentationComponent pres, Browser browser);
}
