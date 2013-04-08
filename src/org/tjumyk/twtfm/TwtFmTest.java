package org.tjumyk.twtfm;

import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Test class for the {@link org.tjumyk.twtfm.TwtFmUtil TwtFmUtil}.
 * 
 * @author tjumyk
 * @version 1.0
 */
public class TwtFmTest {

	public static void testLoadList() {
		try {
			FileWriter writer = new FileWriter("list.txt");
			for (TwtFmChannel channel : TwtFmUtil.getChannelList()) {
				writer.write(channel.toString());
				writer.write("\n-----------------------------------------\n");
				for (TwtFmMusic music : TwtFmUtil.getMusicList(channel.getID())) {
					writer.write(music.toString());
					writer.write("\n");
				}
				writer.write("==========================================");
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		testLoadList();
		testCoverImage();
	}

	public static void testCoverImage() {
		try {
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new FlowLayout());
			TwtFmMusic music = TwtFmUtil.getMusicList("25").get(0);
			JLabel label1 = new JLabel(new ImageIcon(new URL(
					music.getCoverImageUrl())));
			frame.add(label1);
			JLabel label2 = new JLabel(new ImageIcon(new URL(
					music.getCoverImageThumbUrl())));
			frame.add(label2);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

}
