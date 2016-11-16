/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.distance;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author edimar
 */
public class CurrencyDistance extends NumberDistance {


    /**
     * standard unit = grams
     *
     * @param numericValue
     * @return
     */
    @Override
    public Double normalize(String numericValue) throws NoiseException {
        String aux = numericValue;
        Locale local;
        aux = aux.toUpperCase(); //tem que estar em maiúsculo o R do R$
        double multiplication = 1;

        if (aux.contains("R$")) {
            multiplication = 1;
            local = new Locale("pt", "BR");
        } else if (aux.contains("$")) { //evitar que mm entre aqui
            multiplication = 1;
            local = new Locale("en", "US");
            
            if (aux.trim().startsWith("$ ")){
                aux = aux.replaceAll("\\$\\s+", "\\$");
            }
            
        } else if (aux.contains("€")) {
            multiplication = 1; //não tem como converter para mesma moeda pq cambio muda todo dia
            local = new Locale("fr", "FR");
            if (aux.trim().startsWith("€")) {//O € tem que ser no final
                aux = aux.replaceAll("€", "").trim() + " €";
            }
            if (aux.matches(".*\\.\\d\\d €")) { //as vezes está no formato "1,000.35 €", ai tem que tirar a virgula e trocar ponto por virgula
                aux = aux.replaceAll(",", "").replaceAll("\\.", ",");
            }
        } else {
            throw new NoiseException(aux, DataType.CURRENCY);
        }

        NumberFormat form01 = NumberFormat.getCurrencyInstance(local);
        try {
            return form01.parse(aux).doubleValue() * multiplication;
        } catch (ParseException ex) {
            Logger.getLogger(CurrencyDistance.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
