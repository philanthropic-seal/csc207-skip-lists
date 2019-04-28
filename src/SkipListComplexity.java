import java.util.Random;

public class SkipListComplexity {

  public static void main(String[] args) throws Exception {
    SkipList<Integer, Integer> skipTest = new SkipList<Integer, Integer>((i, j) -> i - j);
    
    for (int l = 1000; l < 20000; l = l * 2) {     
      Random rand = new Random();
      
      for (int i = 0; i < l/4; i++) {
        skipTest.set(Math.abs(rand.nextInt() % l), i);
      }
      int sum = 0;
      for (int j = 0; j < 1000; j++) {
        skipTest.counter = 0;
        skipTest.set(rand.nextInt() % l, j);
        sum += skipTest.counter;
      }
      System.out.println("set (" + l + ")" + ": " + (double) sum/1000.0);
      sum = 0;
      
      
      
      for (int i = 0; i < l/4; i++) {
        skipTest.set(Math.abs(rand.nextInt() % l), i);
      }
      for (int j = 0; j < 1000; j++) {
        skipTest.counter = 0;
        skipTest.get(rand.nextInt() % l);
        sum += skipTest.counter;
      }
      System.out.println("get (" + l + ")" + ": " + (double) sum/1000.0);
      sum = 0;
      
      
      
      for (int i = 0; i < l/4; i++) {
        skipTest.set(Math.abs(rand.nextInt() % l), i);
      }
      for (int j = 0; j < 1000; j++) {
        skipTest.counter = 0;
        skipTest.remove(rand.nextInt() % l);
        sum += skipTest.counter;
      }
      System.out.println("remove (" + l + ")" + ": " + (double) sum/1000.0);
      sum = 0;
      
    }
    // for(int j = 0; j < 100; j++) {
    // skipTest.counter = 0;
    // skipTest.set(rand.nextInt(), 2);
    // sum += skipTest.counter;
    // System.out.println(skipTest.counter);
    // }


  }

}
