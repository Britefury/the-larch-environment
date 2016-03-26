//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;

public abstract class AbstractEditRule
{
	public class Decorator {
		private PyObject method;
		public PyObject __dispatch_unwrapped_method__;
		public PyObject __name__;

		public Decorator(PyObject method) {
			this.method = method;
			this.__dispatch_unwrapped_method__ = method;
			try {
				this.__name__ = method.__getattr__( "__name__" );
			}
			catch (PyException e) {
				if ( e.match( Py.AttributeError ) )
				{
					this.__name__ = Py.None;
				}
				else
				{
					throw e;
				}
			}
		}

		public PyObject __call__(PyObject[] args) {
			Pres p = Py.tojava(method.__call__(args), Pres.class);
			FragmentView fragment = Py.tojava( args[1], FragmentView.class );
			p = applyToFragment( p, fragment.getModel(), fragment.getInheritedState() );
			return Py.java2py( p );
		}
	}

	protected SequentialController controller;
	
	
	public AbstractEditRule(SequentialController controller)
	{
		this.controller = controller;
	}
	

	public abstract Pres applyToFragment(Pres view, Object model, SimpleAttributeTable inheritedState);

	/// Defining a __call__ method allows an AbstractEditRule instance to be used as a Python decorator
	public PyObject __call__(PyObject method) {
		return Py.java2py(new Decorator(method));
	}
}
