package test.junit.oldTests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import utility.Helper;

/**
 * Tests for {@link utility.Helper}: concatenate (renamed from concatinate),
 * setIfNone, isNullOrEmpty, listIsNullOrEmpty, restrict, copyStringA,
 * enumToString, removeWhitespace, stringAToString.
 */
public class HelperTest {

    /* ----- concatenate ----- */
    @Test
    public void concatenateEmptyWithExtra() {
        String[] in = new String[0];
        String[] out = Helper.concatenate(in, "a", "b");
        assertNotNull(out);
        assertArrayEquals(new String[] { "a", "b" }, out);
    }

    @Test
    public void concatenateWithExtra() {
        String[] in = new String[] { "x", "y" };
        String[] out = Helper.concatenate(in, "a");
        assertNotNull(out);
        assertArrayEquals(new String[] { "x", "y", "a" }, out);
    }

    @Test
    public void concatenatePreservesOrder() {
        String[] in = new String[] { "first" };
        String[] out = Helper.concatenate(in, "second", "third");
        assertArrayEquals(new String[] { "first", "second", "third" }, out);
    }

    @Test
    public void concatenateNoExtra() {
        String[] in = new String[] { "a", "b" };
        String[] out = Helper.concatenate(in);
        assertArrayEquals(in, out);
    }

    /* ----- firstToUpper ----- */
    @Test
    public void firstToUpper() {
        assertEquals("Abc", Helper.firstToUpper("abc"));
        assertEquals("A", Helper.firstToUpper("a"));
    }

    /* ----- setIfNone ----- */
    @Test
    public void setIfNoneReturnsInputWhenNonNull() {
        assertEquals("value", Helper.setIfNone("value", "default"));
        assertEquals(Integer.valueOf(5), Helper.setIfNone(Integer.valueOf(5), Integer.valueOf(10)));
    }

    @Test
    public void setIfNoneReturnsDefaultWhenNull() {
        assertEquals("default", Helper.setIfNone((String) null, "default"));
        assertEquals(Integer.valueOf(10), Helper.setIfNone((Integer) null, Integer.valueOf(10)));
    }

    @Test
    public void setIfNoneReturnsDefaultWhenEmptyString() {
        assertEquals("default", Helper.setIfNone("", "default"));
    }

    /* ----- isNullOrEmpty ----- */
    @Test
    public void isNullOrEmptyTrueForNullAndEmpty() {
        assertTrue(Helper.isNullOrEmpty(null));
        assertTrue(Helper.isNullOrEmpty(""));
    }

    @Test
    public void isNullOrEmptyFalseForNonEmpty() {
        assertFalse(Helper.isNullOrEmpty("x"));
        assertFalse(Helper.isNullOrEmpty(" "));
    }

    /* ----- listIsNullOrEmpty ----- */
    @Test
    public void listIsNullOrEmptyTrueForNullAndEmpty() {
        assertTrue(Helper.listIsNullOrEmpty(null));
        assertTrue(Helper.listIsNullOrEmpty(Collections.emptyList()));
    }

    @Test
    public void listIsNullOrEmptyFalseWhenHasElements() {
        List<String> list = new LinkedList<>();
        list.add("a");
        assertFalse(Helper.listIsNullOrEmpty(list));
    }

    /* ----- restrict ----- */
    @Test
    public void restrictClampsToRange() {
        assertEquals(5, Helper.restrict(3, 10, 0));
        assertEquals(0, Helper.restrict(-1, 10, 0));
        assertEquals(10, Helper.restrict(15, 10, 0));
        assertEquals(7, Helper.restrict(7, 10, 0));
    }

    /* ----- copyStringA ----- */
    @Test
    public void copyStringACopiesContent() {
        String[] in = new String[] { "a", "b", "c" };
        String[] out = Helper.copyStringA(in);
        assertArrayEquals(in, out);
        assertNotNull(out);
        assertTrue(out != in);
    }

    /* ----- enumToString ----- */
    @Test
    public void enumToStringReturnsSpaceSeparated() {
        String s = Helper.enumToString(dataIO.Log.Tier.class);
        assertNotNull(s);
        assertTrue(s.contains("CRITICAL"));
        assertTrue(s.contains("NORMAL"));
    }

    /* ----- removeWhitespace ----- */
    @Test
    public void removeWhitespaceStripsSpacesAndNewlines() {
        assertEquals("abc", Helper.removeWhitespace("a b c"));
        assertEquals("", Helper.removeWhitespace("   \n\t  "));
    }

    /* ----- stringAToString ----- */
    @Test
    public void stringAToStringCommaSeparated() {
        assertEquals("a,b,c", Helper.stringAToString(new String[] { "a", "b", "c" }));
    }

    @Test
    public void stringAToStringWithDelimiter() {
        assertEquals("a;b;c", Helper.stringAToString(new String[] { "a", "b", "c" }, ";"));
    }

    @Test
    public void stringAToStringNullReturnsEmpty() {
        assertEquals("", Helper.stringAToString(null));
    }
}
