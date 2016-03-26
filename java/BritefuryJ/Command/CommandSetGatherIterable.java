//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Command;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Focus.Target;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;

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
					List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( GatherCommandSetInteractor.class );
					if ( interactors != null )
					{
						for (AbstractElementInteractor interactor: interactors)
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