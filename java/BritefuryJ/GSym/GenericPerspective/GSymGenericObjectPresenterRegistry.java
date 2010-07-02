//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.GenericPerspective;

import java.awt.Color;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyTuple;
import org.python.core.PyType;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.ListView.ListViewStyleSheet;
import BritefuryJ.DocPresent.ListView.SeparatorElementFactory;
import BritefuryJ.DocPresent.ListView.SpanListViewLayoutStyleSheet;
import BritefuryJ.DocPresent.ListView.TrailingSeparator;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.ObjectPresentation.GSymObjectPresenterRegistry;
import BritefuryJ.GSym.ObjectPresentation.ObjectPresenter;
import BritefuryJ.GSym.ObjectPresentation.PyObjectPresenter;
import BritefuryJ.GSym.View.GSymFragmentView;

public class GSymGenericObjectPresenterRegistry extends GSymObjectPresenterRegistry
{
	public GSymGenericObjectPresenterRegistry()
	{
		registerJavaObjectPresenter( Character.class, presenter_Character );
		registerJavaObjectPresenter( String.class,  presenter_String );
		registerJavaObjectPresenter( Integer.class,  presenter_Integer );
		registerJavaObjectPresenter( Short.class,  presenter_Short );
		registerJavaObjectPresenter( Long.class,  presenter_Long );
		registerJavaObjectPresenter( Byte.class,  presenter_Byte );
		registerJavaObjectPresenter( Float.class,  presenter_Float );
		registerJavaObjectPresenter( Double.class,  presenter_Double );
		registerJavaObjectPresenter( Boolean.class,  presenter_Boolean );

		registerPythonObjectPresenter( PyTuple.TYPE,  presenter_PyTuple );
		registerPythonObjectPresenter( PyType.TYPE,  presenter_PyType );
		registerPythonObjectPresenter( PyFunction.TYPE,  presenter_PyFunction );
		
		registerJavaObjectPresenter( PyException.class,  presenter_PyException );

		registerJavaObjectPresenter( Exception.class,  presenter_Exception );
		registerJavaObjectPresenter( List.class,  presenter_List );
		registerJavaObjectPresenter( Map.class,  presenter_Map );
		registerJavaObjectPresenter( BufferedImage.class,  presenter_BufferedImage );
		registerJavaObjectPresenter( Shape.class,  presenter_Shape );
		registerJavaObjectPresenter( Color.class,  presenter_Color );
		registerJavaObjectPresenter( Class.class,  presenter_Class );
	}

	
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Boolean = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentBoolean( (Boolean)x, fragment, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Byte = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentByte( (Byte)x, fragment, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Character = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentChar( (Character)x, fragment, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Short = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentShort( (Short)x, fragment, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Integer = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentInt( (Integer)x, fragment, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Long = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentLong( (Long)x, fragment, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Float = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentDouble( ((Float)x).doubleValue(), fragment, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Double = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentDouble( (Double)x, fragment, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_String = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentString( (String)x, fragment, styleSheet, state );
		}
	};
	
	
	

	public static final PyObjectPresenter<GenericPerspectiveStyleSheet> presenter_PyTuple = new PyObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(PyObject x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			PyTuple tuple = (PyTuple)x;
			
			ArrayList<DPElement> itemViews = new ArrayList<DPElement>();
			for (Object item: tuple)
			{
				itemViews.add( fragment.presentFragmentWithGenericPerspective( item ) );
			}
			
			return tupleListViewStyle.createListElement( itemViews, TrailingSeparator.NEVER );
		}
	};

	public static final PyObjectPresenter<GenericPerspectiveStyleSheet> presenter_PyType = new PyObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(PyObject x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			PyType type = (PyType)x;
			
			ArrayList<DPElement> lines = new ArrayList<DPElement>();
			
			ArrayList<DPElement> header = new ArrayList<DPElement>();
			header.add( classKeywordStyle.staticText( "Class " ) );
			header.add( classNameStyle.staticText( type.getName() ) );
			
			PyTuple bases = (PyTuple)type.getBases();
			
			if ( bases.size() > 0 )
			{
				header.add( staticStyle.staticText( " " ) );
				header.add( classPunctuationStyle.staticText( "(" ) );
				header.add( staticStyle.paragraphIndentMarker() );
				boolean bFirst = true;
				for (PyObject base: bases.getArray())
				{
					PyType baseType = (PyType)base;
					if ( !bFirst )
					{
						header.add( classPunctuationStyle.staticText( ", " ) );
					}
					header.add( classNameStyle.staticText( baseType.getName() ) );
					bFirst = false;
				}
				header.add( staticStyle.paragraphDedentMarker() );
				header.add( classPunctuationStyle.staticText( ")" ) );
			}
			
			lines.add( staticStyle.paragraph( header ) );
			
			return styleSheet.objectBoxWithFields( "Python Class", lines.toArray( new DPElement[0] ) );
		}
	};

	private static PyFunction pyFunction_inspectFn = null;
	public static final PyObjectPresenter<GenericPerspectiveStyleSheet> presenter_PyFunction = new PyObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		
		private PyFunction getInspectFunction()
		{
			if ( pyFunction_inspectFn == null )
			{
				String code = "import inspect\n" +
				"\n" +
				"def _flatten(xs):\n" +
				"	ys = []\n" +
				"	for x in xs:\n" +
				"		if isinstance( x, list ):\n" +
				"			ys.extend( _flatten( x ) )\n" +
				"		else:\n" +
				"			ys.append( x )\n" +
				"	return ys\n" +
				"\n" +
				"def inspectFunction(f):\n" +
				"	args, varargs, varkw, defaults = inspect.getargspec( f )\n" +
				"	args = _flatten( args )\n" +
				"	kwargs = []\n" + 
				"	if defaults is not None:\n" +
				"		kwargs = args[-len(defaults):]\n" +
				"		del args[-len(defaults):]\n" +
				"	return args, kwargs, varargs, varkw\n";
				
				PyDictionary locals = new PyDictionary();
				
				Py.exec( new PyString( code ), locals, locals );
				
				pyFunction_inspectFn = (PyFunction)locals.get( new PyString( "inspectFunction" ) );
			}
			
			return pyFunction_inspectFn;
		}
		
		private PyTuple inspectFunction(PyFunction fun)
		{
			return (PyTuple)getInspectFunction().__call__( fun );
		}
		
		
		
		
		
		public DPElement presentObject(PyObject x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			PyFunction fun = (PyFunction)x;
			
			PyTuple argSpec = inspectFunction( fun );
			PyObject args = argSpec.getArray()[0];
			PyObject kwargs = argSpec.getArray()[1];
			PyObject varargs = argSpec.getArray()[2];
			PyObject varkw = argSpec.getArray()[3];
			
			
			ArrayList<DPElement> lines = new ArrayList<DPElement>();
			
			ArrayList<DPElement> header = new ArrayList<DPElement>();
			header.add( fnNameStyle.staticText( fun.__name__.toString() ) );
			header.add( fnPunctuationStyle.staticText( "(" ) );
			boolean bFirst = true;
			for (PyObject arg: ((PyList)args).getArray())
			{
				if ( !bFirst )
				{
					header.add( fnPunctuationStyle.staticText( ", " ) );
				}
				header.add( fnArgStyle.staticText( arg.toString() ) );
				bFirst = false;
			}
			for (PyObject arg: ((PyList)kwargs).getArray())
			{
				if ( !bFirst )
				{
					header.add( fnPunctuationStyle.staticText( ", " ) );
				}
				header.add( fnKWArgStyle.staticText( arg.toString() ) );
				bFirst = false;
			}
			if ( varargs != Py.None )
			{
				if ( !bFirst )
				{
					header.add( fnPunctuationStyle.staticText( ", " ) );
				}
				header.add( fnPunctuationStyle.staticText( "*" ) );
				header.add( fnVarArgStyle.staticText( varargs.toString() ) );
				bFirst = false;
			}
			if ( varkw != Py.None )
			{
				if ( !bFirst )
				{
					header.add( fnPunctuationStyle.staticText( ", " ) );
				}
				header.add( fnPunctuationStyle.staticText( "**" ) );
				header.add( fnVarArgStyle.staticText( varkw.toString() ) );
				bFirst = false;
			}
			header.add( fnPunctuationStyle.staticText( ")" ) );
			
			lines.add( staticStyle.paragraph( header ) );
			
			return styleSheet.objectBoxWithFields( "Python Function", lines.toArray( new DPElement[0] ) );
		}
	};

	

	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_PyException = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			PyException e = (PyException)x;
			
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			e.printStackTrace( new PrintStream( buf ) );
			String stackTrace = e.toString();
			String stackTraceLines[] = stackTrace.split( "\n" );
			DPElement stackTraceElements[] = new DPElement[stackTraceLines.length];
			
			for (int i = 0; i < stackTraceLines.length; i++)
			{
				stackTraceElements[i] = stackTraceStyle.staticText( stackTraceLines[i] );
			}
			
			DPElement fields[] = {
					styleSheet.verticalObjectField( "Message", fragment.presentFragmentWithGenericPerspective( e.value ) ),
					styleSheet.verticalObjectField( "Traceback", stackTraceStyle.vbox( stackTraceElements ) )
			};
			
			return styleSheet.errorBoxWithFields( "PYTHON EXCEPTION", fields );
		}
	};

	
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Exception = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			Exception e = (Exception)x;
			
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			e.printStackTrace( new PrintStream( buf ) );
			String stackTrace = e.toString();
			String stackTraceLines[] = stackTrace.split( "\n" );
			DPElement stackTraceElements[] = new DPElement[stackTraceLines.length];
			
			for (int i = 0; i < stackTraceLines.length; i++)
			{
				stackTraceElements[i] = stackTraceStyle.staticText( stackTraceLines[i] );
			}
			
			DPElement fields[] = {
					styleSheet.verticalObjectField( "Message", fragment.presentFragmentWithGenericPerspective( e.getMessage() ) ),
					styleSheet.verticalObjectField( "Traceback", stackTraceStyle.vbox( stackTraceElements ) )
			};
			
			return styleSheet.errorBoxWithFields( "JAVA EXCEPTION", fields );
		}
	};


	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_List = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			List<?> list = (List<?>)x;
			
			ArrayList<DPElement> itemViews = new ArrayList<DPElement>();
			for (Object item: list)
			{
				itemViews.add( fragment.presentFragmentWithGenericPerspective( item ) );
			}
			
			return listListViewStyle.createListElement( itemViews, TrailingSeparator.NEVER );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Map = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			Map<?,?> map = (Map<?,?>)x;
			
			ArrayList<DPElement> itemViews = new ArrayList<DPElement>();
			for (Map.Entry<?,?> entry: map.entrySet())
			{
				DPElement lineElems[] = { fragment.presentFragmentWithGenericPerspective( entry.getKey() ), staticStyle.whitespace( " ", 10.0 ), delimStyle.staticText( ":" ),
						staticStyle.whitespace( " ", 10.0 ), fragment.presentFragmentWithGenericPerspective( entry.getValue() ) };
				itemViews.add( staticStyle.span( lineElems ) );
			}
			
			return mapListViewStyle.createListElement( itemViews, TrailingSeparator.NEVER );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Shape = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			Shape shape = (Shape)x;
//			Rectangle2D bounds = shape.getBounds2D();
//			double offsetX = -bounds.getMinX(), offsetY = -bounds.getMinY();
//			double width = bounds.getWidth(), height = bounds.getHeight();
//			
//			double scale = 1.0;
//			if ( width > height  &&  width > 96.0 )
//			{
//				scale = 96.0 / width;
//			}
//			else if ( height > width  &&  height > 96.0 )
//			{
//				scale = 96.0 / height;
//			}
			
			return styleSheet.objectBox( x.getClass().getName(), staticStyle.shape( shape ) );
		}
	};

	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_BufferedImage = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			BufferedImage image = (BufferedImage)x;
			double width = (double)image.getWidth();
			double height = (double)image.getHeight();
			
			if ( width > height  &&  width > 96.0 )
			{
				height *= ( 96.0 / width );
				width = 96.0;
			}
			else if ( height > width  &&  height > 96.0 )
			{
				width *= ( 96.0 / height );
				height = 96.0;
			}
			
			return staticStyle.image( image, width, height );
		}
	};

	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Color = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			Color colour = (Color)x;
			
			DPElement title = colourObjectBoxStyle.objectTitle( "java.awt.Color" );
			
			DPElement red = colourRedStyle.staticText( "R=" + String.valueOf( colour.getRed() ) );
			DPElement green = colourGreenStyle.staticText( "G=" + String.valueOf( colour.getGreen() ) );
			DPElement blue = colourBlueStyle.staticText( "B=" + String.valueOf( colour.getBlue() ) );
			DPElement alpha = colourAlphaStyle.staticText( "A=" + String.valueOf( colour.getAlpha() ) );
			
			DPElement components = colourBoxStyle.hbox( new DPElement[] { red, green, blue, alpha } );
			
			DPElement textBox = staticStyle.vbox( new DPElement[] { title, components } );
			
			DPElement swatch = staticStyle.withShapePainter( new FillPainter( colour ) ).box( 50.0, 20.0 ).alignVExpand();
			
			DPElement contents = colourBoxStyle.hbox( new DPElement[] { textBox, swatch } );
			
			return colourObjectBoxStyle.objectBorder( contents );
		}
	};

	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Class = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			Class<?> cls = (Class<?>)x;
			Class<?> superClass = cls.getSuperclass();
			Class<?> interfaces[] = cls.getInterfaces();
			
			ArrayList<DPElement> lines = new ArrayList<DPElement>();
			
			DPElement title = classKeywordStyle.staticText( "Class " );
			DPElement name = classNameStyle.staticText( cls.getName() );
			lines.add( staticStyle.hbox( new DPElement[] { title, name } ) );
			
			if ( superClass != null  ||  interfaces.length > 0 )
			{
				ArrayList<DPElement> inheritance = new ArrayList<DPElement>();
				if ( superClass != null )
				{
					inheritance.add( classKeywordStyle.staticText( "Extends " ) );
					inheritance.add( classNameStyle.staticText( superClass.getName() ) );
				}
				
				if ( interfaces.length > 0 )
				{
					if ( superClass != null )
					{
						inheritance.add( classKeywordStyle.staticText( " " ) );
					}
					inheritance.add( classKeywordStyle.staticText( "Implements " ) );
					boolean bFirst = true;
					for (Class<?> iface: interfaces)
					{
						if ( !bFirst )
						{
							inheritance.add( classPunctuationStyle.staticText( ", " ) );
						}
						inheritance.add( classNameStyle.staticText( iface.getName() ) );
						bFirst = false;
					}
				}
				
				DPElement para = staticStyle.paragraph( inheritance );
				lines.add( para.padX( 45.0, 0.0 ) );
			}
			
			return styleSheet.objectBoxWithFields( "Java Class", lines.toArray( new DPElement[0] ) );
		}
	};




	private static final PrimitiveStyleSheet staticStyle = PrimitiveStyleSheet.instance.withNonEditable();
	
	
	private static final PrimitiveStyleSheet punctuationStyle = staticStyle.withForeground( Color.blue );
	private static final PrimitiveStyleSheet delimStyle = staticStyle.withForeground( new Color( 0.1f, 0.3f, 0.4f ) ).withFontBold( true ).withFontSize( 14 );
	
	
	private static final PrimitiveStyleSheet stackTraceStyle = staticStyle.withForeground( new Color( 0.75f, 0.1f, 0.4f ) );

	
	private static final GenericPerspectiveStyleSheet colourObjectBoxStyle = GenericPerspectiveStyleSheet.instance.withObjectBorderAndTitlePaint( new Color( 0.0f, 0.1f, 0.4f ) );
	private static final PrimitiveStyleSheet colourRedStyle = staticStyle.withFontSize( 12 ).withForeground( new Color( 0.75f, 0.0f, 0.0f ) );
	private static final PrimitiveStyleSheet colourGreenStyle = staticStyle.withFontSize( 12 ).withForeground( new Color( 0.0f, 0.75f, 0.0f ) );
	private static final PrimitiveStyleSheet colourBlueStyle = staticStyle.withFontSize( 12 ).withForeground( new Color( 0.0f, 0.0f, 0.75f ) );
	private static final PrimitiveStyleSheet colourAlphaStyle = staticStyle.withFontSize( 12 ).withForeground( new Color( 0.3f, 0.3f, 0.3f ) );
	private static final PrimitiveStyleSheet colourBoxStyle = staticStyle.withHBoxSpacing( 5.0 );

	
	private static final PrimitiveStyleSheet classKeywordStyle = staticStyle.withForeground( new Color( 0.0f, 0.0f, 0.5f ) ).withFontBold( true ).withTextSmallCaps( true );
	private static final PrimitiveStyleSheet classPunctuationStyle = staticStyle.withForeground( new Color( 0.25f, 0.0f, 0.5f ) );
	private static final PrimitiveStyleSheet classNameStyle = staticStyle.withForeground( new Color( 0.0f, 0.25f, 0.5f ) );

	private static final PrimitiveStyleSheet fnPunctuationStyle = staticStyle.withForeground( new Color( 0.25f, 0.0f, 0.5f ) );
	private static final PrimitiveStyleSheet fnNameStyle = staticStyle.withForeground( new Color( 0.0f, 0.25f, 0.5f ) );
	private static final PrimitiveStyleSheet fnArgStyle = staticStyle.withForeground( new Color( 0.0f, 0.5f, 0.25f ) );
	private static final PrimitiveStyleSheet fnKWArgStyle = staticStyle.withForeground( new Color( 0.0f, 0.5f, 0.25f ) ).withFontItalic( true );
	private static final PrimitiveStyleSheet fnVarArgStyle = staticStyle.withForeground( new Color( 0.0f, 0.5f, 0.25f ) );


	private static final SeparatorElementFactory commaFactory = new SeparatorElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet, int index, DPElement child)
		{
			return punctuationStyle.staticText( "," );
		}
	};

	private static final ElementFactory spaceFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return staticStyle.staticText( " " );
		}
	};
	
	private static final ElementFactory mapSpaceFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return staticStyle.whitespace( " ", 25.0 );
		}
	};
	
	private static final ElementFactory openBracketFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return delimStyle.staticText( "[ " );
		}
	};
	
	private static final ElementFactory closeBracketFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return delimStyle.staticText( " ]" );
		}
	};
	
	private static final ElementFactory openParenFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return delimStyle.staticText( "( " );
		}
	};
	
	private static final ElementFactory closeParenFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return delimStyle.staticText( " )" );
		}
	};
	
	private static final ElementFactory openBraceFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return delimStyle.staticText( "{ " );
		}
	};
	
	private static final ElementFactory closeBraceFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return delimStyle.staticText( " }" );
		}
	};
	
	
	private static SpanListViewLayoutStyleSheet span_listViewLayout = SpanListViewLayoutStyleSheet.instance.withAddLineBreaks( true ).withAddParagraphIndentMarkers( true ).withAddLineBreakCost( true );
	private static ListViewStyleSheet listListViewStyle = ListViewStyleSheet.instance.withSeparatorFactory( commaFactory ).withSpacingFactory( spaceFactory )
		.withBeginDelimFactory( openBracketFactory ).withEndDelimFactory( closeBracketFactory ).withListLayout( span_listViewLayout );
	private static ListViewStyleSheet tupleListViewStyle = ListViewStyleSheet.instance.withSeparatorFactory( commaFactory ).withSpacingFactory( spaceFactory )
		.withBeginDelimFactory( openParenFactory ).withEndDelimFactory( closeParenFactory ).withListLayout( span_listViewLayout );
	private static ListViewStyleSheet mapListViewStyle = ListViewStyleSheet.instance.withSeparatorFactory( commaFactory ).withSpacingFactory( mapSpaceFactory )
		.withBeginDelimFactory( openBraceFactory ).withEndDelimFactory( closeBraceFactory ).withListLayout( span_listViewLayout );
}
