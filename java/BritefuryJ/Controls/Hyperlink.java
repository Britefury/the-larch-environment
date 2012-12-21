//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Interactor.ContextMenuElementInteractor;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Image;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Projection.Subject;
import BritefuryJ.StyleSheet.StyleValues;

public class Hyperlink extends AbstractHyperlink
{
	private static Image browserIcon;
	private static boolean browserIconRetrieved = false;
	
	private static Image getBrowserIcon()
	{
		if ( !browserIconRetrieved )
		{
			File f = null;
			try
			{
				f = File.createTempFile( "icon", ".html" );
				Icon icon = FileSystemView.getFileSystemView().getSystemIcon( f );
				if ( icon instanceof ImageIcon )
				{
					java.awt.Image img = ((ImageIcon)icon).getImage();
					if ( img instanceof BufferedImage )
					{
						BufferedImage bImg = (BufferedImage)img;
						double w = bImg.getWidth(), h = bImg.getHeight();
						browserIcon = new Image( bImg, 16.0, 16.0 * h / w );
					}
				}
			}
			catch (IOException e)
			{
				browserIcon = null;
			}
			finally
			{
				if ( f != null )
				{
					f.delete();
				}
			}
			browserIconRetrieved = true;
		}
		
		return browserIcon;
	}
	
	
	private static Pres uriPres(String labelText)
	{
		Image i = getBrowserIcon();
		Pres label = new Text( labelText );
		if ( i == null )
		{
			return label;
		}
		else
		{
			return new Row( new Pres[] { i, label } ).alignVCentre();
		}
	}
	
	
	public static class HyperlinkControl extends AbstractHyperlink.AbstractHyperlinkControl
	{
		protected HyperlinkControl(PresentationContext ctx, StyleValues style, LSElement element, LinkListener listener, boolean bClosePopupOnActivate)
		{
			super( ctx, style, element, listener, bClosePopupOnActivate );
		}
	}

	
	
	public Hyperlink(String text, LinkListener listener)
	{
		super( new Text( text ), listener );
	}
	
	public Hyperlink(String text, Subject targetSubject)
	{
		super( new Text( text ), targetSubject );
	}
	
	public Hyperlink(String text, URI uri)
	{
		super( uriPres( text ), uri );
	}
	
	
	@Override
	protected Control createHyperlinkControl(PresentationContext ctx, StyleValues style, LSElement contentsElement, boolean bClosePopupOnActivate, LinkListener listener,
			ContextMenuElementInteractor contextMenuInteractor)
	{
		return new HyperlinkControl( ctx, style, contentsElement, listener, bClosePopupOnActivate );
	}
}
