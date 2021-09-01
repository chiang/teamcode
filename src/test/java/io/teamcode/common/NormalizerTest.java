package io.teamcode.common;

import org.junit.Test;

import java.text.Normalizer;

/**
 * Created by chiang on 2017. 3. 28..
 */
public class NormalizerTest {

    private void printIt(String string) {
        System.out.println(string);
        for (int i = 0; i < string.length(); i++) {
            System.out.print(String.format("U+%04X ", string.codePointAt(i)));
        }
        System.out.println();
    }

    @Test
    public void nfdAndNfc() {
        String han = "한";
        printIt(han);

        String nfd = Normalizer.normalize(han, Normalizer.Form.NFD);
        printIt(nfd);

        String nfc = Normalizer.normalize(nfd, Normalizer.Form.NFC);
        printIt(nfc);

        nfc = Normalizer.normalize(nfd, Normalizer.Form.NFC);
        printIt(nfc);
    }
}
