package javacson;

import java.nio.file.Paths;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App {
    public static class Book {
        public String bookTitle;
        public boolean isPaperback;
        public float rating;
        
        public Book(String title, boolean isPaperback, float therating) {
            this.bookTitle = title;
            this.isPaperback = isPaperback;
            this.rating = therating;
        }
    }

    public static void main(String[] args) {
        List<Object> data = List.of(
            new Book("Foo", true, 2.5f)
        );
        CsonObjectSerializer serializer = new CsonObjectSerializer();
        String csonText = serializer.serialize(data);
        System.out.println(csonText);
        Util.writeTextToFile(csonText, Paths.get("./data.cson"));
    }
}
