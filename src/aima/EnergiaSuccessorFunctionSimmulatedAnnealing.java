package aima;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

public class EnergiaSuccessorFunctionSimmulatedAnnealing implements SuccessorFunction {

    @Override
    public List getSuccessors(Object o) {
        ArrayList                retVal = new ArrayList();
        EnergiaEstado            estatVell  = (EnergiaEstado) o;
        EnergiaHeuristicFunction HF  = new EnergiaHeuristicFunction();
        Random                   myRandom = new Random();
        int i,j;

        // Nos ahorramos generar todos los sucesores escogiendo un par de clientes al azar
        i = myRandom.nextInt(estatVell.getNClientes());
        do{
            j = myRandom.nextInt(estatVell.getNClientes());
        } while (i==j);

        if (estatVell.sePuedenIntercambiarClientes(i,j)) {
            EnergiaEstado estatNou = estatVell;
            estatNou.intercambiarClientes(i, j);
            double   v = HF.getHeuristicValue(estatNou);
            String S = EnergiaEstado.INTERCAMBIO + " " + i + " " + j + " Coste(" + v + ") ---> " + estatNou.toString();

            retVal.add(new Successor(S, estatNou));
        }


        return retVal;
    }
}
