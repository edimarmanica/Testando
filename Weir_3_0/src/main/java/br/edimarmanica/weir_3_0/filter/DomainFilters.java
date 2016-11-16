/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.filter;

import br.edimarmanica.configuration.General;
import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;

/**
 * Aplica todos os filtros em todos os sites do domínio
 *
 * @author edimar
 */
public class DomainFilters {

    private final Domain domain;
    private final String path;

    public DomainFilters(Domain domain, String path) {
        this.domain = domain;
        this.path = path;
    }

    public void executeIntraSiteFilters() {
        if (General.DEBUG) {
            System.out.println("IntraSiteFilter ...");
        }
        for (Site site : domain.getSites()) {
            if (General.DEBUG) {
                System.out.println("\tSite: " + site);
            }
            Filter filter = new FirstFilter(site, path);
            filter.execute();

            filter = new NullValuesFilter(site, path, FirstFilter.NAME);
            filter.execute();

            filter = new IdenticalValuesFilter(site, path, NullValuesFilter.NAME);
            filter.execute();
        }
    }

    public void executeInterSiteFilters() {
        if (General.DEBUG) {
            System.out.println("InterSiteFilter ...");
        }
        for (Site site : domain.getSites()) {
            if (General.DEBUG) {
                System.out.println("\tSite: " + site);
            }
            Filter filter = new WeakRulesFilter(site, path, IdenticalValuesFilter.NAME);
            filter.execute();
        }
    }

    public static void main(String[] args) {
        General.DEBUG = true;
        Domain domain = br.edimarmanica.dataset.orion.Domain.DRIVER;
        String path = Paths.PATH_INTRASITE;
        DomainFilters filters = new DomainFilters(domain, path);
        filters.executeIntraSiteFilters();
        //filters.executeInterSiteFilters();
    }
}
