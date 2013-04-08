package org.tjumyk.douban;

import java.util.ArrayList;

/**
 * Test class for the {@link org.tjumyk.douban.DoubanUtil DoubanUtil}
 * 
 * @author tjumyk
 * @version 1.0
 */
public class DoubanTest {

	public static void main(String[] args) {
		System.out.println("==========Testing [Get Music Info] ===========");
		DoubanMusicInfo info = DoubanUtil.getMusicInfo("2272292");
		if (info != null) {
			System.out.println(info.getTitle());
			System.out.println(info.getSummary());
			System.out.println(info.getAuthors());
			System.out.println(info.getLinks());
		} else {
			System.err.println("Error!");
		}
		System.out
				.println("============Testing [Search Music By Tag] ============");
		ArrayList<DoubanMusicInfo> results = DoubanUtil.searchMusicByTag(
				"你是我的眼", 1, 3);
		if (results != null && results.size() > 0) {
			for (DoubanMusicInfo info2 : results) {
				info2 = DoubanUtil.getMusicInfo(info2.getID());
				System.out.println("Title:\n" + info2.getTitle());
				System.out.println("Authors:\n" + info2.getAuthors());
				System.out.println("Summary:\n" + info2.getSummary());
			}
		} else {
			System.err.println("Error!");
		}

		System.out
				.println("============Testing [Search Music By Keyword] ============");
		ArrayList<DoubanMusicInfo> results2 = DoubanUtil.searchMusicByKeyword(
				"你是我的眼", 1, 3);
		if (results != null && results.size() > 0) {
			for (DoubanMusicInfo info2 : results2) {
				info2 = DoubanUtil.getMusicInfo(info2.getID());
				System.out.println("Title:\n" + info2.getTitle());
				System.out.println("Authors:\n" + info2.getAuthors());
				System.out.println("Summary:\n" + info2.getSummary());
			}
		} else {
			System.err.println("Error!");
		}
	}

}
