package org.tjumyk.twtfm;

import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;

/**
 * TwtFm utility class for retrieving music info and resources from TWT FM<br>
 * (Website: fm.twt.edu.cn)
 * 
 * @author tjumyk
 * @version 1.0
 */
public class TwtFmUtil {

	/**
	 * Get a list of all the music channels.
	 * 
	 * @return an {@link java.util.ArrayList ArrayList} of all the channels
	 */
	public static ArrayList<TwtFmChannel> getChannelList() {
		return getChannelList(null);
	}

	/**
	 * Get a list of all the music channels, and send the original XML document
	 * to the writer if parsed successfully.
	 * 
	 * @return an {@link java.util.ArrayList ArrayList} of all the channels
	 */
	public static ArrayList<TwtFmChannel> getChannelList(Writer writer) {
		try {
			KXmlParser parser = new KXmlParser();
			String str = Request
					.Get("http://fm.twt.edu.cn/api/twtfm.channel.php")
					.version(HttpVersion.HTTP_1_1).connectTimeout(10000)
					.socketTimeout(10000).execute().returnContent().asString();
			parser.setInput(new StringReader(str));
			Document doc = new Document();
			doc.parse(parser);
			ArrayList<TwtFmChannel> list = TwtFmChannel.parseChannel(doc);
			if (writer != null && list != null && list.size() > 0) {
				writer.write(str);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get a list of all the music in a specific channel.
	 * 
	 * @param channelID
	 *            the id(or index) of the music channel
	 * @return an {@link java.util.ArrayList ArrayList} of all the music
	 */
	public static ArrayList<TwtFmMusic> getMusicList(String channelID) {
		return getMusicList(channelID, null);
	}

	/**
	 * Get a list of all the music in a specific channel, and send the original
	 * XML document to the writer if parsed successfully.
	 * 
	 * @param channelID
	 *            the id(or index) of the music channel
	 * @return an {@link java.util.ArrayList ArrayList} of all the music
	 */
	public static ArrayList<TwtFmMusic> getMusicList(String channelID,
			Writer writer) {
		try {
			KXmlParser parser = new KXmlParser();
			String str = Request
					.Get("http://fm.twt.edu.cn/api/twtfm.php?cid=" + channelID)
					.version(HttpVersion.HTTP_1_1).connectTimeout(10000)
					.socketTimeout(10000).execute().returnContent().asString();
			parser.setInput(new StringReader(str));
			Document doc = new Document();
			doc.parse(parser);
			ArrayList<TwtFmMusic> list = TwtFmMusic.parseMusic(doc);
			if (writer != null && list != null && list.size() > 0) {
				writer.write(str);
			}
			return list;
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Sort the existing music list with the "rank" value(in descending order)
	 * and return a new sorted list. No change happens in the original list.
	 * 
	 * @param list
	 *            the existing music list
	 * @return a new sorted music list
	 */
	public static ArrayList<TwtFmMusic> sortMusicByRank(
			ArrayList<TwtFmMusic> list) {
		Object[] array = list.toArray();
		Arrays.sort(array, new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				TwtFmMusic m1 = (TwtFmMusic) o1, m2 = (TwtFmMusic) o2;
				int rank1 = Integer.parseInt(m1.getRank());
				int rank2 = Integer.parseInt(m2.getRank());
				return rank2 - rank1;
			}
		});
		ArrayList<TwtFmMusic> newList = new ArrayList<TwtFmMusic>();
		for (Object obj : array) {
			TwtFmMusic m = (TwtFmMusic) obj;
			newList.add(m);
		}
		return newList;
	}

	/**
	 * Shuffle a music list and return a new random-ordered list. No change
	 * happens in the original list.
	 * 
	 * @param list
	 *            the existing music list
	 * @return a new random-ordered music list
	 */
	public static ArrayList<TwtFmMusic> shuffleList(ArrayList<TwtFmMusic> list) {
		ArrayList<Integer> ind = new ArrayList<Integer>();
		for(int i = 0  ; i < list.size() ;  i++)
			ind.add(i);
		ArrayList<TwtFmMusic> newList = new ArrayList<TwtFmMusic>();
		for (int i = 0 ;  i< list.size() ; i ++) {
			int index = (int) (Math.random()*ind.size());
			newList.add(list.get(ind.get(index)));
			ind.remove(index);
		}
		return newList;
	}
}
