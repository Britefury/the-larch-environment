//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;
import java.util.Arrays;

import BritefuryJ.Command.Command;
import BritefuryJ.Command.CommandSet;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSProxy;
import BritefuryJ.LSpace.PageController;
import BritefuryJ.Pres.ElementRef;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.StyleSheet.StyleSheet;

public class CommandConsoleTestPage extends TestPage
{
	protected CommandConsoleTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Command console test";
	}
	
	protected String getDescription()
	{
		return "The command console can be used to quickly execute commands.";
	}
	
	
	protected static class CommandContentChanger implements Command.CommandAction
	{
		private ElementRef parentElement;
		private Pres newContents;
		
		
		public CommandContentChanger(ElementRef parentElement, Pres newContents)
		{
			this.parentElement = parentElement;
			this.newContents = newContents;
		}


		public void commandAction(Object context, PageController pageController)
		{
			for (LSElement element: parentElement.getElements())
			{
				LSProxy proxy = (LSProxy)element;
				proxy.setChild( newContents.present( parentElement.getContextForElement( element ), parentElement.getStyleForElement( element ) ) );
			}
		}
	}

	protected static class CommandFailAction implements Command.CommandAction
	{
		public CommandFailAction()
		{
		}


		public void commandAction(Object context, PageController pageController)
		{
			throw new RuntimeException( "Fail" );
		}
	}

	

	private static StyleSheet styleSheet = StyleSheet.instance;
	private static StyleSheet blackText = styleSheet.withValues( Primitive.foreground.as( Color.black ) );
	private static StyleSheet redText = styleSheet.withValues( Primitive.foreground.as( Color.red ) );
	private static StyleSheet greenText = styleSheet.withValues( Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.0f ) ) );
	private static StyleSheet blueText = styleSheet.withValues( Primitive.foreground.as( new Color( 0.0f, 0.25f, 0.5f ) ) );


	
	private static Pres colouredText(StyleSheet style)
	{
		return style.withValues( Primitive.editable.as( false ) ).applyTo(
				new NormalText( "Change the colour of this text, using the command console." ) );
	}
	
	protected Pres createContents()
	{
		ElementRef colouredTextProxyRef = new Proxy( colouredText( blackText ) ).elementRef();
		Command blackCmd = new Command( "&Blac&k", new CommandContentChanger( colouredTextProxyRef, colouredText( blackText ) ) );
		Command redCmd = new Command( "&R&ed", new CommandContentChanger( colouredTextProxyRef, colouredText( redText ) ) );
		Command greenCmd = new Command( "&G&reen", new CommandContentChanger( colouredTextProxyRef, colouredText( greenText ) ) );
		Command blueCmd = new Command( "&B&lue", new CommandContentChanger( colouredTextProxyRef, colouredText( blueText ) ) );
		Command failCmd = new Command( "&Fail", new CommandFailAction() );
		CommandSet cmds = new CommandSet( "test.TestCommandSet", Arrays.asList( blackCmd, redCmd, greenCmd, blueCmd, failCmd ) );
		Pres cmdText = new NormalText( "Place the caret within this text, and invoke the command console by pressing ESC. " + 
					"Type one of the following commands and hit enter to execute: 'bk', 're', 'gr', 'bl' and 'f'." );
		Pres colourBox = new Column( new Pres[] { colouredTextProxyRef, cmdText } ).withCommands( cmds );
		
		return new Body( new Pres[] { new Heading2( "Commands" ), colourBox } );
	}
}
