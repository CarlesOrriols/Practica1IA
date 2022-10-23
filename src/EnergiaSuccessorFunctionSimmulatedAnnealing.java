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

        switch (op) {
            case 0: //mover
                do {
                    i = myRandom.nextInt(estatVell.getNClientes());
                    j = myRandom.nextInt(estatVell.getNCentrales()+1)-1;
                } while(!estatVell.sePuedeMoverCliente(i, j));

                estatNou = new EnergiaEstado(estatVell);
                estatNou.moverCliente(i, j);

                retVal.add(new Successor("", estatNou));
                break;

            case 1:  //intercambio
                // Nos ahorramos generar todos los sucesores escogiendo un par de clientes al azar

                do{
                    i = myRandom.nextInt(estatVell.getNClientes());
                    j = myRandom.nextInt(estatVell.getNClientes());
                } while (!estatVell.sePuedenIntercambiarClientes(i,j));

                estatNou = new EnergiaEstado(estatVell);
                estatNou.intercambiarClientes(i, j);

                retVal.add(new Successor("", estatNou));
                break;

            case 2: //volcar central i a j

                do{
                    i = myRandom.nextInt(estatVell.getNCentrales()+1)-1;
                    j = myRandom.nextInt(estatVell.getNCentrales()+1)-1;
                } while (!estatVell.sePuedeVaciarCentral(i, j));

                estatNou = new EnergiaEstado(estatVell);
                estatNou.vaciarCentral(i, j);

                retVal.add(new Successor("", estatNou));
                break;
        }

        return retVal;
    }
}
