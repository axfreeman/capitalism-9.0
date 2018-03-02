package capitalism.view.custom;

import capitalism.view.command.DisplayCommand;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class ImageButton extends Button {

	private final String STYLE_NORMAL = "-fx-background-color: transparent; -fx-padding: 5, 5, 5, 5;";
	private final String STYLE_PRESSED = "-fx-background-color: transparent; -fx-padding: 6 4 4 6;";
	private Tooltip onTip;
	private Tooltip offTip;
	private ImageView onView;
	private ImageView offView;
	private String onURL;
	private String offURL;
	private boolean state;
	
	public ImageButton(String onImageURL, String offImageURL,DisplayCommand command, String offTipText, String onTipText) {
		state=true;
		onURL=onImageURL;
		offURL=offImageURL;
		onView = new ImageView(onURL);
		onView.setFitHeight(20);
		onView.setFitWidth(20);
		if(offImageURL==null) offURL=onImageURL;// null URL quietly suppressed
		offView = new ImageView(offURL);
		offView.setFitHeight(20);
		offView.setFitWidth(20);
		setGraphic(onView);
		setPrefWidth(60);
		setMaxWidth(60);
		setMinWidth(60);
		onTip=new Tooltip(onTipText);
		offTip= new Tooltip (offTipText);
		ImageButton thisButton=this;
		setTooltip(onTip);
		
		setStyle(STYLE_NORMAL);

		setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				setStyle(STYLE_PRESSED);
				command.execute(thisButton);
			}
		});

		setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				setStyle(STYLE_NORMAL);
			}
		});
		
	}
	public void setImageWidth(int width) {
		onView.setFitWidth(width);
		offView.setFitWidth(width);
	}
	public void setImageHeight(int height) {
		onView.setFitHeight(height);
		offView.setFitHeight(height);
	}
	public void setOnState() {
		state=true;
		setTooltip(onTip);
		setGraphic(onView);
	}
	public void setOffState() {
		state=false;
		setTooltip(offTip);
		setGraphic(offView);
	}
	public void switchStates() {
		if(state) {
			setOffState();
		}else {
			setOnState();
		}
	}
}
