//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPLink;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemLocationResolver;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class Browser implements PageController
{
	private static String COMMAND_BACK = "back";
	private static String COMMAND_FORWARD = "forward";
	
	private DPPresentationArea area;
	private JToolBar toolbar;
	private JTextField locationField;
	private JPanel locationPanel, panel;
	private String location;
	
	private LocationResolver resolver;
	private Page page;
	
	
	
	public Browser(LocationResolver resolver, String location)
	{
		this.resolver = resolver;
		this.location = location;
		
		area = new DPPresentationArea();
		area.setPageController( this );
		
		
		toolbar = new JToolBar();
		toolbar.setFloatable( false );
		initialiseToolbar( toolbar );
		
		
		JLabel locationLabel = new JLabel( "Location:" );
		locationLabel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 10 ) );
		locationField = new JTextField( location );
		locationField.setMaximumSize( new Dimension( locationField.getMaximumSize().width, locationField.getMinimumSize().height ) );
		locationField.setBorder( BorderFactory.createLineBorder( Color.black, 1 ) );
	
		ActionListener locationActionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				onLocationField( locationField.getText() );
			}
		};
		
		locationField.addActionListener( locationActionListener );
		
		
		locationPanel = new JPanel();
		locationPanel.setLayout( new BoxLayout( locationPanel, BoxLayout.X_AXIS ) );
		locationPanel.add( locationLabel );
		locationPanel.add( locationField );
		locationPanel.setBorder( BorderFactory.createEmptyBorder( 5, 0, 5, 5 ) );

		
		
		JPanel header = new JPanel( new BorderLayout() );
		header.add( toolbar, BorderLayout.PAGE_START );
		header.add( locationPanel, BorderLayout.PAGE_END );
	
	
		panel = new JPanel();
		panel.setLayout( new BorderLayout() );
		panel.add( header, BorderLayout.PAGE_START );
		panel.add( area.getComponent(), BorderLayout.CENTER );
		
		
		resolve();
	}
	
	
	
	public JComponent getComponent()
	{
		return panel;
	}
	
	
	
	public String getLocation()
	{
		return location;
	}
	
	public void setLocation(String location)
	{
		this.location = location;
		locationField.setText( location );
		resolve();
	}
	
	
	
	private void resolve()
	{
		if ( page != null )
		{
			page.setBrowser( null );
			page = null;
		}
		
		if ( location.equals( "" ) )
		{
			area.setChild( createWelcomeElement() );
		}
		else
		{
			page = SystemLocationResolver.getSystemResolver().resolveLocation( location );
			
			if ( page == null  &&  resolver != null )
			{
				page = resolver.resolveLocation( location );
			}
			
			if ( page == null )
			{
				area.setChild( createResolveErrorElement() );
			}
			else
			{
				page.setBrowser( this );
				area.setChild( page.getContentsElement() );		
			}
		}
	}
	
	
	protected void onPageContentsModified(Page page)
	{
		if ( page != this.page )
		{
			throw new RuntimeException( "Received page contents modified notification from invalid page" );
		}
		
		area.setChild( page.getContentsElement() );		
	}
	
	
	
	private DPWidget createResolveErrorElement()
	{
		VBoxStyleSheet pageBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 40.0, false, 10.0 );
		DPVBox pageBox = new DPVBox( pageBoxStyle );
		

		HBoxStyleSheet linkBoxStyle = new HBoxStyleSheet( VAlignment.BASELINES, 0.0, false, 10.0 );
		DPHBox linkBox = new DPHBox( linkBoxStyle );
		
		linkBox.append( new DPLink( "WELCOME PAGE", "" ) );
		
		
		TextStyleSheet titleStyle = new TextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPText title = new DPText( titleStyle, "Could Not Resolve Location" );
		
		VBoxStyleSheet errorBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 10.0, false, 0.0 );
		DPVBox errorBox = new DPVBox( errorBoxStyle );
		
		TextStyleSheet locationStyle = new TextStyleSheet( new Font( "SansSerif", Font.PLAIN, 16 ), Color.BLACK );
		TextStyleSheet errorStyle = new TextStyleSheet( new Font( "SansSerif", Font.PLAIN, 16 ), Color.BLACK );

		DPText loc = new DPText( locationStyle, location );
		DPText error = new DPText( errorStyle, "could not be resolved" );
		
		errorBox.append( loc );
		errorBox.append( error );

		pageBox.append( linkBox );
		pageBox.append( title );
		pageBox.append( errorBox );
		
		return pageBox;
	}
	
	
	private DPWidget createWelcomeElement()
	{
		VBoxStyleSheet pageBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 40.0, false, 10.0 );
		DPVBox pageBox = new DPVBox( pageBoxStyle );
		
		HBoxStyleSheet linkBoxStyle = new HBoxStyleSheet( VAlignment.BASELINES, 0.0, false, 10.0 );
		DPHBox linkBox = new DPHBox( linkBoxStyle );
		
		linkBox.append( new DPLink( "SYSTEM PAGE", "system" ) );
		
		
		TextStyleSheet titleStyle = new TextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPText title = new DPText( titleStyle, "Welcome to gSym" );
		
		VBoxStyleSheet contentBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 10.0, false, 0.0 );
		DPVBox contentBox = new DPVBox( contentBoxStyle );
		
		TextStyleSheet instructionsStyle = new TextStyleSheet( new Font( "SansSerif", Font.PLAIN, 16 ), Color.BLACK );

		DPText ins = new DPText( instructionsStyle, "Please enter a location in the location box above." );
		
		contentBox.append( ins );

		pageBox.append( linkBox );
		pageBox.append( title );
		pageBox.append( contentBox );
		
		return pageBox;
	}
	
	
	private void onLocationField(String location)
	{
		this.location = location;
		resolve();
	}



	public void goToLocation(String location)
	{
		this.location = location;
		locationField.setText( location );
		resolve();
	}
	
	
	
	private void initialiseToolbar(JToolBar toolbar)
	{
		ActionListener backListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				onBack();
			}
		};

		ActionListener forwardListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				onForward();
			}
		};
		
		
		toolbar.add( makeToolButton( "back arrow.png", COMMAND_BACK, "Go back", "Back", backListener ) );
		toolbar.add( makeToolButton( "forward arrow.png", COMMAND_FORWARD, "Go forward", "Forward", forwardListener ) );
	}
	
	
	private JButton makeToolButton(String imageFilename, String actionCommand, String tooltipText, String altText, ActionListener listener)
	{
		String imagePath = "icons/" + imageFilename;
		
		JButton button = new JButton();
		button.setActionCommand( actionCommand );
		button.setToolTipText( tooltipText );
		button.addActionListener( listener );
		button.setFocusable( false );
		
		ImageIcon icon = new ImageIcon( imagePath, altText );
		if ( icon.getImageLoadStatus() != MediaTracker.ABORTED  &&  icon.getImageLoadStatus() != MediaTracker.ERRORED )
		{
			button.setIcon( icon );
		}
		else
		{
			button.setText( altText );
			System.err.println( "Could not load image " + imagePath );
		}
		
		return button;
	}
	
	
	
	private void onBack()
	{
		System.out.println( "Browser.onBack" );
	}
	
	private void onForward()
	{
		System.out.println( "Browser.onForward" );
	}
}
