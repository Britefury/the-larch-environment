//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.AttributeTable.AttributeValues;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class ContextMenuStyleSheet extends StyleSheet
{
	private static final AttributeValues defaultSectionTitleAttrs = new AttributeValues( new String[] { "fontSize", "foreground", "fontBold" }, new Object[] { 16, new Color( 0.0f, 0.3f, 0.5f), true } );
	private static final double defaultSectionVBoxSpacing = 5.0;
	private static final double defaultControlsHBoxSpacing = 10.0;
	private static final double defaultControlsVBoxSpacing = 2.0;

	
	public static final ContextMenuStyleSheet instance = new ContextMenuStyleSheet();
	
	
	
	public ContextMenuStyleSheet()
	{
		super();
		
		initAttr( "primitiveStyle", PrimitiveStyleSheet.instance.withNonEditable() );
		initAttr( "controlsStyle", ControlsStyleSheet.instance );
		
		
		initAttr( "sectionTitleAttrs", defaultSectionTitleAttrs );
		initAttr( "sectionVBoxSpacing", defaultSectionVBoxSpacing );
		initAttr( "controlsHBoxSpacing", defaultControlsHBoxSpacing );
		initAttr( "controlsVBoxSpacing", defaultControlsVBoxSpacing );
	}



	protected StyleSheet newInstance()
	{
		return new ContextMenuStyleSheet();
	}
	

	
	public ContextMenuStyleSheet withPrimitiveStyleSheet(PrimitiveStyleSheet styleSheet)
	{
		return (ContextMenuStyleSheet)withAttr( "primitiveStyle", styleSheet );
	}

	public ContextMenuStyleSheet withPrimitiveStyleSheet(ControlsStyleSheet styleSheet)
	{
		return (ContextMenuStyleSheet)withAttr( "controlsStyle", styleSheet );
	}



	public ContextMenuStyleSheet withSectionTitleAttrs(AttributeValues attrs)
	{
		return (ContextMenuStyleSheet)withAttr( "sectionTitleAttrs", attrs );
	}

	public ContextMenuStyleSheet withSectionVBoxSpacingAttrs(double spacing)
	{
		return (ContextMenuStyleSheet)withAttr( "sectionVBoxSpacing", spacing );
	}

	public ContextMenuStyleSheet withControlsHBoxSpacing(double spacing)
	{
		return (ContextMenuStyleSheet)withAttr( "controlsHBoxSpacing", spacing );
	}

	public ContextMenuStyleSheet withControlsVBoxSpacing(double spacing)
	{
		return (ContextMenuStyleSheet)withAttr( "controlsVBoxSpacing", spacing );
	}

	
	
	private PrimitiveStyleSheet sectionTitleStyleSheet = null;

	private PrimitiveStyleSheet getSectionTitleStyleSheet()
	{
		if ( sectionTitleStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues attrs = getNonNull( "sectionTitleAttrs", AttributeValues.class, defaultSectionTitleAttrs );
			sectionTitleStyleSheet = (PrimitiveStyleSheet)primitive.withAttrValues( attrs );
		}
		return sectionTitleStyleSheet;
	}

	
	
	private PrimitiveStyleSheet sectionBoxStyleSheet = null;

	private PrimitiveStyleSheet getSectionBoxStyleSheet()
	{
		if ( sectionBoxStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			double vSpacing = getNonNull( "sectionVBoxSpacing", Double.class, defaultSectionVBoxSpacing );
			sectionBoxStyleSheet = primitive.withVBoxSpacing( vSpacing );
		}
		return sectionBoxStyleSheet;
	}

	
	
	private PrimitiveStyleSheet controlsBoxStyleSheet = null;

	private PrimitiveStyleSheet getControlsBoxStyleSheet()
	{
		if ( controlsBoxStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			double hSpacing = getNonNull( "controlsHBoxSpacing", Double.class, defaultControlsHBoxSpacing );
			double vSpacing = getNonNull( "controlsVBoxSpacing", Double.class, defaultControlsVBoxSpacing );
			controlsBoxStyleSheet = primitive.withHBoxSpacing( hSpacing ).withVBoxSpacing( vSpacing );
		}
		return controlsBoxStyleSheet;
	}

	
	

	
	public DPElement sectionTitle(String text)
	{
		return getSectionTitleStyleSheet().staticText( text ).alignHCentre();
	}
	
	public DPElement sectionVBox(DPElement children[])
	{
		return getSectionBoxStyleSheet().vbox( children );
	}

	public DPElement sectionVBox(ArrayList<DPElement> children)
	{
		return getSectionBoxStyleSheet().vbox( children );
	}
	
	public DPElement sectionWithTitle(String text, DPElement sectionContents)
	{
		return sectionVBox( new DPElement[] { sectionTitle( text ), sectionContents } );
	}

	
	public DPElement controlsHBox(DPElement children[])
	{
		return getControlsBoxStyleSheet().hbox( children );
	}

	public DPElement controlsHBox(ArrayList<DPElement> children)
	{
		return getControlsBoxStyleSheet().hbox( children );
	}
	
	public DPElement controlsVBox(DPElement children[])
	{
		return getControlsBoxStyleSheet().vbox( children );
	}

	public DPElement controlsVBox(ArrayList<DPElement> children)
	{
		return getControlsBoxStyleSheet().vbox( children );
	}
}
