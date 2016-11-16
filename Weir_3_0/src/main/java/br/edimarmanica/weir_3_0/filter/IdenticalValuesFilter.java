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
import br.edimarmanica.weir_3_0.bean.Value;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author edimar
 */
public class IdenticalValuesFilter extends Filter {

    public static final String NAME = "identical_filter";

    public IdenticalValuesFilter(Site site, String path, String lastFilter) {
        this.site = site;
        this.path = path;
        this.lastFilter = lastFilter;
    }

    @Override
    protected void execute() {
        List<Rule> rules = new ArrayList<>(loadCurrentRules());
        int nrRulesInitial = rules.size();
        for (int i = 0; i < rules.size() - 1; i++) {
            for (int j = i + 1; j < rules.size(); j++) {
                if (hasIdenticalValues(rules.get(i), rules.get(j))) {
                    rules.remove(j);
                    j--;//pq eliminei o j atual e o for vai incrementar
                }
            }
        }

        Set<Integer> keptRules = new HashSet<>();
        for (Rule rule : rules) {
            keptRules.add(rule.getRuleID());
        }
        persiste(keptRules);

        if (General.DEBUG) {
            System.out.println("Removed rules: " + (nrRulesInitial - keptRules.size()));
            System.out.println("Remaining rules: " + keptRules.size());
        }
    }

    private boolean hasIdenticalValues(Rule r1, Rule r2) {
        return r1.getValues().equals(r2.getValues());
    }

    @Override
    protected String getFilterName() {
        return NAME;
    }

    public static void main(String[] args) {
        General.DEBUG = true;
        Site site = br.edimarmanica.dataset.orion.driver.Site.GPUPDATE;
        String path = Paths.PATH_INTRASITE;
        IdenticalValuesFilter filter = new IdenticalValuesFilter(site, path, NullValuesFilter.NAME);
        filter.execute();
    }

}
