/*
 * To change this license HEADER, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.filter;

import br.edimarmanica.configuration.General;
import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.bean.Value;
import br.edimarmanica.weir_3_0.util.SiteUtils;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author edimar
 */
public class NullValuesFilter extends Filter {

    public static final String NAME = "null_filter";
    private final static int PR_NULL_VALUES = 80; //cada regra não pode ter mais de PR_NULL_VALUES null -- no artigo era 80

    /**
     *
     * @param site
     * @param path caminho onde estão as regras
     * @param lastFilter último filtro aplicado
     */
    public NullValuesFilter(Site site, String path, String lastFilter) {
        this.site = site;
        this.path = path;
        this.lastFilter = lastFilter;
    }

    protected void execute() {
        int nrPagesSite = SiteUtils.getNrPages(site);

        Set<Rule> rules = loadCurrentRules();
        int nrRulesInitial = rules.size();
        Set<Integer> keptRules = new HashSet<>();
        for (Rule rule : rules) {
            if (getNrNullValues(rule) * 100 / nrPagesSite <= PR_NULL_VALUES) {
                keptRules.add(rule.getRuleID());
            }
        }
        persiste(keptRules);
        
        if (General.DEBUG) {
            System.out.println("Removed rules: " + (nrRulesInitial - keptRules.size()));
            System.out.println("Remaining rules: " + keptRules.size());
        }
    }

    private int getNrNullValues(Rule rule) {
        int nrNullValues = 0;
        for (Value value : rule.getValues()) {
            if (value.getValue() == null) {
                nrNullValues++;
            }
        }
        return nrNullValues;
    }

    @Override
    protected String getFilterName() {
        return NAME;
    }

    public static void main(String[] args) {
        General.DEBUG = true;
        Site site = br.edimarmanica.dataset.orion.driver.Site.GPUPDATE;
        String path = Paths.PATH_INTRASITE;
        NullValuesFilter filter = new NullValuesFilter(site, path, FirstFilter.NAME);
        filter.execute();
    }
}
