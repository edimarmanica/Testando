/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.filter.weakfilter;

import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.bean.ScoredPair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author edimar
 */
public class ParallelScorePairs {

    private static final int NR_CORES = 4;
    private final List<Rule> rulesSite;
    private final List<Rule> rulesComparedSite;

    public ParallelScorePairs(List<Rule> rulesSite, List<Rule> rulesComparedSite) {
        this.rulesSite = rulesSite;
        this.rulesComparedSite = rulesComparedSite;
    }

    private List<ScoredPair> scorePairsIntraSite() {
        List<ScoredPair> scores = new ArrayList<>();
        Thread[] threads = new Thread[NR_CORES];
        ScorePairsIntraSite[] sp = new ScorePairsIntraSite[NR_CORES];

        int resto = rulesSite.size() % NR_CORES;
        int partes;
        if (resto == 0) {
            partes = rulesSite.size() / NR_CORES;
        } else {
            partes = (rulesSite.size() / NR_CORES) + 1;
        }

        for (int i = 0; i < NR_CORES; i++) {

            int start = i * partes;
            int end = start + partes;
            if (end > rulesSite.size()) {
                end = rulesSite.size();
            }

            ScorePairsIntraSite scorePairs = new ScorePairsIntraSite(i, rulesSite, start, end);
            sp[i] = scorePairs;
            threads[i] = new Thread(scorePairs);
            threads[i].start();
        }

        for (int i = 0; i < NR_CORES; i++) {
            try {
                threads[i].join();
                scores.addAll(sp[i].getScoredPairs());
            } catch (InterruptedException ex) {
                Logger.getLogger(ParallelScorePairs.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return scores;

    }

    private List<ScoredPair> scorePairsInterSite() {
        List<ScoredPair> scores = new ArrayList<>();
        Thread[] threads = new Thread[NR_CORES];
        ScorePairsInterSite[] sp = new ScorePairsInterSite[NR_CORES];

        int resto = rulesSite.size() % NR_CORES;
        int partes;
        if (resto == 0) {
            partes = rulesSite.size() / NR_CORES;
        } else {
            partes = (rulesSite.size() / NR_CORES) + 1;
        }

        for (int i = 0; i < NR_CORES; i++) {

            int start = i * partes;
            int end = start + partes;
            if (end > rulesSite.size()) {
                end = rulesSite.size();
            }

            ScorePairsInterSite scorePairs = new ScorePairsInterSite(i, rulesSite.subList(start, end), rulesComparedSite);
            sp[i] = scorePairs;
            threads[i] = new Thread(scorePairs);
            threads[i].start();
        }

        for (int i = 0; i < NR_CORES; i++) {
            try {
                threads[i].join();
                scores.addAll(sp[i].getScoredPairs());

            } catch (InterruptedException ex) {
                Logger.getLogger(ParallelScorePairs.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        return scores;
    }

    public List<ScoredPair> scorePairs() {
        List<ScoredPair> scores = scorePairsIntraSite();
        scores.addAll(scorePairsInterSite());

        Collections.sort(scores); //non-decresing order
        return scores;
    }
}
