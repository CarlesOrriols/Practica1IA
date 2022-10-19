import IA.Energia.Centrales;
import IA.Energia.Clientes;
import IA.Energia.VEnergia;
import aima.EnergiaEstado;
import aima.EnergiaHeuristicFunctionHillClimbing;
import aima.EnergiaSuccessorFunctionHillClimbing;
import aima.search.framework.*;
import aima.search.informed.HillClimbingSearch;

public class Main {

    public static void main(String[] args) throws Exception {
//        System.out.println("Hello world!");

        int[] num_tipos_centrales = new int[] {2,3,3};
        int semilla_random_centrales = 100;
        Centrales centrales = new Centrales(num_tipos_centrales, semilla_random_centrales);

        int num_clientes = 10;
        double[] proporciones_cli = new double[] {0.3, 0.3, 0.4};
        double proporcion_garantizado = 0.6;
        int semilla_random_clientes = 7;
        Clientes clientes = new Clientes(num_clientes, proporciones_cli, proporcion_garantizado, semilla_random_clientes);
/*
        print(centrales);
        print(clientes);
        print_energia();
*/
        Search typeOfSearch = new HillClimbingSearch();
        SuccessorFunction successorFunction =  new EnergiaSuccessorFunctionHillClimbing();
        HeuristicFunction heuristicFunction = new EnergiaHeuristicFunctionHillClimbing();
        int semilla_random_estado_inicial = 3;
        EnergiaEstado estado_inicial = EnergiaEstado.estadoInicial(centrales, clientes, semilla_random_estado_inicial);
        estado_inicial.print();

//        Problem problem = new Problem(estado_inicial, successorFunction, new EnergiaGoalTest(), heuristicFunction);
//        SearchAgent agent = new SearchAgent(problem, typeOfSearch);
//
//        EnergiaEstado goalState = (EnergiaEstado) typeOfSearch.getGoalState();

    }

    private static void print(Centrales centrales) {
        System.out.println("------ CENTRALES ------");
        for (int i = 0; i < centrales.size(); i++) {
            System.out.println("Central -> " + i);
            System.out.println("\tX -> " + centrales.get(i).getCoordX());
            System.out.println("\tY -> " + centrales.get(i).getCoordY());
            System.out.println("\tProd -> " + centrales.get(i).getProduccion());
            System.out.println("\tTipo -> " + centrales.get(i).getTipo());
        }
        System.out.println("-----------------------");
    }

    private static void print(Clientes clientes) {
        System.out.println("------ CLIENTES ------");
        for (int i = 0; i < clientes.size(); i++) {
            System.out.println("Cliente -> " + i);
            System.out.println("\tX -> " + clientes.get(i).getCoordX());
            System.out.println("\tY -> " + clientes.get(i).getCoordY());
            System.out.println("\tConsumo -> " + clientes.get(i).getConsumo());
            System.out.println("\tContrato -> " + clientes.get(i).getContrato());
            System.out.println("\tTipo -> " + clientes.get(i).getTipo());
        }
        System.out.println("-----------------------");
    }

    private static void print_energia() {
        try {
            System.out.println("------ DATOS ENERGIA ------");
            System.out.println("XG tarifa garant -> " + VEnergia.getTarifaClienteGarantizada(0));
            System.out.println("MG tarifa garant -> " + VEnergia.getTarifaClienteGarantizada(1));
            System.out.println("G tarifa garant -> " + VEnergia.getTarifaClienteGarantizada(2));
            System.out.println("XG tarifa NO garant -> " + VEnergia.getTarifaClienteNoGarantizada(0));
            System.out.println("MG tarifa NO garant -> " + VEnergia.getTarifaClienteNoGarantizada(1));
            System.out.println("G tarifa NO garant -> " + VEnergia.getTarifaClienteNoGarantizada(2));
            System.out.println("XG penalizacion -> " + VEnergia.getTarifaClientePenalizacion(0));
            System.out.println("MG penalizacion -> " + VEnergia.getTarifaClientePenalizacion(1));
            System.out.println("G penalizacion -> " + VEnergia.getTarifaClientePenalizacion(2));
            System.out.println("A MW -> " + VEnergia.getCosteProduccionMW(0));
            System.out.println("B MW -> " + VEnergia.getCosteProduccionMW(1));
            System.out.println("C MW -> " + VEnergia.getCosteProduccionMW(2));
            System.out.println("A encender -> " + VEnergia.getCosteMarcha(0));
            System.out.println("B encender -> " + VEnergia.getCosteMarcha(1));
            System.out.println("C encender -> " + VEnergia.getCosteMarcha(2));
            System.out.println("A parada -> " + VEnergia.getCosteParada(0));
            System.out.println("B parada -> " + VEnergia.getCosteParada(1));
            System.out.println("C parada -> " + VEnergia.getCosteParada(2));
            System.out.println("Perdida hasta 10km -> " + VEnergia.getPerdida(10));
            System.out.println("Perdida hasta 25km -> " + VEnergia.getPerdida(25));
            System.out.println("Perdida hasta 50km -> " + VEnergia.getPerdida(50));
            System.out.println("Perdida hasta 75km -> " + VEnergia.getPerdida(75));
            System.out.println("Perdida más de 75km + -> " + VEnergia.getPerdida(76));
            System.out.println("-----------------------");
        } catch(Exception e) {
            System.out.println("Problemes amb l'estat de les cuotes d'energia");
        }
    }
}