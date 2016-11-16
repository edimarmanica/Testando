/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0;

/**
 *
 * @author edimar
 */
public class Main {
    public static void main(String[] args) {
        //Primeiro elimina logicamente (gerar csv com as que ficaram) as regras com mais de 80% dos valores null
        //Segundo elimina logicamente as regras que extraem sempre a mesma coisa (pq é um template)
        //depois elimina duplicatas
        //contar quantas regras ficaram em cada
        //depois calcula o score de todos com todos (entre todos do mesmo site e com o gpupdate entre outros sites pq só ele que tem intersecção praticamente)
        //depois faz um índice de regras que tem sobreposição (mesmo valor na mesma página)
        //depois faz o weak-removal (atualizar para usar os índices)
        //depois integra com o weir 
    }
}
