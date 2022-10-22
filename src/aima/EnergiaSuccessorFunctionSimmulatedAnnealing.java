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

        int op = myRandom.nextInt(3);
        int i,j;

        EnergiaEstado estatNou;
        double v;
        String S;

        switch (op) {
            case 0:
                do {
                    i = myRandom.nextInt(estatVell.getNClientes());
                    j = myRandom.nextInt(estatVell.getNCentrales());
                } while(!estatVell.sePuedeMoverCliente(i, j));

                estatNou = estatVell;
                estatNou.moverCliente(i, j);
                v = HF.getHeuristicValue(estatNou);

                S = estatNou.MOVIMIENTO + " " + i + " " + j + " Coste(" + v + ") ---> " + estatNou.toString();
                retVal.add(new Successor(S, estatNou));
                break;

            case 1:
                // Nos ahorramos generar todos los sucesores escogiendo un par de clientes al azar
                i = myRandom.nextInt(estatVell.getNClientes());
                do{
                    j = myRandom.nextInt(estatVell.getNClientes());
                } while (i==j || !estatVell.sePuedenIntercambiarClientes(i,j));

                estatNou = estatVell;
                estatNou.intercambiarClientes(i, j);
                v = HF.getHeuristicValue(estatNou);
                S = EnergiaEstado.INTERCAMBIO + " " + i + " " + j + " Coste(" + v + ") ---> " + estatNou.toString();

                retVal.add(new Successor(S, estatNou));
                break;

            case 2:
                i = myRandom.nextInt(estatVell.getNCentrales());
                do{
                    j = myRandom.nextInt(estatVell.getNCentrales());
                } while (i==j || !estatVell.sePuedeVaciarCentral(i, j));

                estatNou = estatVell;
                estatNou.vaciarCentral(i, j);
                v = HF.getHeuristicValue(estatNou);

                S = estatNou.VACIADO + " " + i + " " + j + " Coste(" + v + ") ---> " + estatNou.toString();
                retVal.add(new Successor(S, estatNou));
                break;
        }

        return retVal;
    }
}
