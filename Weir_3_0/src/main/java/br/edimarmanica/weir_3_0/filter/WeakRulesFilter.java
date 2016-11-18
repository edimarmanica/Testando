/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.filter;

import br.edimarmanica.configuration.General;
import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.bean.ScoredPair;
import br.edimarmanica.weir_3_0.distance.TypeAwareDistance;
import br.edimarmanica.weir_3_0.util.Conjuntos;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Remove as regras fracas No nosso caso só compara com as regras do próprio
 * site e as regras do GP_UPDATE (pq ele que tem as entidades compartilhadas) Já
 * tem que ter aplicado os outros filtros tanto no site atual quando no
 * GP_UPDATE OBS: quando o site alvo é o GP_UPDATE o outro site é o F1
 *
 * @author edimar
 */
public class WeakRulesFilter extends Filter {

    public static final String NAME = "weak_filter";
    private final Site comparedSite;
    private final Conjuntos<String> util = new Conjuntos<>();

    public WeakRulesFilter(Site site, String path, String lastFilter) {
        this.site = site;
        this.path = path;
        this.lastFilter = lastFilter;

        if (site == br.edimarmanica.dataset.orion.driver.Site.GPUPDATE) {
            comparedSite = br.edimarmanica.dataset.orion.driver.Site.F1;
        } else {
            comparedSite = br.edimarmanica.dataset.orion.driver.Site.GPUPDATE;
        }
    }

    @Override
    protected void execute() {
        printLog("Logging ............. ", false);

        if (General.DEBUG) {
            System.out.println("Loading ...");
        }
        List<Rule> rulesSite = new ArrayList<>(loadCurrentRules(site));
        int nrRulesInitial = rulesSite.size();
        List<Rule> rulesComparedSite = new ArrayList<>(loadCurrentRules(comparedSite));

        if (General.DEBUG) {
            System.out.println("Scoring ...");
        }
        List<ScoredPair> scores = scorePairs(rulesSite, rulesComparedSite);
        Set<Rule> completeRules = new HashSet<>();

        if (General.DEBUG) {
            System.out.println("Processing ...");
        }

        for (ScoredPair pair : scores) {
            printLog(pair.getR1().getSite() + ";" + pair.getR1().getRuleID() + ";" + pair.getR2().getSite() + ";" + pair.getR2().getRuleID() + ";" + pair.getDistance(), true);

            //se não existe uma regra completa que tem intersecção não vazia com R1 ou R2, marque R1 e R2 como completas
            boolean hasIntersection = false;
            for (Rule rc : completeRules) {
                if (rc.getSite() == pair.getR1().getSite()) {//só pode haver intersecção não vazia entre regras do mesmo site
                    if (!util.intersection(rc.getPairsPageValue(), pair.getR1().getPairsPageValue()).isEmpty()) {
                        hasIntersection = true;
                        printLog("\t " + pair.getR1().getRuleID() + "(Site: " + pair.getR1().getSite() + ") has intersection with " + rc.getRuleID() + " (Site: " + rc.getSite() + ")", true);
                        break;
                    }
                }
                if (rc.getSite() == pair.getR2().getSite()) {//só pode haver intersecção não vazia entre regras do mesmo site
                    if (!util.intersection(rc.getPairsPageValue(), pair.getR2().getPairsPageValue()).isEmpty()) {
                        hasIntersection = true;
                        printLog("\t " + pair.getR2().getRuleID() + "(Site: " + pair.getR2().getSite() + ") has intersection with " + rc.getRuleID() + " (Site: " + rc.getSite() + ")", true);
                        break;
                    }
                }
            }
            if (!hasIntersection) {
                completeRules.add(pair.getR1());
                completeRules.add(pair.getR2());
            }
        }

        Set<Integer> keptRules = new HashSet<>();
        for (Rule rule : completeRules) {
            if (rule.getSite() == site) { //só adiciona as regras do site alvo
                keptRules.add(rule.getRuleID());
            }
        }
        persiste(keptRules);

        if (General.DEBUG) {
            System.out.println("Removed rules: " + (nrRulesInitial - keptRules.size()));
            System.out.println("Remaining rules: " + keptRules.size());
        }
    }

    /**
     * calcula o score entre as regras de rulesSite e entre as regras de
     * rulesSite e as regras de rulesComparedsite Retorna os pares ordenados em
     * ordem não decrescente
     *
     * @param rulesSite
     * @param rulesComparedSite
     * @return
     */
    private List<ScoredPair> scorePairs(List<Rule> rulesSite, List<Rule> rulesComparedSite) {
        List<ScoredPair> scores = new ArrayList<>();

        //calcula o escore entre as regras do mesmo site
        for (int i = 0; i < rulesSite.size() - 1; i++) {
            for (int j = i + 1; j < rulesSite.size(); j++) {
                //só calcula o score das regras cuja intersecção é vazia    
                if (!util.intersection(rulesSite.get(i).getPairsPageValue(), rulesSite.get(j).getPairsPageValue()).isEmpty()) {
                    continue;
                }
                double score = TypeAwareDistance.typeDistance(rulesSite.get(i), rulesSite.get(j));
                if (score == -1) {//Número insuficiente de instâncias compartilhadas
                    continue;
                }
                if (score == 1) {//regras completas não terão distância 1
                    continue;
                }
                scores.add(new ScoredPair(rulesSite.get(i), rulesSite.get(j), score));
            }
        }

        //calcula o escore entre as regras dos sites diferentes
        int faltam = rulesSite.size()*rulesComparedSite.size();
        for (int i = 0; i < rulesSite.size() - 1; i++) {
            for (int j = 0; j < rulesComparedSite.size(); j++) {
                if (General.DEBUG){
                    System.out.println("\tFaltam "+faltam+" comparações!");
                    faltam--;
                }
                
                //sempre a intersecção será vazia pois são de sites diferentes
                double score = TypeAwareDistance.typeDistance(rulesSite.get(i), rulesComparedSite.get(j));
                if (score == -1) { //Número insuficiente de instâncias compartilhadas
                    continue;
                }
                if (score == 1) { //regras completas não terão distância 1
                    continue;
                }
                scores.add(new ScoredPair(rulesSite.get(i), rulesComparedSite.get(j), score));
            }
        }

        Collections.sort(scores); //non-decresing order
        return scores;
    }

    protected void printLog(String line, boolean append) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path + "/" + site.getPath() + "/weak_filter_log.txt", append))) {
            bw.write(line);
            bw.newLine();
        } catch (IOException ex) {
            Logger.getLogger(WeakRulesFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static long factorial(long n) {
        long fact = 1; // this  will be the result
        for (long i = 1; i <= n; i++) {
            fact *= i;
        }
        return fact;
    }

    @Override
    protected String getFilterName() {
        return NAME;
    }

    public static void main(String[] args) {
        General.DEBUG = true;
        Site site = br.edimarmanica.dataset.orion.driver.Site.F1;
        String path = Paths.PATH_INTRASITE;
        WeakRulesFilter filter = new WeakRulesFilter(site, path, IdenticalValuesFilter.NAME);
        filter.execute();

    }
}
