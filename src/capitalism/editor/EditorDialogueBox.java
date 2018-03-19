package capitalism.editor;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * This class creates and maintains the dialogue boxes which capture information from the user when creating new editable entities
 */
public class EditorDialogueBox extends VBox {
	private HBox controlsBox = new HBox();
	private Button btnSave = new Button("Save");
	private Button btnCancel = new Button("Cancel");
	private VBox fieldsBox = new VBox();
	private HBox buttonsBox = new HBox();
	private Label warningLabel;
	private ArrayList<Node> disableList;

	public EditorDialogueBox(List<Node> fields, String initialPrompt,EventHandler<ActionEvent> btnSaveHandler,ArrayList<Node> disableList) {
		super();
		this.disableList=disableList;
		warningLabel = new Label();

		fieldsBox.setSpacing(10);
		buttonsBox.setSpacing(10);
		warningLabel.setMaxWidth(Double.MAX_VALUE);
		warningLabel.setMinWidth(300);
		warningLabel.setPrefWidth(300);
		warningLabel.setText(initialPrompt);
		btnSave.setOnAction(btnSaveHandler);
		Insets sidePadding = new Insets(5, 5, 5, 5);
		btnSave.setPadding(sidePadding);
		btnCancel.setPadding(sidePadding);
		btnSave.setMinWidth(60);
		btnCancel.setMinWidth(60);
		for (Node n : fields) {
			if (n.getClass().equals(TextField.class)) {
				TextField t=(TextField) n;
				t.setPadding(sidePadding);
				t.setPrefWidth(200);
				t.setMaxWidth(200);
			}
		}
		btnCancel.setOnAction(btnCancelHandler);
		controlsBox.getChildren().addAll(fieldsBox);
		fieldsBox.getChildren().addAll(fields);
		fieldsBox.getChildren().addAll(buttonsBox);
		buttonsBox.getChildren().addAll(btnSave, btnCancel);
		getChildren().addAll(warningLabel, controlsBox);
	}

	EventHandler<ActionEvent> btnCancelHandler = new EventHandler<ActionEvent>() {
		@Override public void handle(ActionEvent t) {
			hideDialogue();
		}
	};

	/**
	 * Make this dialogue visible and enabled
	 * and disable everything else, effectively making the dialogue modal
	 */
	public void showDialogue() {
		setVisible(true);
		Editor.setControlsDisabled(true);
		for (Node n:disableList) {
			n.setDisable(true);
		}
		setDisable(false);
	}

	/**
	 * Hide this dialogue
	 */
	public void hideDialogue() {
		setVisible(false);
		Editor.setControlsDisabled(false);
		for (Node n:disableList) {
			n.setDisable(false);
		}
		setDisable(true);
	}

	public void warn(String text) {
		warningLabel.setText(text);
	}

}
