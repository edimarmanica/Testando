/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.distance;

import br.edimarmanica.configuration.InterSite;
import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.bean.Value;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author edimar
 */
public abstract class TypeAwareDistance {

    public static double typeDistance(Rule r1, Rule s1) {
        /*if (General.DEBUG) {
            System.out.println("Computing the distance between rule " + r1.getRuleID() + " of Site " + r1.getSite().getFolderName() + " with rule " + s1.getRuleID() + " of Site " + s1.getSite().getFolderName());
        }*/

        DataType type = DataTypeController.getMostSpecificType(r1, s1);
        TypeAwareDistance distance;

        switch (type) {
            case DATE:
                distance = new DateDistance();
                break;
            case ISBN:
                distance = new ISBNDistance();
                break;
            case PHONE:
                distance = new PhoneDistance();
                break;
            case CURRENCY:
                distance = new CurrencyDistance();
                break;
            case LENGHT:
                distance = new LenghtDistance();
                break;
            case WEIGHT:
                distance = new WeightDistance();
                break;
            case NUMBER:
                distance = new NumberDistance();
                break;
            case STRING:
                distance = new StringDistance();
                break;
            default:
                distance = null;
        }

        double distanceValue;
        try {
            distanceValue = distance.distance(r1, s1);
        } catch (InsufficientOverlapException ex) {
            distanceValue = 1;
        }
        /*if (General.DEBUG) {
            System.out.println("\tScore: " + distanceValue);
        }*/

        return distanceValue;
    }

    public double distance(Rule r1, Rule s1) throws InsufficientOverlapException {

        Set<String> sharedEntityIds = new HashSet<>();
        double distance = 0;
        for (Value vR1 : r1.getValues()) {
            if (vR1.getEntityID() == null) {
               // System.out.println("Valor sem entidade associada: " + vR1.getEntityID());
                continue;
            }

            for (Value vS1 : s1.getValues()) {
                if (vS1.getEntityID() == null) {
                 //   System.out.println("Valor sem entidade associada: " + vS1.getEntityID());
                    continue;
                }

                //soma a similaridade entre r1 e s1 só se for para a mesma entidade
                if (vR1.getEntityID().equals(vS1.getEntityID())) {
                    //System.out.println("Avaliando a entity: " + vR1.getEntityID());
                    //System.out.println("Pages: "+vR1.getPageID()+" X "+vS1.getPageID());                    

                    if (vR1.getValue() == null && vS1.getValue() == null) {
                        distance += 0; //só para constar -- não entrar no shareEntityIds, logo não dividirá ali embaixo pq se os dois são nulos ignora (não pode significar acerto senão prioriza duas regras que só extraem em uma página)
                    } else if (vR1.getValue() == null || vS1.getValue() == null) {
                        distance += 1;
                        sharedEntityIds.add(vR1.getEntityID());
                    } else {
                        distance += distance(vR1.getValue(), vS1.getValue());
                        sharedEntityIds.add(vR1.getEntityID());
                    }
                }
            }
        }

        if (sharedEntityIds.size() < InterSite.MIN_SHARED_ENTITIES) {
           // System.out.println("Número insuficiente de instâncias compartilhadas.");
          //  System.out.println("SIZE: " + sharedEntityIds.size() + "-" + r1.getValues().size() + " - " + s1.getValues().size());
            throw new InsufficientOverlapException(sharedEntityIds.size());
        }

        return distance / sharedEntityIds.size(); //tem que dividir pelo nr de instâncias compartilhadas entre os sites
    }

    public abstract double distanceSpecific(String vR1, String vS1);

    public double distance(String vR1, String vS1) {
        //System.out.println("["+vR1+"] - [" + vS1+"]");
        if (vR1.equals(vS1)) {
            return 0;
        }

        if (vR1.isEmpty() || vS1.isEmpty()) {
            return 1;
        }
        //System.out.println("\tScore: "+distanceSpecific(vR1, vS1));
        return distanceSpecific(vR1, vS1);
    }
}
