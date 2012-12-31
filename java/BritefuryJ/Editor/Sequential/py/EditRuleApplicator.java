//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential.py;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;

import BritefuryJ.Editor.Sequential.AbstractEditRule;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Util.Jython.DescriptorBase;

public class EditRuleApplicator extends DescriptorBase
{
	// Has to extend DescriptorBase to work around that prevents Java objects from acting as descriptors by defining descriptor methods, if they directly derive from Object.
	

	public static class BoundEditRuleApplicator
	{
		// Must be public in order for jython to find __call__
		public class BoundEditRuleApplicatorFn
		{
			// Must be public in order for jython to find __dispatch_unwrapped_method__, __name__ and __call__
			private PyObject method;
			public PyObject __dispatch_unwrapped_method__;
			public PyObject __name__;
			
			
			public BoundEditRuleApplicatorFn(PyObject method)
			{
				this.method = method;
				this.__dispatch_unwrapped_method__ = method;
				try
				{
					this.__name__ = method.__getattr__( "__name__" );
				}
				catch (PyException e)
				{
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
			
			
			public PyObject __call__(PyObject args[])
			{
				Pres p = Py.tojava( method.__call__( args ), Pres.class );
				FragmentView fragment = Py.tojava( args[1], FragmentView.class );
				p = rule.applyToFragment( p, fragment.getModel(), fragment.getInheritedState() );
				return Py.java2py( p );
			}
		}
		
		
		private AbstractEditRule rule;
		
		
		public BoundEditRuleApplicator(AbstractEditRule rule)
		{
			this.rule = rule;
		}
		
		
		public AbstractEditRule getRule()
		{
			return rule;
		}
		
		
		public PyObject __call__(PyObject method)
		{
			BoundEditRuleApplicatorFn fn = new BoundEditRuleApplicatorFn( method );
			return Py.java2py( fn );
		}
	}
	
	
	
	private PyObject ruleFn;
	
	
	public EditRuleApplicator(PyObject ruleFn)
	{
		this.ruleFn = ruleFn;
	}
	
	
	public PyObject __get__(PyObject instance, PyObject type)
	{
		if ( instance == Py.None )
		{
			return Py.java2py( this );
		}
		else
		{
			AbstractEditRule rule = Py.tojava( ruleFn.__call__( instance ), AbstractEditRule.class );
			BoundEditRuleApplicator bound = new BoundEditRuleApplicator( rule );
			return Py.java2py( bound );
		}
	}

}
