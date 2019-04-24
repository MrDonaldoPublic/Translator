package scanner;

import java.io.*;
import java.util.List;

public class FastScanner implements AutoCloseable {
    // for blockRead
    private final int blockLength = 256;
    private byte[] block = new byte[blockLength];
    private InputStream input;

    // for Scan and Parse
    private StringBuilder currStr = new StringBuilder();
    private String lineSeparate = System.lineSeparator();
    private boolean hasNextLine = false;
    private int letterQty = 0, currPos = 0;

    // for fast return value
    private String nextStr;

    // for matching part of word
    private List<Character> patterns = List.of('\'', ' ', '\t', '.', ',', '_', '/', '+', '~');

    public FastScanner() {
        this.input = System.in;
    }

    public FastScanner(final File f) throws FileNotFoundException {
        this.input = new FileInputStream(f);
    }

    private char getNextChar() throws IOException {
        int CharacterByte = 0;
        --currPos;
        byte firstByte = getNextByte();
        while (((firstByte >> 7) & 1) == 1) {
            firstByte <<= 1;
            ++CharacterByte;
        }
        firstByte >>= CharacterByte;
        int resultBits = firstByte;
        while (CharacterByte > 1) {
            // Looks like 10xxxxxx
            resultBits = (resultBits << 6) + getNextByte() + (1 << 7);
            --CharacterByte;
        }

        return (char) resultBits;
    }

    private byte getNextByte() throws IOException {
        if (currPos >= letterQty - 1 || letterQty <= 0) {
            getNextBlock();
        } else {
            ++currPos;
        }
        return block[currPos];
    }

    private void getNextBlock() throws IOException {
        letterQty = input.read(block, 0, blockLength);
        currPos = 0;
    }

    //String next() throws IOException {
    private String getNextStr() throws IOException {
        if (hasNextLine) {
            hasNextLine = false;
            return lineSeparate;
        }
        while (letterQty != -1) {
            char currChar = getNextChar();

            if (letterQty == -1) {
                return null;
            }
            if (lineSeparate.contains(Character.toString(currChar))) {
                for (int i = 0; i < lineSeparate.length(); ++i) {
                    ++currPos;
                }
                if (currStr.length() > 0) {
                    hasNextLine = true;
                    String res = currStr.toString();
                    currStr = new StringBuilder();
                    return res;
                } else {
                    return lineSeparate;
                }
            }

            if (Character.DASH_PUNCTUATION == Character.getType(currChar)
                    || Character.isLetterOrDigit(currChar) || patterns.contains(currChar)) {
                currStr.append(currChar);
            } else if (currStr.length() > 0) {
                ++currPos;
                String res = currStr.toString();
                currStr = new StringBuilder();
                return res;
            }
            ++currPos;
        }
        return null;
    }

    public boolean hasNext() throws IOException {
        if (nextStr == null) {
            nextStr = getNextStr();
        }
        return nextStr != null;
    }

    public boolean hasNextNoLineSeparate() throws IOException {
        if (nextStr != null) {
            return true;
        }

        while (hasNext() && lineSeparate.contains(nextStr)) {
            nextStr = getNextStr();
        }
        return hasNext();
    }

    public String next() throws IOException {
        if (nextStr == null) {
            nextStr = getNextStr();
        }
        String res = nextStr;
        nextStr = null;
        return res;
    }

    public String nextNoLineSeparate() throws IOException {
        String res = next();
        while (res != null && lineSeparate.contains(res)) {
            res = next();
        }

        return res;
    }

    public String nextLine() throws IOException {
        StringBuilder res = new StringBuilder(nextNoLineSeparate());
        while (true) {
            String currWord = next();
            if (lineSeparate.contains(currWord)) {
                break;
            }

            res.append(currWord);
        }

        return res.toString();
    }

    public void close() throws IOException {
        input.close();
    }
}