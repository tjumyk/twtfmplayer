package org.tjumyk.douban;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.TreeMap;

import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;

/**
 * Douban Music Info class for containing, parsing, and retrieving info from the
 * XML document returned by the Douban API.
 * 
 * @author tjumyk
 * @version 1.0
 */
public class DoubanMusicInfo {
	/**
	 * The helper enum class for identifing different link types.
	 * 
	 * @author tjumyk
	 * @version 1.0
	 */
	public enum LinkType {
		/**
		 * Self link of this XML document
		 */
		SELF,
		/**
		 * Alternate link about this music (The web page url)
		 */
		ALTERNATE,
		/**
		 * The link of cover image of this music (small size)
		 */
		SMALL_IMAGE,
		/**
		 * The link of cover image of this music (large size)
		 */
		LARGE_IMAGE,
		/**
		 * The link of the web page for mobile devices about this music
		 */
		MOBILE
	}

	private Element xml;

	/**
	 * Parse an XML element to construct a music info object.
	 * 
	 * @param xml
	 *            the XML element representing a sheet of info about a piece of
	 *            music
	 */
	public DoubanMusicInfo(Element xml) {
		this.xml = xml;
	}

	/**
	 * Parse an XML document to construct a music info object.
	 * 
	 * @param xmlDoc
	 *            the XML document containing only one sheet of music info,
	 *            should be retrieved by the Get Music Request, not Search Music
	 *            Request.
	 */
	public DoubanMusicInfo(Document xmlDoc) {
		this.xml = xmlDoc.getRootElement();
	}

	/**
	 * @return the ID of the music
	 */
	public String getID() {
		try {
			String str = xml.getElement(null, "id").getText(0);
			return str.substring(str.lastIndexOf('/') + 1);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * @return the title of the music
	 */
	public String getTitle() {
		try {
			return xml.getElement(null, "title").getText(0);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * @return the summary of the music
	 */
	public String getSummary() {
		try {
			return xml.getElement(null, "summary").getText(0);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Get a list of all the authors.
	 * 
	 * @return an {@link java.util.ArrayList ArrayList} of the names of all the
	 *         authors
	 */
	public ArrayList<String> getAuthors() {
		ArrayList<String> list = new ArrayList<String>();
		int size = xml.getChildCount();
		for (int i = 0; i < size; i++) {
			try {
				Object child = xml.getChild(i);
				if (!(child instanceof Element))
					continue;
				Element e = (Element) child;
				if (e.getName().equals("author"))
					list.add(((Element) (e.getChild(1))).getText(0));
			} catch (Exception e) {
			}
		}
		return list;
	}

	/**
	 * Get the links of the music.
	 * 
	 * @return a tree map containing different types of links.
	 */
	public TreeMap<LinkType, String> getLinks() {
		TreeMap<LinkType, String> list = new TreeMap<LinkType, String>();
		int size = xml.getChildCount();
		for (int i = 0; i < size; i++) {
			try {
				Object child = xml.getChild(i);
				if (!(child instanceof Element))
					continue;
				Element e = (Element) child;
				if (e.getName().equals("link")) {
					String type = e.getAttributeValue(null, "rel");
					if (type.equals("self"))
						list.put(LinkType.SELF,
								e.getAttributeValue(null, "href"));
					else if (type.equals("alternate"))
						list.put(LinkType.ALTERNATE,
								e.getAttributeValue(null, "href"));
					else if (type.equals("image")) {
						String url = e.getAttributeValue(null, "href");
						list.put(LinkType.SMALL_IMAGE, url);
						if (url != null && url.length() > 0) {
							String url2 = url.replace("spic", "lpic");
							list.put(LinkType.LARGE_IMAGE, url2);
						}
					} else if (type.equals("mobile"))
						list.put(LinkType.MOBILE,
								e.getAttributeValue(null, "href"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * Parse the XML document and generate a list of the music info objects.
	 * 
	 * @param doc
	 *            the XML document returned by the Douban server
	 * @return an {@link java.util.ArrayList ArrayList} of music info objects
	 */
	public static ArrayList<DoubanMusicInfo> parseResult(Document doc) {
		ArrayList<DoubanMusicInfo> list = new ArrayList<DoubanMusicInfo>();
		int size = doc.getRootElement().getChildCount();
		for (int i = 0; i < size; i++) {
			Object child = doc.getRootElement().getChild(i);
			if (!(child instanceof Element))
				continue;
			Element e = (Element) child;
			if (e.getName().equals("entry"))
				list.add(new DoubanMusicInfo(e));
		}
		return list;
	}

	/**
	 * Extract the content of the XML stored in a music info object
	 */
	@Override
	public String toString() {
		KXmlSerializer slr = new KXmlSerializer();
		StringWriter writer = new StringWriter();
		slr.setOutput(writer);
		try {
			xml.write(slr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}
}
