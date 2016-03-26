//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.DefaultPerspective.Presenters;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.ObjectPresenter;
import BritefuryJ.ObjectPresentation.ObjectPresenterRegistry;
import BritefuryJ.Pres.Pres;

import com.kitfox.svg.SVGDiagram;

public class PresentersSVG extends ObjectPresenterRegistry
{
	public PresentersSVG()
	{
		registerJavaObjectPresenter( SVGDiagram.class,  presenter_SVGDiagram );
	}


	public static final ObjectPresenter presenter_SVGDiagram = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			SVGDiagram diagram = (SVGDiagram)x;
			double width = (double)diagram.getWidth();
			double height = (double)diagram.getHeight();
			
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
			
			return new BritefuryJ.Pres.Primitive.Image( diagram, width, height );
		}
	};
}
