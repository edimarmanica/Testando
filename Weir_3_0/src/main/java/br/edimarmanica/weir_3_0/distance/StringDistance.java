/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.distance;

import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.bean.Value;
import com.wcohen.ss.BasicStringWrapperIterator;
import com.wcohen.ss.DirichletJS;
import com.wcohen.ss.api.StringWrapper;
import com.wcohen.ss.tokens.NGramTokenizer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author edimar
 */
public class StringDistance extends TypeAwareDistance {

    private DirichletJS distance;

    @Override
    public double distance(Rule r1, Rule s1) throws InsufficientOverlapException {
        train(r1, s1);

        return super.distance(r1, s1);
    }

    /**
     * train the distance on some strings - in general, this would be a large
     * corpus of existing strings, so that some meaningful frequency estimates
     * can be accumulated. for efficiency, you train on an iterator over
     * StringWrapper objects, which are produced with the 'prepare' function.
     *
     * @param r1
     * @param s1
     * @return
     */
    private void train(Rule r1, Rule s1) {
        distance = new DirichletJS(NGramTokenizer.DEFAULT_TOKENIZER, 1.0);

        /**
         * add the values of r1 and s1 to the corpus
         */
        List<StringWrapper> list = new ArrayList<>();
        for (Value v : r1.getValues()) {
            /**
             * Na linha 32 do JensenShannonDistance tem um bug que ele chama o
             * next duas vezes. Desta forma ele sempre perde os valores da
             * posição par. Lembrando que índice começa em zero
             */
            if (v.getValue() != null) {
                list.add(distance.prepare("nulo"));
                list.add(distance.prepare(normalize(v.getValue())));
            }
        }

        for (Value v : s1.getValues()) {
            /**
             * Na linha 32 do JensenShannonDistance tem um bug que ele chama o
             * next duas vezes. Desta forma ele sempre perde os valores da
             * posição par. Lembrando que índice começa em zero
             */
            if (v.getValue() != null) {
                list.add(distance.prepare("nulo"));
                list.add(distance.prepare(normalize(v.getValue())));
            }
        }




        distance.train(new BasicStringWrapperIterator(list.iterator()));
    }

    @Override
    public double distanceSpecific(String vR1, String vS1) {
        return 1 - distance.score(vR1, vS1); //1- pois quero a distância e ele dá a similaridade
    }

    public String normalize(String st) {
        return st.toLowerCase().trim().replaceAll("\\s+", "");
    }
}
