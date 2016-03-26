//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.Command.CommandSetSource;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleValues;

public class AddCommandSets extends Pres
{
	private CommandSetSource commandSetSource;
	private Pres child;
	
	
	public AddCommandSets(Pres child, CommandSetSource commandSetSource)
	{
		this.commandSetSource = commandSetSource;
		this.child = child;
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		element.addElementInteractor( commandSetSource.getInteractor() );
		return element;
	}
}
