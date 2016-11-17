/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.check;

import br.edimarmanica.dataset.Site;
import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.distance.TypeAwareDistance;
import br.edimarmanica.weir_3_0.load.LoadRules;
import br.edimarmanica.weir_3_0.util.Conjuntos;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author edimar
 */
public class IntersectionCheck {

    private final int r1;
    private final int r2;
    private final Site siteR1;
    private final Site siteR2;

    public IntersectionCheck(int r1, int r2, Site siteR1, Site siteR2) {
        this.r1 = r1;
        this.r2 = r2;
        this.siteR1 = siteR1;
        this.siteR2 = siteR2;
    }

    private void print() {
        Rule rule1 = loadRule(r1, siteR1);
        Rule rule2 = loadRule(r2, siteR2);
        
        Conjuntos<String> util = new Conjuntos<>();

        System.out.println("Intersection: " + !util.intersection(rule1.getPairsPageValue(), rule2.getPairsPageValue()).isEmpty());
    }

    private Rule loadRule(int ruleID, Site site) {
        Set<Integer> rules = new HashSet<>();
        rules.add(ruleID);
        LoadRules load = new LoadRules(site);
        List<Rule> rulesList = new ArrayList<>(load.getRules(rules));
        if (rulesList.size() != 1) {
            System.out.println("Erro com a regra: " + ruleID + " do site " + site);
        }
        return rulesList.get(0);
    }

    public static void main(String[] args) {
        Site siteR1 = br.edimarmanica.dataset.orion.driver.Site.EURO_SPORTS;
        int r1 = 0;
        Site siteR2 = br.edimarmanica.dataset.orion.driver.Site.EURO_SPORTS;
        int r2 = 16;

        IntersectionCheck check = new IntersectionCheck(r1, r2, siteR1, siteR2);
        check.print();
    }
}
