package mri_searcher.mri_searcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import mri_searcher_util.CustomFields;
import mri_searcher_parsers.Cranfield_Parser;

public class Indexer {

	static final String DATE_FORMAT = "d-MMM-yyyy HH:mm:ss.SS";
	
	// Entero concurrente para la cantidad de bytes indexados por este indexer
	/**
	 * Este entero atómico representa la cantidad de bytes procesados por el indice.
	 */
	public AtomicLong indexedBytes = new AtomicLong(0);
	protected final Path indexPath;
	protected final Path docsPath;
	protected final OpenMode openMode;
	final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT,Locale.ENGLISH); // SimpleDateFormat is not thread-safe

	public Indexer(Path indexPath, Path docs, OpenMode openMode) {
		this.indexPath = indexPath;
		this.docsPath = docs;
		this.openMode = openMode;
	}

	public void index() throws IOException {
		Directory dir = FSDirectory.open(indexPath);
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(openMode);
		try (IndexWriter writer = new IndexWriter(dir, iwc)) {
			indexDocs(writer, docsPath);
		}
	}

	protected void indexDocs(final IndexWriter writer, Path path) throws IOException {
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						if (file.getFileName().toString().endsWith("1400")) {
							indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
						}
					} catch (IOException ignore) {
						// don't index files that can't be read.
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
		}
	}
	
	protected void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
		String hostname = execReadToString("hostname");

		try (Scanner scan = new Scanner(file)) {

			// Leemos el archivo entero a un StringBuffer como exige la
			// interfaz del parser
			scan.useDelimiter("\\A");
			StringBuffer content = new StringBuffer(scan.next());
			List<List<String>> fields = Cranfield_Parser.parseString(content);

			for (int artn = 0; artn < fields.size(); artn++) {
				List<String> art = fields.get(artn);

				// make a new, empty document
				Document doc = new Document();

				// Host del que proceden los archivos
				Field hostField = new StringField("host", hostname, Store.YES);
				doc.add(hostField);
				// Ruta de los archivos
				Field pathField = new StringField("path", file.toString(), Store.YES);
				doc.add(pathField);
				// Número de artículo dentro del archivo
				Field orderField = new StringField("order", new Integer(artn).toString(), Store.YES);
				doc.add(orderField);
				// Campos propios del artículo
				int i = 0;
				Field I = new Field("I", art.get(i++), CustomFields.TYPE_STORED);
				doc.add(I);
				Field T = new Field("T", art.get(i++), CustomFields.TYPE_STORED);
				doc.add(T);
				Field A = new TextField("A", art.get(i++), Store.NO);
				doc.add(A);
				Field B = new TextField("B", art.get(i++), Store.NO);
				doc.add(B);
				Field W = new TextField("W", art.get(i++), Store.NO);
				doc.add(W);
				Instant now = Instant.now();
				Date dateNow = Date.from(now);
				Field indexedDate = new LongPoint("msIndexed", dateNow.getTime());
				Field stringDate = new TextField("dateIndexed", DateTools.dateToString(dateNow, Resolution.MILLISECOND), Store.YES);
				doc.add(indexedDate);
				doc.add(stringDate);
				//System.out.println(titleField.stringValue() + " " + topicsField.stringValue() + " " + datelineField.stringValue() + " " + dateField.stringValue());
				writer.addDocument(doc);
			}
			
			indexedBytes.addAndGet(file.toFile().length());
		}
	}

	private static String execReadToString(String execCommand) throws IOException {
		Process proc = Runtime.getRuntime().exec(execCommand);
		try (InputStream stream = proc.getInputStream()) {
			try (Scanner s = new Scanner(stream)) {
				s.useDelimiter("\\A");
				return s.hasNext() ? s.next() : "";
			}
		}
	}

	@Override
	public String toString() {
		return "Indexer [indexPath=" + indexPath + ", docsPath=" + docsPath + "]";
	}
}
