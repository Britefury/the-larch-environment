//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
