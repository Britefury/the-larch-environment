//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.StyleSheet;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Paint;
import java.util.HashMap;

import BritefuryJ.AttributeTable.AttributeValues;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.DocPresent.StyleParams.ContentLeafStyleParams;
import BritefuryJ.DocPresent.StyleParams.FractionStyleParams;
import BritefuryJ.DocPresent.StyleParams.GridRowStyleParams;
import BritefuryJ.DocPresent.StyleParams.HBoxStyleParams;
import BritefuryJ.DocPresent.StyleParams.MathRootStyleParams;
import BritefuryJ.DocPresent.StyleParams.ParagraphStyleParams;
import BritefuryJ.DocPresent.StyleParams.ScriptStyleParams;
import BritefuryJ.DocPresent.StyleParams.ShapeStyleParams;
import BritefuryJ.DocPresent.StyleParams.TableStyleParams;
import BritefuryJ.DocPresent.StyleParams.TextStyleParams;
import BritefuryJ.DocPresent.StyleParams.VBoxStyleParams;

public class StyleSheetValues extends StyleSheet
{
	private static final String defaultFontFace = "Sans serif";
	private static final int defaultFontSize = 14;

	private static final Painter default_shapePainter = new FillPainter( Color.black );

	private static final Border default_border = new SolidBorder( 1.0, 2.0, Color.black, null );

	public static final float defaultFractionFontScale = 0.9f;
	public static final float defaultFractionMinFontScale = 0.9f;
	
	public static final float defaultScriptFontScale = 0.9f;
	public static final float defaultScriptMinFontScale = 0.9f;
	
	
	public static StyleSheetValues instance = new StyleSheetValues();
	
	
	protected StyleSheetValues()
	{
		super();
	}
	
	
	protected StyleSheetValues newInstance()
	{
		return new StyleSheetValues();
	}




	public StyleSheetValues withAttr(String fieldName, Object value)
	{
		return (StyleSheetValues)super.withAttr( fieldName, value );
	}
	
	public StyleSheetValues withAttrs(HashMap<String, Object> valuesMap)
	{
		return (StyleSheetValues)super.withAttrs( valuesMap );
	}
		
	public StyleSheetValues withAttrValues(AttributeValues attribs)
	{
		return (StyleSheetValues)super.withAttrValues( attribs );
	}
	
	public StyleSheetValues withoutAttr(String fieldName)
	{
		return (StyleSheetValues)super.withoutAttr( fieldName );
	}



	private Font styleSheetFont = null;
	
	public Font getFont()
	{
		if ( styleSheetFont == null )
		{
			String fontFace = getNonNull( "fontFace", String.class, defaultFontFace );
			boolean bBold = getNonNull( "fontBold", Boolean.class, false );
			boolean bItalic = getNonNull( "fontItalic", Boolean.class, false );
			int size = getNonNull( "fontSize", Integer.class, defaultFontSize );
			float scale = getNonNull( "fontScale", Float.class, 1.0f );
			int flags = ( bBold ? Font.BOLD : 0 )  |  ( bItalic ? Font.ITALIC : 0 );
			styleSheetFont = new Font( fontFace, flags, size ).deriveFont( (float)size * scale );
		}
		return styleSheetFont;
	}

	
	
	public static class BorderParams
	{
		public Border border;
		
		public BorderParams(Border border)
		{
			this.border = border;
		}
	}
	
	BorderParams borderParams = null;

	public BorderParams getBorderParams()
	{
		if ( borderParams == null )
		{
			borderParams = new BorderParams(
					get( "border", Border.class, default_border ) );
		}
		return borderParams;
	}

	
	private ContainerStyleParams containerParams = null;

	public ContainerStyleParams getContainerParams()
	{
		if ( containerParams == null )
		{
			containerParams = new ContainerStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ) );
		}
		return containerParams;
	}

	
	private ContentLeafStyleParams contentLeafParams = null;

	public ContentLeafStyleParams getContentLeafStyleParams()
	{
		if ( contentLeafParams == null )
		{
			contentLeafParams = new ContentLeafStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ) );
		}
		return contentLeafParams;
	}

	
	private FractionStyleParams fractionParams = null;

	public FractionStyleParams getFractionParams()
	{
		if ( fractionParams == null )
		{
			fractionParams = new FractionStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "fractionVSpacing", Double.class, 2.0 ),
					getNonNull( "fractionHPadding", Double.class, 3.0 ),
					getNonNull( "fractionRefYOffset", Double.class, 5.0 ),
					getFractionBarParams() );
		}
		return fractionParams;
	}
	
	
	private FractionStyleParams.BarStyleParams fractionBarParams = null;

	public FractionStyleParams.BarStyleParams getFractionBarParams()
	{
		if ( fractionBarParams == null )
		{
			fractionBarParams = new FractionStyleParams.BarStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "editable", Boolean.class, true ),
					getNonNull( "foreground", Paint.class, Color.black ),
					get( "hoverForeground", Paint.class, null ) );
		}
		return fractionBarParams;
	}
	
	
	private GridRowStyleParams gridRowParams = null;

	public GridRowStyleParams getGridRowParams()
	{
		if ( gridRowParams == null )
		{
			gridRowParams = new GridRowStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ) );
		}
		return gridRowParams;
	}
	
	
	private HBoxStyleParams hboxParams = null;

	public HBoxStyleParams getHBoxParams()
	{
		if ( hboxParams == null )
		{
			hboxParams = new HBoxStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "hboxSpacing", Double.class, 0.0 ) );
		}
		return hboxParams;
	}

	
	private MathRootStyleParams mathRootParams = null;

	public MathRootStyleParams getMathRootParams()
	{
		if ( mathRootParams == null )
		{
			mathRootParams = new MathRootStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getFont(),
					getNonNull( "foreground", Paint.class, Color.black ),
					get( "hoverForeground", Paint.class, null ),
					getNonNull( "mathRootThickness", Double.class, 1.5 ) );
		}
		return mathRootParams;
	}
	
	
	private ParagraphStyleParams paragraphParams = null;

	public ParagraphStyleParams getParagraphParams()
	{
		if ( paragraphParams == null )
		{
			paragraphParams = new ParagraphStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "paragraphSpacing", Double.class, 0.0 ),
					getNonNull( "paragraphLineSpacing", Double.class, 0.0 ),
					getNonNull( "paragraphIndentation", Double.class, 0.0 ) );
		}
		return paragraphParams;
	}
	
	
	private ShapeStyleParams shapeParams = null;

	public ShapeStyleParams getShapeParams()
	{
		if ( shapeParams == null )
		{
			shapeParams = new ShapeStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "shapePainter", Painter.class, default_shapePainter ),
					get( "hoverShapePainter", Painter.class, null ) );
		}
		return shapeParams;
	}
	
	
	private ScriptStyleParams scriptParams = null;

	public ScriptStyleParams getScriptParams()
	{
		if ( scriptParams == null )
		{
			scriptParams = new ScriptStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "scriptColumnSpacing", Double.class, 1.0 ),
					getNonNull( "scriptRowSpacing", Double.class, 1.0 ) );
		}
		return scriptParams;
	}
	
	
	private TextStyleParams staticTextParams = null;

	public TextStyleParams getStaticTextParams()
	{
		if ( staticTextParams == null )
		{
			staticTextParams = new TextStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					false,
					getFont(),
					getNonNull( "foreground", Paint.class, Color.black ),
					get( "hoverForeground", Paint.class, null ),
					get( "textSquiggleUnderlinePaint", Paint.class, null ),
					getNonNull( "textSmallCaps", Boolean.class, false ) );
		}
		return staticTextParams;
	}
	
	
	private TableStyleParams tableParams = null;

	public TableStyleParams getTableParams()
	{
		if ( tableParams == null )
		{
			tableParams = new TableStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "tableColumnSpacing", Double.class, 0.0 ),
					getNonNull( "tableColumnExpand", Boolean.class, false ),
					getNonNull( "tableRowSpacing", Double.class, 0.0 ),
					getNonNull( "tableRowExpand", Boolean.class, false ) );
		}
		return tableParams;
	}
	
	
	private TextStyleParams textParams = null;

	public TextStyleParams getTextParams()
	{
		if ( textParams == null )
		{
			textParams = new TextStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "editable", Boolean.class, true ),
					getFont(),
					getNonNull( "foreground", Paint.class, Color.black ),
					get( "hoverForeground", Paint.class, null ),
					get( "textSquiggleUnderlinePaint", Paint.class, null ),
					getNonNull( "textSmallCaps", Boolean.class, false ) );
		}
		return textParams;
	}
	
	
	private VBoxStyleParams vboxParams = null;

	public VBoxStyleParams getVBoxParams()
	{
		if ( vboxParams == null )
		{
			vboxParams = new VBoxStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "vboxSpacing", Double.class, 0.0 ) );
		}
		return vboxParams;
	}
}
