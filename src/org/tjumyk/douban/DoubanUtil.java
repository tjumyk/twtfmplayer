package org.tjumyk.douban;

import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;

/**
 * Douban utility class for searching and retrieving info from Douban databse
 * via Douban API.<br>
 * Currently only the music module is supported and only part of info about the music is supported.
 * 
 * @author tjumyk
 * @version 1.0
 */
public class DoubanUtil {

	/**
	 * Get information of the music with the specific subjectID.
	 * 
	 * @param subjectID
	 *            subjectID of the music
	 * @return music information object or {@code null} if not found or error
	 *         occurs
	 */
	public static DoubanMusicInfo getMusicInfo(String subjectID) {
		try {
			KXmlParser parser = new KXmlParser();
			String str = Request
					.Get("http://api.douban.com/music/subject/" + subjectID)
					.version(HttpVersion.HTTP_1_1).connectTimeout(10000)
					.socketTimeout(10000).execute().returnContent().asString();
			parser.setInput(new StringReader(str));
			Document doc = new Document();
			doc.parse(parser);
			return new DoubanMusicInfo(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Search music with the specific tag, return the results with the specific
	 * start index and maximum result amount.
	 * 
	 * @param tag
	 *            tag of the music
	 * @param startIndex
	 *            start index of the search results
	 * @param maxResults
	 *            maximum amount of the search results
	 * @return an {@link java.util.ArrayList ArrayList} of the search results or
	 *         {@code null} if error occurs
	 */
	public static ArrayList<DoubanMusicInfo> searchMusicByTag(String tag,
			int startIndex, int maxResults) {
		try {
			KXmlParser parser = new KXmlParser();
			String str = Request
					.Get("http://api.douban.com/music/subjects?tag=" + URLEncoder.encode(tag,"utf-8")
							+ "&start-index=" + startIndex + "&max-results="
							+ maxResults).version(HttpVersion.HTTP_1_1)
					.connectTimeout(10000).socketTimeout(10000).execute()
					.returnContent().asString();
			parser.setInput(new StringReader(str));
			Document doc = new Document();
			doc.parse(parser);
			return DoubanMusicInfo.parseResult(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Search music with the specific keyword, return the results with the
	 * specific start index and maximum result amount.
	 * 
	 * @param keyword
	 *            keyword of the music
	 * @param startIndex
	 *            start index of the search results
	 * @param maxResults
	 *            maximum amount of the search results
	 * @return an {@link java.util.ArrayList ArrayList} of the search results or
	 *         {@code null} if error occurs
	 */
	public static ArrayList<DoubanMusicInfo> searchMusicByKeyword(
			String keyword, int startIndex, int maxResults) {
		try {
			KXmlParser parser = new KXmlParser();
			String str = Request
					.Get("http://api.douban.com/music/subjects?q=" + URLEncoder.encode(keyword, "utf-8")
							+ "&start-index=" + startIndex + "&max-results="
							+ maxResults).version(HttpVersion.HTTP_1_1)
					.connectTimeout(10000).socketTimeout(10000).execute()
					.returnContent().asString();
			parser.setInput(new StringReader(str));
			Document doc = new Document();
			doc.parse(parser);
			return DoubanMusicInfo.parseResult(doc);
		} catch (Exception  e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Search music with the specific title and authors' names
	 * 
	 * @param title
	 *            title of the music
	 * @param authors
	 *            names of the authors
	 * @return music info of the search result or {@code null} if not found or
	 *         error occurs
	 */
	public static DoubanMusicInfo searchMusic(String title, String... authors) {
		for (DoubanMusicInfo result : searchMusicByKeyword(title, 0, 8)) {
			boolean match = true;
			ArrayList<String> authorList = result.getAuthors();
			for (String author : authors)
				if (!authorList.contains(author)) {
					match = false;
					break;
				}
			if (match)
				return result;
		}
		return null;
	}
}
