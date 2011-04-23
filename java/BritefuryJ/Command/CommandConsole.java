//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.awt.Color;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.DocPresent.StreamValue.StreamValueVisitor;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Projection.AbstractPerspective;
import BritefuryJ.Projection.ProjectiveBrowserContext;
import BritefuryJ.Projection.Subject;
import BritefuryJ.StyleSheet.StyleSheet;

public class CommandConsole extends AbstractCommandConsole implements Presentable
{
	private class CommandConsoleSubject extends Subject
	{
		public Object getFocus()
		{
			return CommandConsole.this;
		}
		
		
		public AbstractPerspective getPerspective()
		{
			return null;
		}
		
		public String getTitle()
		{
			return "Command console";
		}

		public SimpleAttributeTable getSubjectContext()
		{
			return SimpleAttributeTable.instance;
		}
		
		public ChangeHistory getChangeHistory()
		{
			return null;
		}
	}
	
	
	
	private TreeEventListener treeEventListener = new TreeEventListener()
	{
		@Override
		public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
		{
			StreamValueVisitor visitor = new StreamValueVisitor();
			StreamValue value = visitor.getStreamValue( element );
			onEdit( value );
			return true;
		}
	};
	
	
	
	
	
	private abstract class Contents implements Presentable
	{
	}
	
	
	private class UnreckognisedContents extends Contents
	{
		private String text;
		
		
		public UnreckognisedContents(String text)
		{
			this.text = text;
		}
		
		
		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return new Text( text );
		}
	}
	
	private class CommandContents extends Contents
	{
		private Command cmd;
		
		
		public CommandContents(Command cmd)
		{
			this.cmd = cmd;
		}
		
		
		
		public void execute()
		{
			cmd.execute();
		}
		
		
		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return Pres.coerce( cmd.getName() );
		}
	}
	
	
	
	
	private CommandConsoleSubject subject = new CommandConsoleSubject();
	private ProjectiveBrowserContext browserContext;
	private PresentationComponent presentation;
	private PresentationStateListenerList listeners = null;
	private Contents contents;
	
	
	
	
	public CommandConsole(ProjectiveBrowserContext browserContext, PresentationComponent presentation)
	{
		this.browserContext = browserContext;
		this.presentation = presentation;
		contents = new UnreckognisedContents( "" );
	}
	
	
	@Override
	public Subject getSubject()
	{
		return subject;
	}

	@Override
	public ProjectiveBrowserContext getBrowserContext()
	{
		return browserContext;
	}


	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		listeners = PresentationStateListenerList.addListener( listeners, fragment );
		Pres prompt = promptStyle.applyTo( new Border( new Label( "Cmd:" ) ) );
		
		return cmdRowStyle.applyTo( new Row( new Object[] { prompt, contents } ).withTreeEventListener( treeEventListener ) );
	}
	
	
	
	private void onEdit(StreamValue value)
	{
		if ( value.isTextual() )
		{
			String text = value.textualValue();
			
			if ( text.contains( "\n" ) )
			{
				// Attempt to execute the command
				if ( contents instanceof CommandContents )
				{
					((CommandContents)contents).execute();
					contents = new UnreckognisedContents( "" );
					notifyFinished();
				}
				else
				{
					contents = new UnreckognisedContents( text.replace( "\n", "" ) );
				}
			}
			else
			{
				CommandSetGatherIterable commandSets = new CommandSetGatherIterable( presentation );
				
				
				Command cmd = null;
				for (CommandSet commands: commandSets)
				{
					Command c = commands.getCommand( text );
					if ( c != null )
					{
						cmd = c;
						break;
					}
				}
				
				
				if ( cmd != null )
				{
					contents = new CommandContents( cmd );
				}
				else
				{
					contents = new UnreckognisedContents( text );
				}
			}
			
			PresentationStateListenerList.onPresentationStateChanged( listeners, this );
		}
		else
		{
			throw new RuntimeException( "Stream value contains structural items" );
		}
	}
	
	
	
	private static final StyleSheet promptStyle = StyleSheet.instance.withAttr( Primitive.border, Command.cmdBorder( new Color( 0.5f, 0.5f, 0.5f ), new Color( 0.9f, 0.9f, 0.9f ) ) );
	private static final StyleSheet cmdRowStyle = StyleSheet.instance.withAttr( Primitive.rowSpacing, 7.0 );
}
