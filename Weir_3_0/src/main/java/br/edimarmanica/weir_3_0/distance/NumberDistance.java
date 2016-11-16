/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.distance;

import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.bean.Value;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author edimar
 */
public class NumberDistance extends TypeAwareDistance {

    private static final double TETA = 0.02;
    private Double p;

    @Override
    public double distanceSpecific(String vR1, String vS1) {

        if (p == null) {
            throw new UnsupportedOperationException("You should train first!");
        }

        double numericValueR1;
        try {
            numericValueR1 = normalize(vR1);
        } catch (NoiseException ex) {
            //System.out.println(ex.getMessage());
            return 1; //é um lixo que a regra pegou. Por ex: London
        }
        double numericValueS1;
        try {
            numericValueS1 = normalize(vS1);
        } catch (NoiseException ex) {
            //System.out.println(ex.getMessage());
            return 1; //é um lixo que a regra pegou. Por ex: London
        }
        
        if (Math.abs(numericValueR1 - numericValueS1) > p) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public double distance(Rule r1, Rule s1) throws InsufficientOverlapException {
        train(r1, s1);

        return super.distance(r1, s1);
    }

    /**
     *
     * @param numericValues : list, pois um set eliminaria valores iguais
     * @return
     */
    private double getAverageAbsoluteValues(List<Double> numericValues) {
        double sum = 0;

        for (Double v : numericValues) {
            sum += Math.abs(v); //absolute values
        }

        return sum / numericValues.size();//lembrando que each value de uma rule é de uma entidade diferente, pois é de uma página diferente e cada página representa uma entidade diferente
    }

    private void train(Rule r1, Rule s1) {
        List<Double> numericValuesR1 = new ArrayList<>();
        for (Value v : r1.getValues()) {
            if (v.getValue() != null && !v.getValue().trim().isEmpty()) {
                try {
                    numericValuesR1.add(normalize(v.getValue())); //lembrando que each value de uma rule é de uma entidade diferente, pois é de uma página diferente e cada página representa uma entidade diferente
                } catch (NoiseException ex) {
                    //Não faz nada -- possívelmente é algum lixo que a regra está pegando
                    //Logger.getLogger(NumberDistance.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        double avgR1 = getAverageAbsoluteValues(numericValuesR1);

        List<Double> numericValuesS1 = new ArrayList<>();
        for (Value v : s1.getValues()) {
            if (v.getValue() != null && !v.getValue().trim().isEmpty()) {
                try {
                    numericValuesS1.add(normalize(v.getValue())); //lembrando que each value de uma rule é de uma entidade diferente, pois é de uma página diferente e cada página representa uma entidade diferente
                } catch (NoiseException ex) {
                    //Não faz nada -- possívelmente é algum lixo que a regra está pegando
                   // Logger.getLogger(NumberDistance.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        double avgS1 = getAverageAbsoluteValues(numericValuesS1);
        
        p = Math.min(avgR1, avgS1) * TETA;

    }

    public Double normalize(String numericValue) throws NoiseException  {
        String aux = numericValue;
        aux = aux.replaceAll("R\\$", "").replaceAll("\\$", "").replaceAll("€", ""); //retirando o simbolo de moeda pq currencyXnumber=numberXnumber, ou seja, se uma das regras e number, compara tudo com number
        aux = aux.replaceAll("[a-zA-Z]", ""); //retirando cm, m, etc. mesmo motivo acima

        NumberFormat form01 = NumberFormat.getNumberInstance(new Locale("en", "US"));
        try {
            return form01.parse(aux.trim()).doubleValue();
        } catch (ParseException ex) {
            throw new NoiseException(numericValue, DataType.NUMBER);
        }
    }
}
