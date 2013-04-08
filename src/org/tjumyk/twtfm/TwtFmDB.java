package org.tjumyk.twtfm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * Experimental Lucene application for TWT FM
 * 
 * @author tjumyk
 * @version 1.0
 */
public class TwtFmDB {
	public TreeMap<String, TwtFmMusic> musicList = new TreeMap<String, TwtFmMusic>();
	public TreeMap<String, ArrayList<TwtFmMusic>> artistMap = new TreeMap<String, ArrayList<TwtFmMusic>>();
	public TreeMap<String, ArrayList<TwtFmMusic>> albumMap = new TreeMap<String, ArrayList<TwtFmMusic>>();
	private IndexWriter writer;
	private Analyzer analyzer;
	private final String INDEX_DIR = System.getenv("APPDATA") + File.separator
			+ "TwtFMDB" + File.separator;
	private boolean ready;

	public TwtFmDB() {
		try {
			ready = false;
			analyzer = new IKAnalyzer(true);
			Directory fsDirectory = FSDirectory.open(new File(INDEX_DIR));
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,
					analyzer);
			config.setOpenMode(OpenMode.CREATE);
			config.setMaxBufferedDocs(1000);
			writer = new IndexWriter(fsDirectory, config);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isReady() {
		return ready;
	}

	public void addMusicAll(TwtFmMusic... musicList) {
		if (ready)
			return;
		for (TwtFmMusic m : musicList) {
			addMusic(m);
		}
	}

	public void addMusicAll(Collection<TwtFmMusic> musicList) {
		if (ready)
			return;
		for (TwtFmMusic m : musicList) {
			addMusic(m);
		}
	}

	public void addMusic(TwtFmMusic music) {
		if (ready || musicList.containsKey(music.getID()))
			return;
		musicList.put(music.getID(), music);
		String artist = music.getSinger();
		String album = music.getAlbum();
		ArrayList<TwtFmMusic> list = artistMap.get(artist);
		if (list != null)
			list.add(music);
		else {
			ArrayList<TwtFmMusic> newList = new ArrayList<TwtFmMusic>();
			artistMap.put(artist, newList);
			newList.add(music);
		}

		list = albumMap.get(album);
		if (list != null)
			list.add(music);
		else {
			ArrayList<TwtFmMusic> newList = new ArrayList<TwtFmMusic>();
			albumMap.put(artist, newList);
			newList.add(music);
		}

		if (writer != null) {
			Document doc = new Document();
			doc.add(new Field("id", music.getID(), Field.Store.YES,
					Field.Index.NO, Field.TermVector.NO));
			doc.add(new Field("contents", music.getTitle() + ","
					+ music.getAlbum() + "," + music.getSinger(),
					Field.Store.YES, Field.Index.ANALYZED,
					Field.TermVector.WITH_POSITIONS_OFFSETS));
			try {
				writer.addDocument(doc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void commit() {
		try {
			writer.commit();
			writer.close();
			ready = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<TwtFmMusic> searchMusic(String searchWord) {
		return searchMusic(searchWord, 0);
	}

	public ArrayList<TwtFmMusic> searchMusic(String searchWord, int limitHits) {
		if (!ready)
			return null;
		try {
			Directory fsDirectory = FSDirectory.open(new File(INDEX_DIR));
			IndexSearcher indexSearcher = new IndexSearcher(
					IndexReader.open(fsDirectory));

			QueryParser queryParser = new QueryParser(Version.LUCENE_36,
					"contents", analyzer);
			Query query = queryParser.parse(searchWord);
			if (limitHits <= 0)
				limitHits = Integer.MAX_VALUE;
			TopDocs hits = indexSearcher.search(query, limitHits);

			int totalHits = hits.totalHits;
			int len = Math.min(limitHits, totalHits);

			ScoreDoc[] docs = hits.scoreDocs;
			ArrayList<TwtFmMusic> list = new ArrayList<TwtFmMusic>();
			for (int i = 0; i < len; i++) {
				Document doc = indexSearcher.doc(docs[i].doc);
				TwtFmMusic m = musicList.get(doc.get("id"));
				if (m != null && !list.contains(m))
					list.add(m);
			}
			indexSearcher.close();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<TwtFmMusic> getAllMusic() {
		if (!ready)
			return null;
		try {
			Directory fsDirectory = FSDirectory.open(new File(INDEX_DIR));
			IndexSearcher indexSearcher = new IndexSearcher(
					IndexReader.open(fsDirectory));
			ArrayList<TwtFmMusic> list = new ArrayList<TwtFmMusic>();
			for (int i = 0; i < indexSearcher.maxDoc(); i++) {
				Document doc = indexSearcher.doc(i);
				TwtFmMusic m = musicList.get(doc.get("id"));
				if (m != null && !list.contains(m))
					list.add(m);
			}
			indexSearcher.close();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
