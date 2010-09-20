//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.CommandHistory;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.View.GSymFragmentView;

public abstract class Command implements Presentable
{
	public static class CannotJoinCommandException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected abstract void execute();
	protected abstract void unexecute();
	protected abstract String getDescription();
	
	
	protected boolean canMergeFrom(Command command)
	{
		return false;
	}
	
	protected void mergeFrom(Command command)
	{
		throw new CannotJoinCommandException();
	}

	
	@Override
	public Pres present(GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new Label( getDescription() );
	}
}
