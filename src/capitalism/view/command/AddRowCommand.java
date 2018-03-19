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
*/package capitalism.view.command;

import capitalism.editor.Editor;
import capitalism.editor.EditorManager;
import capitalism.view.custom.ImageButton;
import javafx.stage.Stage;

/**
 * This class, invoked via {@code EditorControlBar}, lets the user create a new row in a table in one of the three tabs
 * displayed in the editor window: socialClass, Industry or Commodity. It does so by invoking one of the three
 * methods {@link Editor#addIndustry()}, {@link Editor#addSocialClass()} or {@link Editor#addCommodity()}.
 * These in turn display a dialogue box in the tab pane that holds the table concerned, in which the user
 * can edit the fields relevant to that table and save or cancel. The save and cancel buttons involve eventhandlers
 * also specific to the tabs concerned, which do the heavy lifting.
 */

public class AddRowCommand implements DisplayCommand {
	Stage editorStage;
	EditorManager editorManager;

	@Override public void execute(ImageButton caller) {
		switch (Editor.selectedTab()) {
		case COMMODITY:
			Editor.addCommodity();
			break;
		case INDUSTRY:
			Editor.addIndustry();
			break;
		case SOCIALCLASS:
			Editor.addSocialClass();
			break;
		default:
		}
	}
}
