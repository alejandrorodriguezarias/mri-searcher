package mri_searcher_util;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

public class CustomFields {
	/* Indexed, tokenized, stored. */
	public static final FieldType TYPE_STORED = new FieldType();

	static final IndexOptions options = IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS;

	static {
		TYPE_STORED.setIndexOptions(options);
		TYPE_STORED.setTokenized(true);
		TYPE_STORED.setStored(true);
		TYPE_STORED.setStoreTermVectors(true);
		TYPE_STORED.setStoreTermVectorPositions(true);
		TYPE_STORED.freeze();
	}
}
