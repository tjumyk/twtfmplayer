package org.tjumyk.twtfm;

import java.util.ArrayList;

import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;

/**
 * Channel class for containing, parsing, and retrieving channel info from the
 * XML document returned by the TwtFm server.
 * 
 * @author tjumyk
 * @version 1.0
 */
public class TwtFmChannel {
	private String index, title, avatar, intro, theme;

	/**
	 * Parse an XML element to construct a channel object.
	 * 
	 * @param xml
	 *            the XML element representing a channel object
	 */
	public TwtFmChannel(Element xml) {
		index = ((Element) xml.getChild(3)).getText(0);
		title = ((Element) xml.getChild(5)).getText(0);
		avatar = ((Element) xml.getChild(7)).getText(0);
		intro = ((Element) xml.getChild(9)).getText(0);
		theme = ((Element) xml.getChild(11)).getText(0);
	}

	/**
	 * @return the id(or index) of the channel
	 */
	public String getID() {
		return index;
	}

	/**
	 * @return the title of the channel
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the avatar of the channel
	 */
	public String getAvatar() {
		return avatar;
	}

	/**
	 * @return the introduction of the channel
	 */
	public String getIntro() {
		return intro;
	}

	/**
	 * @return the theme of the channel<br>
	 *         (in web color syntax, e.g. "#0011FF")
	 */
	public String getTheme() {
		return theme;
	}

	public ArrayList<TwtFmMusic> getMusicList() {
		return TwtFmUtil.getMusicList(this.getID());
	}

	/**
	 * Generate a string to show all the data inside a channel object
	 */
	@Override
	public String toString() {
		return "TwtFmChannel [index=" + index + ", title=" + title
				+ ", avatar=" + avatar + ", intro=" + intro + ", theme="
				+ theme + "]";
	}

	/**
	 * Parse an XML document and generate a list of channels.
	 * 
	 * @param xmlDoc
	 *            the document returned by the TwtFm server
	 * @return an {@link java.util.ArrayList ArrayList} of the channels
	 */
	public static ArrayList<TwtFmChannel> parseChannel(Document xmlDoc) {
		Element xml = xmlDoc.getRootElement();
		ArrayList<TwtFmChannel> list = new ArrayList<TwtFmChannel>();
		int size = xml.getChildCount();
		for (int i = 0; i < size; i++) {
			try {
				Object child = xml.getChild(i);
				if (!(child instanceof Element))
					continue;
				Element e = (Element) child;
				if (e.getName().equals("channel"))
					list.add(new TwtFmChannel(e));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}
}
