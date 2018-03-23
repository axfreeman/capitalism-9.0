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
package capitalism.editor;


import javafx.geometry.Insets;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class EditorBox extends VBox {

	EditorBox(TableView<?> table, EditorDialogueBox dialogueBox,String userGuide) {
		setPrefWidth(Double.MAX_VALUE);
		HBox dialogueContainer = new HBox();
		TextArea textArea =new TextArea();
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setWrapText(true);
		HBox.setHgrow(textArea, Priority.ALWAYS);
		setMaxHeight(Double.MAX_VALUE);
		textArea.setFont(new Font(16));
		textArea.appendText(userGuide);
		
		textArea.setPadding(new Insets(3, 3, 3, 3));
		final String cssBackground = "-fx-background-color: LIGHTSTEELBLUE;\n"
				+ "-fx-border-color: silver;\n"
				+ "-fx-border-width: 1;\n";
		setStyle(cssBackground);
//		final String cssTextArea= "-fx-background-color: #F4F4F4;\n"
//				+ "-fx-border-color: silver;\n"
//				+ "-fx-border-width: 1;\n";
		textArea.setStyle(cssBackground);
		textArea.setPadding(new Insets(2, 2, 2, 2));
		DropShadow dropShadow = new DropShadow();
		dropShadow.setOffsetX(0.5);
		dropShadow.setOffsetY(0.5);
		dropShadow.setColor(Color.CADETBLUE);
		textArea.setEffect(dropShadow);
		
		dialogueContainer.getChildren().addAll(dialogueBox,textArea);
		getChildren().addAll(table,dialogueContainer);
	}
}
