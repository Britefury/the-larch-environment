//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.Anchor;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.LSpace.SequentialRichStringVisitor;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.LSpace.Browser.BrowserPage;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Focus.Target;
import BritefuryJ.LSpace.Input.Keyboard.Keyboard;
import BritefuryJ.LSpace.Input.Keyboard.KeyboardInteractor;
import BritefuryJ.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.LineBreak;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Spacer;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Pres.UI.BubblePopup;
import BritefuryJ.Projection.AbstractPerspective;
import BritefuryJ.Projection.ProjectiveBrowserContext;
import BritefuryJ.Projection.Subject;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.Util.RichString.RichString;

public class CommandConsole extends AbstractCommandConsole
{
	private class CommandKeyboardInteractor extends KeyboardInteractor
	{
		public boolean keyPressed(Keyboard keyboard, KeyEvent event)
		{
			for (BoundCommandSet cmdSet: page.getBoundCommandSets())
			{
				BoundCommand cmd = cmdSet.getCommandForKeyPressed( event );
				if ( cmd != null )
				{
					cmd.execute();
					return true;
				}
			}
			return false;
		}


		public boolean keyReleased(Keyboard keyboard, KeyEvent event)
		{
			return false;
		}


		public boolean keyTyped(Keyboard keyboard, KeyEvent event)
		{
			return false;
		}
	}
	
	
	private class CommandConsoleSubject extends Subject
	{
		public CommandConsoleSubject()
		{
			super( null );
		}
		
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
		public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
		{
			SequentialRichStringVisitor visitor = new SequentialRichStringVisitor();
			RichString value = visitor.getRichString( element );
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
			return contentsBorder.surround( new Text( text ).alignHPack() );
		}
	}
	
	private class CommandContents extends Contents
	{
		private BoundCommand cmd;
		
		
		public CommandContents(BoundCommand cmd, String params)
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
			return contentsBorder.surround( Pres.coerce( cmd.getCommand().getName().executableVisual() ).alignHPack() );
		}
	}
	
	private class CommandFailedContents extends Contents
	{
		private String name;
		private Throwable error;
		
		Hyperlink.LinkListener listener = new Hyperlink.LinkListener()
		{
			@Override
			public void onLinkClicked(Hyperlink.AbstractHyperlinkControl link, PointerButtonClickedEvent event)
			{
				BubblePopup.popupInBubbleAdjacentTo( error, link.getElement(), Anchor.TOP, true, true );
			}
		};
		
		
		public CommandFailedContents(String name, Throwable error)
		{
			this.name = name;
			this.error = error;
		}
		
		
		
		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Pres contents = new Row( new Object[] { new Label( name + " FAILED  " ), new Hyperlink( "SHOW ERROR", listener ) } );
			return new Row( new Object[] { cmdFailBorder.surround( cmdFailStyle.applyTo( contents ) ), new Text( "" ) } ).alignHPack();
		}
	}
	
	
	
	
	private CommandConsoleSubject subject = new CommandConsoleSubject();
	private CommandKeyboardInteractor keyInteractor = new CommandKeyboardInteractor();
	private ProjectiveBrowserContext browserContext;
	private BrowserPage page;
	private PresentationComponent presentation;
	private PresentationStateListenerList listeners = null;
	private Contents contents;
	private List<Pres> autocomplete = null;
	
	
	
	
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
	public void setPage(BrowserPage page)
	{
		this.page = page;
	}

	@Override
	public KeyboardInteractor getKeyboardInteractor()
	{
		return keyInteractor;
	}

	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		listeners = PresentationStateListenerList.addListener( listeners, fragment );
		
		Pres prompt = promptStyle.applyTo( new Label( "Cmd:" ) );
		Pres commandEntry = cmdRowStyle.applyTo( new Row( new Object[] { prompt.alignHPack(), contents } ).alignHExpand().alignVRefY().withTreeEventListener( treeEventListener ) );
		
		if ( autocomplete != null )
		{
			Pres[] acArray = new Pres[autocomplete.size() * 2 - 1];
			for (int i = 0; i < autocomplete.size(); i++)
			{
				acArray[i*2] = Pres.coerce( autocomplete.get( i ) );
				if ( i != autocomplete.size() - 1 )
				{
					acArray[i*2+1] = new LineBreak();
				}
			}
			Pres ac = cmdAutocompleteStyle.applyTo( new Paragraph( autocomplete.toArray() ) );
			
			Pres col = new Column( new Pres[] { commandEntry, new Spacer( 0.0, 5.0 ), ac } );
	
			return consoleBorder.surround( col );
		}
		else
		{
			return consoleBorder.surround( commandEntry );
		}
	}
	
	
	
	private void onEdit(RichString value)
	{
		if ( value.isTextual() )
		{
			String text = value.textualValue();
			
			if ( contents instanceof CommandFailedContents )
			{
				if ( text.contains( "\n" ) )
				{
					contents = new UnreckognisedContents( "" );
					notifyFinished();
				}
				else
				{
					CommandFailedContents f = (CommandFailedContents)contents;
					contents = new CommandFailedContents( f.name, f.error );
				}
				PresentationStateListenerList.onPresentationStateChanged( listeners, this );
			}
			else
			{
				if ( text.contains( "\n" ) )
				{
					// Attempt to execute the command
					if ( contents instanceof CommandContents )
					{
						CommandContents cmdC = (CommandContents)contents;
						
						Throwable error = null;
						try
						{
							cmdC.execute();
						}
						catch (Throwable t)
						{
							error = t;
						}
						
						if ( error == null )
						{
							contents = new UnreckognisedContents( "" );
							notifyFinished();
						}
						else
						{
							contents = new CommandFailedContents( cmdC.cmd.getCommand().getName().getName(), error );
						}
					}
					else
					{
						contents = new UnreckognisedContents( text.replace( "\n", "" ) );
					}
	
					PresentationStateListenerList.onPresentationStateChanged( listeners, this );
				}
				else
				{
					contents = null;
					
					// Get command by mnemonic
					BoundCommand cmd = getTargetCommand( text );
					
					if ( cmd == null )
					{
						cmd = getPageCommand( text );
					}
					
					if ( cmd != null )
					{
						contents = new CommandContents( cmd, text );
					}
					
					if ( contents == null )
					{
						contents = new UnreckognisedContents( text );
						
						if ( text.length() > 0 )
						{
							// Autocomplete
							String autocompleteText = text.toLowerCase();

							ArrayList<BoundCommand> autocompleteCommands = new ArrayList<BoundCommand>();
							buildTargetAutocompleteList( autocompleteCommands, autocompleteText );
							buildPageAutocompleteList( autocompleteCommands, autocompleteText );
							
							if ( autocompleteCommands.size() > 0 )
							{
								autocomplete = new ArrayList<Pres>();
								
								for (BoundCommand c: autocompleteCommands)
								{
									autocomplete.add( c.getCommand().getName().autocompleteVisual( autocompleteText ) );
								}
							}
							else
							{
								autocomplete = null;
							}
						}
					}
				}
				
				PresentationStateListenerList.onPresentationStateChanged( listeners, this );
			}
		}
		else
		{
			throw new RuntimeException( "Rich string contains structural items" );
		}
	}


	//
	// Find a command with the specified name, that is accessible at the current target (e.g. caret) 
	//
	private BoundCommand getTargetCommand(String name)
	{
		Target target = presentation.getRootElement().getTarget();
		if ( target.isValid() )
		{
			CommandSetGatherIterable commandSets = new CommandSetGatherIterable( target );
			
			
			for (BoundCommandSet commands: commandSets)
			{
				BoundCommand c = commands.getCommand( name );
				if ( c != null )
				{
					return c;
				}
			}
		}

		return null;
	}
	
	private void buildTargetAutocompleteList(List<BoundCommand> autocomplete, String text)
	{
		Target target = presentation.getRootElement().getTarget();
		if ( target.isValid() )
		{
			CommandSetGatherIterable commandSets = new CommandSetGatherIterable( target );
			
			
			for (BoundCommandSet commands: commandSets)
			{
				commands.buildAutocompleteList( autocomplete, text );
			}
		}
	}

	
	//
	// Find a command with the specified name, that is accessible in the current page
	//
	private BoundCommand getPageCommand(String text)
	{
		if ( page != null )
		{
			for (BoundCommandSet commands: page.getBoundCommandSets())
			{
				BoundCommand c = commands.getCommand( text );
				if ( c != null )
				{
					return c;
				}
			}
		}

		return null;
	}

	private void buildPageAutocompleteList(List<BoundCommand> autocomplete, String text)
	{
		if ( page != null )
		{
			for (BoundCommandSet commands: page.getBoundCommandSets())
			{
				commands.buildAutocompleteList( autocomplete, text );
			}
		}
	}


	private static final StyleSheet promptStyle = StyleSheet.style( Primitive.fontSize.as( 14 ), Primitive.fontItalic.as( true ) );
	private static final AbstractBorder cmdFailBorder = Command.cmdBorder( new Color( 0.5f, 0.0f, 0.0f, 0.8f ), new Color( 0.5f, 0.0f, 0.0f, 0.1f ) );
	private static final StyleSheet cmdFailStyle = StyleSheet.style( Primitive.foreground.as( new Color( 1.0f, 0.0f, 0.0f ) ) );
	private static final StyleSheet cmdRowStyle = StyleSheet.style( Primitive.rowSpacing.as( 7.0 ), Primitive.caretColour.as( Color.white ), Primitive.foreground.as( Color.white ) );
	private static final StyleSheet cmdAutocompleteStyle = StyleSheet.style( Primitive.paragraphSpacing.as( 7.0 ) );
	private static final SolidBorder contentsBorder = new SolidBorder( 1.0, 1.0, 5.0, 5.0, new Color( 1.0f, 1.0f, 1.0f, 0.5f ), new Color( 0.0f, 0.0f, 0.0f, 0.05f ) );
	private static final SolidBorder consoleBorder = new SolidBorder( 1.0, 3.0, 7.0, 7.0, new Color( 0.25f, 0.3f, 0.35f, 0.95f ), new Color( 0.0f, 0.1f, 0.2f, 0.9f ) );
}
