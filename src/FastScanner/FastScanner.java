package FastScanner;

import java.io.*;

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
            if (Character.toString(currChar).equals(lineSeparate)) {
                ++currPos;
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
                    || Character.isLetter(currChar) || currChar == '\''
                    || currChar == ' ' || currChar == '.'
                    || Character.isDigit(currChar)) {
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
        while (res != null && res.equals(lineSeparate)) {
            res = next();
        }

        return res;
    }

    public void close() throws IOException {
        input.close();
    }
}