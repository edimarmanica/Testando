/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.util;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Site;
import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author edimar
 */
public class SiteUtils {

    public static int getNrPages(Site site) {
        File dir = new File(Paths.PATH_BASE + "/" + site.getPath());
        return dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".html");
            }
        }).length;
    }
    
    public static void main(String[] args) {
        Site site = br.edimarmanica.dataset.orion.driver.Site.F1;
        System.out.println(getNrPages(site));
    }
}
