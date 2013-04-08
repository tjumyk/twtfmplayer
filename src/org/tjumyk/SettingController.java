package org.tjumyk;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import com.fxexperience.javafx.animation.FlipOutYTransition;

public class SettingController implements Initializable {
	/*************************************
	 * Single Instance
	 *************************************/
	private static SettingController instance;
	public static SettingController getInstance(){
		return instance;
	}
	
	/*************************************
	 * FXML Nodes
	 *************************************/
	@FXML
	ImageView settingBackBtn;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		instance = this;
		
		settingBackBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Transition tran = new FlipOutYTransition(PlayerController
						.getInstance().settingPanel);
				tran.setOnFinished(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						PlayerController.getInstance().backToMain();
					}
				});
				tran.play();
			}
		});
	}

}
