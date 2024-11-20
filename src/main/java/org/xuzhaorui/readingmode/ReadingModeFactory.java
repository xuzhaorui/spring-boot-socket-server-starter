package org.xuzhaorui.readingmode;


/**
 * 读取模式工厂
 */
public class ReadingModeFactory {
    public static ReadWriteMode createReadingMode(String mode) {
        if (mode.equals("line")) {
            return new ReadWriteModeLine();
        } else if (mode.equals("length")) {
            return new ReadWriteModeLength();
        } else {
            throw new IllegalArgumentException("Unsupported reading mode: " + mode);
        }
    }
}
