package org.tjumyk.twtfm;

import java.util.ArrayList;

import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;

/**
 * Music class for containing, parsing, and retrieving music info from the XML
 * document returned by the TwtFm server.
 * 
 * @author tjumyk
 * @version 1.0
 */
public class TwtFmMusic {
	private String title, index, rank, singer, album, duration, quality, url,
			id;

	/**
	 * Parse an XML element to construct a music object.
	 * 
	 * @param xml
	 *            the XML element representing a music object
	 */
	public TwtFmMusic(Element xml) {
		title = ((Element) xml.getChild(3)).getText(0);
		index = ((Element) xml.getChild(5)).getText(0);
		rank = ((Element) xml.getChild(7)).getText(0);
		singer = ((Element) xml.getChild(9)).getText(0);
		album = ((Element) xml.getChild(11)).getText(0);
		duration = ((Element) xml.getChild(13)).getText(0);
		quality = ((Element) xml.getChild(15)).getText(0);
		url = ((Element) xml.getChild(17)).getText(0);
		id = ((Element) xml.getChild(19)).getText(0);
	}

	/**
	 * Generate a string to show all the data inside a music object
	 */
	@Override
	public String toString() {
		return "TwtFmMusic [title=" + title + ", index=" + index + ", rank="
				+ rank + ", singer=" + singer + ", album=" + album
				+ ", duration=" + duration + ", quality=" + quality + ", url="
				+ url + ", id=" + id + "]";
	}

	/**
	 * @return the title of the music
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the ID(or index) of the music
	 */
	public String getID() {
		return index;
	}

	/**
	 * @return the rank of the music
	 */
	public String getRank() {
		return rank;
	}

	/**
	 * @return the singer of the music
	 */
	public String getSinger() {
		return singer;
	}

	/**
	 * @return the album of the music
	 */
	public String getAlbum() {
		return album;
	}

	/**
	 * @return the duration of the music
	 */
	public String getDuration() {
		return duration;
	}

	/**
	 * @return the quality of the music
	 */
	public String getQuality() {
		return quality;
	}

	/**
	 * @return the url of the music file
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the index in the music list
	 */
	public String getIndexInList() {
		return id;
	}

	public String getCoverImageUrl() {
		return "http://fm.twt.edu.cn/image/?m=cover&d=music&id=" + index;
	}

	public String getCoverImageThumbUrl() {
		return "http://fm.twt.edu.cn/image/?m=cover&d=music&pic=thumb&id="
				+ index;
	}

	/**
	 * Parse an XML document and generate a list of music.
	 * 
	 * @param xmlDoc
	 *            the document returned by the TwtFm server
	 * @return an {@link java.util.ArrayList ArrayList} of the music
	 */
	public static ArrayList<TwtFmMusic> parseMusic(Document xmlDoc) {
		Element xml = xmlDoc.getRootElement();
		ArrayList<TwtFmMusic> list = new ArrayList<TwtFmMusic>();
		int size = xml.getChildCount();
		for (int i = 0; i < size; i++) {
			try {
				Object child = xml.getChild(i);
				if (!(child instanceof Element))
					continue;
				Element e = (Element) child;
				if (e.getName().equals("item"))
					list.add(new TwtFmMusic(e));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}

}
