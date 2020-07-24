import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A thread-safe version of the InvertedIndex class.
 * @author Jackson
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
	
	/** The lock used to protect concurrent access to the underlying set. */
	private final SimpleReadWriteLock lock;
	
	/**
	 * Initializes a thread-safe InvertedIndex with a custom lock.
	 */
	public ThreadSafeInvertedIndex() {
		this.lock = new SimpleReadWriteLock();

	}
	
	@Override
	public void add(String word, String path, int count) {
		lock.writeLock().lock();
		
		try     { super.add(word, path, count); }
		finally { lock.writeLock().unlock(); }
	}
	
	//@Override
	public void addAll(InvertedIndex ind) {
		lock.writeLock().lock();

		try     { super.addAll(ind); }
		finally { lock.writeLock().unlock(); }
	}

	@Override
	public boolean contains(String word) {
		lock.readLock().lock();
		
		try     { return super.contains(word); }
		finally { lock.readLock().unlock(); }
	}
	
	@Override
	public boolean contains(String word, String location) {
		lock.readLock().lock();
		
		try     { return super.contains(word, location); }
		finally { lock.readLock().unlock(); }
	}
	
	@Override
	public boolean contains(String word, String location, int position) {
		lock.readLock().lock();
		
		try     { return super.contains(word, location, position); }
		finally { lock.readLock().unlock(); }
	}

	@Override
	public Collection<Integer> getPathCountSet(String word, String path) {
		lock.readLock().lock();
		
		try     { return super.getPathCountSet(word, path);  }
		finally { lock.readLock().unlock(); }
	}
	
	@Override
	public Collection<String> getPathSet(String word) {
		lock.readLock().lock();
		
		try     { return super.getPathSet(word);}
		finally { lock.readLock().unlock(); }
	}

	@Override
	public Collection<String> getStemSet() {
		lock.readLock().lock();
		
		try     { return super.getStemSet(); }
		finally { lock.readLock().unlock(); }
	}
	
	@Override
	public Map<String, Integer> getCount() {
		lock.readLock().lock();
		
		try     { return super.getCount(); }
		finally { lock.readLock().unlock(); }
	}
		
	@Override
	public ArrayList<SearchResult> partialSearch(Set<String> toSearchFor) {
		lock.readLock().lock();
		
		try     { return super.partialSearch(toSearchFor); }
		finally { lock.readLock().unlock(); }
	}
	
	@Override
	public ArrayList<SearchResult> exactSearch(Set<String> toSearchFor) {
		lock.readLock().lock();
		
		try     { return super.exactSearch(toSearchFor); }
		finally { lock.readLock().unlock(); }
	}
	
	@Override
	public void writeIndex(Path printPath) throws IOException {
		lock.readLock().lock();
		
		try     { super.writeIndex(printPath);  }
		finally { lock.readLock().unlock(); }
	}
	
	@Override
	public String toString() {
		lock.readLock().lock();
		
		try     { return super.toString();  }
		finally { lock.readLock().unlock(); }
	}

	
}
