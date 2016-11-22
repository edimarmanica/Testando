/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.check;

import br.edimarmanica.dataset.Site;
import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.load.LoadRules;
import br.edimarmanica.weir_3_0.util.Conjuntos;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Verifica se duas regras do mesmo site extraem o mesmo valor em pelo menos uma
 * página
 *
 * @author edimar
 */
public class IntersectionCheck {

    private final int r1;
    private final int r2;
    private final Site site;

    public IntersectionCheck(int r1, int r2, Site site) {
        this.r1 = r1;
        this.r2 = r2;
        this.site = site;
    }

    private void print() {
        Rule rule1 = loadRule(r1);
        Rule rule2 = loadRule(r2);

        Conjuntos<String> util = new Conjuntos<>();
        boolean intersection = util.hasIntersection(rule1.getPairsPageValue(), rule2.getPairsPageValue());
        System.out.println("Intersection: " + intersection);
        if (intersection) {
            for (String pv : rule1.getPairsPageValue()) {
                if (rule2.getPairsPageValue().contains(pv)) {
                    System.out.println(pv);
                }
            }
        }

    }

    private Rule loadRule(int ruleID) {
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
        Site site = br.edimarmanica.dataset.orion.driver.Site.F1;
        int r1 = 4;
        int r2 = 43;

        IntersectionCheck check = new IntersectionCheck(r1, r2, site);
        check.print();
    }
}
