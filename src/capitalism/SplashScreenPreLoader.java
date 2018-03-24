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
package capitalism;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.utils.Reporter;
import capitalism.view.ViewManager;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashScreenPreLoader extends Preloader {
	private static final Logger logger = LogManager.getLogger(SplashScreenPreLoader.class);

	private static Stage preloaderStage;
	private static Scene scene;
	private static StackPane root;
	private static ImageView economy;

	private static Label progress;

	public SplashScreenPreLoader() {
		// Constructor is called before everything.
		logger.debug("Preloader constructor called, thread: " + Thread.currentThread().getName());
	}

	@Override public void init() throws Exception {
		logger.debug("Preloader#init, thread: " + Thread.currentThread().getName());

		// If preloader has complex UI its initialization can be done in MyPreloader#init
		Platform.runLater(() -> {
			// Nothing at present
		});
		Reporter.initialiseLoggerFiles();
	}

	@Override public void start(Stage primaryStage) throws Exception {
		System.out.println("Preloader#start (showing preloader stage), thread: " + Thread.currentThread().getName());

		SplashScreenPreLoader.preloaderStage = primaryStage;
		progress = new Label("Watch this space");
		progress.setFont(new Font(36));
		progress.setTextFill(Color.ORANGERED);

		economy = new ImageView("worldeconomy.png");
		economy.setFitWidth(ViewManager.windowWidth);
		economy.setFitHeight(ViewManager.windowHeight + 10);
		root = new StackPane(economy);
		root.getChildren().add(progress);
		progress.setTranslateX(110);
		progress.setTranslateY(-50);
		root.setAlignment(Pos.CENTER);

		scene = new Scene(root, ViewManager.windowWidth, ViewManager.windowHeight);
		// Set preloader scene and show stage.
		preloaderStage.setAlwaysOnTop(true);
		preloaderStage.setScene(scene);
		preloaderStage.centerOnScreen();
		preloaderStage.initStyle(StageStyle.UNDECORATED);

		Platform.runLater(() -> {
			preloaderStage.show();
			animatePreloader();
		});
		// TODO have this run fully asynchronously
		// TODO close on signal from ViewManager instead of fixed time
	}

	@Override public void handleApplicationNotification(PreloaderNotification info) {
		if (info instanceof ProgressNotification) {
			progress.setText(((ProgressNotification) info).getProgress() + "%");
		}
	}

	@Override public void handleStateChangeNotification(StateChangeNotification info) {
		// Handle state change notifications.
		StateChangeNotification.Type type = info.getType();
		switch (type) {
		case BEFORE_LOAD:
			// Called after Preloader#start is called.
			System.out.println("BEFORE_LOAD");
			break;
		case BEFORE_INIT:
			// Called before Capitalism#init is called.
			System.out.println("BEFORE_INIT");
			break;
		case BEFORE_START:
			// Called after Capitalism#init and before Capitalism#start is called.
			System.out.println("BEFORE_START");
			break;
		}
	}

	/**
	 * 'Close' the preloaderStage window (actally, just hide it, it isn't doing any harm)
	 * TODO make sure it is properly clsed on application shutdown
	 */
	public static void closePreloader() {
		preloaderStage.hide();
	}

	/**
	 * Set the progress label
	 * 
	 * @param text
	 *            the text of the progress label
	 */
	public static void setProgress(String text) {
		progress.setText(text);
	}

	/**
	 * Preloader animation
	 */
	public static void animatePreloader() {
		logger.debug("Preloader#fadePreloader, thread: " + Thread.currentThread().getName());

		ScaleTransition scaleTransition = new ScaleTransition();
		scaleTransition.setDuration(Duration.millis(3500));
		scaleTransition.setNode(progress);
		scaleTransition.setByY(1.2);
		scaleTransition.setByX(1.2);
		scaleTransition.setCycleCount(1);
		scaleTransition.setAutoReverse(false);

		scaleTransition.setOnFinished(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {
				System.out.println("Preloader#closePreloader, thread: " + Thread.currentThread().getName());
				closePreloader();
			}
		});
		scaleTransition.play();
	}

}
