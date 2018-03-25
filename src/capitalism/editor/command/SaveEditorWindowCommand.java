/*
 * Copyright (C) Alan Freeman 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in the remainder of this project
 *
 *  Capsim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either project 3 of the License, or
 *  (at your option) any later project.
*
*   Capsim is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with Capsim.  If not, see <http://www.gnu.org/licenses/>.
*/package capitalism.editor.command;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.editor.EditorLoader;
import capitalism.model.OneProject;
import capitalism.reporting.Dialogues;
import capitalism.utils.XMLStuff;
import capitalism.view.command.DisplayCommand;
import capitalism.view.custom.ImageButton;

/**
 * This class, invoked via {@code EditorControlBar}, lets the user save the project that is currently being edited
 * to an external file on the user's computer.
 */

public class SaveEditorWindowCommand implements DisplayCommand {
	private static final Logger logger = LogManager.getLogger("SaveEditorWindowCommand");

	@Override public void execute(ImageButton caller) {
		File file = Dialogues.saveFileChooser("Where should this project be saved?");
		if (file == null)
			return;
		logger.debug("Saving new project to {}", file.getAbsolutePath());
		OneProject oneProject = EditorLoader.wrappedOneProject();
		XMLStuff.exportToXML(oneProject, file);
	}
}
