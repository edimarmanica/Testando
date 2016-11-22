/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.filter.weakfilter;

import br.edimarmanica.configuration.General;
import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.bean.ScoredPair;
import br.edimarmanica.weir_3_0.distance.TypeAwareDistance;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author edimar
 */
public class ScorePairsInterSite implements Runnable {

    private final int id;
    private final List<Rule> rulesSite;
    private final List<Rule> rulesComparedSite;
    private final List<ScoredPair> scoredPairs = new ArrayList<>();
    private boolean finished = false;

    public ScorePairsInterSite(int id, List<Rule> rulesSite, List<Rule> rulesComparedSite) {
        this.id = id;
        this.rulesSite = rulesSite;
        this.rulesComparedSite = rulesComparedSite;
    }

    @Override
    public void run() {
        if (General.DEBUG) {
            System.out.println("\t Starting ScorePairsInterSite thread " + id);
        }

        //calcula o escore entre as regras dos sites diferentes
        long nrComparacoes = 0;
        for (int i = 0; i < rulesSite.size(); i++) {
            for (int j = 0; j < rulesComparedSite.size(); j++) {
                if (General.DEBUG){
                    System.out.println("ScorePairsInterSite - rule: "+id+" - comparações: "+nrComparacoes);
                    nrComparacoes++;
                }
                
                //sempre a intersecção será vazia pois são de sites diferentes
                double score = TypeAwareDistance.typeDistance(rulesSite.get(i), rulesComparedSite.get(j));
                if (score == -1) { //Número insuficiente de instâncias compartilhadas
                    continue;
                }
                if (score == 1) { //regras completas não terão distância 1
                    continue;
                }
                scoredPairs.add(new ScoredPair(rulesSite.get(i), rulesComparedSite.get(j), score));
            }
        }
        finished = true;
        if (General.DEBUG) {
            System.out.println("\t ending ScorePairsInterSite thread " + id);
        }
    }

    public List<ScoredPair> getScoredPairs() {
        if (!finished) {
            throw new UnsupportedOperationException("Você não pode chamar o método getScoredPairs de ScorePairsInterSite antes dela ter sido finalizada! Thread: "+id);
        }
        return scoredPairs;
    }

}
