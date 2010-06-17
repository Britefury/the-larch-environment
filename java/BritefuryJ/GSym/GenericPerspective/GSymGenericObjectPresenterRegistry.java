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
import java.util.ArrayList;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyDictionary;
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
		
		registerJavaObjectPresenter( List.class,  presenter_List );
		registerJavaObjectPresenter( BufferedImage.class,  presenter_BufferedImage );
		registerJavaObjectPresenter( Shape.class,  presenter_Shape );
		registerJavaObjectPresenter( Color.class,  presenter_Color );
		registerJavaObjectPresenter( Class.class,  presenter_Class );
	}

	
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Boolean = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentBoolean( (Boolean)x, ctx, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Byte = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentByte( (Byte)x, ctx, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Character = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentChar( (Character)x, ctx, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Short = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentShort( (Short)x, ctx, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Integer = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentInt( (Integer)x, ctx, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Long = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentLong( (Long)x, ctx, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Float = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentDouble( ((Float)x).doubleValue(), ctx, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Double = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentDouble( (Double)x, ctx, styleSheet, state );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_String = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return GSymPrimitivePresenter.presentString( (String)x, ctx, styleSheet, state );
		}
	};
	
	
	

	public static final PyObjectPresenter<GenericPerspectiveStyleSheet> presenter_PyTuple = new PyObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(PyObject x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			PyTuple tuple = (PyTuple)x;
			
			ArrayList<DPElement> itemViews = new ArrayList<DPElement>();
			for (Object item: tuple)
			{
				itemViews.add( ctx.presentFragmentWithGenericPerspective( item ) );
			}
			
			return tupleListViewStyle.createListElement( itemViews, TrailingSeparator.NEVER );
		}
	};

	public static final PyObjectPresenter<GenericPerspectiveStyleSheet> presenter_PyType = new PyObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(PyObject x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			PyType type = (PyType)x;
			
			ArrayList<DPElement> lines = new ArrayList<DPElement>();
			
			ArrayList<DPElement> header = new ArrayList<DPElement>();
			header.add( classKeywordStyle.staticText( "Class " ) );
			header.add( classNameStyle.staticText( type.getName() ) );
			
			PyTuple bases = (PyTuple)type.getBases();
			
			if ( bases.size() > 0 )
			{
				header.add( PrimitiveStyleSheet.instance.staticText( " " ) );
				header.add( classPunctuationStyle.staticText( "(" ) );
				header.add( PrimitiveStyleSheet.instance.paragraphIndentMarker() );
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
				header.add( PrimitiveStyleSheet.instance.paragraphDedentMarker() );
				header.add( classPunctuationStyle.staticText( ")" ) );
			}
			
			lines.add( PrimitiveStyleSheet.instance.paragraph( header ) );
			
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
		
		
		
		
		
		public DPElement presentObject(PyObject x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
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
			
			lines.add( PrimitiveStyleSheet.instance.paragraph( header ) );
			
			return styleSheet.objectBoxWithFields( "Python Function", lines.toArray( new DPElement[0] ) );
		}
	};

	

	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_List = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			List<?> list = (List<?>)x;
			
			ArrayList<DPElement> itemViews = new ArrayList<DPElement>();
			for (Object item: list)
			{
				itemViews.add( ctx.presentFragmentWithGenericPerspective( item ) );
			}
			
			return listListViewStyle.createListElement( itemViews, TrailingSeparator.NEVER );
		}
	};
	
	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Shape = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
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
			
			return styleSheet.objectBox( x.getClass().getName(), PrimitiveStyleSheet.instance.shape( shape ) );
		}
	};

	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_BufferedImage = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
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
			
			return PrimitiveStyleSheet.instance.image( image, width, height );
		}
	};

	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Color = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			Color colour = (Color)x;
			
			DPElement title = colourObjectBoxStyle.objectTitle( "java.awt.Color" );
			
			DPElement red = colourRedStyle.staticText( "R=" + String.valueOf( colour.getRed() ) );
			DPElement green = colourGreenStyle.staticText( "G=" + String.valueOf( colour.getGreen() ) );
			DPElement blue = colourBlueStyle.staticText( "B=" + String.valueOf( colour.getBlue() ) );
			DPElement alpha = colourAlphaStyle.staticText( "A=" + String.valueOf( colour.getAlpha() ) );
			
			DPElement components = colourBoxStyle.hbox( new DPElement[] { red, green, blue, alpha } );
			
			DPElement textBox = PrimitiveStyleSheet.instance.vbox( new DPElement[] { title, components } );
			
			DPElement swatch = PrimitiveStyleSheet.instance.withShapePainter( new FillPainter( colour ) ).box( 50.0, 20.0 ).alignVExpand();
			
			DPElement contents = colourBoxStyle.hbox( new DPElement[] { textBox, swatch } );
			
			return colourObjectBoxStyle.objectBorder( contents );
		}
	};

	public static final ObjectPresenter<GenericPerspectiveStyleSheet> presenter_Class = new ObjectPresenter<GenericPerspectiveStyleSheet>()
	{
		public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			Class<?> cls = (Class<?>)x;
			Class<?> superClass = cls.getSuperclass();
			Class<?> interfaces[] = cls.getInterfaces();
			
			ArrayList<DPElement> lines = new ArrayList<DPElement>();
			
			DPElement title = classKeywordStyle.staticText( "Class " );
			DPElement name = classNameStyle.staticText( cls.getName() );
			lines.add( PrimitiveStyleSheet.instance.hbox( new DPElement[] { title, name } ) );
			
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
				
				DPElement para = PrimitiveStyleSheet.instance.paragraph( inheritance );
				lines.add( para.padX( 45.0, 0.0 ) );
			}
			
			return styleSheet.objectBoxWithFields( "Java Class", lines.toArray( new DPElement[0] ) );
		}
	};




	private static final PrimitiveStyleSheet punctuationStyle = PrimitiveStyleSheet.instance.withForeground( Color.blue );
	private static final PrimitiveStyleSheet delimStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.1f, 0.3f, 0.4f ) ).withFontBold( true ).withFontSize( 14 );
	
	
	private static final GenericPerspectiveStyleSheet colourObjectBoxStyle = GenericPerspectiveStyleSheet.instance.withObjectBorderAndTitlePaint( new Color( 0.0f, 0.1f, 0.4f ) );
	private static final PrimitiveStyleSheet colourRedStyle = PrimitiveStyleSheet.instance.withFontSize( 12 ).withForeground( new Color( 0.75f, 0.0f, 0.0f ) );
	private static final PrimitiveStyleSheet colourGreenStyle = PrimitiveStyleSheet.instance.withFontSize( 12 ).withForeground( new Color( 0.0f, 0.75f, 0.0f ) );
	private static final PrimitiveStyleSheet colourBlueStyle = PrimitiveStyleSheet.instance.withFontSize( 12 ).withForeground( new Color( 0.0f, 0.0f, 0.75f ) );
	private static final PrimitiveStyleSheet colourAlphaStyle = PrimitiveStyleSheet.instance.withFontSize( 12 ).withForeground( new Color( 0.3f, 0.3f, 0.3f ) );
	private static final PrimitiveStyleSheet colourBoxStyle = PrimitiveStyleSheet.instance.withHBoxSpacing( 5.0 );

	
	private static final PrimitiveStyleSheet classKeywordStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.0f, 0.0f, 0.5f ) ).withFontBold( true ).withTextSmallCaps( true );
	private static final PrimitiveStyleSheet classPunctuationStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.25f, 0.0f, 0.5f ) );
	private static final PrimitiveStyleSheet classNameStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.0f, 0.25f, 0.5f ) );

	private static final PrimitiveStyleSheet fnPunctuationStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.25f, 0.0f, 0.5f ) );
	private static final PrimitiveStyleSheet fnNameStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.0f, 0.25f, 0.5f ) );
	private static final PrimitiveStyleSheet fnArgStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.0f, 0.5f, 0.25f ) );
	private static final PrimitiveStyleSheet fnKWArgStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.0f, 0.5f, 0.25f ) ).withFontItalic( true );
	private static final PrimitiveStyleSheet fnVarArgStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.0f, 0.5f, 0.25f ) );


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
			return PrimitiveStyleSheet.instance.staticText( " " );
		}
	};
	
	private static final ElementFactory openBracketFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return delimStyle.staticText( "[" );
		}
	};
	
	private static final ElementFactory closeBracketFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return delimStyle.staticText( "]" );
		}
	};
	
	private static final ElementFactory openParenFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return delimStyle.staticText( "(" );
		}
	};
	
	private static final ElementFactory closeParenFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return delimStyle.staticText( ")" );
		}
	};
	
	
	private static SpanListViewLayoutStyleSheet span_listViewLayout = SpanListViewLayoutStyleSheet.instance.withAddLineBreaks( true ).withAddParagraphIndentMarkers( true ).withAddLineBreakCost( true );
	private static ListViewStyleSheet listListViewStyle = ListViewStyleSheet.instance.withSeparatorFactory( commaFactory ).withSpacingFactory( spaceFactory )
		.withBeginDelimFactory( openBracketFactory ).withEndDelimFactory( closeBracketFactory ).withListLayout( span_listViewLayout );
	private static ListViewStyleSheet tupleListViewStyle = ListViewStyleSheet.instance.withSeparatorFactory( commaFactory ).withSpacingFactory( spaceFactory )
		.withBeginDelimFactory( openParenFactory ).withEndDelimFactory( closeParenFactory ).withListLayout( span_listViewLayout );
}
