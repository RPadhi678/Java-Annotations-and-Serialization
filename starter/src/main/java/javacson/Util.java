package javacson;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class Util {
    public static void writeTextToFile(String text, Path path) {
        try {
            Files.write(
                path, 
                text.getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Escapes a string value for use in CSON. Will replace all
     * nonprintable characters ascii characters with their hexadecimal representation,
     * all backslashes with a \\, and all new lines with \n.
     */
    public static String escapeString(String string) {
        return string.codePoints()
            .mapToObj(c -> escapeCodepoint(c))
            .collect(Collectors.joining());
    }

    /**
     * Escapes a unicode codepoint for use in a CSON string
     */
    private static String escapeCodepoint(int codepoint) {
        boolean isPrintable = codepoint >= ' ' && codepoint <= '~';
        if (isPrintable) {
            if (codepoint == '\\') {
                return "\\\\";
            }
            return String.valueOf((char) codepoint);
        } else if (codepoint == '\n') {
            return "\\n";
        } else {
            return String.format("\\u%04x", codepoint);
        }
    }
    
