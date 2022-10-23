import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
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

        /*
        FileOutputStream os = null;
        try {
            os = new FileOutputStream("simulatedannelaing.txt", true);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        PrintStream ps = new PrintStream(os);
        ps.println(estatVell.beneficioTotal());
        */
        int i,j;
        EnergiaEstado estatNou;
        int op;

        long n_mover = estatVell.getNCentrales() * estatVell.getNClientes();
        long n_intercambiar = (estatVell.getNClientes() * (estatVell.getNClientes()-1))/2;
        long n_vaciar = estatVell.getNCentrales() * estatVell.getNCentrales();

        long total_successors = n_mover + n_intercambiar + n_vaciar;

        long n_rand = myRandom.nextLong(total_successors);

        if ( n_rand < n_mover ) {
            op = 0; // operador mover
        } else if ( n_rand < (n_mover+n_intercambiar) ) {
            op = 1; // operador intercambiar
        } else {
            op = 2; // operador vaciar
        }

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
