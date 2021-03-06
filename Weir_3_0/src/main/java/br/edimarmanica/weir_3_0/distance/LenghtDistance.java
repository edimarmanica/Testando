/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.distance;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author edimar
 */
public class LenghtDistance extends NumberDistance {


    /**
     * standard unit = meters
     *
     * @param numericValue
     * @return
     */
    @Override
    public Double normalize(String numericValue) throws NoiseException {

        numericValue = numericValue.toLowerCase();

        double multipication = 1;

        if (numericValue.contains("km")) {
            multipication = 1000;
        } else if (numericValue.contains("cm")) {
            multipication = 1.00 / 100;
        } else if (numericValue.matches("([^(a-zA-Z)])+m([^(a-zA-Z)])*")) { //evitar que mm entre aqui
            multipication = 1;
        } else {
            throw new NoiseException(numericValue, DataType.LENGHT);
        }

        NumberFormat form01 = NumberFormat.getNumberInstance();
        try {
            return form01.parse(numericValue.replaceAll("(a-zA-Z)+", "")).doubleValue() * multipication;
        } catch (ParseException ex) {
            Logger.getLogger(LenghtDistance.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
