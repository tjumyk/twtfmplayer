package org.tjumyk;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;

import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.tjumyk.twtfm.TwtFmChannel;
import org.tjumyk.twtfm.TwtFmMusic;
import org.tjumyk.twtfm.TwtFmUtil;
import org.tjumyk.util.ImageUtil;

public class CacheManager {
	/*********************************************
	 * Global state value indicating whether it need to keep working
	 *********************************************/
	private static boolean keepWorking = true;
	public static void cancelWork(){
		keepWorking = false;
	};
	
	/*********************************************
	 * Location of the cache directories
	 *********************************************/
	private static final String ROOT_CACHE_DIR = System.getenv("APPDATA")
			+ File.separator + "FXPlayer" + File.separator;
	private static final String COVERIMG_CACHE_DIR = ROOT_CACHE_DIR
			+ "coverImg" + File.separator;
	private static final String COVERIMG_THUMB_CACHE_DIR = ROOT_CACHE_DIR
			+ "coverImgThumb" + File.separator;
	private static final String MUSIC_CACHE_DIR = ROOT_CACHE_DIR + "music"
			+ File.separator;
	private static final String CHANNEL_LIST_CACHE_DIR = ROOT_CACHE_DIR
			+ "channelList" + File.separator;
	private static final String MUSIC_LIST_CACHE_DIR = ROOT_CACHE_DIR
			+ "musicList" + File.separator;

	public static void init() {
		System.out.println("Initializing cache system...");
		File dir = new File(COVERIMG_CACHE_DIR);
		if (!dir.exists())
			dir.mkdirs();
		dir = new File(COVERIMG_THUMB_CACHE_DIR);
		if (!dir.exists())
			dir.mkdirs();
		dir = new File(CHANNEL_LIST_CACHE_DIR);
		if (!dir.exists())
			dir.mkdirs();
		dir = new File(MUSIC_LIST_CACHE_DIR);
		if (!dir.exists())
			dir.mkdirs();
		dir = new File(MUSIC_CACHE_DIR);
		if (!dir.exists())
			dir.mkdirs();
	}

	/*********************************************
	 * Retrieve data or file from cache directory
	 *********************************************/
	public static ArrayList<TwtFmChannel> getCachedChannelList() {
		System.out.println("Fetching channel list from cache...");
		try {
			File file = new File(CHANNEL_LIST_CACHE_DIR + "list.xml");
			if (!file.exists())
				return null;
			DataInputStream input = new DataInputStream(new FileInputStream(
					file));
			byte[] b = new byte[1024];
			int l = 0;
			StringBuilder sb = new StringBuilder();
			while ((l = input.read(b, 0, 1024)) > 0) {
				sb.append(new String(b, 0, l));
			}
			input.close();
			String str = sb.toString();
			if (str.length() <= 0)
				return null;
			KXmlParser parser = new KXmlParser();
			parser.setInput(new StringReader(str));
			Document doc = new Document();
			doc.parse(parser);
			return TwtFmChannel.parseChannel(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<TwtFmMusic> getCachedMusicList(String channelID) {
		System.out.println("Fetching music list for " + channelID
				+ " from cache...");
		try {
			File file = new File(MUSIC_LIST_CACHE_DIR + channelID + ".xml");
			if (!file.exists())
				return null;
			DataInputStream input = new DataInputStream(new FileInputStream(
					file));
			byte[] b = new byte[1024];
			int l = 0;
			StringBuilder sb = new StringBuilder();
			while ((l = input.read(b, 0, 1024)) > 0) {
				sb.append(new String(b, 0, l));
			}
			input.close();
			String str = sb.toString();
			if (str.length() <= 0)
				return null;
			KXmlParser parser = new KXmlParser();
			parser.setInput(new StringReader(str));
			Document doc = new Document();
			doc.parse(parser);
			return TwtFmMusic.parseMusic(doc);
		} catch (Exception  e) {
			e.printStackTrace();
		}
		return null;
	}

	public static URL getCachedCoverImage(String musicID) {
		System.out.println("Fetching cover image for " + musicID
				+ " from cache...");
		try {
			File dir = new File(COVERIMG_CACHE_DIR);
			if (!dir.exists() || !dir.isDirectory())
				return null;
			File[] files = dir.listFiles();
			for (File file : files) {
				String name = file.getName();
				int index = name.lastIndexOf('.');
				if (index >= 0)
					name = name.substring(0, index);
				if (name.equals(musicID))
					return new URL("file:///" + file.getCanonicalPath());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static URL getCachedCoverImageThumb(String musicID) {
		System.out.println("Fetching cover image thumb for " + musicID
				+ " from cache...");
		try {
			File dir = new File(COVERIMG_THUMB_CACHE_DIR);
			if (!dir.exists() || !dir.isDirectory())
				return null;
			File[] files = dir.listFiles();
			for (File file : files) {
				String name = file.getName();
				int index = name.lastIndexOf('.');
				if (index >= 0)
					name = name.substring(0, index);
				if (name.equals(musicID))
					return new URL("file:///" + file.getCanonicalPath());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static URL getCachedMusic(String musicID) {
		System.out.println("Fetching music for " + musicID + " from cache...");
		try {
			File dir = new File(MUSIC_CACHE_DIR);
			if (!dir.exists() || !dir.isDirectory())
				return null;
			File[] files = dir.listFiles();
			for (File file : files) {
				String name = file.getName();
				int index = name.lastIndexOf('.');
				if (index >= 0)
					name = name.substring(0, index);
				if (name.equals(musicID))
					return new URL("file:///" + file.getCanonicalPath());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*********************************************
	 * Download data or file from the server and store them into the cache
	 * directory or update the existing data or file.
	 *********************************************/
	public static ArrayList<TwtFmChannel> downloadChannelList() {
		System.out.println("Downloading channel list from server...");
		try {
			File file = new File(CHANNEL_LIST_CACHE_DIR + "list.xml");
			FileWriter writer = new FileWriter(file);
			ArrayList<TwtFmChannel> list = TwtFmUtil.getChannelList(writer);
			writer.close();
			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<TwtFmMusic> downloadMusicList(String channelID) {
		System.out.println("Downloading music list for " + channelID
				+ " from server...");
		try {
			File file = new File(MUSIC_LIST_CACHE_DIR + channelID + ".xml");
			FileWriter writer = new FileWriter(file);
			ArrayList<TwtFmMusic> list = TwtFmUtil.getMusicList(channelID,
					writer);
			writer.close();
			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static URL downloadCoverImage(String musicID, String urlStr) {
		System.out.println("Downloading cover image for " + musicID
				+ " from server...");
		try {
			URL url = new URL(urlStr);
			InputStream input = url.openStream();
			byte[] b = new byte[1024];
			int l = 0;
			File outputFile = new File(COVERIMG_CACHE_DIR + musicID);
			FileOutputStream output = new FileOutputStream(outputFile);
			while ((l = input.read(b, 0, 1024)) > 0) {
				if(keepWorking)
					output.write(b, 0, l);
				else{
					break;
				}
			}
			input.close();
			output.close();
			if(!keepWorking){
				outputFile.delete();
				return null;
			}
			String format = ImageUtil.getFormatName(outputFile);
			if (format != null) {
				File newFile = new File(outputFile.getCanonicalPath() + "." + format);
				if (outputFile.renameTo(newFile))
					outputFile = newFile;
			}
			return new URL("file:///" + outputFile.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static URL downloadCoverImageThumb(String musicID, String urlStr) {
		System.out.println("Downloading cover image thumb for " + musicID
				+ " from server...");
		try {
			URL url = new URL(urlStr);
			InputStream input = url.openStream();
			byte[] b = new byte[1024];
			int l = 0;
			File outputFile = new File(COVERIMG_THUMB_CACHE_DIR + musicID);
			FileOutputStream output = new FileOutputStream(outputFile);
			while ((l = input.read(b, 0, 1024)) > 0) {
				if(keepWorking)
					output.write(b, 0, l);
				else{
					break;
				}
			}
			input.close();
			output.close();
			if(!keepWorking){
				outputFile.delete();
				return null;
			}
			String format = ImageUtil.getFormatName(outputFile);
			if (format != null) {
				File newFile = new File(outputFile.getCanonicalFile() + "." + format);
				if (outputFile.renameTo(newFile))
					outputFile = newFile;
			}
			return new URL("file:///" + outputFile.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static URL downloadMusic(String musicID, String urlStr) {
		System.out.println("Downloading music for " + musicID
				+ " from server...");
		try {
			int index = urlStr.lastIndexOf('.');
			String suffix = index >= 0 ? urlStr.substring(index) : "";
			URL url = new URL(urlStr);
			InputStream input = url.openStream();
			byte[] b = new byte[1024];
			int l = 0;
			File outputFile = new File(MUSIC_CACHE_DIR + musicID + suffix);
			FileOutputStream output = new FileOutputStream(outputFile);
			while ((l = input.read(b, 0, 1024)) > 0) {
				if(keepWorking)
					output.write(b, 0, l);
				else{
					break;
				}
			}
			input.close();
			output.close();
			if(!keepWorking){
				outputFile.delete();
				return null;
			}
			return new URL("file:///" + outputFile.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*********************************************
	 * Try to retrieve data or URL from cache directory and then try to download
	 * data if not found in cache.
	 *********************************************/
	public static ArrayList<TwtFmChannel> loadChannelList() {
		ArrayList<TwtFmChannel> list = getCachedChannelList();
		if (list != null)
			return list;
		return downloadChannelList();
	}

	public static ArrayList<TwtFmMusic> loadMusicList(String channelID) {
		ArrayList<TwtFmMusic> list = getCachedMusicList(channelID);
		if (list != null)
			return list;
		return downloadMusicList(channelID);
	}

	public static URL loadCoverImage(String musicID, String urlStr) {
		URL url = getCachedCoverImage(musicID);
		if (url != null)
			return url;
		return downloadCoverImage(musicID, urlStr);
	}

	public static URL loadCoverImageThumb(String musicID, String urlStr) {
		URL url = getCachedCoverImageThumb(musicID);
		if (url != null)
			return url;
		return downloadCoverImageThumb(musicID, urlStr);
	}

	public static URL loadMusic(String musicID, String urlStr) {
		URL url = getCachedMusic(musicID);
		if (url != null)
			return url;
		return downloadMusic(musicID, urlStr);
	}
}
