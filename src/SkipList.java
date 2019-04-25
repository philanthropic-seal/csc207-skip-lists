import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiConsumer;
// import SkipList.SkipNode;

/**
 * An implementation of skip lists.
 */
public class SkipList<K, V> implements SimpleMap<K, V> {

  // +-----------+---------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The initial height of the skip list.
   */
  static final int INITIAL_HEIGHT = 16;

  // +---------------+-----------------------------------------------
  // | Static Fields |
  // +---------------+

  static Random rand = new Random();

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * Pointers to all the front elements.
   */
  ArrayList<SLNode<K, V>> front;

  /**
   * The comparator used to determine the ordering in the list.
   */
  Comparator<K> comparator;

  /**
   * The number of values in the list.
   */
  int size;

  /**
   * The current height of the skiplist.
   */
  int height;

  /**
   * The probability used to determine the height of nodes.
   */
  double prob = 0.5;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new skip list that orders values using the specified comparator.
   */
  public SkipList(Comparator<K> comparator) {
    this.front = new ArrayList<SLNode<K, V>>(INITIAL_HEIGHT);
    for (int i = 0; i < INITIAL_HEIGHT; i++) {
      front.add(null);
    } // for
    this.comparator = comparator;
    this.size = 0;
    this.height = INITIAL_HEIGHT;
  } // SkipList(Comparator<K>)

  /**
   * Create a new skip list that orders values using a not-very-clever default comparator.
   */
  public SkipList() {
    this((k1, k2) -> k1.toString().compareTo(k2.toString()));
  } // SkipList()


  // +-------------------+-------------------------------------------
  // | SimpleMap methods |
  // +-------------------+


  public void frontUpdate(int newLevel) {
    // Store old front array temporarily
    ArrayList<SLNode<K, V>> oldFront = this.front;
    // Create new front array
    this.front = new ArrayList<SLNode<K, V>>(newLevel);

    // Copy old values from smaller original front array
    for (int i = 0; i < this.height; i++) {
      this.front.set(i, oldFront.get(i));
    } // for

    // Add placeholders for new levels of front array
    for (int i = this.height; i <= newLevel; i++) {
      this.front.set(i, null);
    } // for

    // Update height
    this.height = newLevel;
  }

  @Override
  public V set(K key, V value) {
    // Store value for return
    V result = value;
    // Generate new level
    int newLevel = randomHeight();

    // Alter front if front is shorter than new height
    if (newLevel > this.height) {
      frontUpdate(newLevel);
    }

    // Move through list and find the location for the new element to be inserted
    ArrayList<SLNode<K, V>> update = this.front;
    ArrayList<SLNode<K, V>> x = this.front;
    for (int i = this.height; i >= 0; i--) {
      while (x.get(i) != null && this.comparator.compare(key, x.get(i).key) > 0) {
        x = x.get(i).next;
      } // while
      update.set(i, x.get(i));
    } // for

    // Check for node update
    if (this.comparator.compare(key, x.get(0).key) == 0) {
      // Set value
      x.get(0).value = value;
      return value;
    } else {
      // If there is no node to update, make a new one and insert it
      SLNode<K, V> newNode = new SLNode<K, V>(key, value, newLevel);

      // Move through each pointer level
      for (int i = 0; i <= newLevel; i++) {
        // Update each pointer in the new node
        newNode.next.set(i, update.get(i).next.get(i));
        // Update each pointer which should point to the new node
        update.get(i).next.set(i, newNode);
      }
      // Increment size to reflect added entry
      this.size++;
      return result;
    }
  } // set(K,V)

  @Override
  public V get(K key) {
    if (key == null) {
      throw new NullPointerException("null key");
    }
    SLNode<K, V> current = new SLNode<K, V>(null, null, this.height);
    current.next = this.front;
    for (int i = this.height - 1; i >= 0; i--) {
      while (this.comparator.compare(key, current.next.get(i).key) > 0) {
        current = current.next.get(i);
      }
    }
    if (this.comparator.compare(key, current.key) == 0) {
      return current.value;
    } else {
      return null;
    }
  } // get(K,V)

  @Override
  public int size() {
    return this.size;
  } // size()

  @Override
  public boolean containsKey(K key) {
    return get(key) != null;
  } // containsKey(K)

  @Override
  public V remove(K key) {
    if (key == null) {
      throw new NullPointerException("null key");
    }
    ArrayList<SLNode<K, V>> update = this.front;
    SLNode<K, V> current = new SLNode<K, V>(null, null, this.height);
    current.next = this.front;
    for (int i = 0; i < this.height; i++) {
      update.set(i, current);
    }

    while (this.comparator.compare(key, current.next.get(0).key) > 0) {
      current = current.next.get(0);
      for (int i = 0; i < current.next.size(); i++) {
        update.set(i, current);
      } // for
    } // while

    if (this.comparator.compare(key, current.key) == 0) {
      V result = current.value;
      for (int i = 0; i < this.height; i++) {
        SLNode<K, V> updateNode = update.get(i);
        if (updateNode.key == null) {
          this.front.set(i, updateNode.next.get(i).next.get(i));
        } else {
          updateNode.next.set(i, updateNode.next.get(i));
        } // if (updateNode.key == null)
      } // for
      return result;
    } else {
      return null;
    }
  } // remove(K)

  @Override
  public Iterator<K> keys() {
    return new Iterator<K>() {
      Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

      @Override
      public boolean hasNext() {
        return nit.hasNext();
      } // hasNext()

      @Override
      public K next() {
        return nit.next().key;
      } // next()

      @Override
      public void remove() {
        nit.remove();
      } // remove()
    };
  } // keys()

  @Override
  public Iterator<V> values() {
    return new Iterator<V>() {
      Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

      @Override
      public boolean hasNext() {
        return nit.hasNext();
      } // hasNext()

      @Override
      public V next() {
        return nit.next().value;
      } // next()

      @Override
      public void remove() {
        nit.remove();
      } // remove()
    };
  } // values()

  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    // TODO Auto-generated method stub

  } // forEach

  // +----------------------+----------------------------------------
  // | Other public methods |
  // +----------------------+

  /**
   * Dump the tree to some output location.
   */
  public void dump(PrintWriter pen) {
    // Forthcoming
  } // dump(PrintWriter)

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Pick a random height for a new node.
   */
  int randomHeight() {
    int result = 1;
    while (rand.nextDouble() < prob) {
      result = result + 1;
    }
    return result;
  } // randomHeight()

  /**
   * Get an iterator for all of the nodes. (Useful for implementing the other iterators.)
   */
  Iterator<SLNode<K, V>> nodes() {
    return new Iterator<SLNode<K, V>>() {

      /**
       * A reference to the next node to return.
       */
      SLNode<K, V> next = SkipList.this.front.get(0);

      @Override
      public boolean hasNext() {
        return this.next != null;
      } // hasNext()

      @Override
      public SLNode<K, V> next() {
        if (this.next == null) {
          throw new IllegalStateException();
        }
        SLNode<K, V> temp = this.next;
        this.next = this.next.next.get(0);
        return temp;
      } // next();
    }; // new Iterator
  } // nodes()

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

} // class SkipList


/**
 * Nodes in the skip list.
 */
class SLNode<K, V> {

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The key.
   */
  K key;

  /**
   * The value.
   */
  V value;

  /**
   * Pointers to the next nodes.
   */
  ArrayList<SLNode<K, V>> next;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new node of height n with the specified key and value.
   */
  public SLNode(K key, V value, int n) {
    this.key = key;
    this.value = value;
    this.next = new ArrayList<SLNode<K, V>>(n);
    for (int i = 0; i < n; i++) {
      this.next.add(null);
    } // for
  } // SLNode(K, V, int)

  // +---------+-----------------------------------------------------
  // | Methods |
  // +---------+

} // SLNode<K,V>
