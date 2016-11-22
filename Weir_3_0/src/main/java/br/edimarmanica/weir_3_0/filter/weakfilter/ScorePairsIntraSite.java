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
import br.edimarmanica.weir_3_0.util.Conjuntos;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author edimar
 */
public class ScorePairsIntraSite implements Runnable {

    private final int id;
    private final List<Rule> rulesSite;
    private final int start;
    private final int end;
    private final List<ScoredPair> scoredPairs = new ArrayList<>();
    private boolean finished = false;
    private final Conjuntos<String> util = new Conjuntos<>();

    public ScorePairsIntraSite(int id, List<Rule> rulesSite, int start, int end) {
        this.id = id;
        this.rulesSite = rulesSite;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        if (General.DEBUG) {
            System.out.println("\t Starting ScorePairsIntraSite thread " + id);
        }
        //calcula o escore entre as regras do mesmo site
        long nrComparacoes = 0;
        for (int i = start; i < end; i++) {
            for (int j = i + 1; j < rulesSite.size(); j++) {//compara até no fim
                if (General.DEBUG){
                    System.out.println("ScorePairsIntraSite - rule: "+id+" - comparações: "+nrComparacoes);
                    nrComparacoes++;
                }
                
                //só calcula o score das regras cuja intersecção é vazia    
                if (util.hasIntersection(rulesSite.get(i).getPairsPageValue(), rulesSite.get(j).getPairsPageValue())) {
                    continue;
                }
                double score = TypeAwareDistance.typeDistance(rulesSite.get(i), rulesSite.get(j));
                if (score == -1) {//Número insuficiente de instâncias compartilhadas
                    continue;
                }
                if (score == 1) {//regras completas não terão distância 1
                    continue;
                }
                scoredPairs.add(new ScoredPair(rulesSite.get(i), rulesSite.get(j), score));
            }
        }
        finished = true;
        if (General.DEBUG) {
            System.out.println("\t Ending ScorePairsIntraSite thread " + id);
        }
    }

    public List<ScoredPair> getScoredPairs() {
        if (!finished) {
            throw new UnsupportedOperationException("Você não pode chamar o método getScoredPairs de ScorePairsIntraSite antes dela ter sido finalizada! Thread: "+id);
        }
        return scoredPairs;
    }

}
