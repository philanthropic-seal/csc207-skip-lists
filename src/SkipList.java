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
      this.front.add(oldFront.get(i));
    } // for

    // Add placeholders for new levels of front array
    for (int i = this.height; i <= newLevel; i++) {
      this.front.add(null);
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
      this.height = newLevel;
    }

    // Move through list and find the location for the new element to be inserted
    ArrayList<SLNode<K, V>> update = new ArrayList<SLNode<K, V>>(this.height);
    SLNode<K, V> current = new SLNode<K, V>(null, null, this.height);
    for (int i = 0; i < this.height; i++) {
      update.add(current);
      current.next.set(i, this.front.get(i));
    }

    for (int i = this.height - 1; i >= 0; i--) {
      while (current.next.get(i) != null && current.next.get(i).key != null
          && this.comparator.compare(key, current.next.get(i).key) > 0) {
        current = current.next.get(i);
      } // while
      update.set(i, current);
    } // for
    // Check for node update
    current = current.next.get(0);
    if (current != null && current.key != null && this.comparator.compare(key, current.key) == 0) {
      result = current.value;
      current.value = value;
      return result;
    } else {
      // If there is no node to update, make a new one and insert it
      SLNode<K, V> newNode = new SLNode<K, V>(key, value, newLevel);

      // Move through each pointer level
      for (int i = 0; i < newLevel; i++) {
        // Update each pointer in the new node
        if (update.get(i).key == null) {
          newNode.next.set(i, this.front.get(i));
          this.front.set(i, newNode);
        } else {
          newNode.next.set(i, update.get(i).next.get(i));
          // Update each pointer which should point to the new node
          update.get(i).next.set(i, newNode);
        }
      }
      // Increment size to reflect added entry
      this.size++;
      return result;
    }
  } // set(K,V)

  @Override
  public V get(K key) {
    // Check for valid key
    if (key == null) {
      throw new NullPointerException("null key");
    } // if

    // Create node to track iteration down the list
    SLNode<K, V> current = new SLNode<K, V>(null, null, this.height);
    for (int i = 0; i < this.height; i++) {
      current.next.set(i, this.front.get(i));
    }
    // Iterate down the skip list
    for (int i = this.height - 1; i >= 0; i--) {
      while (current.next.get(i) != null && current.next.get(i).key != null
          && this.comparator.compare(key, current.next.get(i).key) >= 0) {
        current = current.next.get(i);
      } // while
    } // for
    // If the key is found, return the value
    if (current.key != null && this.comparator.compare(key, current.key) == 0) {
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
    // Check for valid key
    if (key == null) {
      throw new NullPointerException("null key");
    } // if

    // Create node and ArrayList to track current position in skip list
    ArrayList<SLNode<K, V>> update = new ArrayList<SLNode<K, V>>(this.height);
    SLNode<K, V> current = new SLNode<K, V>(null, null, this.height);
    for (int i = 0; i < this.height; i++) {
      update.add(current);
      current.next.set(i, this.front.get(i));
    } // for

    // Iterate down the list
    while (current.next.get(0) != null && current.next.get(0).key != null
        && this.comparator.compare(key, current.next.get(0).key) > 0) {
      current = current.next.get(0);
      for (int i = 0; i < current.next.size(); i++) {
        update.set(i, current);
      } // for
    } // while

    // Check if the element to remove was found
    if (current != null && current.key != null && this.comparator.compare(key, current.key) == 0) {
      V result = current.value;
      // Remove the element and rearrange pointers
      for (int i = 0; i < this.height; i++) {
        SLNode<K, V> updateNode = update.get(i);
        if (updateNode.key == null) {
          this.front.set(i, current.next.get(i));
        } // if
        else {
          updateNode.next.set(i, current.next.get(i));
        } // if (updateNode.key == null)
      } // for
      return result;
    } // if
    // Return null if element to be removed is not found
    else {
      return null;
    } // else
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
    SLNode<K, V> current = this.front.get(0);
    System.out.println(this.front.get(0).key);
    // while(current != null) {
    // action(current.key, current.value);
    // current = current.next.get(0);
    // }

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
