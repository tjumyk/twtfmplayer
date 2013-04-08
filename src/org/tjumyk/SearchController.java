package org.tjumyk;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import org.tjumyk.twtfm.TwtFmDB;
import org.tjumyk.twtfm.TwtFmMusic;
import org.tjumyk.util.DragUtil;
import org.tjumyk.util.DragUtil.DragLimitArea;

import com.fxexperience.javafx.animation.FlipOutYTransition;

public class SearchController implements Initializable {
	/*************************************
	 * Single Instance
	 *************************************/
	private static SearchController instance;

	public static SearchController getInstance() {
		return instance;
	}

	/*************************************
	 * FXML Nodes
	 *************************************/
	@FXML
	ImageView searchBackBtn;
	@FXML
	TextField searchField;
	@FXML
	VBox searchResultBox;
	@FXML
	AnchorPane searchResultContainer;
	@FXML
	Label searchResultLine;

	/*************************************
	 * Settings
	 *************************************/
	private int maxSearchResult = 100;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		instance = this;

		searchBackBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Transition tran = new FlipOutYTransition(PlayerController
						.getInstance().searchPanel);
				tran.setOnFinished(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						PlayerController.getInstance().backToMain();
					}
				});
				tran.play();
			}
		});
		
		DragUtil.setPaneClip(searchResultContainer);
		final DragLimitArea limitRect = new DragLimitArea(
				searchResultBox.getLayoutX(), searchResultBox.getLayoutY(), 0, 0);
		searchResultBox.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				limitRect.height = Math.min(0, searchResultContainer.getHeight() - newValue.doubleValue());
			}
		});
		DragUtil.setDraggable(searchResultContainer, searchResultBox, false,
				true, 10, limitRect);

		searchField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				searchResultBox.getChildren().clear();
				searchResultLine.setText("");
				if (newValue.trim().length() <= 0)
					return;
				TwtFmDB musicDB = PlayerController.getInstance().musicDB;
				if (musicDB.isReady()) {
					ArrayList<TwtFmMusic> result = musicDB.searchMusic(
							newValue,maxSearchResult);
					if (result == null || result.size() <= 0) {
						searchResultBox.getChildren().add(
								LabelBuilder.create().text("No result.")
										.build());
					} else {
						if(result.size() < maxSearchResult)
							searchResultLine.setText("Results: "+result.size());
						else
							searchResultLine.setText("Results: >="+maxSearchResult+", but only top "+maxSearchResult +" results will be shown.");
						for (final TwtFmMusic music : result) {
							String time = "";
							try {
								int value = (int) Math.round(Double
										.parseDouble(music.getDuration()) / 1000);
								time = " (" + value / 60 + "m" + value % 60
										+ "s)";
							} catch (Exception e) {
							}
							searchResultBox.getChildren().add(
									LabelBuilder
											.create().styleClass("search-result-item")
											.text(music.getTitle() + " - "
													+ music.getSinger() + " - "
													+ music.getAlbum() + time).onMouseClicked(new EventHandler<MouseEvent>() {
														@Override
														public void handle(
																MouseEvent event) {
															PlayListController
															.getInstance()
															.addMusic(
																	music);
														}
													})
											.build());
						}
					}
				} else {
					searchResultBox
							.getChildren()
							.add(LabelBuilder
									.create()
									.text("Database is being initialized, please wait for a while and then try again.")
									.build());
				}
			}
		});
	}

}
