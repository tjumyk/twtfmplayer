package org.tjumyk;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransitionBuilder;
import javafx.animation.TimelineBuilder;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import org.tjumyk.thread.WorkFlowRunner;
import org.tjumyk.util.DragUtil;

import com.fxexperience.javafx.animation.FadeInTransition;
import com.fxexperience.javafx.animation.FlipOutXTransition;

public class Main extends Application {

	private Stage stage = null;
	/**
	 * Workaround for the troublesome default ESC KeyEvent handling in full
	 * screen mode
	 */
	private boolean alreadyFullScreen = false;

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		Font.loadFont(this.getClass().getResource("font/wqy-microhei.ttf")
				.toExternalForm(), 0);
		final Parent root = FXMLLoader.load(this.getClass().getResource(
				"player.fxml"));
		root.getStylesheets().add(
				this.getClass().getResource("style-dark.css").toExternalForm());
		CacheManager.init();

		Scene scene = new Scene(root);
		scene.setFill(Color.TRANSPARENT);
		stage.setScene(scene);
		stage.initStyle(StageStyle.TRANSPARENT);
		// stage.setFullScreen(true);
		stage.setTitle("TWT FM FXPlayer");
		root.setOpacity(0);

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				event.consume();
				cleanAndQuit();
			}
		});

		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				Stage stage = Main.this.stage;
				if (event.getCode() == KeyCode.ESCAPE) {
					event.consume();
					if (alreadyFullScreen)
						alreadyFullScreen = false;
					else
						cleanAndQuit();
				} else if (event.getCode() == KeyCode.F5) {
					event.consume();
					stage.setFullScreen(true);
					alreadyFullScreen = true;
				} 
			};
		});

		stage.show();
		// ScenicView.show(scene);
		setWindowDraggable();
		new FadeInTransition(root).play();
		PlayerController.getInstance().loadHome();
		PlayerController.getInstance().showWelcome();
	}

	private void setWindowDraggable() {
		stage.fullScreenProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				PlayerController.getInstance().setFullScreen(
						newValue.booleanValue());
				if (newValue.booleanValue() == true) {
					alreadyFullScreen = true;
					/**
					 * Workaround for the the bad drag location
					 */
					Main.this.stage.setX(0);
					Main.this.stage.setY(0);
				} else {
					/**
					 * Workaround for the the bad drag location
					 */
					Rectangle2D bounds = Screen.getPrimary().getBounds();
					stage.setX((bounds.getWidth() - stage.getWidth()) / 2);
					stage.setY((bounds.getHeight() - stage.getHeight()) / 2);
				}
			}
		});

		Node titleBar = PlayerController.getInstance().titleBar;
		DragUtil.setDraggable(stage, titleBar);
		titleBar.addEventHandler(MouseEvent.MOUSE_RELEASED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (event.getScreenY() <= 0)
							Main.this.stage.setFullScreen(true);
					}
				});
		titleBar.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (event.getClickCount() >= 2) {// double click
							if (!Main.this.stage.isFullScreen())
								Main.this.stage.setFullScreen(true);
						}
					}
				});
	}

	private void cleanAndQuit() {
		final MediaPlayer player = PlayListController.getInstance().mediaPlayer;
		Node node = stage.getScene().getRoot();
		Transition tran;
		if (player != null && player.getStatus() == MediaPlayer.Status.PLAYING)
			tran = ParallelTransitionBuilder
					.create()
					.children(
							new FlipOutXTransition(node),
							TimelineBuilder
									.create()
									.keyFrames(
											new KeyFrame(Duration.seconds(1.0),
													new KeyValue(player
															.volumeProperty(),
															0))).build())
					.build();
		else
			tran = new FlipOutXTransition(node);
		tran.setOnFinished(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				if(player != null)
					player.stop();
				Platform.exit();
			}
		});
		tran.play();
	}

	@Override
	public void stop() throws Exception {
		System.out.println("Canceling existing work flows...");
		WorkFlowRunner.cancelNewWorks();
		System.out.println("Canceling downloading cache resources...");
		CacheManager.cancelWork();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
