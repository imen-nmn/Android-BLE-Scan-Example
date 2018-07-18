package com.example.joelwasserman.androidbletutorial;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Imen nmn on 18/07/2018.
 */

public class DecoderTest {

    @Test
    public void testDecode(){
        System.out.println("INFO: "+ StringHelpers.fromDecimalToBinary(26)+" "+StringHelpers.fromDecimalToBinary(26).length()) ;
        assertTrue(StringHelpers.fromDecimalToBinary(26).contains("11010"));
    }
}
