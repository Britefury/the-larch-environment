##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.GSym.PresCom import InnerFragment

# Define before other imports, otherwise CodeGenerator import will fail
presTemplateRuntimeModuleName = '__prestemplate_runtime'
presTemplatePrelude = 'from GSymCore.PresTemplate.Runtime import PresTemplateRuntime as %s\n'  %  ( presTemplateRuntimeModuleName, )


from GSymCore.PresTemplate.TemplateEditor.View import perspective as templateEditorPerspective
from GSymCore.PresTemplate import Schema
from GSymCore.PresTemplate import ViewSchema
from GSymCore.PresTemplate import CodeGenerator



def newTemplate():
	return Schema.Template( body=Schema.Body( contents=[] ) )

def pythonExternalExpressionPresenter(model, inheritedState):
	viewModel = ViewSchema.TemplateView( None, model )
	return templateEditorPerspective.applyTo( InnerFragment( viewModel, inheritedState ) )

def pythonExternalExpressionCodeGeneratorFactory(pythonCodeGen):
	return CodeGenerator.PresTemplateCodeGenerator( pythonCodeGen )


