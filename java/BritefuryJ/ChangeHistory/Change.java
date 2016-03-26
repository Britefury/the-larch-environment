//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.ChangeHistory;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;

public abstract class Change implements Presentable
{
	public static class CannotJoinCommandException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected abstract void execute();
	protected abstract void unexecute();
	protected abstract String getDescription();
	
	
	protected boolean canMergeFrom(Change change)
	{
		return false;
	}
	
	protected void mergeFrom(Change change)
	{
		throw new CannotJoinCommandException();
	}

	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new Label( getDescription() );
	}
}
