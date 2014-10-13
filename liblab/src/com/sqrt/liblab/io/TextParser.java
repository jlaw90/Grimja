/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This file is part of LibLab.
 *
 *     LibLab is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sqrt.liblab.io;

import com.sqrt.liblab.threed.Angle;
import com.sqrt.liblab.threed.Vector3f;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TextParser {
    private final DataSource source;
    public final List<String> lines = new LinkedList<String>();
    private int idx, charIdx;
    private String line;

    public TextParser(DataSource dataSource) throws IOException {
        this.source = dataSource;
        while(dataSource.remaining() > 0) {
            String line = dataSource.getLine();
            line = line.trim();
            if(line.isEmpty() || line.charAt(0) == '#')
                continue;
            lines.add(line);
        }
        line = lines.isEmpty()? null: lines.get(idx++);
        charIdx = 0;
    }

    private void error(String message) throws IOException {
        StringBuilder footer = new StringBuilder("\t").append(line).append("\n\t");
        for(int i = 0; i < charIdx; i++)
            footer.append(' ');
        footer.append('^');
        throw new IOException(message + " in " + source.getName() + ", line " + (idx - 1) + "\n" + footer.toString());
    }

    public void nextLine() {
        line = idx >= lines.size()? null: lines.get(idx);
        idx += 1;
        charIdx = 0;
    }

    public String readString() {
        skipWhitespace(false);
        StringBuilder sb = new StringBuilder();
        int start = charIdx;
        int end = line.length();
        for(; start < end; start++) {
            char c = peek();
            if(c == ' ' || c == '\n')
                break;
            skip(1);
            sb.append(c);
        }
        return sb.toString();
    }

    public int remainingChars() {
        return line.length() - charIdx;
    }

    public String read(int length) throws IOException {
        if(length > remainingChars())
            error("Tried to read " + length + " chars, only " + remainingChars() + " remaining");
        String s = line.substring(charIdx, charIdx+length);
        skip(length);
        return s;
    }

    public void skip(int count) {
        int skipped = 0;
        while(skipped < count) {
            int toSkip = Math.min(line.length(), count - skipped);
            skipped += toSkip;
            charIdx += toSkip;
            if(charIdx >= line.length())
                nextLine();
        }
    }

    public TextParser expectString(String str) throws IOException {
        String read = read(str.length());
        if(!read.equalsIgnoreCase(str))
            error("Expecting '" + str + "', got '" + read + "'");
        return this;
    }

    public String currentLine() {
        return line;
    }

    public char peek() {
        if(charIdx >= line.length())
            return '\n';
        return line.charAt(charIdx);
    }

    public char read() {
        char c = line.charAt(charIdx);
        skip(1);
        return c;
    }

    public String remaining() {
        String s = line.substring(charIdx, line.length());
        skip(remainingChars());
        return s;
    }

    public boolean isNumber(char c) {
        return isCharBetween(c, '0', '9');
    }

    public boolean isUppercaseAlpha(char c) {
        return isCharBetween(c, 'A', 'Z');
    }

    public boolean isLowercaseAlpha(char c) {
        return isCharBetween(c, 'a', 'z');
    }

    public boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n';
    }

    public boolean isAlpha(char c) {
        return isLowercaseAlpha(c) || isUppercaseAlpha(c);
    }

    public boolean isCharBetween(char c, char start, char end) {
        return c >= start && c <= end;
    }

    public void skipWhitespace(boolean skipNewline) {
        char c;
        while(isWhitespace(c = peek()) && (skipNewline || c != '\n')) {
            if(!skipNewline)
                charIdx++;
            else
                skip(1);
        }
    }

    public void skipWhitespace() {
        skipWhitespace(true);
    }

    public int readHex() throws IOException {
        skipWhitespace();
        boolean negative = false;
        if(peek() == '-') {
            negative = true;
            skip(1);
        }
        if(peek() == '+')
            skip(1);
        expectString("0x");
        int val = 0;
        final int curLine = idx;
        while(idx == curLine && line != null && charIdx < line.length() && (isNumber(peek()) || isCharBetween(peek(), 'A', 'F'))) {
            char c = read();
            int n = isNumber(c)? c - '0': ((c - 'A') + 10);
            val = (val * 16) + n;
        }
        return negative? -val: val;
    }

    public int readInt(boolean skipWhitespace, boolean required) throws IOException {
        if(skipWhitespace)
            skipWhitespace();
        boolean negative = false;
        final int start = charIdx;
        if(peek() == '-') {
            negative = true;
            skip(1);
        }
        if(peek() == '+')
            skip(1);
        int val = 0;
        final int curLine = idx;
        while(curLine == idx && line != null && charIdx < line.length() && isNumber(peek()))
            val = (val * 10) + (read() - '0');
        if(required && charIdx - start == 0)
            error("Number expected");
        return negative? -val : val;
    }

    public int readInt() throws IOException {
        return readInt(true, true);
    }

    public float readFloat() throws IOException {
        int startLine = idx;
        skipWhitespace();
        boolean negative = peek() == '-';
        float result = readInt();
        if(peek() == '.') {
            skip(1);
            float divisor = 10f;
            while(startLine == idx && peek() == '0') {
                divisor /= 10f;
                skip(1);
            }
            char peek = eof()? '\n': peek();
            if(startLine != idx || peek == ' ' || peek == '\t' || peek=='\r' || peek == '\n') // We've changed line or float ended in all 0's
                return result;
            int floatPart = readInt();
            while(floatPart >= divisor)
                divisor *= 10f;
            float asDecimal = ((float) floatPart) / divisor;
            if(result == 0 && negative)
                result = -(result + asDecimal);
            else
                result += asDecimal;
        }
        return result;
    }

    public boolean checkString(String needle) {
        return line != null && line.toLowerCase().contains(needle.toLowerCase());
    }

    public Vector3f readVector3() throws IOException {
        float x = readFloat();
        float y = readFloat();
        float z = readFloat();
        return new Vector3f(x, y, z);
    }

    public Angle readAngle() throws IOException {
        return new Angle(readFloat());
    }

    public boolean eof() {
        return idx >= lines.size();
    }

    public String readString(int len) throws IOException {
        if(charIdx + len >= line.length())
            error("Not enough characters remaining (expected " + len + ", got " + (line.length() - charIdx) + ")");
        String r = line.substring(charIdx, charIdx + len);
        skip(len);
        return r;
    }
}