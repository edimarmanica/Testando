/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0;

import br.edimarmanica.weir_3_0.bean.Value;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author edimar
 */
public class Teste {
    public static void main(String[] args) {
        Set<Value> set1 = new HashSet<>();
        set1.add(new Value("teste", "01", "Edmar"));
        set1.add(new Value("valor", "02", "Carla"));
        set1.add(new Value("Credo", "03", "Cris"));
        
        Set<Value> set2 = new HashSet<>();
        set2.add(new Value("teste", "01", "Edmar"));
        set2.add(new Value("valor", "02", "Carla"));
        set2.add(new Value("Credo", "03", "Cris"));
        
        System.out.println(set1.equals(set2));
        
    }
}
