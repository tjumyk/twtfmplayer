package org.tjumyk;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TimelineBuilder;
import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import org.tjumyk.thread.BackgroundRunner;
import org.tjumyk.twtfm.TwtFmMusic;
import org.tjumyk.util.DragUtil;
import org.tjumyk.util.DragUtil.DragLimitArea;

import com.fxexperience.javafx.animation.FlipOutYTransition;

public class PlayListController implements Initializable {
	/*************************************
	 * Single Instance
	 *************************************/
	private static PlayListController instance;

	public static PlayListController getInstance() {
		return instance;
	}

	/*************************************
	 * FXML Nodes
	 *************************************/
	@FXML
	ImageView playListBackBtn, bigCoverImage;
	@FXML
	VBox playListBox;
	@FXML
	Label songName, artistName, albumName;
	@FXML
	AnchorPane playListContainer;

	/*************************************
	 * Player things
	 *************************************/
	private ObservableList<TwtFmMusic> musicList = FXCollections
			.observableArrayList();
	public MediaPlayer mediaPlayer;
	private int curreentSongIndex = -1;
	private boolean playListFinished;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		instance = this;

		playListBackBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				Transition tran = new FlipOutYTransition(PlayerController
						.getInstance().playListPanel);
				tran.setOnFinished(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						PlayerController.getInstance().backToMain();
					}
				});
				tran.play();
			};
		});

		DragUtil.setPaneClip(playListContainer);
		final DragLimitArea limitRect = new DragLimitArea(
				playListBox.getLayoutX(), playListBox.getLayoutY(), 0, 0);
		playListBox.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				limitRect.height = Math.min(0, playListContainer.getHeight() - newValue.doubleValue());
			}
		});
		DragUtil.setDraggable(playListContainer, playListBox, false,
				true, 10, limitRect);
		
		setPlayList();
	}
	
	public TwtFmMusic getCurrentMusic(){
		if(musicList != null && curreentSongIndex >= 0 && curreentSongIndex < musicList.size())
			return musicList.get(curreentSongIndex);
		return null;
	}

	public void setPlayList() {
		musicList.addListener(new ListChangeListener<TwtFmMusic>() {
			@Override
			public void onChanged(Change<? extends TwtFmMusic> change) {
				playListBox.getChildren().clear();
				for (int i = 0; i < musicList.size(); i++) {
					final int ii = i;
					TwtFmMusic music = musicList.get(i);
					playListBox.getChildren().add(
							LabelBuilder
									.create()
									.onMouseClicked(
											new EventHandler<MouseEvent>() {
												@Override
												public void handle(
														MouseEvent event) {
													curreentSongIndex = ii;
													play();
													highlightPlaying();
												}
											})
									.styleClass("play-list-items")
									.text(music.getTitle() + " - "
											+ music.getSinger() + " - "
											+ music.getAlbum()).build());
				}
				highlightPlaying();
				PlayerController.getInstance().playlistsNav.getOnMouseClicked()
						.handle(null);
			}
		});
	}

	public void addMusic(TwtFmMusic music) {
		musicList.add(music);
		if (musicList.size() == 1) {// auto start
			curreentSongIndex = 0;
			play();
		} else if (playListFinished) {
			curreentSongIndex = musicList.size() - 1;
			play();
		}
	}
	
	public void addMusicAll(Collection<? extends TwtFmMusic> list) {
		musicList.addAll(list);
		if (musicList.size() == list.size()) {// auto start
			curreentSongIndex = 0;
			play();
		} else if (playListFinished) {
			curreentSongIndex = musicList.size() - list.size();
			play();
		}
	}

	public void clearList() {
		musicList.clear();
	}

	public void addInstantPlayMusic(TwtFmMusic music) {
		musicList.add(0, music);
		curreentSongIndex = 0;
		play();
	}

	public void next() {
		curreentSongIndex++;
		if (curreentSongIndex >= 0 && curreentSongIndex < musicList.size()) {
			play();
		} else
			curreentSongIndex--;
	}

	public void prev() {
		curreentSongIndex--;
		if (curreentSongIndex >= 0 && curreentSongIndex < musicList.size()) {
			play();
		} else
			curreentSongIndex++;
	}

	public boolean playOrPause() {
		if (mediaPlayer == null) {
			play();
			return true;
		} else if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
			Timeline tl = new Timeline(new KeyFrame(Duration.seconds(1.0),
					new KeyValue(mediaPlayer.volumeProperty(), 0)));
			tl.setOnFinished(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					mediaPlayer.pause();
				}
			});
			tl.play();
			return false;
		} else {
			mediaPlayer.play();
			Timeline tl = new Timeline(new KeyFrame(Duration.seconds(1.0),
					new KeyValue(mediaPlayer.volumeProperty(), 1.0)));
			tl.play();
			return true;
		}
	}

	private void highlightPlaying() {
		for (int i = 0; i < playListBox.getChildren().size(); i++) {
			Node node = playListBox.getChildren().get(i);
			if (i == curreentSongIndex) {
				node.getStyleClass().remove("play-list-items");
				if (!node.getStyleClass().contains("play-list-current")) {
					node.getStyleClass().add("play-list-current");
				}
			} else {
				node.getStyleClass().remove("play-list-current");
				if (!node.getStyleClass().contains("play-list-items")) {
					node.getStyleClass().add("play-list-items");
				}
			}
		}
	}

	private void play() {
		play(curreentSongIndex);
	}

	private void play(final int songIndex) {
		PlayerController.getInstance().setTrackBar(0);
		final EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (mediaPlayer != null)
					mediaPlayer.stop();
				TwtFmMusic music = musicList.get(songIndex);
				URL cacheUrl = CacheManager.getCachedMusic(music.getID());
				final Media media = new Media(cacheUrl == null ? music.getUrl():cacheUrl.toExternalForm());
				PlayerController.getInstance().checkCacheMusic(music);
				mediaPlayer = new MediaPlayer(media);
				mediaPlayer.setAutoPlay(true);
				playListFinished = false;
				mediaPlayer.setOnError(new Runnable() {
					@Override
					public void run() {
						PlayerController.getInstance().showError(
								mediaPlayer.getError());
					}
				});
				setPlayingMusic(music);
				highlightPlaying();
				PlayerController.getInstance().setPlayingMusic(music);
				mediaPlayer.currentTimeProperty().addListener(
						new ChangeListener<Duration>() {
							@Override
							public void changed(
									ObservableValue<? extends Duration> observable,
									Duration oldValue, Duration newValue) {
								PlayerController.getInstance().setTrackBar(
										newValue.toSeconds()
												/ mediaPlayer
														.getTotalDuration()
														.toSeconds());
							}
						});
				mediaPlayer.setOnEndOfMedia(new Runnable() {

					@Override
					public void run() {
						if (curreentSongIndex < musicList.size() - 1)
							next();
						else
							playListFinished = true;
					}
				});
			}
		};

		if (mediaPlayer != null
				&& mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
			Timeline tl = TimelineBuilder
					.create()
					.keyFrames(
							new KeyFrame(Duration.seconds(1.0), new KeyValue(
									mediaPlayer.volumeProperty(), 0))).build();
			tl.setOnFinished(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					mediaPlayer.stop();
					handler.handle(event);
				}
			});
			tl.play();
		} else
			handler.handle(new ActionEvent());
	}

	private void setPlayingMusic(final TwtFmMusic music) {
		if (music != null) {
			songName.setText(music.getTitle());
			artistName.setText(music.getSinger());
			albumName.setText(music.getAlbum());
			new BackgroundRunner() {
				URL url;

				@Override
				public void handleException(Exception e) throws Exception {
					PlayerController.getInstance().showError(e);
				}

				@Override
				public void background() throws Exception {
					url = CacheManager.loadCoverImage(music.getID(),
							music.getCoverImageUrl());
				}

				@Override
				public void finish() throws Exception {
					bigCoverImage.setImage(new Image(url.toExternalForm()));
				}
			}.run();
		}
	}

	public int lazy(ArrayList<TwtFmMusic> arrayList) {
		return lazy(arrayList, 20);
	}
	
	public int lazy(ArrayList<TwtFmMusic> arrayList, int maxNum) {
		//musicList.clear();
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i = 0 ; i < arrayList.size() ; i++){
			list.add(i);
		}
		ArrayList<TwtFmMusic> musicList = new ArrayList<TwtFmMusic>();
		int count = 0;
		for(int i = 0 ; i < maxNum ; i++){
			if(list.size() <= 0)
				break;
			int rand = (int) (Math.random() * list.size());
			musicList.add(arrayList.get(list.get(rand)));
			list.remove(rand);
			count ++;
		}
		addMusicAll(musicList);
		return count;
	}
}
