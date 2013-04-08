package org.tjumyk;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import org.tjumyk.thread.BackgroundRunner;
import org.tjumyk.thread.WorkFlowRunner;
import org.tjumyk.twtfm.TwtFmChannel;
import org.tjumyk.twtfm.TwtFmDB;
import org.tjumyk.twtfm.TwtFmMusic;
import org.tjumyk.twtfm.TwtFmUtil;
import org.tjumyk.util.DragUtil;
import org.tjumyk.util.DragUtil.DragLimitArea;

import com.fxexperience.javafx.animation.BounceInLeftTransition;
import com.fxexperience.javafx.animation.FadeInUpTransition;
import com.fxexperience.javafx.animation.FadeOutTransition;
import com.fxexperience.javafx.animation.FlipInYTransition;

public class PlayerController implements Initializable {
	/*************************************
	 * Single Instance
	 *************************************/
	private static PlayerController instance;

	public static PlayerController getInstance() {
		return instance;
	}

	/*************************************
	 * FXML Nodes
	 *************************************/
	@FXML
	HBox metroContents;
	@FXML
	AnchorPane backgroundPane, metroContentsContainer, toolPanelContainer,
			searchPanel, tuningPanel, settingPanel, playListPanel, titleBar;
	@FXML
	Label homeNav, channelsNav, artistsNav, albumsNav, playlistsNav,
			playerTitle, titleMessage;
	@FXML
	ImageView tuningToolBtn, settingToolBtn, searchToolBtn, miniPlayerThumb,
			controllerPlayImage, downloadBtnImg;
	@FXML
	HBox navBar, toolBar;
	@FXML
	StackPane prevBtn, nextBtn, playBtn, lazyBtn, downloadBtn;
	@FXML
	ProgressBar trackLine;
	@FXML
	ProgressIndicator downloadProInd;

	Timeline messageTimeline;

	/*************************************
	 * Data
	 *************************************/
	public ArrayList<TwtFmChannel> channels = null;
	public TwtFmDB musicDB = new TwtFmDB();

	public void initChannels() {
		channels = CacheManager.loadChannelList();
	}

	public void loadHome() {
		new WorkFlowRunner() {

			@Override
			public void handleException(Exception e) throws Exception {
				showError(e);
			}

			@Override
			public void initWorkFlow() {
				addWorks(new ForeWork("Load history") {
					@Override
					public void start() {
						loadHistory();
					}
				}, new BackWork("Initialize channel list") {
					@Override
					public void start() {
						initChannels();
					}
				}, new ForeWork("Load channels") {

					@Override
					public void start() {
						// loadNewest();
						// loadHit();
						loadChannels();
					}

				});
			}
		}.run();
	}

	public void loadHistory() {

	}

	/**
	 * This function is only for simple test!
	 */
	public void loadChannels() {
		new WorkFlowRunner() {
			ArrayList<TwtFmMusic> tempList = null;
			boolean gotoNext = true;

			@Override
			public void handleException(Exception e) throws Exception {
				showError(e);
			}

			@Override
			public void finish() throws Exception {
				showInfo("Music database is ready!");
			}

			@Override
			public void initWorkFlow() {
				for (final TwtFmChannel channel : channels) {
					addWorks(
							new BackWork("Loading Channel: "
									+ channel.getTitle()) {

								@Override
								public void start() {
									tempList = CacheManager
											.loadMusicList(channel.getID());
									if (tempList != null && tempList.size() > 0) {
										tempList = TwtFmUtil
												.sortMusicByRank(tempList);
										musicDB.addMusicAll(tempList);
									}
								}

							},
							new ForeWork("Building Channel: "
									+ channel.getTitle()) {
								@Override
								public void start() {
									String title = channel.getTitle();
									if (tempList != null && tempList.size() > 0) {
										gotoNext = false;
										Node square = MetroBoxBuilder
												.buildMetroSquare(75, 10,
														tempList);
										final VBox box = VBoxBuilder
												.create()
												.spacing(35)
												.opacity(0)
												.children(
														LabelBuilder
																.create()
																.text(title)
																.styleClass(
																		"metro-title")
																.onMouseClicked(
																		new EventHandler<MouseEvent>() {
																			@Override
																			public void handle(
																					MouseEvent event) {
																				ArrayList<TwtFmMusic> list = TwtFmUtil.shuffleList(CacheManager
																						.loadMusicList(channel
																								.getID()));
																				PlayListController.getInstance().addMusicAll(list);
																				showInfo(list.size()+" pieces of music from Channel "+channel.getTitle()+" added!");
																			}
																		})
																.build(),
														square).build();
										metroContents.getChildren().add(box);
										square.visibleProperty().addListener(
												new ChangeListener<Boolean>() {
													@Override
													public void changed(
															ObservableValue<? extends Boolean> observable,
															Boolean oldValue,
															Boolean newValue) {
														if (newValue
																.booleanValue() == true) {
															gotoNext = true;
															new FadeInUpTransition(
																	box).play();
														}
													}
												});
									} else
										showError("Loading " + title
												+ "...Failed to fetch the list");
								}
							});
					addWorks(new BackWork("Wait for a while") {
						@Override
						public void start() throws Exception {
							do {
								Thread.sleep(1000);
							} while (WorkFlowRunner.keepRunning && !gotoNext);
						}
					});
				}

				addWorks(new BackWork("Commit Music DB") {
					@Override
					public void start() throws Exception {
						musicDB.commit();
					}
				});
			}
		}.run();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		instance = this;

		DragUtil.setPaneClip(metroContentsContainer);
		final DragLimitArea limitRect = new DragLimitArea(
				metroContents.getLayoutX(), metroContents.getLayoutY(), 0, 0);
		metroContents.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				limitRect.width = 585 - newValue.doubleValue();
			}
		});
		DragUtil.setDraggable(metroContentsContainer, metroContents, true,
				false, 10, limitRect);

		setNavBar();
		setToolBar();
		setController();
		setMiniThumb();
		setTrackBar(0);
		setPlayingMusic(null);
	}

	public void backToMain() {
		toolPanelContainer.setVisible(false);
		navBar.setVisible(true);
		toolBar.setVisible(true);
		metroContentsContainer.setVisible(true);
		final Node[] navItems = new Node[] { homeNav, channelsNav, artistsNav,
				albumsNav, playlistsNav };
		for (Node item : navItems) {
			if (item == homeNav)
				item.getStyleClass().remove("deactivated");
			else if (!item.getStyleClass().contains("deactivated"))
				item.getStyleClass().add("deactivated");
		}
	}

	private void setNavBar() {
		final Node[] navItems = new Node[] { homeNav, channelsNav, artistsNav,
				albumsNav, playlistsNav };

		homeNav.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				for (Node item : navItems) {
					if (item == homeNav)
						item.getStyleClass().remove("deactivated");
					else if (!item.getStyleClass().contains("deactivated"))
						item.getStyleClass().add("deactivated");
				}
			}
		});
		channelsNav.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				for (Node item : navItems) {
					if (item == channelsNav)
						item.getStyleClass().remove("deactivated");
					else if (!item.getStyleClass().contains("deactivated"))
						item.getStyleClass().add("deactivated");
				}
			}
		});
		artistsNav.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				for (Node item : navItems) {
					if (item == artistsNav)
						item.getStyleClass().remove("deactivated");
					else if (!item.getStyleClass().contains("deactivated"))
						item.getStyleClass().add("deactivated");
				}
			}
		});
		albumsNav.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				for (Node item : navItems) {
					if (item == albumsNav)
						item.getStyleClass().remove("deactivated");
					else if (!item.getStyleClass().contains("deactivated"))
						item.getStyleClass().add("deactivated");
				}
			}
		});
		playlistsNav.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				for (Node item : navItems) {
					if (item == playlistsNav)
						item.getStyleClass().remove("deactivated");
					else if (!item.getStyleClass().contains("deactivated"))
						item.getStyleClass().add("deactivated");
				}
				navBar.setVisible(false);
				toolBar.setVisible(false);
				metroContentsContainer.setVisible(false);
				searchPanel.setVisible(false);
				settingPanel.setVisible(false);
				tuningPanel.setVisible(false);
				playListPanel.setOpacity(0);
				playListPanel.setVisible(true);
				toolPanelContainer.setVisible(true);
				new FlipInYTransition(playListPanel).play();
			}
		});
	}

	private void setToolBar() {
		tuningToolBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				navBar.setVisible(false);
				toolBar.setVisible(false);
				metroContentsContainer.setVisible(false);
				searchPanel.setVisible(false);
				settingPanel.setVisible(false);
				playListPanel.setVisible(false);
				tuningPanel.setOpacity(0);
				tuningPanel.setVisible(true);
				toolPanelContainer.setVisible(true);
				new FlipInYTransition(tuningPanel).play();
			}
		});
		settingToolBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				navBar.setVisible(false);
				toolBar.setVisible(false);
				metroContentsContainer.setVisible(false);
				searchPanel.setVisible(false);
				tuningPanel.setVisible(false);
				playListPanel.setVisible(false);
				settingPanel.setOpacity(0);
				settingPanel.setVisible(true);
				toolPanelContainer.setVisible(true);
				new FlipInYTransition(settingPanel).play();
			}
		});
		searchToolBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				navBar.setVisible(false);
				toolBar.setVisible(false);
				metroContentsContainer.setVisible(false);
				settingPanel.setVisible(false);
				tuningPanel.setVisible(false);
				playListPanel.setVisible(false);
				searchPanel.setOpacity(0);
				searchPanel.setVisible(true);
				toolPanelContainer.setVisible(true);
				new FlipInYTransition(searchPanel).play();
			}
		});
	}

	private void setController() {
		prevBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				PlayListController.getInstance().prev();
			}
		});

		nextBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				PlayListController.getInstance().next();
			}
		});

		playBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				boolean state = PlayListController.getInstance().playOrPause();
				if (state) {// playing
					setControllerPlayState(true);
				} else {// pausing
					setControllerPlayState(false);
				}
			}
		});

		lazyBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (musicDB.isReady()) {
					int count = PlayListController.getInstance().lazy(
							musicDB.getAllMusic());
					showInfo(count
							+ " pieces of random music added, \"lazy-gaga\"!");
				} else
					showInfo("Sorry, our music database is been initialized, please wait for a while!");
			}
		});
		downloadBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				new Thread() {
					public void run() {
						final TwtFmMusic music = PlayListController
								.getInstance().getCurrentMusic();
						if (music == null)
							return;
						if (CacheManager.getCachedMusic(music.getID()) == null) {
							Platform.runLater(new Thread() {
								public void run() {
									downloadBtn.setDisable(true);
									downloadProInd.setVisible(true);
									showInfo("Downloading music: \""
											+ music.getTitle()
											+ "\", please wait...");
								};
							});
							CacheManager.downloadMusic(music.getID(),
									music.getUrl());
							Platform.runLater(new Thread() {
								public void run() {
									downloadProInd.setVisible(false);
									downloadBtn.setDisable(false);
								}
							});
						}
						Platform.runLater(new Thread() {
							public void run() {
								if (CacheManager.getCachedMusic(music.getID()) != null)
									showInfo("Download music: \""
											+ music.getTitle()
											+ "\" finished, now you can play it offline!");
								checkCacheMusic(PlayListController
								.getInstance().getCurrentMusic());
							}
						});
					};
				}.start();
			}
		});
	}

	public boolean checkCacheMusic(TwtFmMusic music) {
		if (CacheManager.getCachedMusic(music.getID()) != null) {
			downloadBtnImg.getStyleClass().clear();
			downloadBtnImg.getStyleClass().add("controller-ok-image");
			downloadBtn.setDisable(true);
			return true;
		} else {
			downloadBtnImg.getStyleClass().clear();
			downloadBtnImg.getStyleClass().add("controller-download-image");
			downloadBtn.setDisable(false);
			return false;
		}
	}

	private void setControllerPlayState(boolean isPlaying) {
		if (isPlaying) {
			controllerPlayImage.getStyleClass().remove("controller-play-image");
			if (!controllerPlayImage.getStyleClass().contains(
					"controller-pause-image"))
				controllerPlayImage.getStyleClass().add(
						"controller-pause-image");
		} else {
			controllerPlayImage.getStyleClass()
					.remove("controller-pause-image");
			if (!controllerPlayImage.getStyleClass().contains(
					"controller-play-image"))
				controllerPlayImage.getStyleClass()
						.add("controller-play-image");
		}
	}

	private void setMiniThumb() {
		miniPlayerThumb.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				PlayerController.getInstance().playlistsNav.getOnMouseClicked()
						.handle(null);
			}
		});
	}

	public void setTrackBar(double progress) {
		if (progress <= 0) {
			// if(trackLine.getProgress() > 0)
			// new FadeOutDownTransition(trackLine).play();
		} else {
			if (trackLine.getProgress() <= 0) {
				trackLine.setProgress(progress);
				new FadeInUpTransition(trackLine).play();
			}
		}
		trackLine.setProgress(progress);
	}

	public void setPlayingMusic(final TwtFmMusic music) {
		if (music == null) {
			setControllerPlayState(false);
			if (!miniPlayerThumb.getStyleClass().contains("cover-image"))
				miniPlayerThumb.getStyleClass().add("cover-image");
			playerTitle.setText("Waiting for you...^_^");
		} else {
			if (miniPlayerThumb.getStyleClass().contains("cover-image"))
				miniPlayerThumb.getStyleClass().remove("cover-image");
			playerTitle.setText(music.getTitle());
			setControllerPlayState(true);
			new BackgroundRunner() {
				Image image = null;

				@Override
				public void handleException(Exception e) throws Exception {
					showError(e);
				}

				@Override
				public void background() throws Exception {
					URL url = CacheManager.getCachedCoverImageThumb(music
							.getID());
					if (url == null)
						url = CacheManager.getCachedCoverImage(music.getID());
					if (url == null) {
						runForeground();
						url = CacheManager.downloadCoverImageThumb(
								music.getID(), music.getCoverImageThumbUrl());
					}
					if (url != null)
						image = new Image(url.toExternalForm());
				}

				@Override
				public void foreground() throws Exception {
					if (!miniPlayerThumb.getStyleClass()
							.contains("cover-image"))
						miniPlayerThumb.getStyleClass().add("cover-image");
				}

				public void finish() throws Exception {
					if (image != null)
						miniPlayerThumb.setImage(image);
					else {
						if (!miniPlayerThumb.getStyleClass().contains(
								"cover-image"))
							miniPlayerThumb.getStyleClass().add("cover-image");
					}
				};
			}.run();
		}
	}

	public void setFullScreen(boolean isFullScreen) {
		if (isFullScreen) {
			AnchorPane.setBottomAnchor(backgroundPane, 0.0);
			AnchorPane.setTopAnchor(backgroundPane, 0.0);
			AnchorPane.setLeftAnchor(backgroundPane, 0.0);
			AnchorPane.setRightAnchor(backgroundPane, 0.0);
		} else {
			double border = 15;
			AnchorPane.setBottomAnchor(backgroundPane, border);
			AnchorPane.setTopAnchor(backgroundPane, border);
			AnchorPane.setLeftAnchor(backgroundPane, border);
			AnchorPane.setRightAnchor(backgroundPane, border);
		}
	}

	public void showMessage(final String str, final Paint paint,
			final long delay) {
		final EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (messageTimeline != null)
					messageTimeline.stop();
				titleMessage.setOpacity(0);
				titleMessage.setVisible(true);
				titleMessage.setText(str);
				titleMessage.setTextFill(paint);
				titleMessage.setTranslateX(0);
				titleMessage.setTranslateY(0);
				final Transition tran = new BounceInLeftTransition(titleMessage);
				tran.setOnFinished(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						new Thread() {
							public void run() {
								try {
									Thread.sleep(delay);
								} catch (Exception e) {

								}
								Platform.runLater(new Runnable() {

									@Override
									public void run() {
										Transition t = new FadeOutTransition(
												titleMessage);
										t.setOnFinished(new EventHandler<ActionEvent>() {

											@Override
											public void handle(ActionEvent event) {
												titleMessage.setVisible(false);
											}
										});
										t.play();
									}
								});
							};
						}.start();
					}
				});
				tran.play();
			}
		};
		if (Platform.isFxApplicationThread())
			handler.handle(new ActionEvent());
		else
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					handler.handle(new ActionEvent());
				}
			});

	}

	public void showInfo(String str) {
		showMessage(str, homeNav.getTextFill(), 5000);
	}

	public void showError(String str) {
		showMessage(str, Color.RED, 10000);
	}

	public void showError(Throwable e) {
		e.printStackTrace();
		showError(e.getLocalizedMessage());
	}

	public void showText(String lontText) {
		final String[] arr = lontText.split("\n");
		new Thread() {
			public void run() {
				for (String str : arr) {
					try {
						Thread.sleep(8000);
					} catch (InterruptedException e) {
					}
					if (!WorkFlowRunner.keepRunning)
						return;
					final String ss = str;
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							showInfo(ss);
						}
					});
				}
			};
		}.start();
	}

	public void showWelcome() {
		// showText("Welcome to use FXPlayer for TWT\n"
		// + "---- A simple Music player built with JavaFx2.1 (WIP)\n"
		// + "HAVE FUN!\n" + "By the way...Why not double click here?\n"
		// + "Thank you... you may exit this program by pressing ESC.");
	}
}
