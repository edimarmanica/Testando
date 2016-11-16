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
import br.edimarmanica.weir_3_0.load.LoadRules;
import java.util.HashSet;
import java.util.Set;

/**
 * Esse filtro é só para carregar todas as regras para o csv
 *
 * @author edimar
 */
public class FirstFilter extends Filter {

    public static final String NAME = "first_filter";

    public FirstFilter(Site site, String path) {
        this.site = site;
        this.path = path;
    }

    @Override
    protected void execute() {
        LoadRules load = new LoadRules(site);
        Set<Rule> rules = load.getRules(null); //le todas as regras
        Set<Integer> keptRules = new HashSet<>();
        for (Rule rule : rules) {
            keptRules.add(rule.getRuleID());
        }
        persiste(keptRules);

        if (General.DEBUG) {
            System.out.println("Remaining rules: " + keptRules.size());
        }
    }

    @Override
    protected String getFilterName() {
        return NAME;
    }

    public static void main(String[] args) {
        General.DEBUG = true;
        Site site = br.edimarmanica.dataset.orion.driver.Site.GPUPDATE;
        String path = Paths.PATH_INTRASITE;
        Filter filter = new FirstFilter(site, path);
        filter.execute();
    }
}
