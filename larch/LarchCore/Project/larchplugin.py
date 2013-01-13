##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from LarchCore.Project import Project
from LarchCore.Project.ProjectPage import ProjectPage
from LarchCore.Project.ProjectEditor.Subject import ProjectSubject
from LarchCore.Worksheet import Worksheet
from LarchCore.Worksheet.WorksheetViewer.View import WorksheetViewerSubject



def initPlugin(plugin, world):
	@world.documentContentFactory('Larch Project')
	def newProjectContent():
		return Project.newProject()

	@world.documentContentFactory('Quickstart: worksheet (in a new project)')
	def newWorksheetInProject():
		project = Project.newProject()
		pageData = Worksheet.WorksheetPageData()
		page = ProjectPage( 'Worksheet', pageData )
		project.append( page )
		return project

	@newWorksheetInProject.firstPageSubjectFn
	def newWorksheetInProjectSubjFn(documentContentSubject):
		firstPageSubj = documentContentSubject
		# Ensure its a project
		if isinstance( documentContentSubject, ProjectSubject ):
			project = documentContentSubject.getFocus()
			# Search immediate children
			for child in project:
				# If child is a page
				if isinstance( child, ProjectPage ):
					# Get a subject for it
					firstPageSubj = documentContentSubject._pageSubject( child )
					# Check its a worksheet viewer subject
					if isinstance( firstPageSubj, WorksheetViewerSubject ):
						# Get the edit subject
						return firstPageSubj.editSubject

		return firstPageSubj




