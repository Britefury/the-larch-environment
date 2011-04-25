//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.Arrays;

import BritefuryJ.Command.Command;
import BritefuryJ.Command.CommandSet;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.Pres.ElementRef;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.StyleSheet.StyleSheet;

public class CommandConsoleTestPage extends SystemPage
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


		public void commandAction(Object context)
		{
			for (DPElement element: parentElement.getElements())
			{
				DPProxy proxy = (DPProxy)element;
				proxy.setChild( newContents.present( parentElement.getContextForElement( element ), parentElement.getStyleForElement( element ) ) );
			}
		}
	}

	

	private static StyleSheet styleSheet = StyleSheet.instance;
	private static StyleSheet blackText = styleSheet.withAttr( Primitive.foreground, Color.black );
	private static StyleSheet redText = styleSheet.withAttr( Primitive.foreground, Color.red );
	private static StyleSheet greenText = styleSheet.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) );
	private static StyleSheet blueText = styleSheet.withAttr( Primitive.foreground, new Color( 0.0f, 0.25f, 0.5f ) );


	
	private static Pres colouredText(StyleSheet style)
	{
		return style.withAttr( Primitive.editable, false ).applyTo(
				new NormalText( "Change the colour of this text, using the buttons below." ) );
	}
	
	protected Pres createContents()
	{
		ElementRef colouredTextProxyRef = new Proxy( colouredText( blackText ) ).elementRef();
		Command blackCmd = new Command( "bk", "black", new CommandContentChanger( colouredTextProxyRef, colouredText( blackText ) ) );
		Command redCmd = new Command( "re", "red", new CommandContentChanger( colouredTextProxyRef, colouredText( redText ) ) );
		Command greenCmd = new Command( "gr", "green", new CommandContentChanger( colouredTextProxyRef, colouredText( greenText ) ) );
		Command blueCmd = new Command( "bl", "blue", new CommandContentChanger( colouredTextProxyRef, colouredText( blueText ) ) );
		CommandSet cmds = new CommandSet( Arrays.asList( new Command[] { blackCmd, redCmd, greenCmd, blueCmd } ) );
		Pres cmdText = new Text( "Place the caret within this text, and use the command console. 'bk', 're', 'gr' and 'bl' are the commands available." ).withCommandSet( cmds );
		Pres colourBox = new Column( new Pres[] { colouredTextProxyRef, cmdText } );
		
		return new Body( new Pres[] { new Heading2( "Action button" ), colourBox } );
	}
}
