/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.check;

import br.edimarmanica.dataset.Site;
import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.bean.Value;
import br.edimarmanica.weir_3_0.load.LoadRules;
import br.edimarmanica.weir_3_0.util.Conjuntos;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Verifica se duas regras extraem valores para a mesma entidade
 *
 * @author edimar
 */
public class Overlap {

    private final int r1;
    private final int r2;
    private final Site site1;
    private final Site site2;

    public Overlap(int r1, int r2, Site site1, Site site2) {
        this.r1 = r1;
        this.r2 = r2;
        this.site1 = site1;
        this.site2 = site2;
    }

    private void print() {
        System.out.println("entity;value_r1;value_r2");
        Rule rule1 = loadRule(r1, site1);
        Rule rule2 = loadRule(r2, site2);

        for (Value v1 : rule1.getValues()) {
            for (Value v2 : rule2.getValues()) {
                if (v1.getEntityID().equals(v2.getEntityID())) {
                    System.out.println(v1.getEntityID() + ";" + v1.getValue() + ";" + v2.getValue() + "]");
                }
            }
        }

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
        Site site1 = br.edimarmanica.dataset.orion.driver.Site.F1;
        int r1 = 4;
        Site site2 = br.edimarmanica.dataset.orion.driver.Site.GPUPDATE;
        int r2 = 5605;

        Overlap overlap = new Overlap(r1, r2, site1, site2);
        overlap.print();
    }
}
