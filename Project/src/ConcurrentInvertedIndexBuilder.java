import java.io.IOException;
import java.nio.file.Path;

/** 
 * A concurrent version of the InvertedIndexBuilder class
 * @author Jackson Raffety
 */
public class ConcurrentInvertedIndexBuilder extends InvertedIndexBuilder {

	/**
	 * The ThreadSafeInvertedIndex.
	 */
	private final ThreadSafeInvertedIndex index;
	
	/** 
	 * The number of threads for the WorkQueue object.
	 */
	private final int threads;
	
	/**
	 * Constructs the concurrent inverted index builder.
	 * @param index   The thread safe inverted index to build.
	 * @param threads The number of threads to create in the WorkQueue.
	 */
	public ConcurrentInvertedIndexBuilder(ThreadSafeInvertedIndex index, int threads) {
		super(index);
		this.index = index;
		this.threads = threads;
	}
	
	/**
	 * Builds and stems the given InvertedIndex of index using a WorkQueue.
	 * @param startPath The starting path of our pathwalk.
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	@Override
	public void buildInvertedIndex(Path startPath)
		throws IOException, InterruptedException
	{
		WorkQueue queue = new WorkQueue(this.threads);
		for (Path path : getTextFiles(startPath)) {
			queue.execute(new Task(path));
		}
		queue.finish();
		queue.shutdown();
	}
	
	/**
	 * The task class.
	 * @author Jackson Raffety
	 *
	 */
	private class Task implements Runnable {
		
		/**
		 * The path from which to build.
		 */
		private Path path;

		/**
		 * The task subclass which assigns a job to a thread.
		 * @param path The path at which to begin building the InvertedIndex.
		 */
		public Task(Path path) {
			this.path = path;
		}

		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				parseFile(path, local);
				index.addAll(local);
			}
			catch (IOException e) {
				System.err.println("Thread failure: ConcurrentInvertedIndexBuilder");
			}
		}
	}
}
