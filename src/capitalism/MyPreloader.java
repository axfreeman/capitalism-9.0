package capitalism;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class MyPreloader extends Preloader {

    private static final double WIDTH = 1300;
    private static final double HEIGHT = 800;

    private static Stage preloaderStage;
    private Scene scene;

    private Label progress;

    public MyPreloader() {
        // Constructor is called before everything.
        System.out.println("Preloader constructor called, thread: " + Thread.currentThread().getName());
    }

    @Override
    public void init() throws Exception {
        System.out.println("Preloader#init (could be used to initialize preloader view), thread: " + Thread.currentThread().getName());

        // If preloader has complex UI its initialization can be done in MyPreloader#init
        Platform.runLater(() -> {
            progress = new Label("Watch this space");
            progress.setFont(new Font(24));

            VBox root = new VBox(progress);
            root.setAlignment(Pos.CENTER);

            scene = new Scene(root, WIDTH, HEIGHT);
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Preloader#start (showing preloader stage), thread: " + Thread.currentThread().getName());

        this.preloaderStage = primaryStage;

        // Set preloader scene and show stage.
        preloaderStage.setScene(scene);
        preloaderStage.centerOnScreen();
        preloaderStage.show();
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        // Handle application notification in this point (see MyApplication#init).
        if (info instanceof ProgressNotification) {
            progress.setText(((ProgressNotification) info).getProgress() + "%");
        }
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
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
	 * @return the preloaderStage
	 */
	public static void closePreloader() {
		preloaderStage.hide();
	}
    
}
