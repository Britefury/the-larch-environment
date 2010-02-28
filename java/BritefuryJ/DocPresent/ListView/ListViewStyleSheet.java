//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ListView;

import java.util.List;

import org.python.core.PyObject;

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.PyElementFactory;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class ListViewStyleSheet extends StyleSheet
{
	private static class ListViewParams
	{
		private PrimitiveStyleSheet primitiveStyle;
		private ElementFactory beginDelim, endDelim;
		private SeparatorElementFactory separator;
		private ElementFactory spacing;
		private ListViewLayoutStyleSheet listLayout;
		
		private ListViewParams(PrimitiveStyleSheet primitiveStyle, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator,
				ElementFactory spacing, ListViewLayoutStyleSheet listLayout)
		{
			this.primitiveStyle = primitiveStyle;
			this.beginDelim = beginDelim;
			this.endDelim = endDelim;
			this.separator = separator;
			this.spacing = spacing;
			this.listLayout = listLayout;
		}
	}
	
	
	private ListViewParams listViewParams = null;
	
	
	public static ListViewStyleSheet instance = new ListViewStyleSheet();
	
	
	public ListViewStyleSheet()
	{
		super();
		
		initAttr( "primitiveStyle", PrimitiveStyleSheet.instance );
		initAttr( "beginDelimFactory", null );
		initAttr( "endDelimFactory", null );
		initAttr( "separatorFactory", null );
		initAttr( "spacingFactory", null );
		initAttr( "listLayout", ParagraphListViewLayoutStyleSheet.instance );
	}
		

	
	protected StyleSheet newInstance()
	{
		return new ListViewStyleSheet();
	}
	

	
	
	//
	// GENERAL
	//
	
	public ListViewStyleSheet withPrimitiveStyle(PrimitiveStyleSheet primitiveStyle)
	{
		return (ListViewStyleSheet)withAttr( "primitiveStyle", primitiveStyle );
	}

	public ListViewStyleSheet withBeginDelimFactory(ElementFactory beginDelimFactory)
	{
		return (ListViewStyleSheet)withAttr( "beginDelimFactory", beginDelimFactory );
	}

	public ListViewStyleSheet withBeginDelimFactory(PyObject beginDelimFactory)
	{
		return (ListViewStyleSheet)withAttr( "beginDelimFactory", new PyElementFactory( beginDelimFactory ) );
	}

	public ListViewStyleSheet withEndDelimFactory(ElementFactory endDelimFactory)
	{
		return (ListViewStyleSheet)withAttr( "endDelimFactory", endDelimFactory );
	}

	public ListViewStyleSheet withEndDelimFactory(PyObject endDelimFactory)
	{
		return (ListViewStyleSheet)withAttr( "endDelimFactory", new PyElementFactory( endDelimFactory ) );
	}

	public ListViewStyleSheet withSeparatorFactory(SeparatorElementFactory separatorFactory)
	{
		return (ListViewStyleSheet)withAttr( "separatorFactory", separatorFactory );
	}

	public ListViewStyleSheet withSeparatorFactory(PyObject separatorFactory)
	{
		return (ListViewStyleSheet)withAttr( "separatorFactory", new PySeparatorElementFactory( separatorFactory ) );
	}

	public ListViewStyleSheet withSpacingFactory(ElementFactory spacingFactory)
	{
		return (ListViewStyleSheet)withAttr( "spacingFactory", spacingFactory );
	}

	public ListViewStyleSheet withSpacingFactory(PyObject spacingFactory)
	{
		return (ListViewStyleSheet)withAttr( "spacingFactory", new PyElementFactory( spacingFactory ) );
	}

	public ListViewStyleSheet withListLayout(ListViewLayoutStyleSheet listLayout)
	{
		return (ListViewStyleSheet)withAttr( "listLayout", listLayout );
	}


	

	
	
	//
	// PARAMS
	//
	private ListViewParams getListViewParams()
	{
		if ( listViewParams == null )
		{
			listViewParams = new ListViewParams(
					get( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance ),
					get( "beginDelimFactory", ElementFactory.class, null ),
					get( "endDelimFactory", ElementFactory.class, null ),
					get( "separatorFactory", SeparatorElementFactory.class, null ),
					get( "spacingFactory", ElementFactory.class, null ),
					get( "listLayout", ListViewLayoutStyleSheet.class, ParagraphListViewLayoutStyleSheet.instance ) );
		}
		return listViewParams;
	}

	
	
	
	//
	// LIST VIEW
	//
	
	public DPWidget createListElement(List<DPWidget> children, TrailingSeparator trailingSeparator)
	{
		ListViewParams params = getListViewParams();
		
		return params.listLayout.createListElement( children, params.primitiveStyle, params.beginDelim, params.endDelim, params.separator,
				params.spacing, trailingSeparator );
	}




	public static boolean trailingSeparatorRequired(List<DPWidget> children, TrailingSeparator trailingSeparator)
	{
		return children.size() > 0  &&  ( trailingSeparator == TrailingSeparator.ALWAYS  ||  ( trailingSeparator == TrailingSeparator.ONE_ELEMENT && children.size() == 1 ) );
	}
}
