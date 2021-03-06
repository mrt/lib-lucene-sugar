package com.gilt.lucene

import java.io.File
import org.apache.lucene.store.{MMapDirectory, SimpleFSDirectory, RAMDirectory, Directory}
import org.apache.lucene.index.{IndexNotFoundException, DirectoryReader}
import org.apache.lucene.search.IndexSearcher
import javax.annotation.Nonnull

/**
 * Base trait for configuring the Directory of a Lucene index
 */
trait LuceneDirectory {
  /**
   * The Lucene Directory object, must be defined by one of the concrete classes
   */
  protected def directory: Directory

  /**
   * Calls the provided function with an instance of an Option[DirectoryReader] that
   * gets created from the current Lucene Directory object.
   * When the parameter is None, it means that the index has not
   * been created yet, or it's corrupted.
   */
  def withDirectoryReader[T](@Nonnull f: Option[DirectoryReader] => T): T =
    try {
      val indexReader = DirectoryReader.open(directory)
      try {
        f(Some(indexReader))
      } finally {
        indexReader.close()
      }
    } catch {
      case e: IndexNotFoundException =>
        f(None)
    }

  /**
   * Calls the provided function with an instance of an Option[IndexSearcher] that
   * gets created from the current Lucene Directory object.
   * When the parameter is None, it means that the index has not
   * been created yet, or it's corrupted.
   */
  def withIndexSearcher[T](@Nonnull f: Option[IndexSearcher] => T): T =
    withDirectoryReader { indexReaderOpt =>
      f(indexReaderOpt.map(new IndexSearcher(_)))
    }

}

/**
 * A LuceneDirectory that uses a RAM based directory to store the index
 */
trait RamLuceneDirectory extends LuceneDirectory {
  /**
   * The instance of a RAM based Directory
   */
  protected lazy val directory = new RAMDirectory()
}

/**
 * A LuceneDirectory that based on filesystem
 */
trait FSLuceneDirectory extends LuceneDirectory {
  self: LuceneIndexPathProvider with FSLuceneDirectoryCreator =>
  protected lazy val directory = withIndexPath(luceneDirectoryFromPath)
}

/**
 * A LuceneDirectory that uses a SimpleFSDirectory based on the ServiceRootLucenePathProvider
 */
trait DefaultFSLuceneDirectory
  extends FSLuceneDirectory
  with ServiceRootLucenePathProvider
  with SimpleFSLuceneDirectoryCreator
