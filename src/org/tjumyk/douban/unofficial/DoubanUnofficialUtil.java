package org.tjumyk.douban.unofficial;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class DoubanUnofficialUtil {
	public static class PhotoType {
		public static final int THUMB = 0, PHOTO = 1;
	}

	private static boolean initialized = false;

	private static void checkInit() throws ClientProtocolException, IOException {
		if (!initialized) {
			Request.Head("http://music.douban.com/")
					.version(HttpVersion.HTTP_1_1).connectTimeout(10000)
					.socketTimeout(10000).execute().discardContent();
			initialized = true;
		}
	}

	public static String getMusicianID(String name) {
		try {
			checkInit();
			String requestUrl = "http://music.douban.com/subject_search?search_text="
					+ URLEncoder.encode(name, "utf-8") + "&cat=1001";
			String html = Request.Get(requestUrl).version(HttpVersion.HTTP_1_1)
					.connectTimeout(10000).socketTimeout(10000).execute()
					.returnContent().asString();
			Document doc = Jsoup.parse(html);
			Element ele = doc.getElementsByClass("musician").first();
			if (ele != null) {// find musician
				ele = ele.getElementsByClass("nbg").first();
				if (ele != null) {// extract musician id from the image link
					String url = ele.attr("href");
					if (url.endsWith("/"))
						url = url.substring(0, url.length() - 1);
					return url.substring(url.lastIndexOf('/') + 1);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<String[]> getMusicianPhotos(String musicianID) {
		ArrayList<String[]> list = new ArrayList<String[]>();
		try {
			checkInit();
			int pageCount = 1, pageIndex = 0;
			do {
				String html = Request
						.Get("http://music.douban.com/musician/" + musicianID
								+ "/photos/?type=D&start=" + 40 * pageIndex
								+ "&sortby=all&size=a&subtype=a")
						.version(HttpVersion.HTTP_1_1).connectTimeout(10000)
						.socketTimeout(10000).execute().returnContent()
						.asString();
				Document doc = Jsoup.parse(html);
				if (pageIndex <= 0) {
					Element nav = doc.getElementsByClass("paginator").first();
					if (nav != null) {
						pageCount = nav.childNodes().size() - 3;
					}
				}
				Element ele = doc.getElementsByClass("poster-col4").first();
				if (ele != null) {// find photos
					for (Element img : ele.getElementsByTag("img")) {
						String url = img.attr("src");
						String url2 = url.replace("thumb", "photo");
						String[] arr = new String[2];
						arr[PhotoType.THUMB] = url;
						arr[PhotoType.PHOTO] = url2;
						list.add(arr);
					}
				}
				pageIndex++;
			} while (pageIndex < pageCount);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public static void main(String[] args) {
		System.out
				.println("===========Testing Get Musician ID [Unofficial]=========");
		String mid = getMusicianID("周杰伦");
		if (mid == null)
			System.out.println("Musician Not found");
		else {
			int count = 0;
			for (String[] imgs : getMusicianPhotos(mid)) {
				System.out.println("[Thumb Url]: " + imgs[PhotoType.THUMB]);
				System.out.println("[Photo Url]: " + imgs[PhotoType.PHOTO]);
				count ++;
			}
			System.out.println("Total images found:" + count/2);
		}
	}

}
