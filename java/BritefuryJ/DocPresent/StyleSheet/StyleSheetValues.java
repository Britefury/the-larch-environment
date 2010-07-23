//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.StyleSheet;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Paint;
import java.util.HashMap;

import BritefuryJ.AttributeTable.AttributeBase;
import BritefuryJ.AttributeTable.AttributeTable2;
import BritefuryJ.DocPresent.Border.Border;
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

public class StyleSheetValues extends AttributeTable2
{
	
	
	public static StyleSheetValues instance = new StyleSheetValues();
	
	
	protected StyleSheetValues()
	{
		super();
	}
	
	
	protected StyleSheetValues newInstance()
	{
		return new StyleSheetValues();
	}




	public StyleSheetValues withAttr(AttributeBase fieldName, Object value)
	{
		return (StyleSheetValues)super.withAttr( fieldName, value );
	}
	
	public StyleSheetValues withAttrs(HashMap<AttributeBase, Object> valuesMap)
	{
		return (StyleSheetValues)super.withAttrs( valuesMap );
	}
		
	public StyleSheetValues withAttrs(AttributeTable2 attribs)
	{
		return (StyleSheetValues)super.withAttrs( attribs );
	}
		
	public StyleSheetValues withoutAttr(AttributeBase fieldName)
	{
		return (StyleSheetValues)super.withoutAttr( fieldName );
	}
	
	public StyleSheetValues useAttr(AttributeBase fieldName)
	{
		return (StyleSheetValues)super.useAttr( fieldName );
	}

	

	
	
	private Font styleSheetFont = null;
	
	public Font getFont()
	{
		if ( styleSheetFont == null )
		{
			String face = get( StyleSheet2.fontFace, String.class );
			boolean bBold = get( StyleSheet2.fontBold, Boolean.class );
			boolean bItalic = get( StyleSheet2.fontItalic, Boolean.class );
			int size = get( StyleSheet2.fontSize, Integer.class );
			double scale = get( StyleSheet2.fontScale, Double.class );
			int flags = ( bBold ? Font.BOLD : 0 )  |  ( bItalic ? Font.ITALIC : 0 );
			styleSheetFont = new Font( face, flags, size ).deriveFont( (float)( size * scale ) );
		}
		return styleSheetFont;
	}
	
	public StyleSheetValues useFont()
	{
		return useAttr( StyleSheet2.fontFace ).useAttr( StyleSheet2.fontBold ).useAttr( StyleSheet2.fontItalic ).useAttr( StyleSheet2.fontSize ).useAttr( StyleSheet2.fontScale );
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
					get( StyleSheet2.border, Border.class ) );
		}
		return borderParams;
	}
	
	public StyleSheetValues useBorderParams()
	{
		return useAttr( StyleSheet2.border );
	}

	
	
	private ContainerStyleParams containerParams = null;

	public ContainerStyleParams getContainerParams()
	{
		if ( containerParams == null )
		{
			containerParams = new ContainerStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ) );
		}
		return containerParams;
	}
	
	public StyleSheetValues useContainerParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor );
	}

	
	
	private ContentLeafStyleParams contentLeafParams = null;

	public ContentLeafStyleParams getContentLeafStyleParams()
	{
		if ( contentLeafParams == null )
		{
			contentLeafParams = new ContentLeafStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ) );
		}
		return contentLeafParams;
	}
	
	public StyleSheetValues useContentLeafParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor );
	}

	
	
	private FractionStyleParams fractionParams = null;

	public FractionStyleParams getFractionParams()
	{
		if ( fractionParams == null )
		{
			fractionParams = new FractionStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ),
					get( StyleSheet2.fractionVSpacing, Double.class ),
					get( StyleSheet2.fractionHPadding, Double.class ),
					get( StyleSheet2.fractionRefYOffset, Double.class ),
					getFractionBarParams() );
		}
		return fractionParams;
	}
	
	public StyleSheetValues useFractionParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor )
				.useAttr( StyleSheet2.fractionVSpacing ).useAttr( StyleSheet2.fractionHPadding ).useAttr( StyleSheet2.fractionRefYOffset );
	}
	
	
	
	private FractionStyleParams.BarStyleParams fractionBarParams = null;

	public FractionStyleParams.BarStyleParams getFractionBarParams()
	{
		if ( fractionBarParams == null )
		{
			fractionBarParams = new FractionStyleParams.BarStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ),
					get( StyleSheet2.editable, Boolean.class ),
					get( StyleSheet2.foreground, Paint.class ),
					get( StyleSheet2.hoverForeground, Paint.class ) );
		}
		return fractionBarParams;
	}
	
	public StyleSheetValues useFractionBarParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor )
				.useAttr( StyleSheet2.editable ).useAttr( StyleSheet2.foreground ).useAttr( StyleSheet2.hoverForeground );
	}
	
	
	
	private GridRowStyleParams gridRowParams = null;

	public GridRowStyleParams getGridRowParams()
	{
		if ( gridRowParams == null )
		{
			gridRowParams = new GridRowStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ) );
		}
		return gridRowParams;
	}
	
	public StyleSheetValues useGridRowParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor );
	}
	
	
	
	private HBoxStyleParams hboxParams = null;

	public HBoxStyleParams getHBoxParams()
	{
		if ( hboxParams == null )
		{
			hboxParams = new HBoxStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ),
					get( StyleSheet2.hboxSpacing, Double.class ) );
		}
		return hboxParams;
	}
	
	public StyleSheetValues useHBoxParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor ).useAttr( StyleSheet2.hboxSpacing );
	}

	
	
	private MathRootStyleParams mathRootParams = null;

	public MathRootStyleParams getMathRootParams()
	{
		if ( mathRootParams == null )
		{
			mathRootParams = new MathRootStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ),
					getFont(),
					get( StyleSheet2.foreground, Paint.class ),
					get( StyleSheet2.hoverForeground, Paint.class ),
					get( StyleSheet2.mathRootThickness, Double.class ) );
		}
		return mathRootParams;
	}
	
	public StyleSheetValues useMathRootParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor ).useFont()
				.useAttr( StyleSheet2.foreground ).useAttr( StyleSheet2.hoverForeground ).useAttr( StyleSheet2.mathRootThickness );
	}
	
	
	
	private ParagraphStyleParams paragraphParams = null;

	public ParagraphStyleParams getParagraphParams()
	{
		if ( paragraphParams == null )
		{
			paragraphParams = new ParagraphStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ),
					get( StyleSheet2.paragraphSpacing, Double.class ),
					get( StyleSheet2.paragraphLineSpacing, Double.class ),
					get( StyleSheet2.paragraphIndentation, Double.class ) );
		}
		return paragraphParams;
	}
	
	public StyleSheetValues useParagraphParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor )
				.useAttr( StyleSheet2.paragraphSpacing ).useAttr( StyleSheet2.paragraphLineSpacing ).useAttr( StyleSheet2.paragraphIndentation );
	}

	
	
	private ShapeStyleParams shapeParams = null;

	public ShapeStyleParams getShapeParams()
	{
		if ( shapeParams == null )
		{
			shapeParams = new ShapeStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ),
					get( StyleSheet2.shapePainter, Painter.class ),
					get( StyleSheet2.hoverShapePainter, Painter.class ) );
		}
		return shapeParams;
	}
	
	public StyleSheetValues useShapeParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor )
				.useAttr( StyleSheet2.shapePainter ).useAttr( StyleSheet2.hoverShapePainter );
	}
	
	
	
	private ScriptStyleParams scriptParams = null;

	public ScriptStyleParams getScriptParams()
	{
		if ( scriptParams == null )
		{
			scriptParams = new ScriptStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ),
					get( StyleSheet2.scriptColumnSpacing, Double.class ),
					get( StyleSheet2.scriptRowSpacing, Double.class ) );
		}
		return scriptParams;
	}
	
	public StyleSheetValues useScriptParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor )
				.useAttr( StyleSheet2.scriptColumnSpacing ).useAttr( StyleSheet2.scriptRowSpacing );
	}

	
	
	private TextStyleParams staticTextParams = null;

	public TextStyleParams getStaticTextParams()
	{
		if ( staticTextParams == null )
		{
			staticTextParams = new TextStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ),
					false,
					getFont(),
					get( StyleSheet2.foreground, Paint.class ),
					get( StyleSheet2.hoverForeground, Paint.class ),
					get( StyleSheet2.textSquiggleUnderlinePaint, Paint.class ),
					get( StyleSheet2.textSmallCaps, Boolean.class ) );
		}
		return staticTextParams;
	}
	
	public StyleSheetValues useStaticTextParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor ).useFont()
				.useAttr( StyleSheet2.foreground ).useAttr( StyleSheet2.hoverForeground ).useAttr( StyleSheet2.textSquiggleUnderlinePaint ).useAttr( StyleSheet2.textSmallCaps );
	}

	
	
	private TextStyleParams labelTextParams = null;

	public TextStyleParams getLabelTextParams()
	{
		if ( labelTextParams == null )
		{
			labelTextParams = new TextStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ),
					false,
					getFont(),
					get( StyleSheet2.foreground, Paint.class ),
					get( StyleSheet2.hoverForeground, Paint.class ),
					get( StyleSheet2.textSquiggleUnderlinePaint, Paint.class ),
					get( StyleSheet2.textSmallCaps, Boolean.class ) );
		}
		return labelTextParams;
	}
	
	public StyleSheetValues useLabelTextParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor ).useFont()
				.useAttr( StyleSheet2.foreground ).useAttr( StyleSheet2.hoverForeground ).useAttr( StyleSheet2.textSquiggleUnderlinePaint ).useAttr( StyleSheet2.textSmallCaps );
	}
	
	
	
	private TextStyleParams textParams = null;

	public TextStyleParams getTextParams()
	{
		if ( textParams == null )
		{
			textParams = new TextStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ),
					get( StyleSheet2.editable, Boolean.class ),
					getFont(),
					get( StyleSheet2.foreground, Paint.class ),
					get( StyleSheet2.hoverForeground, Paint.class ),
					get( StyleSheet2.textSquiggleUnderlinePaint, Paint.class ),
					get( StyleSheet2.textSmallCaps, Boolean.class ) );
		}
		return textParams;
	}
	
	public StyleSheetValues useTextParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor ).useFont()
				.useAttr( StyleSheet2.editable ).useAttr( StyleSheet2.foreground ).useAttr( StyleSheet2.hoverForeground ).useAttr( StyleSheet2.textSquiggleUnderlinePaint )
				.useAttr( StyleSheet2.textSmallCaps );
	}
	
	
	
	private TableStyleParams tableParams = null;

	public TableStyleParams getTableParams()
	{
		if ( tableParams == null )
		{
			tableParams = new TableStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ),
					get( StyleSheet2.tableColumnSpacing, Double.class ),
					get( StyleSheet2.tableColumnExpand, Boolean.class ),
					get( StyleSheet2.tableRowSpacing, Double.class ),
					get( StyleSheet2.tableRowExpand, Boolean.class ) );
		}
		return tableParams;
	}
	
	public StyleSheetValues useTableParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor )
				.useAttr( StyleSheet2.tableColumnSpacing ).useAttr( StyleSheet2.tableColumnExpand ).useAttr( StyleSheet2.tableRowSpacing ).useAttr( StyleSheet2.tableRowExpand );
	}
	
	
	
	private VBoxStyleParams vboxParams = null;

	public VBoxStyleParams getVBoxParams()
	{
		if ( vboxParams == null )
		{
			vboxParams = new VBoxStyleParams(
					get( StyleSheet2.background, Painter.class ),
					get( StyleSheet2.hoverBackground, Painter.class ),
					get( StyleSheet2.cursor, Cursor.class ),
					get( StyleSheet2.vboxSpacing, Double.class ) );
		}
		return vboxParams;
	}
	
	public StyleSheetValues useVBoxParams()
	{
		return useAttr( StyleSheet2.background ).useAttr( StyleSheet2.hoverBackground ).useAttr( StyleSheet2.cursor ).useAttr( StyleSheet2.vboxSpacing );
	}
}
