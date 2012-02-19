//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.awt.Color;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;

public class CommandName extends CommandMnemonic implements Presentable
{
	public CommandName(String charSequence, String name)
	{
		super( charSequence, name );
	}

	public CommandName(String annotatedName)
	{
		super( annotatedName );
	}




	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return cmdBorder.surround( completePres );
	}


	private static final AbstractBorder cmdBorder = Command.cmdBorder( new Color( 0.0f, 0.7f, 0.0f ), new Color( 0.85f, 0.95f, 0.85f ) );
}
