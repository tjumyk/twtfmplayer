package org.tjumyk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPaneBuilder;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.GridPaneBuilder;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.tjumyk.douban.DoubanMusicInfo;
import org.tjumyk.douban.DoubanUtil;
import org.tjumyk.douban.unofficial.DoubanUnofficialUtil;
import org.tjumyk.douban.unofficial.DoubanUnofficialUtil.PhotoType;
import org.tjumyk.thread.WorkFlowRunner;
import org.tjumyk.twtfm.TwtFmMusic;

import com.fxexperience.javafx.animation.FadeInRightTransition;

public class MetroBoxBuilder {
	public static GridPane buildMetroSquare(final int minUnitSize,
			final int space, final ArrayList<TwtFmMusic> musicList) {
		final GridPane pane = GridPaneBuilder.create()
				.styleClass("metro-square").hgap(space).vgap(space)
				.visible(false).padding(new Insets(0)).build();
		final Box[] boxes = BoxGenerator.generate();

		for (int i = 0; i < boxes.length; i++) {
			final int ii = i;
			new WorkFlowRunner() {
				TwtFmMusic music = musicList.get(ii);
				Box box = boxes[ii];
				int realW = box.size * minUnitSize + (box.size - 1) * space;
				int realH = realW;
				boolean isThumb = box.size <= 1;
				Image image = null;

				@Override
				public void handleException(Exception e) throws Exception {
					PlayerController.getInstance().showError(e);
				}

				@Override
				public void initWorkFlow() {
					addWorks(
							new BackWork("Load cover image for: "
									+ music.getID()) {
								public void start() throws Exception {
									URL url = null;
									if (isThumb)
										url = CacheManager.loadCoverImageThumb(
												music.getID(),
												music.getCoverImageThumbUrl());
									else
										url = CacheManager.loadCoverImage(
												music.getID(),
												music.getCoverImageUrl());
									if (url != null)
										image = new Image(url.toExternalForm(),
												true);
								};
							},
							new ForeWork("Building imageview for: "
									+ music.getID()) {

								@Override
								public void start() throws Exception {
									final ImageView view = ImageViewBuilder
											.create().image(image)
											.preserveRatio(false).smooth(true)
											.fitWidth(realW).fitHeight(realH)
											.build();
									final Node node = AnchorPaneBuilder
											.create()
											.styleClass("metro-box")
											.prefWidth(realW)
											.prefHeight(realH)
											.children(view)
											.opacity(0)
											.onMouseClicked(
													new EventHandler<MouseEvent>() {
														@Override
														public void handle(
																MouseEvent event) {
															// testBoxInfo(music);
															if(event.getButton()==MouseButton.PRIMARY)
															PlayListController
															.getInstance()
															.addMusic(
																	music);
															else if(event.getButton()==MouseButton.SECONDARY)
																PlayListController
																.getInstance()
																.addInstantPlayMusic(music);
														}
													}).build();
									new FadeInRightTransition(node).play();
									pane.add(node, box.x, box.y, box.size,
											box.size);
									if (ii >= boxes.length - 1) {// the last one
										pane.setVisible(true);
									}
								}
							});
				}
			}.run();
		}
		return pane;
	}

	@SuppressWarnings("unused")
	/**
	 * Only for test
	 * @param music
	 */
	private static void testBoxInfo(final TwtFmMusic music) {
		new Thread() {
			public void run() {
				JFrame frame = new JFrame();
				frame.setTitle(music.getTitle() + " - " + music.getSinger());
				frame.setLayout(new BorderLayout());
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				JTextArea area = new JTextArea();
				JPanel panel = new JPanel();
				panel.setPreferredSize(new Dimension(100, 200));
				area.setLineWrap(true);
				area.setWrapStyleWord(true);
				frame.add(new JScrollPane(area));
				frame.add(panel, BorderLayout.SOUTH);
				frame.setSize(600, 600);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
				area.append("[Info From TWT]:\n");
				area.append(music.toString());
				area.append("\n\n");
				area.append("[Info From Douban API]:\n");
				DoubanMusicInfo result = DoubanUtil.searchMusic(
						music.getTitle(), music.getSinger());
				if (result == null)
					area.append("Not found.");
				else {
					result = DoubanUtil.getMusicInfo(result.getID());
					area.append(result.getSummary());
				}
				area.append("\n\n[Searching " + music.getSinger()
						+ " in Douban Music...]\n");
				String mid = DoubanUnofficialUtil.getMusicianID(music
						.getSinger());
				final ArrayList<String[]> urls = DoubanUnofficialUtil
						.getMusicianPhotos(mid);
				int num = 5;
				if (urls.size() < 5)
					num = urls.size();
				area.append("\n\n[Loading " + num + " photos from Douban...]\n");
				for (int i = 0; i < num; i++) {
					final int ii = i;
					try {
						JLabel label = new JLabel(new ImageIcon(new URL(
								urls.get(i)[PhotoType.THUMB])));
						panel.add(label);
						label.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(java.awt.event.MouseEvent e) {
								try {
									JFrame frame = new JFrame();
									frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
									frame.add(new JLabel(new ImageIcon(new URL(
											urls.get(ii)[PhotoType.PHOTO]))));
									frame.pack();
									frame.setVisible(true);
								} catch (Exception e1) {
									e1.printStackTrace();
								}
							}
						});
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
				frame.setVisible(false);
				frame.setVisible(true);
			};
		}.start();
	}

	public static class BoxGenerator {
		int[] boxsize = new int[] { 3, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1 };
		int[][] map = new int[7][4];
		Box[] ans = new Box[11];

		private BoxGenerator() {
			for (int i = 0; i < map.length; i++)
				map[i] = new int[4];
			for (int i = 0; i < ans.length; i++)
				ans[i] = new Box(0, 0, 0);
		}

		public static Box[] generate() {
			BoxGenerator gen = new BoxGenerator();
			gen.dfs(0);
			Box[] ans = gen.ans;
			return ans;
		}

		private void dfs(int k) {
			if (k >= 11)
				return;
			if (k == 0) {
				int temp;
				int[] x = new int[] { 0, 0, 2, 2, 4, 4 };
				int[] y = new int[] { 0, 1, 0, 1, 0, 1 };
				temp = (int) Math.floor(Math.random() * 6);
				ans[k].x = x[temp];
				ans[k].y = y[temp];
				ans[k].size = boxsize[k];
			} else if (k == 1 || k == 2) {
				int temp = 0, ok = 0;
				int[] x = new int[] { 0, 0, 2, 2, 3, 3, 5, 5 };
				int[] y = new int[] { 0, 2, 0, 2, 0, 2, 0, 2 };
				while (ok == 0) {
					temp = (int) Math.floor(Math.random() * 8);
					if (map[x[temp]][y[temp]] == 0
							&& map[x[temp]][y[temp] + 1] == 0
							&& map[x[temp] + 1][y[temp]] == 0
							&& map[x[temp] + 1][y[temp] + 1] == 0)
						ok = 1;
				}
				ans[k].x = x[temp];
				ans[k].y = y[temp];
				ans[k].size = boxsize[k];
			} else if (k == 3) {
				int ok = 0;
				int x = 0, y = 0;
				while (ok == 0) {
					x = (int) Math.floor(Math.random() * 6);
					y = (int) Math.floor(Math.random() * 3);
					if (map[x][y] == 0 && map[x][y + 1] == 0
							&& map[x + 1][y] == 0 && map[x + 1][y + 1] == 0)
						ok = 1;
				}
				ans[k].x = x;
				ans[k].y = y;
				ans[k].size = boxsize[k];
			} else if (k >= 4) {
				int x = 0, y = 0;
				for (x = 0; x < 7; x++) {
					for (y = 0; y < 4; y++)
						if (map[x][y] == 0)
							break;
					if (y < 4)
						break;
				}
				ans[k].x = x;
				ans[k].y = y;
				ans[k].size = boxsize[k];
			}
			for (int i = ans[k].x; i < ans[k].x + ans[k].size; i++)
				for (int j = ans[k].y; j < ans[k].y + ans[k].size; j++)
					map[i][j] = ans[k].size;
			dfs(k + 1);
		}
	}

	private static class Box {
		int x, y, size;

		public Box(int x, int y, int size) {
			super();
			this.x = x;
			this.y = y;
			this.size = size;
		}

		@Override
		public String toString() {
			return "Box [x=" + x + ", y=" + y + ", size=" + size + "]";
		}

	}
}
