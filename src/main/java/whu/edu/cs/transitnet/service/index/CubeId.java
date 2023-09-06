package whu.edu.cs.transitnet.service.index;

import java.io.Serializable;
import java.util.Objects;

public class CubeId implements CharSequence, Serializable {
    private final String content;

    public CubeId(String content) {
        this.content = content;
    }

    @Override
    public int length() {
        return content.length();
    }

    @Override
    public char charAt(int index) {
        return content.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return content.subSequence(start, end);
    }

    @Override
    public String toString() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CubeId cubeId = (CubeId) o;
        return Objects.equals(content, cubeId.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }
}
