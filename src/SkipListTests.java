import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

/**
 * Some tests of skip lists.
 *
 * @author Samuel A. Rebelsky
 */
public class SkipListTests {

  // +-----------+---------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * Names of some numbers.
   */
  static final String numbers[] = {"zero", "one", "two", "three", "four", "five", "six", "seven",
      "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
      "seventeen", "eighteen", "nineteen"};

  /**
   * Names of more numbers.
   */
  static final String tens[] =
      {"", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};

  // +--------+----------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * A list of strings for tests. (Gets set by the subclasses.)
   */
  SkipList<String, String> strings;

  /**
   * A sorted list of integers for tests. (Gets set by the subclasses.)
   */
  SkipList<Integer, String> ints;

  /**
   * A random number generator for the randomized tests.
   */
  Random random = new Random();

  /**
   * For reporting errors: a list of the operations we performed.
   */
  ArrayList<String> operations;



  // +---------+---------------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Set up everything. Unfortunately, @BeforeEach doesn't seem to be working, so we do this
   * manually.
   */
  @BeforeEach
  public void setup() {
    this.ints = new SkipList<Integer, String>((i, j) -> i - j);
    this.strings = new SkipList<String, String>((s, t) -> s.compareTo(t));
    this.operations = new ArrayList<String>();
    System.err.println("SETUP");
  } // setup

  /**
   * Dump a SkipList to stderr.
   */
  static <K, V> void dump(SkipList<K, V> map) {
    PrintWriter pen = new PrintWriter(System.err, true);
    System.err.print("[");
    map.dump(pen);
   // map.forEach((key, value) -> System.err.println(key + ":" + value + " "));
    System.err.println("]");
  } // dump

  /**
   * Determine if an iterator only returns values in non-decreasing order.
   */
  static <T extends Comparable<T>> boolean inOrder(Iterator<T> it) {
    // Simple case: The empty iterator is in order.
    if (!it.hasNext()) {
      return true;
    }
    // Otherwise, we need to compare neighboring elements, so
    // grab the first element.
    T current = it.next();
    // Step through the remaining elements
    while (it.hasNext()) {
      // Get the next element
      T next = it.next();
      // Verify that the current node <= next
      if (current.compareTo(next) > 0) {
        return false;
      } // if (current > next)
      // Update the current node
      current = next;
    } // while
    // If we've made it this far, everything is in order
    return true;
  } // inOrder(Iterator<T> it)

  /**
   * Generate a value from a string.
   */
  static String value(String str) {
    return str.toUpperCase();
  } // key(String)

  /**
   * Generate a value from a non-negative integer.
   */
  static String value(Integer i) {
    return value(i, false);
  } // value(integer)

  /**
   * Generate a value from a non-negative integer; if skipZero is true, returns "" for zero.
   */
  static String value(Integer i, boolean skipZero) {
    if ((i == 0) && (skipZero)) {
      return "";
    } else if (i < 20) {
      return numbers[i];
    } else if (i < 100) {
      return (tens[i / 10] + " " + value(i % 10, true)).trim();
    } else if (i < 1000) {
      return (numbers[i / 100] + " hundred " + value(i % 100, true)).trim();
    } else if (i < 1000000) {
      return (numbers[i / 1000] + " thousand " + value(i % 1000, true)).trim();
    } else {
      return "really big";
    }
  } // value(i, skipZero)

  // +--------------------+------------------------------------------
  // | Logging operations |
  // +--------------------+

  /**
   * Set an entry in the ints list.
   */
  void set(Integer i) {
    operations.add("set(" + i + ");");
    ints.set(i, value(i));
  } // set(Integer)

  /**
   * Set an entry in the strings list.
   */
  void set(String str) {
    operations.add("set(\"" + str + "\");");
    strings.set(str, value(str));
  } // set(String)

  /**
   * Remove an integer from the ints list.
   */
  void remove(Integer i) {
    operations.add("remove(" + i + ");");
    ints.remove(i);
  } // remove(Integer)

  /**
   * Remove a string from the strings list.
   */
  void remove(String str) {
    operations.add("remove(\"" + str + "\");");
    strings.remove(str);
  } // remove(String)

  /**
   * Log a failure.
   */
  void log(String str) {
    System.err.println(str);
    operations.add("// " + str);
  } // log

  /**
   * Print code from a failing test.
   */
  void printTest() {
    System.err.println("@Test");
    System.err.println("  public void test" + random.nextInt(1000) + "() {");
    for (String op : operations) {
      System.err.println("    " + op);
    } // for
    System.err.println("  }");
    System.err.println();
  } // printTest()

  // +-------------+-----------------------------------------------------
  // | Basic Tests |
  // +-------------+

  /**
   * A really simple test. Add an element and make sure that it's there.
   */
  @Test
  public void simpleTest() {
    setup();
    set("hello");
    assertTrue(strings.containsKey("hello"));
    assertFalse(strings.containsKey("goodbye"));
  } // simpleTest()

  /**
   * Another simple test. The list should not contain anything when we start out.
   */
  @Test
  public void emptyTest() {
    setup();
    assertFalse(strings.containsKey("hello"));
  } // emptyTest()

  /**
   * The list should return null if the list does not contain what the user wants to remove.
   */
  @Test
  public void removeEmptyTest() {
    setup();
    assertEquals(null, ints.remove(0));
  } // removeEmptyTest()

  /**
   * Verify that a list with a sorted input stays sorted
   */
  @Test
  public void sortedTest() {
    setup();
    // Add a bunch of values
    for (int i = 0; i < 100; i++) {
      set(i);
    } // for
    if (!inOrder(ints.keys())) {
      System.err.println("inOrder() failed in sortedTest() for ints");
      printTest();
      dump(ints);
      System.err.println();
      fail("The instructions did not produce a sorted list.");
    }
  } // sortedTest()

  /**
   * Verify that a list with a reverse sorted input becomes sorted
   */
  @Test
  public void reverseSortedTest() {
    setup();
    // Add a bunch of values
    for (int i = 0; i > 100; i++) {
      set(Math.abs(i - 100));
    } // for
    if (!inOrder(ints.keys())) {
      System.err.println("inOrder() failed in reverseSortedTest() for ints");
      printTest();
      dump(ints);
      System.err.println();
      fail("The instructions did not produce a sorted list.");
    }
  } // reverseSortedTest()

  /**
   * Verify that remove() removes expected elements in list
   */
  @Test
  public void removeEvensTest() {
    setup();
    // Add values
    for (int i = 0; i < 20; i++) {
      set(i);
    } // for
    

    // Remove even keys (also tests removal from the front)
    for (int i = 0; i < 20; i += 2) {
      ints.remove(i);
    } // for

    for (int i = 0; i < 20; i++) {
      // if an odd key does not exist, fail
      if ((i % 2 != 0) && (!ints.containsKey(i))) {
        log("get(" + i + ") failed");
        printTest();
        dump(ints);
        fail(i + " is not in the skip list");
        // if an even key exists, fail
      } else if ((i % 2 == 0) && (ints.containsKey(i))) {
        log("get(" + i + ") failed");
        printTest();
        dump(ints);
        fail(i + " is in the skip list");
      } // if/else
    } // for
  } // removeEvensTest()

  /**
   * Verify that the last element was removed smoothly
   */
  @Test
  public void removeEndTest() {
    int i = 0;
    setup();
    // Add a bunch of values
    for (i = 0; i < 10; i++) {
      set(i);
    } // for
    ints.remove(i - 1);
    if (ints.containsKey(i - 1)) {
      log("contains(" + (i - 1) + ") failed");
      printTest();
      dump(ints);
      fail((i - 1) + " is in the skip list");
    }
  } // removeEndTest()

  /**
   * Check that when the given key does not exist, get returns null
   */
  @Test
  public void keyNotFoundTest() {
    int i = 0;
    setup();
    // Add a bunch of values
    for (i = 0; i < 10; i++) {
      set(i);
    } // for
    if (ints.get(i += 15) != null) {
      log("get(" + i + ") failed");
      printTest();
      dump(ints);
      fail(i + " does not return null");
    }
  } // keyNotFoundTest()

  /**
   * Verify that a list with a sorted string input stays sorted
   */
  @Test
  public void sortedStringTest() {
    setup();
    // Add a bunch of values
    for (int i = 0; i < numbers.length; i++) {
      set(numbers[i]);
    } // for
    if (!inOrder(strings.keys())) {
      System.err.println("inOrder() failed in sortedStringTest() for strings");
      printTest();
      dump(ints);
      System.err.println();
      fail("The instructions did not produce a sorted list.");
    }
  } // sortedStringTest()

  // +-----------------+-------------------------------------------------
  // | RandomizedTests |
  // +-----------------+

  /**
   * Verify that a randomly created list is sorted.
   */
  @Test
  public void testOrdered() {
    setup();
    // Add a bunch of values
    for (int i = 0; i < 100; i++) {
      int rand = random.nextInt(1000);
      set(rand);
    } // for
    if (!inOrder(ints.keys())) {
      System.err.println("inOrder() failed in testOrdered()");
      printTest();
      dump(ints);
      System.err.println();
      fail("The instructions did not produce a sorted list.");
    } // if the elements are not in order.
  } // testOrdered()

  /**
   * Verify that a randomly created list contains all the values we added to the list.
   */
  @Test
  public void testContainsOnlyAdd() {
    setup();
    ArrayList<Integer> keys = new ArrayList<Integer>();

    // Add a bunch of values
    for (int i = 0; i < 100; i++) {
      int rand = random.nextInt(200);
      keys.add(rand);
      set(rand);
    } // for i
    // Make sure that they are all there.
    for (Integer key : keys) {
      if (!ints.containsKey(key)) {
        log("contains(" + key + ") failed");
        printTest();
        dump(ints);
        fail(key + " is not in the skip list");
      } // if (!ints.contains(val))
    } // for key
  } // testContainsOnlyAdd()

  /**
   * An extensive randomized test.
   */
  @Test
  public void randomTest() {
    setup();
    // Keep track of the values that are currently in the sorted list.
    ArrayList<Integer> keys = new ArrayList<Integer>();

    // Add a bunch of values
    boolean ok = true;
    for (int i = 0; ok && i < 1000; i++) {
      int rand = random.nextInt(1000);
      // Half the time we add
      if (random.nextBoolean()) {
        if (!ints.containsKey(rand)) {
          set(rand);
        } // if it's not already there.
        if (!ints.containsKey(rand)) {
          log("After adding " + rand + ", contains(" + rand + ") fails");
          ok = false;
        } // if (!ints.contains(rand))
      } // if we add
      // Half the time we remove
      else {
        remove(rand);
        keys.remove((Integer) rand);
        if (ints.containsKey(rand)) {
          log("After removing " + rand + ", contains(" + rand + ") succeeds");
          ok = false;
        } // if ints.contains(rand)
      } // if we remove
      // See if all of the appropriate elements are still there
      for (Integer key : keys) {
        if (!ints.containsKey(key)) {
          log("ints no longer contains " + key);
          ok = false;
          break;
        } // if the value is no longer contained
      } // for each key
    } // for i
    // Dump the instructions if we've encountered an error
    if (!ok) {
      printTest();
      dump(ints);
      fail("Operations failed");
    } // if (!ok)
  } // randomTest()

  public static void main(String[] args) {
    SkipListTests slt = new SkipListTests();
    slt.setup();
    slt.simpleTest();
  } // main
} // class SkipListTests
