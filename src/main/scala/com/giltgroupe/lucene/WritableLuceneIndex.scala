package com.giltgroupe.lucene

trait WritableLuceneIndex extends LuceneDocumentAdder with LuceneIndexWriter {
  _: LuceneDirectory with LuceneAnalyzerProvider with LuceneVersion =>
}
