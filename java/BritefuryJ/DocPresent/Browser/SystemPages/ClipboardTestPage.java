//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import net.htmlparser.jericho.Source;
import BritefuryJ.Controls.DropDownExpander;
import BritefuryJ.Controls.TextArea;
import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.DocPresent.Clipboard.DataTransfer;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.Target.Target;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.ObjectPres.UnescapedStringAsParagraph;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading5;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class ClipboardTestPage extends SystemPage
{
	private static StyleSheet styleSheet = StyleSheet.instance;
	
	private static StyleSheet placeHolderStyle = styleSheet.withAttr( Primitive.background, new FillPainter( new Color( 1.0f, 0.9f, 0.75f  ) ) );
	
	
	protected ClipboardTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Clipboard test";
	}
	
	protected String getDescription()
	{
		return "Send and receive clipboard contents.";
	}
	
	
	protected Pres makeDest()
	{
		return new Proxy( placeHolderStyle.applyTo( new Bin( new Text( " " ).pad( 8.0, 8.0 ) ) ) );
	}
	
	protected Pres makeDescription()
	{
		return new Bin( new Label( "" ) );
	}
	
	
	protected Pres createPasteReceiver()
	{
		Pres receiverPres = new Pres()
		{
			@Override
			public DPElement present(final PresentationContext ctx, final StyleValues style)
			{
				Pres dest = makeDest(); 
				Pres descr = makeDescription();
				
				final DPElement descrElement = descr.present( ctx, style );

				
				ClipboardHandlerInterface clipboardHandler = new ClipboardHandlerInterface()
				{
					@Override
					public boolean deleteSelection(Selection selection, Target target)
					{
						return false;
					}

					@Override
					public boolean replaceSelectionWithText(Selection selection, Target target, String replacement)
					{
						return false;
					}

					@Override
					public int getExportActions(Selection selection)
					{
						return 0;
					}

					@Override
					public Transferable createExportTransferable(Selection selection)
					{
						return null;
					}

					@Override
					public void exportDone(Selection selection, Target target, Transferable transferable, int action)
					{
					}

					@Override
					public boolean canImport(Target target, Selection selection, DataTransfer dataTransfer)
					{
						return true;
					}

					@Override
					public boolean importData(Target target, Selection selection, DataTransfer dataTransfer)
					{
						handlePaste( dataTransfer );

						return true;
					}
					
					
					private void handlePaste(DataTransfer transfer)
					{
						DataFlavor flavors[] = transfer.getDataFlavors();
						
						ArrayList<Object> elements = new ArrayList<Object>();
						for (DataFlavor flavor: flavors)
						{
							elements.add( presentDataFlavor( transfer, flavor ) );
						}
						DPElement e = new Column( elements ).present( ctx, style );
						
						((DPBin)descrElement).setChild( e );
					}
					
					private Pres presentDataFlavor(DataTransfer transfer, DataFlavor flavor)
					{
						Pres title = new Label( flavor.toString() );
						Pres contents = null;
						
						try
						{
							if ( flavor.getMimeType().startsWith( "text/plain;" ) )
							{
								contents = presentTextPlain( transfer, flavor );
							}
							else if ( flavor.getMimeType().startsWith( "text/html;" ) )
							{
								contents = presentTextHTML( transfer, flavor );
							}
						}
						catch (UnsupportedFlavorException e)
						{
						}
						catch (IOException e)
						{
						}
						
						if ( contents == null )
						{
							return title;
						}
						else
						{
							return new DropDownExpander( title, contents );
						}
					}
					
					private Pres presentTextPlain(DataTransfer transfer, DataFlavor flavor) throws UnsupportedFlavorException, IOException
					{
						if ( flavor.getRepresentationClass() == String.class )
						{
							return new UnescapedStringAsParagraph( (String)transfer.getTransferData( flavor ) );
						}
						else
						{
							return null;
						}
					}

					private Pres presentTextHTML(DataTransfer transfer, DataFlavor flavor) throws UnsupportedFlavorException, IOException
					{
						if ( flavor.getRepresentationClass() == String.class )
						{
							Source html = new Source( (String)transfer.getTransferData( flavor ) );
							return DefaultPerspective.instance.applyTo( html );
						}
						else
						{
							return null;
						}
					}
				};
				
				Region r = new Region( dest, clipboardHandler );
				
				Pres p = new Column( new Object[] { r, descrElement } );
				return p.present( ctx, style );
			}
		};
		
		
		Pres title = new Heading5( "Text element (displays description of pasted content):" );
		Pres borderedReceiver = StyleSheet.instance.withAttr( Primitive.border, new SolidBorder( 2.0, 2.0, new Color( 0.3f, 0.3f, 0.3f ), null ) ).applyTo( receiverPres );
		return new Column( new Pres[] { title, borderedReceiver } );
	}
	
	
	
	protected Pres createTextArea()
	{
		Pres title = new Heading5( "Text area (can send and receive text):" );
		Pres area = new TextArea( "Enter text here", null );
		Pres borderedArea = StyleSheet.instance.withAttr( Primitive.border, new SolidBorder( 2.0, 2.0, new Color( 0.3f, 0.3f, 0.3f ), null ) ).applyTo( area );
		return new Column( new Pres[] { title, borderedArea } );
	}
	
	
	protected Pres createContents()
	{
		return new Body( new Pres[] { createTextArea(), createPasteReceiver() } );
	}
}
