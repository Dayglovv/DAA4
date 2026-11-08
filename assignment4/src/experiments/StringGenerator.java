package experiments;
import matching.NaiveSearch;
import java.util.Random;
public class StringGenerator {
    public static String random(int length, String alphabet, long seed) {
        Random rnd = new Random(seed);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
    public static String randomAbsentPattern(String text, int length, String alphabet, long seed) {
        long s = seed;
        while (true) {
            String candidate = random(length, alphabet, s);
            if (NaiveSearch.search(text, candidate).indices.isEmpty()) {
                return candidate;
            }
            s++;
        }
    }
    public static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}

