/*
 *  Copyright (C) Alan Freeman 2017-2019
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
 */
package capitalism.help;

import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * this little class does little more than deliver a browser in a Vbox
 */

public class Browser extends VBox {

	private WebView browser;
	private WebEngine webEngine;

	public Browser() {
    	
//		styling is handled directly within the html files that \re loaded into the user's help directory.
//		This has many benefits, in particular customised help files.
//		Basically, why keep a dog and bark.
		
		browser = new WebView();
    	webEngine = browser.getEngine();
        getChildren().addAll(browser);
    }

	public void load(String url) {
		webEngine.load(url);
	}
}
