//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.util.Iterator;
import java.util.LinkedList;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;
import BritefuryJ.LSpace.Target.Target;

public class CommandSetGatherIterable implements Iterable<BoundCommandSet>
{
	private class CommandSetGatherIterator implements Iterator<BoundCommandSet>
	{
		private LSElement currentElement = null;
		private LinkedList<CommandSet> commandSets = new LinkedList<CommandSet>();
		
		
		public CommandSetGatherIterator(LSElement element)
		{
			nextElement( element );
		}
		
		
		private void nextElement(LSElement element)
		{
			currentElement = null;
			while ( element != null )
			{
				if ( element.isRealised() )
				{
					Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( GatherCommandSetInteractor.class );
					if ( interactors != null )
					{
						for (AbstractElementInteractor interactor: interactors )
						{
							GatherCommandSetInteractor cmdInt = (GatherCommandSetInteractor)interactor;
							cmdInt.gatherCommandSets( element, commandSets );
						}
						
						if ( !commandSets.isEmpty() )
						{
							currentElement = element;
							break;
						}
					}
				}
				
				element = element.getParent();
			}
		}
	
	
		@Override
		public boolean hasNext()
		{
			return currentElement != null  &&  !commandSets.isEmpty();
		}
	
	
		@Override
		public BoundCommandSet next()
		{
			BoundCommandSet commands = commandSets.removeFirst().bindTo( currentElement );
			if ( commandSets.isEmpty() )
			{
				nextElement( currentElement.getParent() );
			}
			return commands;
		}
	
	
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}


	private LSElement element;


	public CommandSetGatherIterable(LSElement element)
	{
		this.element = element;
	}
	
	public CommandSetGatherIterable(Target target)
	{
		if ( target.isValid() )
		{
			this.element = target.getElement();
		}
		else
		{
			throw new RuntimeException( "Target is not valid" );
		}
	}

	
	
	@Override
	public Iterator<BoundCommandSet> iterator()
	{
		return new CommandSetGatherIterator( element );
	}
}