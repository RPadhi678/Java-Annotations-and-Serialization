package javacson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * We give you a bunch utility code almost entirely writte.
 * However, we left in one small bug just to give you some
 * incentive to glance at it and understand what is going on.
 */
public class TestUtils {
    @Test
    public void shouldLeaveAsciiUnchangedNoBackslash() {
        var s = " !\"#$%&\'()*+,-./0123456789:;<=>?@"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "[]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        assertEquals(s, Util.escapeString(s));
    }

    @Test
    public void shouldEscapeBackslash() {
        assertEquals("\\\\", Util.escapeString("\\"));
    }

    @Test
    public void shouldEscapeBackslashWithOther() {
        assertEquals("foo\\\\bar", Util.escapeString("foo\\bar"));
    }

    @Test
    public void shouldEsapeNewline() {
        assertEquals("foo\\nbar", Util.escapeString("foo\nbar"));
    }

    @Test
    public void shouldEscapeNonascii() {
        var s = "±½";
        assertEquals("\\u00b1\\u00bd", Util.escapeString(s));
    }

    @Test
    public void shouldEscapeBasicEmoji() {
        var s = "so 🧂 salty";
        assertEquals("so \\u1f9c2 salty", Util.escapeString(s));
    }

    @Test
    public void shouldEscapeMulticodepoint() {
        var s = "a🌶️";
        assertEquals("a\\u1f336\\ufe0f", Util.escapeString(s));
    }
}
