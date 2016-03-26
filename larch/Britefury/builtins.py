##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from BritefuryJ.Inspect import JavaInspectorPerspective as _JavaInspectorPerspective
from BritefuryJ.Inspect import InspectorPerspective as _InspectorPerspective
from BritefuryJ.DefaultPerspective import DefaultPerspective as _DefaultPerspective
from BritefuryJ.EditPerspective import EditPerspective as _EditPerspective



javaInspect = _JavaInspectorPerspective.instance
inspect = _InspectorPerspective.instance
defaultPerspective = _DefaultPerspective.instance
edit = _EditPerspective.instance
